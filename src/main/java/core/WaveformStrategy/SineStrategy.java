package core.WaveformStrategy;


import java.util.Random;

public class SineStrategy implements WaveformStrategy {
    private double phase;


    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase % (2 * Math.PI); // Normalize phase
        return Math.sin(this.phase) * volume;
    }

    @Override
    public double getPhase() {
        return phase;
    }

    @Override
    public String Stringify() {
        return "Sine";
    }
}