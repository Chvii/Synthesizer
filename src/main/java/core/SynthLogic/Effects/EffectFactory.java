package core.SynthLogic.Effects;

public class EffectFactory {
    public enum EffectType {
        FILTER("Filter"),
        DELAY("Delay"),
        DISTORTION("Distortion"),
        NOISE("Noise"),
        REVERB("Reverb"),
        SATURATOR("Saturator"),
        EQ("Parametric Equalizer");

        private final String displayName;

        EffectType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static EffectRack createEffect(EffectType type) {
        return switch (type) {
            case FILTER -> new FilterEffect(1.0f, 0.5f);
            case DELAY -> new Delay(0.5f, 0.5f, 0.3f, false);
            case DISTORTION -> new DestructionEffect(DestructionEffect.Parameter.SAUCE.getDefault(), DestructionEffect.Parameter.HIGHENDTAMER.getDefault(), DestructionEffect.Parameter.FREQUENCY.getDefault());
            case NOISE -> new NoiseEffect();
            case SATURATOR -> new SaturatorEffect();
            case REVERB -> new NullEffect();
            case EQ -> new ParametricEQ();
        };
    }
}