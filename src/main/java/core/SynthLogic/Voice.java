package core.SynthLogic;

public interface Voice {
    double[] generateAudio();

    void stopVoice();

    boolean isStopped();

    Note getNote();
}
