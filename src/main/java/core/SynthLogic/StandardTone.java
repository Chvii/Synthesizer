package core.SynthLogic;

import core.SynthLogic.Effects.EffectRack;
import core.WaveformStrategy.*;

import javax.sound.sampled.SourceDataLine;

public class StandardTone implements Tone {
    private Mixer mixer;
    private double octave = 1;
    private WaveformStrategy waveformStrategy;
    private WaveformStrategyPicker waveformStrategyPicker;
    private EffectRack effectRack;

    public StandardTone(SourceDataLine line, WaveformStrategyPicker waveformStrategyPicker, Mixer mixer) {
        this.mixer = mixer;
        this.waveformStrategyPicker = waveformStrategyPicker;
        mixer.start();
    }

    public Mixer getMixer() {
        return mixer;
    }

    @Override
    public void play(Note note, double velocity) { // TODO: Include midiKey as a parameter, or figure out how to implement octave switch with midi controller
        if (mixer.getActiveVoices().stream().anyMatch(v -> v.getNote() == note)){
            mixer.overrideVoice(note);
        }
        this.waveformStrategy = waveformStrategyPicker.chooseWaveformStrategy();
        mixer.addVoice(new StandardVoice(note, velocity, waveformStrategy));
    }
    public void setWaveformStrategy(WaveformStrategy waveformStrategy){
        this.waveformStrategy = waveformStrategy;
    }



    public void stop(Note note) { // TODO: Include midiKey as a parameter, or figure out how to implement octave switch with midi controller
        mixer.removeVoice(note);
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