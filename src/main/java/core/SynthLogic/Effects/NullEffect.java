package core.SynthLogic.Effects;

public class NullEffect implements EffectRack{
    @Override
    public float[] applyEffect(float[] mixBuffer) {
        return mixBuffer;
    }
}
