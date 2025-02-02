package core.SynthLogic.Effects;

import java.util.List;

public class SaturatorEffect implements EffectRack, ParameterizedEffect{
    private double drive;
    private double mix;

    public SaturatorEffect(){
        this.drive = Parameter.DRIVE.getDefault();
        this.mix = Parameter.MIX.getDefault();
    }

    @Override
    public double[] applyEffect(double[] mixBuffer) {
        for (int i = 0; i < mixBuffer.length; i++){
            double input = mixBuffer[i];
            double saturatedBuffer = Math.tanh(input * drive);
            mixBuffer[i] = saturatedBuffer * mix + input * (1.0 - mix);
        }
        return mixBuffer;
    }

    private double clamp(double value, Parameter param) {
        return Math.max(param.getMin(), Math.min(param.getMax(), value));
    }

    public enum Parameter implements ParameterizedEffect.Parameter {
        DRIVE("Drive", 1.0, 10.0, 1.0),
        MIX("Mix", 0.0, 1.0, 1.0);

        private final String displayName;
        private final double min;
        private final double max;
        private final double defaultValue;

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
        @Override
        public Enum<?>[] getOptions() {
            return new Enum<?>[0];
        }
    }

    @Override
    public List<ParameterizedEffect.Parameter> getParameters() {
        return List.of(Parameter.values());
    }

    @Override
    public void setParameter(ParameterizedEffect.Parameter param, double value) {
        if (param instanceof Parameter) {
            switch ((Parameter) param) {
                case DRIVE -> this.drive = clamp(value, Parameter.DRIVE);
                case MIX -> this.mix = clamp(value, Parameter.MIX);
            }
        }
    }

    @Override
    public double getParameter(ParameterizedEffect.Parameter param) {
        if (param instanceof Parameter) {
            return switch ((Parameter) param) {
                case DRIVE -> this.drive;
                case MIX -> this.mix;
            };
        }
        return 0;
    }

}
