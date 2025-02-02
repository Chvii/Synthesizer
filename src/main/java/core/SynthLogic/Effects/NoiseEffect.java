package core.SynthLogic.Effects;

public class NoiseEffect implements EffectRack {
    public NoiseEffect(){
    }
    @Override
    public double[] applyEffect(double[] mixBuffer) {
        for(int i = 0; i < mixBuffer.length; i++){
            mixBuffer[i] += Math.min(mixBuffer[i],Math.random()/80)/5;
        }
        return mixBuffer;
    }

}
