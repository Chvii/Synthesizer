package core.SynthLogic.Effects;

public class NullEffect implements EffectRack{
    @Override
    public double[] applyEffect(double[] mixBuffer) {
        return mixBuffer;
    }

    @Override
    public EffectRack getEffect() {
        return this;
    }
}
