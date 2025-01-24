package core.WaveformStrategy;

public class WaveformStrategyPicker {
    private enum WaveformSelector {SINE, TRIANGLE, SAW, SQUARE, DNB}

    private WaveformSelector waveformSelector; // Instance variable to store the selected waveform

    // Constructor to initialize with a default waveform (SINE in this case)
    public WaveformStrategyPicker() {
        this.waveformSelector = WaveformSelector.SINE;
    }

    // Method to cycle to the next waveform (e.g., when right arrow is clicked)
    public void nextWaveform() {
        int ordinal = (waveformSelector.ordinal() + 1) % WaveformSelector.values().length;
        waveformSelector = WaveformSelector.values()[ordinal];
    }

    // Method to cycle to the previous waveform (e.g., when left arrow is clicked)
    public void previousWaveform() {
        int ordinal = (waveformSelector.ordinal() - 1 + WaveformSelector.values().length) % WaveformSelector.values().length;
        waveformSelector = WaveformSelector.values()[ordinal];
    }

    // Method to choose the appropriate waveform strategy
    public WaveformStrategy chooseWaveformStrategy() {
        return switch (waveformSelector) {
            case SINE -> new SineStrategy();
            case TRIANGLE -> new TriangleStrategy();
            case SAW -> new SawStrategy();
            case SQUARE -> new SquareStrategy();
            case DNB -> new DnBWaveStrategy();
        };
    }

    // Method to get the current waveform selector (optional, for GUI display purposes)
    public WaveformSelector getCurrentWaveform() {
        return waveformSelector;
    }
    public String StringifyWaveformSelector(){
        return waveformSelector.toString();
    }
}
