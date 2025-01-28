package core.SynthLogic;

import core.WaveformStrategy.WaveformStrategy;
import core.WaveformStrategy.WaveformStrategyPicker;

public interface Oscillator {
    double generateSample(double baseFrequency, double volume);

    void setWaveformStrategy(WaveformStrategy waveformStrategy);
    WaveformStrategy getWaveformStrategy();
    void setDetune(double detune);
    void setGain(double gain);
    void setOctaveShift(double octaveShift);
    double getOctaveShift();
    void octaveUp();
    void octaveDown();
    double getFrequency();
    double getDetune();
    double getGain();
    double[] generateWaveformSamples();
}


