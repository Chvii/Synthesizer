package core.SynthLogic.Effects;

public interface EffectRack {
    double[] applyEffect(double[] mixBuffer);
    EffectRack getEffect();
}
