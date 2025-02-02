package core.SynthLogic.Effects;

import core.Constants.ConstantValues;
import core.Visuals.JKnob;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilterEffect implements EffectRack, ParameterizedEffect, Serializable {
    private final double MIN_FREQUENCY = 20.0; // Minimum cutoff frequency (20 Hz)
    private final double MAX_FREQUENCY = 20000.0; // Maximum cutoff frequency (20,000 Hz)
    private double knobPosition;
    private double cutoffFrequency;
    private double resonance; // Resonance (Q factor)
    private int filterStages; // Number of filter stages for roll-off intensity
    private List<BiquadFilterStage> stages; // List of filter stages for chaining
    private static final long serialVersionUID = 1L;

    /**
     * @param knobPosition Position of the cutoff knob, clockwise to increase cutoff frequency,
     *                     counter-clockwise to decrease cutoff frequency
     * @param resonance Resonance bump (Q factor). 0.5 for no bump, 0.707 for Butterworth.
     */
    public FilterEffect(double knobPosition, double resonance) {
        this.knobPosition = knobPosition;
        this.resonance = resonance;
        this.cutoffFrequency = calculateCutoff(knobPosition); // Initialize cutoff frequency

        setIntensity(FilterIntensity._12); // Default to 12 dB/octave
    }

    /** I am not sure why I am using an enum either, it is what my brain can comprehend.
     *
     */
    public enum FilterIntensity {
        _6(6), _12(12), _18(18), _24(24);

        private final int value;

        FilterIntensity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }



    public void setCutoff(double knobPosition) {
        this.knobPosition = knobPosition; // Store the current knob position
        this.cutoffFrequency = calculateCutoff(knobPosition);
        for (BiquadFilterStage stage : stages) {
            stage.updateCoefficients(cutoffFrequency, resonance);
        }
    }

    private double calculateCutoff(double knobPosition) {
        return MIN_FREQUENCY * (double) Math.pow(MAX_FREQUENCY / MIN_FREQUENCY, knobPosition);
    }

    /**
     * Updates the filter intensity (dB roll-off per octave).
     *
     * @param intensity The desired filter intensity (6, 12, 18, 24 dB/octave).
     */
    public void setIntensity(FilterIntensity intensity) {
        int value = intensity.getValue();
        this.filterStages = value / 6; // Number of cascaded 6dB stages
        stages = new ArrayList<>();
        for (int i = 0; i < filterStages; i++) {
            stages.add(new BiquadFilterStage(cutoffFrequency, resonance));
        }
    }

    @Override
    public double[] applyEffect(double[] mixBuffer) {
        for (BiquadFilterStage stage : stages) {
            mixBuffer = stage.applyStage(mixBuffer);
        }
        return mixBuffer;
    }

    public void setCutoffFrequency(double cutoffFrequency) {
        this.cutoffFrequency = cutoffFrequency;
        for (BiquadFilterStage stage : stages) {
            stage.updateCoefficients(cutoffFrequency, resonance);
        }
    }

    public void setResonance(double resonance) {
        this.resonance = resonance;
        for (BiquadFilterStage stage : stages) {
            stage.updateCoefficients(cutoffFrequency, resonance);
        }
    }

    // For changing effects and updating them dynamically with the GUI
    public enum Parameter implements ParameterizedEffect.Parameter {
        CUTOFF("Cutoff",0.0,1.0,1.0),
        RESONANCE("Resonance",0.5,1.5,0.5),
        INTENSITY("Slope", 0, FilterIntensity.values().length-1, 1);
        private final String displayName;
        private final double min;
        private final double max;
        private final double defaultValue;
        private static final long serialVersionUID = 1L;


        Parameter(String displayName, double min, double max, double defaultValue) {
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
            return this == INTENSITY ? ControlType.LIST : ControlType.KNOB;
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
        public Enum<?>[] getOptions() {
            return this == INTENSITY ? FilterIntensity.values() : new Enum<?>[0];
        }
    }

    @Override
    public List<ParameterizedEffect.Parameter> getParameters() {
        return List.of(Parameter.values());
    }

    @Override
    public void setParameter(ParameterizedEffect.Parameter param, double value) {
        if (param instanceof FilterEffect.Parameter) {
            switch ((Parameter) param) {
                case CUTOFF -> setCutoff(value);
                case RESONANCE -> setResonance(value);
                case INTENSITY -> setIntensity(FilterIntensity.values()[(int) Math.round(value)]);
            }
        }
    }

    @Override
    public double getParameter(ParameterizedEffect.Parameter param) {
        if (param instanceof FilterEffect.Parameter) {
            return switch ((Parameter) param) {
                case CUTOFF -> this.knobPosition;
                case RESONANCE -> this.resonance;
                case INTENSITY -> this.filterStages; // times 6 to get slope amount
            };
        }
        return 0;
    }


    /**
     * Nested class for a biquad filter stage.
     */
    private static class BiquadFilterStage {
        private double cutoffFrequency;
        private double resonance;
        private double inputCoefficient0, inputCoefficient1, inputCoefficient2;
        private double feedbackCoefficient1, feedbackCoefficient2;
        private double previousInputSample1, previousInputSample2;
        private double previousOutputSample1, previousOutputSample2;

        public BiquadFilterStage(double cutoffFrequency, double resonance) {
            this.cutoffFrequency = cutoffFrequency;
            this.resonance = resonance;
            calculateCoefficients();
        }

        private void calculateCoefficients() {
            double angularFrequency = (double) (2 * Math.PI * cutoffFrequency / ConstantValues.SAMPLE_RATE);
            double alpha = (double) Math.sin(angularFrequency) / (2 * resonance);
            double cosAngularFrequency = (double) Math.cos(angularFrequency);

            inputCoefficient0 = (1 - cosAngularFrequency) / 2;
            inputCoefficient1 = 1 - cosAngularFrequency;
            inputCoefficient2 = (1 - cosAngularFrequency) / 2;

            double normalizationFactor = 1 + alpha;
            feedbackCoefficient1 = -2 * cosAngularFrequency / normalizationFactor;
            feedbackCoefficient2 = (1 - alpha) / normalizationFactor;

            inputCoefficient0 /= normalizationFactor;
            inputCoefficient1 /= normalizationFactor;
            inputCoefficient2 /= normalizationFactor;
        }

        public double[] applyStage(double[] mixBuffer) {
            for (int i = 0; i < mixBuffer.length; i++) {
                double currentInput = mixBuffer[i];

                // Apply the biquad filter equation
                double currentOutput = inputCoefficient0 * currentInput
                        + inputCoefficient1 * previousInputSample1
                        + inputCoefficient2 * previousInputSample2
                        - feedbackCoefficient1 * previousOutputSample1
                        - feedbackCoefficient2 * previousOutputSample2;

                // Shift previous samples
                previousInputSample2 = previousInputSample1;
                previousInputSample1 = currentInput;
                previousOutputSample2 = previousOutputSample1;
                previousOutputSample1 = currentOutput;

                // Update mixbuffer
                mixBuffer[i] = currentOutput;
            }
            return mixBuffer;
        }

        public void updateCoefficients(double cutoffFrequency, double resonance) {
            this.cutoffFrequency = cutoffFrequency;
            this.resonance = resonance;
            calculateCoefficients();
        }
    }



}
