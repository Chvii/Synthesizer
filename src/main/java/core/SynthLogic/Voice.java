package core.SynthLogic;

import core.Constants.ConstantValues;
import core.WaveformStrategy.WaveformStrategy;

public class Voice {
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

    public synchronized float[] generateAudio() {
        float[] buffer = new float[ConstantValues.BUFFER_SIZE];

        if (volume <= 0.0) return buffer;

        double step = 2 * Math.PI * note.getFrequency() / ConstantValues.SAMPLE_RATE;

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (float) waveformStrategy.generateSample(phase, volume);
            phase += step;
            if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
        }

        if (!isPlaying) {
            volume -= 0.01;
            if (volume < 0) volume = 0;
        }

        return buffer;
    }

    public void stopVoice() {
        isPlaying = false;
    }

    public boolean isStopped() {
        if(!isPlaying && volume <= 0.0){
            return true;
        }
        return false;
    }
    public Note getNote(){
        return this.note;
    }

    public char getKeyChar() {
        return keyChar;
    }
}
