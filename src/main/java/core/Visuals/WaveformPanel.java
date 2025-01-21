package core.Visuals;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class WaveformPanel extends JPanel {
    private final float[] waveformBuffer; // Circular buffer for waveform data
    private int bufferIndex = 0;         // Current write position in the buffer
    private static final int BUFFER_SIZE = 4096; // Larger buffer for smoother transitions

    private final Timer repaintTimer;

    public WaveformPanel() {
        waveformBuffer = new float[BUFFER_SIZE];
        Arrays.fill(waveformBuffer, 0); // Initialize buffer with silence

        // Timer to repaint at 100 FPS (10ms intervals)
        repaintTimer = new Timer(10, e -> repaint());
        repaintTimer.start();
    }

    public synchronized void updateWaveform(byte[] newWaveform) {
        // Convert byte data to normalized float values (-1.0 to 1.0)
        for (byte b : newWaveform) {
            waveformBuffer[bufferIndex] = b / 128f; // Normalize byte to float
            bufferIndex = (bufferIndex + 1) % BUFFER_SIZE; // Wrap around buffer
        }
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw a black background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the waveform
        g.setColor(Color.GREEN);
        int midY = getHeight() / 2;

        int width = getWidth();
        int height = getHeight();

        // Calculate pixel width per sample
        float pixelsPerSample = (float) width / BUFFER_SIZE;

        for (int i = 0; i < BUFFER_SIZE - 1; i++) {
            int x1 = (int) (i * pixelsPerSample);
            int x2 = (int) ((i + 1) * pixelsPerSample);

            // Scale waveform data to panel height
            int y1 = midY - (int) (waveformBuffer[(bufferIndex + i) % BUFFER_SIZE] * midY);
            int y2 = midY - (int) (waveformBuffer[(bufferIndex + i + 1) % BUFFER_SIZE] * midY);

            g.drawLine(x1, y1, x2, y2);
        }
    }
}