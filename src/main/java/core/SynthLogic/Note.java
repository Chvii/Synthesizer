package core.SynthLogic;

class Note {
    /*
     *   Handles note sample rate and sets the basic frequencies for notes
     *
     */
    public static final double SAMPLE_RATE = 44100; // Updated sample rate to 44.1 kHz for compatibility with modern audio standards
    private double frequency;

    public Note(double frequency) {
        this.frequency = frequency;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double f) {
        this.frequency = f;
    }

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
