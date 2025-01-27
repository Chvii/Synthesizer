package core.SynthLogic.Effects;

import core.Constants.ConstantValues;
import java.util.ArrayList;
import java.util.List;

public class FilterEffect implements EffectRack {
    private final float MIN_FREQUENCY = 20.0f; // Minimum cutoff frequency (20 Hz)
    private final float MAX_FREQUENCY = 20000.0f; // Maximum cutoff frequency (20,000 Hz)
    private float knobPosition;
    private float cutoffFrequency;
    private float resonance; // Resonance (Q factor)
    private int filterStages; // Number of filter stages for roll-off intensity
    private List<BiquadFilterStage> stages; // List of filter stages for chaining

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

    /**
     * @param knobPosition Position of the cutoff knob, clockwise to increase cutoff frequency,
     *                     counter-clockwise to decrease cutoff frequency
     * @param resonance Resonance bump (Q factor). 0.5 for no bump, 0.707 for Butterworth.
     */
    public FilterEffect(float knobPosition, float resonance) {
        this.knobPosition = knobPosition;
        this.resonance = resonance;
        this.cutoffFrequency = calculateCutoff(knobPosition); // Initialize cutoff frequency

        setIntensity(FilterIntensity._12); // Default to 12 dB/octave
    }

    public void setCutoff(float knobPosition) {
        this.cutoffFrequency = calculateCutoff(knobPosition);
        for (BiquadFilterStage stage : stages) {
            stage.updateCoefficients(cutoffFrequency, resonance);
        }
        System.out.println(cutoffFrequency);
    }

    private float calculateCutoff(float knobPosition) {
        return MIN_FREQUENCY * (float) Math.pow(MAX_FREQUENCY / MIN_FREQUENCY, knobPosition);
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
    public float[] applyEffect(float[] mixBuffer) {
        for (BiquadFilterStage stage : stages) {
            mixBuffer = stage.applyStage(mixBuffer);
        }
        return mixBuffer;
    }

    public void setCutoffFrequency(float cutoffFrequency) {
        this.cutoffFrequency = cutoffFrequency;
        for (BiquadFilterStage stage : stages) {
            stage.updateCoefficients(cutoffFrequency, resonance);
        }
    }

    public void setResonance(float resonance) {
        this.resonance = resonance;
        for (BiquadFilterStage stage : stages) {
            stage.updateCoefficients(cutoffFrequency, resonance);
        }
    }

    /**
     * Nested class for a biquad filter stage.
     */
    private static class BiquadFilterStage {
        private float cutoffFrequency;
        private float resonance;
        private float inputCoefficient0, inputCoefficient1, inputCoefficient2;
        private float feedbackCoefficient1, feedbackCoefficient2;
        private float previousInputSample1, previousInputSample2;
        private float previousOutputSample1, previousOutputSample2;

        public BiquadFilterStage(float cutoffFrequency, float resonance) {
            this.cutoffFrequency = cutoffFrequency;
            this.resonance = resonance;
            calculateCoefficients();
        }

        private void calculateCoefficients() {
            float angularFrequency = (float) (2 * Math.PI * cutoffFrequency / ConstantValues.SAMPLE_RATE);
            float alpha = (float) Math.sin(angularFrequency) / (2 * resonance);
            float cosAngularFrequency = (float) Math.cos(angularFrequency);

            inputCoefficient0 = (1 - cosAngularFrequency) / 2;
            inputCoefficient1 = 1 - cosAngularFrequency;
            inputCoefficient2 = (1 - cosAngularFrequency) / 2;

            float normalizationFactor = 1 + alpha;
            feedbackCoefficient1 = -2 * cosAngularFrequency / normalizationFactor;
            feedbackCoefficient2 = (1 - alpha) / normalizationFactor;

            inputCoefficient0 /= normalizationFactor;
            inputCoefficient1 /= normalizationFactor;
            inputCoefficient2 /= normalizationFactor;
        }

        public float[] applyStage(float[] mixBuffer) {
            for (int i = 0; i < mixBuffer.length; i++) {
                float currentInput = mixBuffer[i];

                // Apply the biquad filter equation
                float currentOutput = inputCoefficient0 * currentInput
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

        public void updateCoefficients(float cutoffFrequency, float resonance) {
            this.cutoffFrequency = cutoffFrequency;
            this.resonance = resonance;
            calculateCoefficients();
        }
    }

}
