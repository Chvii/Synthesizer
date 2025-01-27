package core.SynthLogic;

public interface Note {
    double getFrequency();

    void setFrequency(double frequency);

    void modulateFrequency(double modulator);
}
