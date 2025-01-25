package core.SynthLogic.Effects;

import java.util.HashMap;
import java.util.Map;

public class EffectPicker implements EffectRack {
    public enum EffectEnums {NONE, DESTRUCTION, NOISE}

    private EffectRack currentEffect = new NullEffect();
    private final Map<EffectEnums, EffectRack> effectMap = new HashMap<>();

    public EffectPicker() {
        effectMap.put(EffectEnums.DESTRUCTION, new DestructionEffect());
        effectMap.put(EffectEnums.NOISE, new NoiseEffect());
        effectMap.put(EffectEnums.NONE, new NullEffect());
    }

    public void setEffect(EffectEnums effectEnum) {
        currentEffect = effectMap.getOrDefault(effectEnum, new NullEffect());
    }

    @Override
    public float[] applyEffect(float[] mixBuffer) {
        return currentEffect.applyEffect(mixBuffer);
    }
}
