package core.WaveformStrategy;

public interface WaveformStrategy {
    double generateSample(double phase, double volume);
    double getPhase();
    String Stringify();
}
