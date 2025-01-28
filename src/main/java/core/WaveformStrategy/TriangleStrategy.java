package core.WaveformStrategy;


public class TriangleStrategy implements WaveformStrategy {
    private double phase;
    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase % (2 * Math.PI); // Normalize phase

        // Generate a sawtooth wave normalized to [0, 1)
        double saw = this.phase / (2 * Math.PI);

        // Convert the sawtooth wave into a triangle wave
        double triangle = 2 * Math.abs(2 * saw - 1) - 1;

        return triangle * volume;
    }


    @Override
    public double getPhase() {
        return phase;
    }

    @Override
    public String Stringify() {
        return "Triangle";
    }
}
