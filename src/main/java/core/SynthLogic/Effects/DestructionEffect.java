package core.SynthLogic.Effects;

public class DestructionEffect implements EffectRack {
    @Override
    public float[] applyEffect(float[] mixBuffer) {
        for (int i = 0; i < mixBuffer.length; i++) {
            mixBuffer[i] += Math.pow(mixBuffer[i], Math.random()/20000000);
            mixBuffer[i] = softClip(mixBuffer[i]);
            if(i % 8 == 0) {
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
}
