package core.SynthLogic;

import core.WaveformStrategy.WaveformStrategy;
import core.WaveformStrategy.WaveformStrategyPicker;

public interface Oscillator {
    double generateSample(Note note, double phase, double velocity);
    void setWaveformStrategy(WaveformStrategy waveformStrategy);
    WaveformStrategy getWaveformStrategy();
    
    void setGain(double gain);
    void setDetune(double detune);
    void setFrequency(double frequency);
    double getFrequency();
    void setEnabled(boolean enabled);
    boolean isEnabled();

    double getDetune();

    double getGain();
}
