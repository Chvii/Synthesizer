package core.SynthLogic.Effects;

import core.Constants.ConstantValues;

import java.util.List;
import java.util.Random;

public class DestructionEffect implements EffectRack, ParameterizedEffect {
    private double sauce;
    private double highEndTamer;
    private double frequency;

    public DestructionEffect(double sauce, double highEndTamer, double frequency){
        this.sauce = sauce;
        this.highEndTamer = highEndTamer;
        this.frequency = frequency;
    }

    @Override
    public double[] applyEffect(double[] mixBuffer) {
        BiquadFilterStage filter = new BiquadFilterStage();
        for (int i = 0; i < mixBuffer.length; i++) {
            // Apply wavefolding and harmonic emphasis
            double sample = mixBuffer[i];

            // Wavefolding to generate harmonics
            sample = waveFold(sample * 3); // Increase gain before folding

            // Targeted phase inversion to emphasize specific harmonics
            if (i % 7 == 0) {
                sample = -sample;
            }

            // Add controlled distortion
            sample += Math.tanh(sample * sauce) * 0.7;
            sample = filter.applyStage(sample, highEndTamer, frequency);   // tame the high freq a bit

            mixBuffer[i] = softClip(sample * 1.4);      // Adjust output level
        }
        return mixBuffer;
    }

    private double waveFold(double sample) {
        while (Math.abs(sample) > 1.0) {
            sample = Math.abs(Math.abs(sample) - 2.0) - 1.0;
        }
        return sample;
    }

    private double softClip(double sample) {
        return Math.tanh(sample);
    }

    public void setSauce(double sauceAmount){
        this.sauce = sauceAmount;
        System.out.println("Sauce: " + this.sauce);
    }

    public void setHighEnd(double highEndTamer){
        this.highEndTamer = highEndTamer;
        System.out.println("high tamer: " + this.highEndTamer);
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
        System.out.println("Frequency: " + this.frequency);
    }

    public enum Parameter implements ParameterizedEffect.Parameter {
        SAUCE("Sauce", 1, 10, 5),
        HIGHENDTAMER("Brightness tamer", 1, 100, 60),
        FREQUENCY("Brightness Frequency", 4000, 10000, 6000);
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
        public ControlType getControlType() {
            return ControlType.KNOB;
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
    }

    @Override
    public List<ParameterizedEffect.Parameter> getParameters() {
        return List.of(Parameter.values());
    }

    @Override
    public void setParameter(ParameterizedEffect.Parameter paramName, double value) {
        if (paramName instanceof DestructionEffect.Parameter){
            switch((DestructionEffect.Parameter)paramName){
                case SAUCE -> setSauce(value);
                case HIGHENDTAMER -> setHighEnd(value);
                case FREQUENCY -> setFrequency(value);
            }
        }
    }

    @Override
    public double getParameter(ParameterizedEffect.Parameter paramName) {
        if (paramName instanceof DestructionEffect.Parameter){
            return switch((DestructionEffect.Parameter)paramName){
                case SAUCE -> this.sauce;
                case HIGHENDTAMER -> this.highEndTamer;
                case FREQUENCY -> this.frequency;
            };
        }
        return 0;
    }

    private static class BiquadFilterStage {
        private double b0, b1, b2, a1, a2;
        private double x1, x2, y1, y2;

        void updateCoefficients(double gainDB, double frequency) {
            double omega = 2 * Math.PI * frequency / ConstantValues.SAMPLE_RATE;
            double alpha = Math.sin(omega) / (2 * 30);
            double A = Math.pow(10, -gainDB/30);
            double a0;

            // Peaking EQ coefficients (RBJ filter design)
            b0 = 1 + alpha * A;
            b1 = -2 * Math.cos(omega);
            b2 = 1 - alpha * A;
            a0 = 1 + alpha / A;
            a1 = -2 * Math.cos(omega);
            a2 = 1 - alpha / A;

            // Normalize coefficients
            b0 /= a0;
            b1 /= a0;
            b2 /= a0;
            a1 /= a0;
            a2 /= a0;
        }

        double applyStage(double buffer, double dbGain, double freq) {
            updateCoefficients(dbGain, freq);
            double output;
                double x0 = buffer;
                double y0 = b0*x0 + b1*x1 + b2*x2 - a1*y1 - a2*y2;

                x2 = x1;
                x1 = x0;
                y2 = y1;
                y1 = y0;
                output = y0;
            return output;
        }
    }
}