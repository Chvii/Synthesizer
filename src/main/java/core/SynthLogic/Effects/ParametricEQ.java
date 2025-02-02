package core.SynthLogic.Effects;

import core.Constants.ConstantValues;

import java.io.Serializable;
import java.util.List;

public class ParametricEQ implements EffectRack, ParameterizedEffect, Serializable {
    public final EQBand[] bands = new EQBand[5];
    private final double sampleRate = ConstantValues.SAMPLE_RATE;
    private static final long serialVersionUID = 1L;


    public ParametricEQ() {
        // Initialize bands
        double[] defaultFreqs = {100, 400, 1600, 6400, 16000};
        for(int i=0; i<bands.length; i++) {
            bands[i] = new EQBand(defaultFreqs[i]);
        }
    }

    @Override
    public double[] applyEffect(double[] mixBuffer) {
        for(EQBand band : bands) {
            mixBuffer = band.process(mixBuffer);
        }
        return mixBuffer;
    }

    // Band configuration class
    public class EQBand {
        public double frequency;
        public double q = 1.0;
        public double gainDB = 0.0;
        private final BiquadFilterStage filter;

        public EQBand(double frequency) {
            this.frequency = frequency;
            this.filter = new BiquadFilterStage();
            updateCoefficients();
        }

        public double[] process(double[] buffer) {
            return filter.applyStage(buffer);
        }

        private void updateCoefficients() {
            filter.updateCoefficients(frequency, q, gainDB);
        }

        public void setFrequency(double frequency) {
            this.frequency = frequency;
            updateCoefficients();
        }

        public void setQ(double q) {
            this.q = q;
            updateCoefficients();
        }

        public void setGainDB(double gainDB) {
            this.gainDB = gainDB;
            updateCoefficients();
        }
    }

    // Biquad filter implementation for peaking EQ
    private static class BiquadFilterStage {
        private double b0, b1, b2, a1, a2;
        private double x1, x2, y1, y2;

        void updateCoefficients(double freq, double q, double gainDB) {
            double omega = 2 * Math.PI * freq / ConstantValues.SAMPLE_RATE;
            double alpha = Math.sin(omega) / (2 * q);
            double A = Math.pow(10, gainDB/40);
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

        double[] applyStage(double[] buffer) {
            double[] output = new double[buffer.length];
            for(int i = 0; i < buffer.length; i++) {
                double x0 = buffer[i];
                double y0 = b0*x0 + b1*x1 + b2*x2 - a1*y1 - a2*y2;

                x2 = x1;
                x1 = x0;
                y2 = y1;
                y1 = y0;
                output[i] = y0;
            }
            return output;
        }
    }

    // Parameter implementation
    public enum Parameter implements ParameterizedEffect.Parameter {
        BAND1_FREQ("Band 1 Freq", 20, 20000, 100),
        BAND1_GAIN("Band 1 Gain", -12, 12, 0),
        BAND1_Q("Band 1 Q", 0.1, 10, 1),
        BAND2_FREQ("Band 2 Freq", 20, 20000, 400),
        BAND2_GAIN("Band 2 Gain", -12, 12, 0),
        BAND2_Q("Band 2 Q", 0.1, 10, 1),
        BAND3_FREQ("Band 3 Freq", 20, 20000, 1600),
        BAND3_GAIN("Band 3 Gain", -12, 12, 0),
        BAND3_Q("Band 3 Q", 0.1, 10, 1),
        BAND4_FREQ("Band 4 Freq", 20, 20000, 6400),
        BAND4_GAIN("Band 4 Gain", -12, 12, 0),
        BAND4_Q("Band 4 Q", 0.1, 10, 1),
        BAND5_FREQ("Band 5 Freq", 20, 20000, 16000),
        BAND5_GAIN("Band 5 Gain", -12, 12, 0),
        BAND5_Q("Band 5 Q", 0.1, 10, 1);

        private final String displayName;
        private final double min;
        private final double max;
        private final double defaultValue;
        private static final long serialVersionUID = 1L;

        Parameter(String name, double min, double max, double def) {
            this.displayName = name;
            this.min = min;
            this.max = max;
            this.defaultValue = def;
        }

        @Override public String getDisplayName() { return displayName; }
        @Override public ControlType getControlType() { return ControlType.KNOB; }
        @Override public double getMin() { return min; }
        @Override public double getMax() { return max; }
        @Override public double getDefault() { return defaultValue; }
        @Override public Enum<?>[] getOptions() { return new Enum<?>[0]; }
    }

    @Override
    public List<ParameterizedEffect.Parameter> getParameters() {
        return List.of(Parameter.values()); // Return all enum values
    }


    @Override
    public void setParameter(ParameterizedEffect.Parameter param, double value) {
        if (param instanceof Parameter) {
            Parameter p = (Parameter) param;
            int bandIndex = p.ordinal() / 3;  // 3 parameters per band (freq, gain, Q)
            int paramType = p.ordinal() % 3;

            EQBand band = bands[bandIndex];

            switch (paramType) {
                case 0 -> band.setFrequency(value);
                case 1 -> band.setGainDB(value);
                case 2 -> band.setQ(value);
            }
        }
    }

    @Override
    public double getParameter(ParameterizedEffect.Parameter param) {
        if (param instanceof Parameter) {
            Parameter p = (Parameter) param;
            int bandIndex = p.ordinal() / 3;
            int paramType = p.ordinal() % 3;

            EQBand band = bands[bandIndex];

            return switch (paramType) {
                case 0 -> band.frequency;
                case 1 -> band.gainDB;
                case 2 -> band.q;
                default -> 0;
            };
        }
        return 0;
    }
}