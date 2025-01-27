package core.SynthLogic;

import core.WaveformStrategy.WaveformStrategy;

public interface Tone {
    Mixer getMixer();

    void play(Note note, double velocity);

    void setWaveformStrategy(WaveformStrategy waveformStrategy);

    void stop(Note note);

    void increaseOctave();

    String getOctaveString();

    void decreaseOctave();

    double getOctave();
}
