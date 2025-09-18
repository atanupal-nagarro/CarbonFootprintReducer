package com.example.CarbonFootprintReducer;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class CarbonFootprintReducerUI extends JFrame {

    private JTextArea textArea;

    public CarbonFootprintReducerUI() {
        setTitle("Carbon Footprint Reducer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Carbon Footprint Reducer", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1, 10, 10));

        JButton compressImagesBtn = new JButton("1. Compress all images (jpg)");
        JButton deleteByCityBtn = new JButton("2. Delete images by City");
        JButton deleteByCountryBtn = new JButton("3. Delete images by Country");
        JButton compressVideosBtn = new JButton("4. Compress all videos");
        JButton deleteByYearBtn = new JButton("5. Delete images older than Year");
        JButton clearBtn = new JButton("Clear messages");
        JButton exitBtn = new JButton("Exit");

        buttonPanel.add(compressImagesBtn);
        buttonPanel.add(deleteByCityBtn);
        buttonPanel.add(deleteByCountryBtn);
        buttonPanel.add(compressVideosBtn);
        buttonPanel.add(deleteByYearBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(exitBtn);


        add(buttonPanel, BorderLayout.WEST);

        // Output area
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);
        add(scroll, BorderLayout.CENTER);

        // ✅ Redirect System.out and System.err
        System.setOut(new java.io.PrintStream(new TextAreaOutputStream(outputArea), true));
        System.setErr(new java.io.PrintStream(new TextAreaOutputStream(outputArea), true));
        //System.setOut(new PrintStream(new TextAreaOutputStream(outputArea), true));
        //System.setErr(new PrintStream(new TextAreaOutputStream(outputArea), true));


        compressImagesBtn.addActionListener(e -> {
            new Thread(() -> {
                CarbonFootprintReducer.compressAllImages("Pictures/");
            }).start();
        });

        deleteByCityBtn.addActionListener(e -> {
            String city = JOptionPane.showInputDialog(this, "Enter City Name:");
            if (city != null && !city.isEmpty()) {
                new Thread(() -> {
                    try {
                        CarbonFootprintReducer.deleteImagesByCity(city, "Pictures/");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        deleteByCountryBtn.addActionListener(e -> {
            String country = JOptionPane.showInputDialog(this, "Enter Country Name:");
            if (country != null && !country.isEmpty()) {
                new Thread(() -> {
                    try {
                        CarbonFootprintReducer.deleteImagesByCountry(country, "Pictures/");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        compressVideosBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    CarbonFootprintReducer.compressAllVideos("Videos/");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        deleteByYearBtn.addActionListener(e -> {
            String yearStr = JOptionPane.showInputDialog(this, "Enter Year (e.g. 2022):");
            try {
                int year = Integer.parseInt(yearStr);
                new Thread(() -> CarbonFootprintReducer.deleteFilesOlderThanYear("Pictures/", year)).start();
            } catch (NumberFormatException ex) {
                System.err.println("❌ Invalid year entered.");
            }
        });


        clearBtn.addActionListener(e -> outputArea.setText(""));


        exitBtn.addActionListener(e -> System.exit(0));
    }


    public static void main(String[] args) {

        //SwingUtilities.invokeLater(CarbonFootprintReducerUI::new);

        SwingUtilities.invokeLater(() -> {
            CarbonFootprintReducerUI ui = new CarbonFootprintReducerUI();
            ui.setVisible(true);
        });


    }





}
