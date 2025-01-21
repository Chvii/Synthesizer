package core.SynthLogic;

import core.Constants.ConstantValues;
import core.WaveformStrategy.WaveformStrategy;

/**
 * Generates audio from the selected waveform
 *
 *
 *
 */

class Voice {
    private final Note note;
    private final char keyChar;
    private volatile boolean isPlaying = true;
    private double phase = 0;
    private double volume = 1.0;
    private WaveformStrategy waveformStrategy;

    public Voice(Note note, char keyChar, WaveformStrategy waveformStrategy) {
        this.note = note;
        this.keyChar = keyChar;
        this.waveformStrategy = waveformStrategy;
    }

    public byte[] generateAudio() {
        // TODO: Implement ADSR

        byte[] buffer = new byte[ConstantValues.BUFFER_SIZE];
        if (!isPlaying && volume <= 0.0) return buffer;

        double step = 2 * Math.PI * note.getFrequency() / Note.SAMPLE_RATE;

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) (waveformStrategy.generateSample(phase, volume)*127); // Use the given WaveformStrategy (given in the Tone class currently)
            phase += step;
            if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
        }

        if (!isPlaying) {
            volume -= 0.1; // Gradual fade-out, TODO: Handle this with ADSR
        }

        return buffer;
    }

    public void stopVoice() {
        isPlaying = false;
    }

    public boolean isStopped() {
        return !isPlaying && volume <= 0.0;
    }

    public char getKeyChar() {
        return keyChar;
    }
}