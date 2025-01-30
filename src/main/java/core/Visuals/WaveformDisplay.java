package core.Visuals;

import core.SynthLogic.Oscillator;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class WaveformDisplay extends Canvas {
    private final Oscillator oscillator;

    public WaveformDisplay(Oscillator oscillator, int width, int height) {
        super(width, height);
        this.oscillator = oscillator;
        drawWaveform();
    }

    public void drawWaveform() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        double[] samples = oscillator.generateWaveformSamples();
        gc.setStroke(Color.web("#64c8ff"));
        gc.setLineWidth(2);

        for(int i = 0; i < samples.length-1; i++) {
            double x1 = map(i, 0, samples.length, 0, getWidth());
            double y1 = map(samples[i], -1, 1, getHeight(), 0);
            double x2 = map(i+1, 0, samples.length, 0, getWidth());
            double y2 = map(samples[i+1], -1, 1, getHeight(), 0);
            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    private double map(double value, double start1, double end1, double start2, double end2) {
        return start2 + (value - start1) * (end2 - start2) / (end1 - start1);
    }
}