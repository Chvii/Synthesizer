package core.Visuals;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class WaveformPanel extends JPanel implements WaveformUpdateListener {
    private static final int MAX_SAMPLES = 1024; // Rolling buffer size
    private final CopyOnWriteArrayList<Float> waveformBuffer; // Holds normalized samples
    private Color waveformColor = new Color(0, 153, 255); // Sleek blue for the waveform
    private Color backgroundColor = Color.BLACK; // Elegant black background
    private int updateRate = 60; // FPS cap for rendering
    private float maxAmplitude = 1.0f; // Dynamic amplitude scaling

    public WaveformPanel() {
        this.waveformBuffer = new CopyOnWriteArrayList<>(Arrays.asList(new Float[MAX_SAMPLES]));
        setPreferredSize(new Dimension(800, 200)); // Default size
        setBackground(backgroundColor);

        // Timer for frame rate control
        new Timer(1000 / updateRate, e -> repaint()).start();
    }

    /**
     * Updates the waveform with new samples.
     *
     * @param newSamples The new audio samples to display.
     */
    @Override
    public void updateWaveform(float[] newSamples) {
        synchronized (waveformBuffer) {
            // Normalize and add new samples to the buffer
            for (float sample : newSamples) {
                waveformBuffer.add(sample / maxAmplitude); // Normalize to max amplitude
                if (waveformBuffer.size() > MAX_SAMPLES) {
                    waveformBuffer.remove(0); // Keep the buffer within the max size
                }
            }

            // Dynamically adjust max amplitude for scaling
            float maxSample = 1.0f;
            for (float sample : newSamples) {
                maxSample = Math.max(maxSample, Math.abs(sample));
            }


            maxAmplitude = Math.max(maxAmplitude * 0.95f, maxSample); // Smooth scaling
        }
    }

    /**
     * Paints the waveform using anti-aliased rendering.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw the waveform
        synchronized (waveformBuffer) {
            if (!waveformBuffer.isEmpty()) {
                g2d.setColor(waveformColor);
                int width = getWidth();
                int height = getHeight();
                int centerY = height / 2;
                float xStep = (float) width / (float) MAX_SAMPLES;

                // Draw connected lines for the waveform
                for (int i = 0; i < waveformBuffer.size() - 1; i++) {
                    float x1 = i * xStep;
                    float x2 = (i + 1) * xStep;

                    float y1 = centerY - waveformBuffer.get(i) * (centerY - 10); // Scale to panel height
                    float y2 = centerY - waveformBuffer.get(i + 1) * (centerY - 10);

                    g2d.draw(new Line2D.Float(x1, y1, x2, y2));
                }
            }
        }
    }

    /**
     * Allows customization of the waveform's appearance.
     */
    public void setWaveformColor(Color color) {
        this.waveformColor = color;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        setBackground(color);
    }

    public void setUpdateRate(int rate) {
        this.updateRate = Math.max(1, rate); // Minimum 1 FPS
    }
}
