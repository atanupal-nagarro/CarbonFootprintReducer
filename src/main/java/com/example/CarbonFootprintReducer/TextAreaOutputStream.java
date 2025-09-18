package com.example.CarbonFootprintReducer;

import javax.swing.*;
import java.io.OutputStream;

class TextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final StringBuilder buffer = new StringBuilder();

    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public synchronized void write(int b) {
        char c = (char) b;
        buffer.append(c);

        if (c == '\n') {
            flush(); // push completed line immediately
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        String text = new String(b, off, len);
        buffer.append(text);

        int newlineIndex;
        while ((newlineIndex = buffer.indexOf("\n")) != -1) {
            // Extract one complete line (with newline)
            String line = buffer.substring(0, newlineIndex + 1);
            buffer.delete(0, newlineIndex + 1);

            SwingUtilities.invokeLater(() -> {
                textArea.append(line);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }

    @Override
    public synchronized void flush() {
        if (buffer.length() > 0) {
            final String text = buffer.toString();
            buffer.setLength(0);

            SwingUtilities.invokeLater(() -> {
                textArea.append(text);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }

    @Override
    public void close() {
        flush();
    }
}
