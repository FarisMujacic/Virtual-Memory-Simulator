package com.project;
 
import java.util.*;
import java.util.List;
 
import javax.swing.*;
import java.awt.*;
 
public class DemandPaging {
 
    public static int simulate(String algorithm, int[] pages, int frameCount) {
        int faults = 0, hits = 0;
        List<Integer> frames = new ArrayList<>();
        Queue<Integer> fifoQueue = new LinkedList<>();
 
       for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            boolean hit = frames.contains(page);
 
            if (hit) {
                hits++;
                if ("LRU".equals(algorithm) || "MRU".equals(algorithm)) {
                    frames.remove(Integer.valueOf(page));
                    frames.add(page);
                }
            } else {
                faults++;
                if (frames.size() < frameCount) {
                    frames.add(page);
                } else {
                    int removeIndex = switch (algorithm) {
                        case "FIFO" -> frames.indexOf(fifoQueue.poll());
                        case "OPT" -> findOptEviction(frames, pages, i);
                        case "MRU" -> frames.size() - 1;
                        default -> 0;
                    };
                    if (!algorithm.equals("FIFO")) fifoQueue.remove(frames.get(removeIndex));
                    frames.remove(removeIndex);
                    frames.add(page);
                }
                fifoQueue.add(page);
            }
        }
 
        return faults;
    }
 
    private static int findOptEviction(List<Integer> frames, int[] pages, int currentIndex) {
        int indexToRemove = 0, farthest = -1;
        for (int j = 0; j < frames.size(); j++) {
            int curr = frames.get(j);
            int nextUse = Integer.MAX_VALUE;
            for (int k = currentIndex + 1; k < pages.length; k++) {
                if (pages[k] == curr) {
                    nextUse = k;
                    break;
                }
            }
            if (nextUse > farthest) {
                farthest = nextUse;
                indexToRemove = j;
            }
        }
        return indexToRemove;
    }
}
 
class BarChartFrame extends JFrame {
    private int[] faultCounts;
    private String[] algorithms;
 
    public BarChartFrame(String[] algorithms, int[] faultCounts) {
        this.algorithms = algorithms;
        this.faultCounts = faultCounts;
 
        setTitle("Algorithm Comparison(fault counts)");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }
 
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int width = 80;
        int max = Arrays.stream(faultCounts).max().orElse(1);
 
        int x = 100;
        for (int i = 0; i < faultCounts.length; i++) {
            int barHeight = (int) ((faultCounts[i] / (double) max) * 200);
            g.setColor(Color.BLUE);
            g.fillRect(x, 300 - barHeight, width, barHeight);
 
            g.setColor(Color.BLACK);
            g.drawRect(x, 300 - barHeight, width, barHeight);
            g.drawString(algorithms[i], x + 10, 320);
            g.drawString(String.valueOf(faultCounts[i]), x + 20, 290 - barHeight);
 
            x += 100;
        }
    }
}
 