package core.SynthLogic.Effects;

import core.Constants.ConstantValues;
import org.jtransforms.fft.FloatFFT_1D;

import java.util.Random;

public class DestructionEffect implements EffectRack {
    @Override
    public float[] applyEffect(float[] mixBuffer) {
        for (int i = 0; i < mixBuffer.length; i++) {
            mixBuffer[i] += Math.pow(mixBuffer[i], randomDistorter(20));
            mixBuffer[i] = softClip(mixBuffer[i]);
            if (i % 8 == 0) {
                // reverse phase every 8th loop
                mixBuffer[i] = -mixBuffer[i];
            }
        }
        return mixBuffer;
    }

    private float softClip(float sample) {
        if (sample > 1.0f) {
            return 1.0f - (1.0f / (1.0f + sample));
        } else if (sample < -1.0f) {
            return -1.0f + (1.0f / (1.0f - sample));
        } else {
            return sample;
        }
    }

    private float randomDistorter(float amount) {
        Random randomGenerator = new Random();

        float distortionValue = randomGenerator.nextFloat();
        float randomDistortion = distortionValue / amount;
        return randomDistortion;
    }

}
