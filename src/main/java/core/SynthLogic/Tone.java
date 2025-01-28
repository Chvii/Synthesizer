package core.SynthLogic;

import core.WaveformStrategy.WaveformStrategy;

public interface Tone {
    Oscillator[] getOscillators();
    Oscillator getOscillator(int i);
    void play(Note note, double velocity);
    void stop(Note note);
    void nextWaveform(int i);
    void previousWaveform(int i);
    void updateOscillatorWaveforms(int i);
    String getWaveformName(int oscillatorIndex);
}
