package core.WaveformStrategy;


public class DnBWaveStrategy implements WaveformStrategy {
    private double phase;
    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase;
        return Math.abs(phase%1.2) * volume;
    }

    @Override
    public double getPhase() {
        return phase;
    }
}
