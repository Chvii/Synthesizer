package core.WaveformStrategy;


public class SineStrategy implements WaveformStrategy {
    private double phase;

    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase; // Update the angle in the strategy
        return Math.sin(phase) * volume;
    }

    @Override
    public double getPhase() {
        return phase;
    }
}