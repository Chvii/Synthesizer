package core.SynthLogic.Effects;

import core.Constants.ConstantValues;
import core.SynthLogic.Mixer;

import java.util.Arrays;
import java.util.Random;

public class NoiseEffect implements EffectRack{
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
