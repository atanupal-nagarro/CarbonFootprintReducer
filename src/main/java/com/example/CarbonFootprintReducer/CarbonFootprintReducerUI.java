package com.example.CarbonFootprintReducer;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class CarbonFootprintReducerUI extends JFrame {

    private JTextPane textPane;
    private StyledDocument doc;
    private Style regular, bold, error;

    public CarbonFootprintReducerUI() {
        setTitle("Carbon Footprint Reducer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Carbon Footprint Reducer", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 1, 10, 10));

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

        // ✅ Use JTextPane instead of JTextArea
        textPane = new JTextPane();
        textPane.setEditable(false);
        doc = textPane.getStyledDocument();

        // Styles
        regular = textPane.addStyle("regular", null);
        StyleConstants.setFontFamily(regular, "Monospaced");
        StyleConstants.setFontSize(regular, 14);

        bold = textPane.addStyle("bold", null);
        StyleConstants.setFontFamily(bold, "Monospaced");
        StyleConstants.setFontSize(bold, 14);
        StyleConstants.setBold(bold, true);

        error = textPane.addStyle("error", null);
        StyleConstants.setFontFamily(error, "Monospaced");
        StyleConstants.setFontSize(error, 14);
        StyleConstants.setForeground(error, Color.RED);

        JScrollPane scroll = new JScrollPane(textPane);
        add(scroll, BorderLayout.CENTER);

        // Redirect System.out & System.err
        System.setOut(new PrintStream(new TextPaneOutputStream(doc, regular, bold), true));
        System.setErr(new PrintStream(new TextPaneOutputStream(doc, error, error), true));

        // Buttons
        compressImagesBtn.addActionListener(e -> new Thread(() ->
                CarbonFootprintReducer.compressAllImages("Pictures/")).start());

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

        compressVideosBtn.addActionListener(e -> new Thread(() -> {
            try {
                CarbonFootprintReducer.compressAllVideos("Videos/");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start());

        deleteByYearBtn.addActionListener(e -> {
            String yearStr = JOptionPane.showInputDialog(this, "Enter Year (e.g. 2022):");
            try {
                int year = Integer.parseInt(yearStr);
                new Thread(() -> CarbonFootprintReducer.deleteFilesOlderThanYear("Pictures/", year)).start();
            } catch (NumberFormatException ex) {
                System.err.println("❌ Invalid year entered.");
            }
        });

        clearBtn.addActionListener(e -> textPane.setText(""));
        exitBtn.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CarbonFootprintReducerUI ui = new CarbonFootprintReducerUI();
            ui.setVisible(true);
        });
    }
}

/**
 * Custom OutputStream for JTextPane with styled text.
 */
class TextPaneOutputStream extends OutputStream {
    private final StyledDocument doc;
    private final Style regular;
    private final Style bold;
    private final StringBuilder buffer = new StringBuilder();

    public TextPaneOutputStream(StyledDocument doc, Style regular, Style bold) {
        this.doc = doc;
        this.regular = regular;
        this.bold = bold;
    }

    @Override
    public void write(int b) {
        char c = (char) b;
        buffer.append(c);
        if (c == '\n') {
            flush();
        }
    }

    @Override
    public void flush() {
        if (buffer.length() > 0) {
            final String line = buffer.toString();
            buffer.setLength(0);

            SwingUtilities.invokeLater(() -> {
                try {
                    // Lines starting with "BOLD:" → bold text
                    if (line.contains("arbon footprint")) {
                        doc.insertString(doc.getLength(), line, bold);
                    } else {
                        doc.insertString(doc.getLength(), line, regular);
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
