import core.Misc.Stubs.SourceDataLineStub;
import core.Misc.Stubs.VoiceStub;
import core.SynthLogic.*;
import core.SynthLogic.Effects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;

public class CoreTester {
    private EffectChain effectChain;
    private Mixer mixer;
    private SourceDataLine lineStub;
    private Voice voiceStub;

    @BeforeEach
    public void setUp() throws LineUnavailableException {
        lineStub = new SourceDataLineStub();
        effectChain = new EffectChain();
        mixer = new StandardMixer(lineStub, effectChain);
        voiceStub = new VoiceStub();
    }
    @Test
    public void effectChainShouldReorderEffectArrayCorrectly(){

        EffectRack effect0 = new NullEffect();
        EffectRack effect1 = new NullEffect();
        EffectRack effect2 = new NullEffect();
        EffectRack effect3 = new NullEffect();
        EffectRack effect4 = new NullEffect();
        EffectRack effect5 = new NullEffect();
        EffectRack effect6 = new NullEffect();
        EffectRack effect7 = new NullEffect();
        effectChain.addEffect(effect0);
        effectChain.addEffect(effect1);
        effectChain.addEffect(effect2);
        effectChain.addEffect(effect3);
        effectChain.addEffect(effect4);
        effectChain.addEffect(effect5);
        effectChain.addEffect(effect6);
        effectChain.addEffect(effect7);
        //ensure the list doesnt have rogue elements in it
        assertThat(effectChain.getEffects().size(), is(8));
        assertThat(effectChain.getEffects().get(4), is(effect4));

        // call reorderEffect
        effectChain.reorderEffect(6,1);

        //
        assertThat(effectChain.getEffects().get(1), is(effect6));
        // Check that index6 is not effect 6
        assertThat(effectChain.getEffects().get(6), not(effect6));

        assertThat(effectChain.getEffects().get(2), is(effect1));
        assertThat(effectChain.getEffects().get(3), is(effect2));
        assertThat(effectChain.getEffects().get(4), is(effect3));
        assertThat(effectChain.getEffects().get(5), is(effect4));
        assertThat(effectChain.getEffects().get(6), is(effect5));

        // try with toIndex > fromIndex
        effectChain.reorderEffect(1,6);

        // should go back to original state

        assertThat(effectChain.getEffects().get(1), is(effect1));
        // Check that index6 is not effect 6
        assertThat(effectChain.getEffects().get(6), not(effect1));

        assertThat(effectChain.getEffects().get(2), is(effect2));
        assertThat(effectChain.getEffects().get(3), is(effect3));
        assertThat(effectChain.getEffects().get(4), is(effect4));
        assertThat(effectChain.getEffects().get(5), is(effect5));
        assertThat(effectChain.getEffects().get(6), is(effect6));
    }
    @Test
    public void mixerShouldCorrectlyApplyEffectsInEffectChain(){
        EffectRack effect0 = new NullEffect();
        EffectRack effect1 = new Delay(10,10,10,false);
        EffectRack effect2 = new FilterEffect(0.6,0.5);

        effectChain.addEffect(effect0);
        effectChain.addEffect(effect1);
        effectChain.addEffect(effect2);

        Mixer newMixer = new StandardMixer(lineStub,effectChain);
        newMixer.addVoice(voiceStub);
        newMixer.startMixer();
        assertThat(newMixer.getActiveEffects(), is(effectChain.getEffects()));


        EffectRack effect3 = new NoiseEffect();
        effectChain.addEffect(effect3);
        assertThat(newMixer.getActiveEffects(), is(effectChain.getEffects()));
        System.out.println(newMixer.getActiveEffects());
        effectChain.reorderEffect(1,3);
        System.out.println(newMixer.getActiveEffects());
        assertThat(newMixer.getActiveEffects(), is(effectChain.getEffects()));
        effectChain.reorderEffect(2,0);
        System.out.println(newMixer.getActiveEffects());
        assertThat(newMixer.getActiveEffects(), is(effectChain.getEffects()));
    }


}
