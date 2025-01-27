package core.SynthLogic;

import core.WaveformStrategy.WaveformStrategy;

public class OscillatorOne implements Oscillator {
    private WaveformStrategy waveformStrategy;
    private double detune; // Frequency offset (in Hz)
    private double gain;   // Individual oscillator volume (0.0 - 1.0)
    private boolean enabled = true; // Default to enabled
    private double frequency;

    public OscillatorOne(WaveformStrategy waveformStrategy, double detune, double gain) {
        this.waveformStrategy = waveformStrategy;
        this.detune = detune;
        this.gain = gain;
    }

    @Override
    public double generateSample(Note note, double phase, double velocity) {
        double frequency = note.getFrequency() + detune;
        return waveformStrategy.generateSample(phase, gain * velocity / 100);
    }
    @Override
    public void setWaveformStrategy(WaveformStrategy waveformStrategy) {
        this.waveformStrategy = waveformStrategy;
    }

    @Override
    public WaveformStrategy getWaveformStrategy() {
        return this.waveformStrategy;
    }

    @Override
    public void setGain(double gain) {
        this.gain = gain;
    }

    @Override
    public void setDetune(double detune) {
        this.detune = detune;
    }

    @Override
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public double getDetune() {
        return this.detune;
    }

    @Override
    public double getGain() {
        return this.gain;
    }
}
