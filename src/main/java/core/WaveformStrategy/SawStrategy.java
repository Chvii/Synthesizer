package core.WaveformStrategy;


public class SawStrategy implements WaveformStrategy {
    private double phase;

    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase % (2 * Math.PI); // Normalize phase
        return ((this.phase / (2 * Math.PI)) * 2 - 1) * volume;
    }

    @Override
    public double getPhase() {
        return phase;
    }
}