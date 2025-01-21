package core.EnvelopeStrategy;

public class EnvelopeWithSlowAttack implements EnvelopeStrategy {
    private final double attackRate = 0.005;
    private final double decayRate = 0.002;
    private final double sustainLevel = 0.6;
    private final double releaseRate = 0.003;
    private double currentVolume = 0.0;
    private boolean isReleasing = false;

    @Override
    public void startEnvelope() {
        currentVolume = 0.0;
        isReleasing = false; // Ensure not in release phase
    }

    @Override
    public double applyEnvelope(boolean isPlaying) {
        if (isPlaying && !isReleasing) {
            // Attack phase
            if (currentVolume < 1.0) {
                currentVolume = Math.min(currentVolume + attackRate, 1.0);
            }
            // Decay phase
            else if (currentVolume > sustainLevel) {
                currentVolume = Math.max(currentVolume - decayRate, sustainLevel);
            }
        } else if (!isPlaying) {
            // Release phase
            isReleasing = true;
            currentVolume = Math.max(currentVolume - releaseRate, 0.0);
        }
        return currentVolume;
    }
}
