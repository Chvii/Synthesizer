package core.SynthLogic.Effects;

import java.util.HashMap;
import java.util.Map;

public class EffectPicker implements EffectRack {
    public enum EffectEnums {NONE, DESTRUCTION, NOISE, DELAYVERB, FILTER}

    private EffectRack currentEffect = new NullEffect();
    private final Map<EffectEnums, EffectRack> effectMap = new HashMap<>();

    public EffectPicker() {
        effectMap.put(EffectEnums.DESTRUCTION, new DestructionEffect());
        effectMap.put(EffectEnums.NOISE, new NoiseEffect());
        effectMap.put(EffectEnums.NONE, new NullEffect());
        effectMap.put(EffectEnums.DELAYVERB, new DelayVerb(0.5, 0.5, 0.3,false));
        effectMap.put(EffectEnums.FILTER, new FilterEffect(1, 0.5));
    }

    public void setEffect(EffectEnums effectEnum) {
        currentEffect = effectMap.getOrDefault(effectEnum, new NullEffect());
    }

    public EffectRack getCurrentEffect() {
        return currentEffect;
    }

    @Override
    public double[] applyEffect(double[] mixBuffer) {
        return currentEffect.applyEffect(mixBuffer);
    }
    @Override
    public EffectRack getEffect() {
        return this;
    }
}
