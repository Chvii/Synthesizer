package core.SynthLogic;

import core.Constants.ConstantValues;
import core.WaveformStrategy.WaveformStrategy;

public class StandardOscillator implements Oscillator{
    private WaveformStrategy waveformStrategy;
    private double detune;  // Frequency offset in Hz
    private double gain;    // Volume multiplier (0.0 to 1.0)
    private double octaveShift; // Octave up/down (-2, -1, 0, +1, +2)
    private double frequency;
    private double phase = 0;
    private int prettyOctave = 0;
    private volatile boolean isActive;

    public StandardOscillator(WaveformStrategy waveformStrategy, double detune, double gain, double octaveShift) {
        this.waveformStrategy = waveformStrategy;
        this.detune = detune;
        this.gain = gain;
        this.octaveShift = octaveShift;
    }



    @Override
    public double generateSample(double baseFrequency, double volume) {
        double frequency = (baseFrequency * octaveShift) + detune;
        double sample = waveformStrategy.generateSample(phase, volume * gain) * gain;
        if(!isActive){
            sample = 0;
        }
        phase += 2 * Math.PI * frequency / ConstantValues.SAMPLE_RATE;
        if (phase > 2 * Math.PI) {
            phase -= 2 * Math.PI;
        }
        return sample;
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
    public void setDetune(double detune) {
        this.detune = detune;
    }

    @Override
    public void setGain(double gain) {
        this.gain = gain;
    }

    @Override
    public void setOctaveShift(double octaveShift) {
        this.octaveShift = octaveShift;
    }

    @Override
    public void octaveUp() {
        this.octaveShift *= 2;
        prettyOctave += 1;
    }

    @Override
    public void octaveDown() {
        this.octaveShift /= 2;
        prettyOctave -= 1;
    }

    public double getDetune(){
        return this.detune;
    }
    public double getGain(){
        return this.gain;
    }

    @Override
    public double getOctaveShift(){
        return octaveShift;
    }

    @Override
    public int getPrettyOctaveValue(){
        return prettyOctave;
    }

    @Override
    public boolean getIsActive() {
        return isActive;
    }

    @Override
    public void setIsActive(boolean active){
        this.isActive = active;
    }

    @Override
    public double getFrequency() {
        return this.frequency;
    }

    @Override
    public double[] generateWaveformSamples() {
        double[] samples = new double[200];
        double phaseIncrement = 2 * Math.PI / samples.length;
        double phase = 0;

        for (int i = 0; i < samples.length; i++) {
            samples[i] = waveformStrategy.generateSample(phase, 1.0);
            phase += phaseIncrement;
            if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
        }
        return samples;
    }
}