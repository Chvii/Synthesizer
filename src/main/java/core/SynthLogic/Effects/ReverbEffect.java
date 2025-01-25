package core.SynthLogic.Effects;

import java.util.Arrays;

public class ReverbEffect implements EffectRack {
    private final float[] delayBuffer;
    private int writeIndex = 0;
    private final float feedback;
    private final float mix;

    public ReverbEffect(int delaySamples, float feedback, float mix) {
        this.delayBuffer = new float[delaySamples];
        this.feedback = feedback;
        this.mix = mix;
    }

    @Override
    public float[] applyEffect(float[] mixBuffer) {
        float[] outputBuffer = Arrays.copyOf(mixBuffer, mixBuffer.length);

        for (int i = 0; i < mixBuffer.length; i++) {
            float delayedSample = delayBuffer[writeIndex];
            float newSample = mixBuffer[i] + delayedSample * feedback;

            delayBuffer[writeIndex] = newSample; // Write to the delay buffer
            outputBuffer[i] = (mixBuffer[i] * (1 - mix)) + (delayedSample * mix);

            writeIndex = (writeIndex + 1) % delayBuffer.length; // Circular buffer
        }

        return outputBuffer;
    }
}