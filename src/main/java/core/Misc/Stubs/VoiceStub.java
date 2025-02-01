package core.Misc.Stubs;

import core.Constants.ConstantValues;
import core.SynthLogic.Note;
import core.SynthLogic.Voice;

public class VoiceStub implements Voice {
    @Override
    public double[] generateAudio() {
        double stubValue = 1;
        double[] stubArray = new double[ConstantValues.BUFFER_SIZE];
        for(int i = 0; i < ConstantValues.BUFFER_SIZE; i++){
            stubArray[i] = stubValue + i;
        }
        return stubArray;
    }

    @Override
    public void stopVoice() {

    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public Note getNote() {
        return null;
    }
}
