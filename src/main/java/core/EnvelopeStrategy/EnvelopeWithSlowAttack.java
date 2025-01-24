package core.EnvelopeStrategy;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class EnvelopeWithSlowAttack implements EnvelopeStrategy {


    private final double attackRate = 10;
    private final double decayRate = 10;
    private final double sustainLevel = 0.6;
    private final double releaseRate = 10;
    private double currentVolume = 0.0;
    private boolean isReleasing = false;

    @Override
    public double startEnvelope(double volume) {
        volume = this.currentVolume;
        for(int i = 0; i<attackRate; i++){
            if(currentVolume > 1.0){
                currentVolume = currentVolume + 0.1;
            }
        }
        for(int i = 0; i<decayRate; i++){
            if(currentVolume>sustainLevel){
                currentVolume = currentVolume -0.1;
            }
        }
        return volume;
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
