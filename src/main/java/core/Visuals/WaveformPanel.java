package core.Visuals;

import core.Constants.ConstantValues;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class WaveformPanel extends JPanel {
    private final float[] waveformBuffer;
    private int bufferIndex = 0;
    private static final int BUFFER_SIZE = 128;

    private final Timer repaintTimer;

    public WaveformPanel() {
        waveformBuffer = new float[BUFFER_SIZE];
        Arrays.fill(waveformBuffer, 0);

        repaintTimer = new Timer(6, e -> repaint());
        repaintTimer.start();
    }

    public synchronized void updateWaveform(float[] newWaveform) {
        // Copy new waveform data to the circular buffer
        for (float sample : newWaveform) {
            waveformBuffer[bufferIndex] = sample;
            bufferIndex = (bufferIndex + 1) % BUFFER_SIZE;
        }
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradient background
        GradientPaint gradient = new GradientPaint(0, 0, Color.BLACK, 0, getHeight(), Color.DARK_GRAY);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw waveform
        g2d.setColor(Color.GREEN);
        int midY = getHeight() / 2;
        float xScale = (float) getWidth() / BUFFER_SIZE;

        int[] xPoints = new int[BUFFER_SIZE];
        int[] yPoints = new int[BUFFER_SIZE];

        for (int i = 0; i < BUFFER_SIZE; i++) {
            xPoints[i] = (int) (i * xScale);
            yPoints[i] = midY - (int) (waveformBuffer[(bufferIndex + i) % BUFFER_SIZE] * midY);
        }

        g2d.drawPolyline(xPoints, yPoints, BUFFER_SIZE);
    }
}
