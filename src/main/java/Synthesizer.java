import core.EnvelopeStrategy.EnvelopeWithSlowAttack;
import core.SynthLogic.*;
import core.SynthLogic.Effects.EffectRack;
import core.SynthLogic.Effects.*;
import core.SynthLogic.Mixer;
import core.Visuals.GUIFrontendStuff;
import core.WaveformStrategy.WaveformStrategyPicker;

import javax.sound.sampled.*;

public class Synthesizer {
    public static void main(String[] args) throws LineUnavailableException {
        final AudioFormat af = new AudioFormat(44100, 16, 2, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open(af, (int) af.getSampleRate());
        line.start();
        WaveformStrategyPicker waveformStrategyPicker = new WaveformStrategyPicker();
        EffectRack noise = new NullEffect();
        Mixer mixer = new Mixer(line,noise);

        Tone tone = new Tone(line, waveformStrategyPicker,mixer);
        GUIFrontendStuff gui = new GUIFrontendStuff(tone.getMixer(),waveformStrategyPicker,tone);
        new SynthController(tone, gui); // Attach the controller
    }
}
