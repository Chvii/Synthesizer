package core.SynthLogic;

import core.Constants.ConstantValues;
import core.WaveformStrategy.WaveformStrategy;

public class Voice {
    private final Note note;
    private volatile boolean isPlaying = true;
    private double phase = 0;
    private double volume = 0.0;
    private final WaveformStrategy waveformStrategy;
    private boolean attackPhase = true;
    private static double attackTime = 0.1;
    private boolean decayPhase = false;
    private static double decayTime = 0.1;
    private static double sustainLevel = 1.0;
    private static double releaseTime = 0.1;
    private double velocity;
    private static double octave = 1;

    public Voice(Note note, double velocity, WaveformStrategy waveformStrategy) {
        this.note = note;
        this.velocity = velocity;

        this.waveformStrategy = waveformStrategy;
    }

    public synchronized double[] generateAudio() {
        double[] buffer = new double[ConstantValues.BUFFER_SIZE];
        if(attackPhase && isPlaying){
            volume += attackTime;
        }
        if(volume >= 1.0 && isPlaying){
            attackPhase = false;
            decayPhase = true;
        }
        if(decayPhase && isPlaying){
            volume -= decayTime;
            if(volume <= sustainLevel && isPlaying){
                decayPhase = false;
                volume = sustainLevel;
            }
        }

        if (volume <= 0.0) return buffer;

        double step = 2 * Math.PI * note.getFrequency() / ConstantValues.SAMPLE_RATE * octave;

        for (int i = 0; i < buffer.length; i++) {
            if(velocity<20){
                velocity = 20;
            }
            buffer[i] = (double) waveformStrategy.generateSample(phase, volume)*(velocity/100);
            phase += step;
            if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
        }

        if (!isPlaying) {
            volume -= releaseTime;
            if (volume < 0){
                volume = 0;
                attackPhase = true;
                decayPhase = false;
            }

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
    public static void increaseOctave() {
        octave = octave * 2;
    }

    public static void decreaseOctave() {
        octave = octave / 2;
    }

    public static double getOctave() {
        return octave;
    }
    public static String getOctaveString(){
        return String.valueOf(octave);
    }

    public static void setAttackTime(double time){
        attackTime = 0.1/time;
    }
    public static double getAttackTime(){
        return attackTime;
    }
    public static void setDecayTime(double time){
        decayTime = 0.1/time;
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
    public static void setReleaseTime(double time){
        releaseTime = 0.1/time;
    }
    public static double getReleaseTime(){
        return releaseTime;
    }
}
