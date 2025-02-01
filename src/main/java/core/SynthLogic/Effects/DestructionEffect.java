package core.SynthLogic.Effects;

import core.Constants.ConstantValues;

import java.util.Random;

public class DestructionEffect implements EffectRack {
    @Override
    public double[] applyEffect(double[] mixBuffer) {
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

    private double softClip(double sample) {
        if (sample > 1.0) {
            return 1.0 - (1.0 / (1.0 + sample));
        } else if (sample < -1.0) {
            return -1.0 + (1.0 / (1.0 - sample));
        } else {
            return sample;
        }
    }

    private double randomDistorter(double amount) {
        Random randomGenerator = new Random();

        double distortionValue = randomGenerator.nextDouble();
        double randomDistortion = distortionValue / amount;
        return randomDistortion;
    }
    @Override
    public EffectRack getEffect() {
        return this;
    }
}
