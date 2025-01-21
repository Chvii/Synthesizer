package core.SynthLogic;

import core.Constants.ConstantValues;
import core.WaveformStrategy.WaveformStrategy;

class Voice {
    private final Note note;
    private final char keyChar;
    private volatile boolean isPlaying = true;
    private double phase = 0;
    private double volume = 1.0;
    private final WaveformStrategy waveformStrategy;

    public Voice(Note note, char keyChar, WaveformStrategy waveformStrategy) {
        this.note = note;
        this.keyChar = keyChar;
        this.waveformStrategy = waveformStrategy;
    }

    public float[] generateAudio() {
        float[] buffer = new float[ConstantValues.BUFFER_SIZE]; // Normalized float samples (-1.0 to 1.0)

        if (!isPlaying && volume <= 0.0) return buffer;

        double step = 2 * Math.PI * note.getFrequency() / Note.SAMPLE_RATE;

        for (int i = 0; i < buffer.length; i++) {
            // Generate sample using the waveform strategy
            double sample = waveformStrategy.generateSample(phase, volume);

            // Normalize to the range [-1.0, 1.0]
            buffer[i] = (float) sample;

            // Update the phase
            phase += step;
            if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
        }

        // Apply fade-out when the voice is stopping
        if (!isPlaying) {
            volume -= 0.01; // Gradual fade-out
            if (volume < 0) volume = 0;
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
