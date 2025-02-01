package core.SynthLogic;

import core.SynthLogic.Effects.EffectRack;
import core.Visuals.WaveformUpdateListener;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public interface Mixer {
    void addWaveformUpdateListener(WaveformUpdateListener listener);

    void removeWaveformUpdateListener(WaveformUpdateListener listener);

    void addVoice(Voice voice);

    void removeVoice(Note note);

    void overrideVoice(Note note);

    CopyOnWriteArrayList<Voice> getActiveVoices();

    void startMixer();

    ArrayList<EffectRack> getActiveEffects();
}