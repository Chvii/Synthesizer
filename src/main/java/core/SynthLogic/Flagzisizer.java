package core.SynthLogic;

import core.SynthLogic.*;
import core.SynthLogic.Controller.*;
import core.SynthLogic.Controller.SynthController;
import core.SynthLogic.Effects.*;
import core.SynthLogic.Mixer;
import core.Visuals.GUIFrontendStuff;
import core.WaveformStrategy.WaveformStrategyPicker;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.List;

public class Flagzisizer {
    private Tone tone;
    private Voice voice;
    private Mixer mixer;
    private Note note;
    private SynthController synthController;


    public Flagzisizer(Tone tone, Voice voice, Mixer mixer, Note note, SynthController synthController) {
        this.tone = tone;
        this.voice = voice;
        this.mixer = mixer;
        this.note = note;
        this.synthController = synthController;
    }
    public void startSynth(SynthController synthController){
        // I would love to do all the synth logic here, and remove some tight-coupling
    }
}