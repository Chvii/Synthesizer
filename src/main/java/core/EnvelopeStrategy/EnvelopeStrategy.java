package core.EnvelopeStrategy;

public interface EnvelopeStrategy {
    double startEnvelope(double volume);
    double applyEnvelope(boolean isPlaying);
}
