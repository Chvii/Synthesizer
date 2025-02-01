package core.SynthLogic.Effects;

import core.Constants.ConstantValues;

import java.util.Arrays;

public class DelayVerb implements EffectRack {
    private double[] delayBuffer;
    private int writeIndex = 0;
    private double feedback;
    private double mix;
    private double delayTimeInSeconds;
    private boolean stereoMode;

    public DelayVerb(double delayTimeInSeconds, double feedback, double mix, boolean stereoMode) {
        this.delayTimeInSeconds = delayTimeInSeconds;
        this.delayBuffer = new double[(int) (ConstantValues.SAMPLE_RATE * delayTimeInSeconds)];
        this.feedback = feedback;
        this.mix = mix;
        this.stereoMode = stereoMode;
    }

    @Override
    public double[] applyEffect(double[] mixBuffer) {
        double[] outputBuffer = Arrays.copyOf(mixBuffer, mixBuffer.length);

        for (int i = 0; i < mixBuffer.length; i++) {
            double delayedSample = delayBuffer[writeIndex];
            double newSample = mixBuffer[i] + delayedSample * feedback;
            if(stereoMode){
                if(i % 2 == 0){
                    newSample = -mixBuffer[i] + -delayedSample * -feedback;
                }
            }
            delayBuffer[writeIndex] = newSample; // Write to the delay buffer

            outputBuffer[i] = (mixBuffer[i] * (1 - mix)) + (delayedSample * mix);
            writeIndex = (writeIndex + 1) % delayBuffer.length;
        }
        return outputBuffer;
    }

    public void setDelayTimeInSeconds(double delayTimeInSeconds) {
        if (this.delayTimeInSeconds != delayTimeInSeconds) {
            this.delayTimeInSeconds = delayTimeInSeconds;

            // Resize the delay buffer
            int newSize = (int) (ConstantValues.SAMPLE_RATE * delayTimeInSeconds);
            double[] newBuffer = new double[newSize];

            // Copy the existing contents into the new buffer, if smaller
            if (newSize < delayBuffer.length) {
                System.arraycopy(delayBuffer, 0, newBuffer, 0, newSize);
            } else {
                System.arraycopy(delayBuffer, 0, newBuffer, 0, delayBuffer.length);
            }

            delayBuffer = newBuffer;

            // Reset the write index to prevent buffer overflow issues
            writeIndex = writeIndex % delayBuffer.length;
        }
    }

    public void setFeedback(double feedback) {
        this.feedback = feedback;
    }

    public void setMix(double mix) {
        this.mix = mix;
    }
    public void setStereoMode(){
        if (stereoMode == false){
            stereoMode = true;
        } else if (stereoMode == true){
            stereoMode = false;
        }
    }
    @Override
    public EffectRack getEffect() {
        return this;
    }
}