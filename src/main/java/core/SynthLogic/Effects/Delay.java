package core.SynthLogic.Effects;

import core.Constants.ConstantValues;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class Delay implements EffectRack, ParameterizedEffect {
    private double[] delayBuffer;
    private int writeIndex = 0;
    private double feedback;
    private double mix;
    private double delayTimeInSeconds;
    private boolean stereoMode;

    public Delay(double delayTimeInSeconds, double feedback, double mix, boolean stereoMode) {
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
    public void setStereoMode(boolean enabled) {
        this.stereoMode = enabled;
    }



    public enum Parameter implements ParameterizedEffect.Parameter{
        TIME("Time",0.05,2,0.5),
        FEEDBACK("Feedback",0,1,0.5),
        MIX("Mix",0,1,0.3),
        STEREO_MODE("Stereo", 0.0, 1.0, 0.0);
        private final String displayName;
        private final double min;
        private final double max;
        private final double defaultValue;
        Parameter(String displayName, double min, double max, double defaultValue){
            this.displayName = displayName;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
        }
        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public double getMin() {
            return min;
        }

        @Override
        public double getMax() {
            return max;
        }

        @Override
        public double getDefault() {
            return defaultValue;
        }
        @Override
        public ControlType getControlType() {
            return this == STEREO_MODE ? ControlType.BUTTON : ControlType.KNOB;
        }
    }

    @Override
    public List<ParameterizedEffect.Parameter> getParameters() {
        return List.of(Parameter.values());
    }

    @Override
    public void setParameter(ParameterizedEffect.Parameter paramName, double value) {
        if (paramName instanceof Delay.Parameter){
            switch((Parameter)paramName){
                case TIME -> setDelayTimeInSeconds(value);
                case FEEDBACK -> setFeedback(value);
                case MIX -> setMix(value);
                case STEREO_MODE -> setStereoMode(value >= 0.5);

            }
        }
    }

    @Override
    public double getParameter(ParameterizedEffect.Parameter paramName) {
        if (paramName instanceof Delay.Parameter){
            return switch((Parameter)paramName){
                case TIME -> this.delayTimeInSeconds;
                case FEEDBACK -> this.feedback;
                case MIX -> this.mix;
                case STEREO_MODE -> stereoMode ? 1.0 : 0.0;
            };
        }
        return 0;
    }
}