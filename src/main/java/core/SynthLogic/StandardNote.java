package core.SynthLogic;

public class StandardNote implements Note {

    private double frequency;

    public StandardNote(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    @Override
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
    @Override
    public void modulateFrequency(double modulator){
        setFrequency(frequency * modulator);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardNote note = (StandardNote) o;
        return Double.compare(note.frequency, frequency) == 0;
    }

    // Changes default behaviour of the equals method
    @Override
    public int hashCode() {
        return Double.hashCode(frequency);
    }

    // for midi
    public static final Note init = new StandardNote(0.0);



    // for keyboard
    public static final Note C4 = new StandardNote(261.63);
    public static final Note Csharp4 = new StandardNote(277.18);
    public static final Note D4 = new StandardNote(293.66);
    public static final Note Dsharp4 = new StandardNote(311.13);
    public static final Note E4 = new StandardNote(329.63);
    public static final Note F4 = new StandardNote(349.23);
    public static final Note Fsharp4 = new StandardNote(369.99);
    public static final Note G4 = new StandardNote(392.00);
    public static final Note Gsharp4 = new StandardNote(415.30);
    public static final Note A4 = new StandardNote(440.00);
    public static final Note Asharp4 = new StandardNote(466.16);
    public static final Note B4 = new StandardNote(493.88);
}