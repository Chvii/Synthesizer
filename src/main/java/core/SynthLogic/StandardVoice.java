package core.SynthLogic;

import core.Constants.ConstantValues;
import core.WaveformStrategy.WaveformStrategy;

public class StandardVoice implements Voice {
    private final Note note;
    private volatile boolean isPlaying = true;
    private double volume = 0.0;
    private boolean attackPhase = true;
    private static double attackTime = 0.1;
    private boolean decayPhase = false;
    private static double decayTime = 0.1;
    private static double sustainLevel = 1.0;
    private static double releaseTime = 0.1;
    private double velocity;

    private Oscillator[] oscillators = new Oscillator[3]; // Three oscillators

    public StandardVoice(Note note, double velocity, Oscillator[] oscillators) {
        this.note = note;
        this.velocity = velocity;
        this.oscillators = oscillators;
    }

    @Override
    public synchronized double[] generateAudio() {
        double[] buffer = new double[ConstantValues.BUFFER_SIZE];
        if (attackPhase && isPlaying) {
            volume += attackTime;
        }
        if (volume >= 1.0 && isPlaying) {
            attackPhase = false;
            decayPhase = true;
        }
        if (decayPhase && isPlaying) {
            volume -= decayTime;
            if (volume <= sustainLevel && isPlaying) {
                decayPhase = false;
                volume = sustainLevel;
            }
        }

        if (volume <= 0.0) return buffer;

        double baseFrequency = note.getFrequency();
        for (int i = 0; i < buffer.length; i++) {
            for (Oscillator osc : oscillators) {
                buffer[i] += osc.generateSample(baseFrequency, volume) / oscillators.length;
            }
        }

        if (!isPlaying) {
            volume -= releaseTime;
            if (volume < 0) {
                volume = 0;
                attackPhase = true;
                decayPhase = false;
            }
        }

        return buffer;
    }

    @Override
    public void stopVoice() {
        isPlaying = false;
    }

    @Override
    public boolean isStopped() {
        return !isPlaying && volume <= 0.0;
    }

    @Override
    public Note getNote() {
        return this.note;
    }
    public static void setAttackTime(double time) {
        attackTime = 1.0 / (time * 1000 / ConstantValues.BUFFER_SIZE);
    }
    public static double getAttackTime(){
        return attackTime;
    }
    public static void setDecayTime(double time) {
        decayTime = 1.0 / (time * 1000 / ConstantValues.BUFFER_SIZE);
    }

    public static double getDecayTime() {
        return decayTime;
    }
    public static void setSustainLevel(double level){
        sustainLevel = level;
    }
    public static double getSustainLevel() {
        return sustainLevel;
    }
    public static void setReleaseTime(double time) {
        releaseTime = 1.0 / (time * 1000 / ConstantValues.BUFFER_SIZE);
    }
    public static double getReleaseTime(){
        return releaseTime;
    }
}