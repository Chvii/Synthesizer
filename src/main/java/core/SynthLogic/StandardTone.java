package core.SynthLogic;

import core.WaveformStrategy.WaveformStrategy;
import core.WaveformStrategy.WaveformStrategyPicker;
import javax.sound.sampled.SourceDataLine;

public class StandardTone implements Tone {
    private final Mixer mixer;
    private WaveformStrategyPicker[] waveformPickers;
    private WaveformStrategy waveformStrategy;
    private Oscillator[] oscillators;

    public StandardTone(SourceDataLine line, WaveformStrategyPicker[] waveformPickers, Mixer mixer) {
        this.mixer = mixer;
        this.waveformPickers = waveformPickers;
        this.oscillators = createOscillators(); // Oscillators are created dynamically
        mixer.startMixer();
    }

    private Oscillator[] createOscillators() {
        Oscillator[] oscillators = new Oscillator[3]; // Example: 3 oscillators
        for (int i = 0; i < oscillators.length; i++) {
            oscillators[i] = new StandardOscillator(
                    waveformPickers[i].chooseWaveformStrategy(),
                    0,  // Slight detune for stereo richness
                    1.0,            // Gain
                    1.0               // Octave shift
            );
        }
        return oscillators;
    }

    @Override
    public Oscillator[] getOscillators() {
        return oscillators;
    }

    @Override
    public Oscillator getOscillator(int i) {
        return oscillators[i];
    }

    @Override
    public void play(Note note, double velocity) {
        if (mixer.getActiveVoices().stream().anyMatch(v -> v.getNote() == note)) {
            mixer.overrideVoice(note);
        }
        // Create new oscillators for the voice
        Oscillator[] voiceOscillators = new Oscillator[oscillators.length];
        for (int i = 0; i < oscillators.length; i++) {
            Oscillator original = oscillators[i];
            voiceOscillators[i] = new StandardOscillator(
                    original.getWaveformStrategy(),
                    original.getDetune(),
                    original.getGain(),
                    original.getOctaveShift()
            );
        }
        mixer.addVoice(new StandardVoice(note, velocity, voiceOscillators));
    }

    @Override
    public void stop(Note note) {
        mixer.removeVoice(note);
    }

    @Override
    public void nextWaveform(int i) {
        waveformPickers[i].nextWaveform();
    }


    @Override
    public void previousWaveform(int i) {
        waveformPickers[i].previousWaveform();
    }

    @Override
    public void updateOscillatorWaveforms(int i) {
        oscillators[i].setWaveformStrategy(waveformPickers[i].chooseWaveformStrategy());
    }

    @Override
    public String getWaveformName(int oscillatorIndex) {
        return waveformPickers[oscillatorIndex].StringifyWaveformSelector();
    }
}