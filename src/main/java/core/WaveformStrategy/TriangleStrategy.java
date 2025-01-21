package core.WaveformStrategy;


public class TriangleStrategy implements WaveformStrategy {
    private double phase;
    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase;

        // Generate a sawtooth wave
        double saw = (phase / (2 * Math.PI)) % 1; // Normalized to range [0, 1)

        // Convert the sawtooth wave into a triangle wave
        double triangle = 2 * Math.abs(2 * saw - 1) - 1;

        return triangle * volume;
    }


    @Override
    public double getPhase() {
        return phase;
    }
}
