package core.SynthLogic;

import core.SynthLogic.Effects.EffectRack;
import core.WaveformStrategy.*;

import javax.sound.sampled.SourceDataLine;

public class Tone {
    private Mixer mixer;
    private double octave = 1;
    private WaveformStrategy waveformStrategy;
    private WaveformStrategyPicker waveformStrategyPicker;
    private EffectRack effectRack;

    public Tone(SourceDataLine line, WaveformStrategyPicker waveformStrategyPicker, Mixer mixer) {
        this.mixer = mixer;
        this.waveformStrategyPicker = waveformStrategyPicker;
        mixer.start();
    }

    public Mixer getMixer() {
        return mixer;
    }

    public void play(Note note, char keyChar) {
        if (mixer.getActiveVoices().stream().anyMatch(v -> v.getKeyChar() == keyChar)){
            mixer.overrideVoice(keyChar);
        }
        this.waveformStrategy = waveformStrategyPicker.chooseWaveformStrategy();

        mixer.addVoice(new Voice(note, keyChar, waveformStrategy));
    }
    public void setWaveformStrategy(WaveformStrategy waveformStrategy){
        this.waveformStrategy = waveformStrategy;
    }



    public void stop(char keyChar) {
        mixer.removeVoice(keyChar);
    }

    public void increaseOctave() {
        octave = octave * 2;
    }

    public void decreaseOctave() {
        octave = octave / 2;
    }

    public double getOctave() {
        return octave;
    }
    public String getOctaveString(){
        return String.valueOf(octave);
    }
}
