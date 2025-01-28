package core.WaveformStrategy;


public class DnBWaveStrategy implements WaveformStrategy {
    private double phase;
    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase % (2 * Math.PI);
        return Math.abs(phase%1.2) * volume;
    }

    @Override
    public double getPhase() {
        return phase;
    }

    @Override
    public String Stringify() {
        return "DnB";
    }
}
