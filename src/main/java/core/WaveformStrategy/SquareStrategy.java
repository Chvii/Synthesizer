package core.WaveformStrategy;


public class SquareStrategy implements WaveformStrategy {
    private double phase;


    @Override
    public double generateSample(double phase, double volume) {
        this.phase = phase; // Update the angle in the strategy
        return (phase > Math.PI ? 1 : -1) * volume;
    }

    @Override
    public double getPhase(){
        return phase;
    }
}