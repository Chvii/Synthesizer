package core.SynthLogic;

public class Note {

    private double frequency;

    public Note(double frequency) {
        this.frequency = frequency;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
    public Note modulateFrequency(double modulator){
        return new Note(frequency * modulator);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Double.compare(note.frequency, frequency) == 0;
    }

    // Changes default behaviour of the equals method
    @Override
    public int hashCode() {
        return Double.hashCode(frequency);
    }

    // for midi
    public static final Note init = new Note(0.0);



    // for keyboard
    public static final Note C4 = new Note(261.63);
    public static final Note Csharp4 = new Note(277.18);
    public static final Note D4 = new Note(293.66);
    public static final Note Dsharp4 = new Note(311.13);
    public static final Note E4 = new Note(329.63);
    public static final Note F4 = new Note(349.23);
    public static final Note Fsharp4 = new Note(369.99);
    public static final Note G4 = new Note(392.00);
    public static final Note Gsharp4 = new Note(415.30);
    public static final Note A4 = new Note(440.00);
    public static final Note Asharp4 = new Note(466.16);
    public static final Note B4 = new Note(493.88);
}