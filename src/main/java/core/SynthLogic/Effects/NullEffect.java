package core.SynthLogic.Effects;

public class NullEffect implements EffectRack{
    @Override
    public double[] applyEffect(double[] mixBuffer) {
        return mixBuffer;
    }
}
