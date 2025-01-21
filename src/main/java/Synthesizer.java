import core.SynthLogic.Tone;

import javax.sound.sampled.*;

public class Synthesizer {
    /*
    Main implementation of the synthesizer, run this class to bask in the glory of this top-of-the-line synthesizer
     */
    public static void main(String[] args) throws LineUnavailableException{
         {
            final AudioFormat af = new AudioFormat(384000, 16, 2, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af, 384000);
            line.start();

            new Tone(line);

        }
    }
}
