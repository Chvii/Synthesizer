package core;

import core.SynthLogic.Controller.KeyboardToSynth;
import core.SynthLogic.Controller.SynthController;
import core.SynthLogic.Effects.EffectController;
import core.SynthLogic.Effects.EffectPicker;
import core.SynthLogic.Mixer;
import core.SynthLogic.Tone;
import core.Visuals.GUIFrontendStuff;
import core.WaveformStrategy.WaveformStrategyPicker;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class MainSynthesizerImplementation {
    public static void main(String[] args) throws LineUnavailableException, MidiUnavailableException {
        final AudioFormat af = new AudioFormat(44100, 16, 2, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open(af, 2500);
        System.out.println(line.getBufferSize());
        line.start();
        WaveformStrategyPicker waveformStrategyPicker = new WaveformStrategyPicker();
        EffectPicker effectPicker = new EffectPicker();
        EffectController effectController = new EffectController(effectPicker);
        Mixer mixer = new Mixer(line, effectPicker); // Mixer uses the EffectPicker
        Tone tone = new Tone(line, waveformStrategyPicker,mixer);
        GUIFrontendStuff gui = new GUIFrontendStuff(mixer, waveformStrategyPicker, tone, effectController);
        Receiver receiver = null;
        SynthController synthController = new SynthController(receiver, tone);
        new KeyboardToSynth(tone).run();
    }
}
