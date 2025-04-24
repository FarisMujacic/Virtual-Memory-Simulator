package com.project;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.IntStream;
import com.example.DemandPaging;
import com.example.BarChartFrame; 
 
 
public class DemandPagingGUI extends JFrame {
    private JTextField frameInput, refStringInput;
    private JButton startButton, nextButton, playPauseButton, chartButton;
    private JLabel statusLabel, statsLabel, stepLabel, evictedLabel;
    private JPanel framePanel;
 
    private java.util.List<Integer> frames = new ArrayList<>();
    private Queue<Integer> fifoQueue = new LinkedList<>();
    private JLabel[] frameLabels;
 
    private int[] pages;
    private int frameCount;
    private int currentStep = 0, hits = 0, faults = 0;
    private String algorithm = "FIFO";
 
    private JComboBox<String> algorithmSelector;
 
    private javax.swing.Timer autoTimer;
    private boolean isPlaying = false;
 
    public DemandPagingGUI() {
        setTitle("Demand Paging Simulation");
        setSize(800, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
 
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
        inputPanel.add(new JLabel("Number of Frames:"));
        frameInput = new JTextField();
        inputPanel.add(frameInput);
 
        inputPanel.add(new JLabel("Reference String (space-separated):"));
        refStringInput = new JTextField();
        inputPanel.add(refStringInput);
 
        inputPanel.add(new JLabel("Algorithm:"));
        algorithmSelector = new JComboBox<>(new String[]{"FIFO", "LRU", "MRU", "OPT"});
        inputPanel.add(algorithmSelector);
 
        startButton = new JButton("Start Simulation");
        inputPanel.add(startButton);
 
        nextButton = new JButton("Next Step");
        nextButton.setEnabled(false);
        inputPanel.add(nextButton);
 
        playPauseButton = new JButton("Play");
        playPauseButton.setEnabled(false);
        inputPanel.add(playPauseButton);
 
        chartButton = new JButton("Show Bar Chart");
        chartButton.setEnabled(false);
        inputPanel.add(chartButton);
 
        add(inputPanel, BorderLayout.NORTH);
 
        JPanel centerPanel = new JPanel(new BorderLayout());
        framePanel = new JPanel();
        centerPanel.add(framePanel, BorderLayout.CENTER);
 
        evictedLabel = new JLabel("Evicted Page: -", SwingConstants.CENTER);
        evictedLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        centerPanel.add(evictedLabel, BorderLayout.SOUTH);
 
        add(centerPanel, BorderLayout.CENTER);
 
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        stepLabel = new JLabel("Step: 0", SwingConstants.CENTER);
        statusLabel = new JLabel("Enter input to start", SwingConstants.CENTER);
        statsLabel = new JLabel("Hits: 0 | Faults: 0 | Hit Ratio: 0.00", SwingConstants.CENTER);
        infoPanel.add(stepLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(statsLabel);
 
        add(infoPanel, BorderLayout.SOUTH);
 
        startButton.addActionListener(e -> startSimulation());
        nextButton.addActionListener(e -> runStep());
        playPauseButton.addActionListener(e -> toggleAutoPlay());
        chartButton.addActionListener(e -> showBarChart());
 
        setVisible(true);
    }
 
    private void startSimulation() {
        try {
            frameCount = Integer.parseInt(frameInput.getText().trim());
            String[] parts = refStringInput.getText().trim().split("\\s+");
            pages = Arrays.stream(parts).mapToInt(Integer::parseInt).toArray();
            algorithm = (String) algorithmSelector.getSelectedItem();
 
            frames.clear();
            fifoQueue.clear();
            currentStep = 0;
            hits = 0;
            faults = 0;
 
            framePanel.removeAll();
            framePanel.setLayout(new GridLayout(1, frameCount, 10, 10));
            frameLabels = new JLabel[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frameLabels[i] = new JLabel("-", SwingConstants.CENTER);
                frameLabels[i].setFont(new Font("Arial", Font.BOLD, 24));
                frameLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                frameLabels[i].setOpaque(true);
                frameLabels[i].setBackground(Color.WHITE);
                framePanel.add(frameLabels[i]);
            }
 
            framePanel.revalidate();
            framePanel.repaint();
 
            stepLabel.setText("Step: 0");
            statusLabel.setText("Simulation ready. Click 'Next Step' or 'Play'.");
            statsLabel.setText("Hits: 0 | Faults: 0 | Hit Ratio: 0.00");
            evictedLabel.setText("Evicted Page: -");
 
            nextButton.setEnabled(true);
            playPauseButton.setEnabled(true);
            playPauseButton.setText("Play");
            isPlaying = false;
            chartButton.setEnabled(false);
 
            if (autoTimer != null && autoTimer.isRunning()) {
                autoTimer.stop();
            }
 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter integers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void runStep() {
        if (currentStep >= pages.length) {
            statusLabel.setText("Simulation complete.");
            nextButton.setEnabled(false);
            playPauseButton.setEnabled(false);
            chartButton.setEnabled(true);
 
            if (autoTimer != null && autoTimer.isRunning()) {
                autoTimer.stop();
                isPlaying = false;
                playPauseButton.setText("Play");
            }
 
            double hitRatio = hits / (double) pages.length;
            double missRatio = faults / (double) pages.length;
 
            String summary = String.format(
                    "Simulation Finished!\n\n" +
                            "Algorithm: %s\n" +
                            "Total References: %d\n" +
                            "Hits: %d\n" +
                            "Faults: %d\n" +
                            "Hit Ratio: %.2f\n" +
                            "Miss Ratio: %.2f",
                    algorithm, pages.length, hits, faults, hitRatio, missRatio
            );
 
            JOptionPane.showMessageDialog(this, summary, "Final Stats", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
 
        int page = pages[currentStep];
        boolean hit = frames.contains(page);
 
        for (JLabel label : frameLabels) {
            label.setBackground(Color.WHITE);
        }
 
        if (hit) {
            hits++;
            if (algorithm.equals("LRU") || algorithm.equals("MRU")) {
                frames.remove(Integer.valueOf(page));
                frames.add(page);
            }
            statusLabel.setText("HIT → Page " + page);
            evictedLabel.setText("Evicted Page: -");
        } else {
            faults++;
            statusLabel.setText("FAULT → Page " + page);
            if (frames.size() < frameCount) {
                frames.add(page);
                evictedLabel.setText("Evicted Page: -");
            } else {
                int indexToRemove = switch (algorithm) {
                    case "FIFO" -> frames.indexOf(fifoQueue.poll());
                    case "OPT" -> findOptEviction(frames, pages, currentStep);
                    case "MRU" -> frames.size() - 1;
                    default -> 0;
                };
                int evicted = frames.get(indexToRemove);
                frames.remove(indexToRemove);
                frames.add(page);
                evictedLabel.setText("Evicted Page: " + evicted);
                if (!algorithm.equals("FIFO")) fifoQueue.remove(evicted);
            }
            fifoQueue.add(page);
        }
 
        for (int i = 0; i < frameCount; i++) {
            if (i < frames.size()) {
                int val = frames.get(i);
                frameLabels[i].setText(String.valueOf(val));
                if (val == page) {
                    frameLabels[i].setBackground(hit ? Color.GREEN : Color.RED);
                }
            } else {
                frameLabels[i].setText("-");
                frameLabels[i].setBackground(Color.WHITE);
            }
        }
 
        currentStep++;
        stepLabel.setText("Step: " + currentStep);
        statsLabel.setText(String.format("Hits: %d | Faults: %d | Hit Ratio: %.2f",
                hits, faults, hits / (double) currentStep));
    }
 
    private void showBarChart() {
        int[] faults = {
                simulate("FIFO", pages, frameCount),
                simulate("LRU", pages, frameCount),
                simulate("MRU", pages, frameCount),
                simulate("OPT", pages, frameCount)
        };
        String[] algos = {"FIFO", "LRU", "MRU", "OPT"};
 
        // Sort by fault count (ascending)
        Integer[] indices = IntStream.range(0, faults.length).boxed()
                .sorted(Comparator.comparingInt(i -> faults[i]))
                .toArray(Integer[]::new);
 
        int[] sortedFaults = new int[faults.length];
        String[] sortedAlgos = new String[algos.length];
        for (int i = 0; i < indices.length; i++) {
            sortedFaults[i] = faults[indices[i]];
            sortedAlgos[i] = algos[indices[i]];
        }
 
        new BarChartFrame(sortedAlgos, sortedFaults);
    }
 
    private void toggleAutoPlay() {
        if (isPlaying) {
            autoTimer.stop();
            playPauseButton.setText("Play");
            isPlaying = false;
        } else {
            autoTimer = new javax.swing.Timer(1000, e -> runStep());
            autoTimer.start();
            playPauseButton.setText("Pause");
            isPlaying = true;
        }
    }
 
    private int findOptEviction(java.util.List<Integer> frames, int[] pages, int currentIndex) {
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
 
    private int simulate(String algo, int[] pages, int frameCount) {
        int faults = 0;
        java.util.List<Integer> tempFrames = new ArrayList<>();
        Queue<Integer> tempQueue = new LinkedList<>();
 
        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            boolean hit = tempFrames.contains(page);
 
            if (!hit) {
                faults++;
                if (tempFrames.size() < frameCount) {
                    tempFrames.add(page);
                } else {
                    int removeIndex = switch (algo) {
                        case "FIFO" -> tempFrames.indexOf(tempQueue.poll());
                        case "OPT" -> findOptEviction(tempFrames, pages, i);
                        case "MRU" -> tempFrames.size() - 1;
                        default -> 0;
                    };
                    if (!algo.equals("FIFO")) tempQueue.remove(tempFrames.get(removeIndex));
                    tempFrames.remove(removeIndex);
                    tempFrames.add(page);
                }
                tempQueue.add(page);
            } else {
                if (algo.equals("LRU") || algo.equals("MRU")) {
                    tempFrames.remove(Integer.valueOf(page));
                    tempFrames.add(page);
                }
            }
        }
 
        return faults;
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(DemandPagingGUI::new);
    }
}