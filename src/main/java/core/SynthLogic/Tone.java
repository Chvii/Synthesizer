// Refactored Tone class
package core.SynthLogic;

import core.Constants.ConstantValues;
import core.EnvelopeStrategy.EnvelopeStrategy;
import core.EnvelopeStrategy.EnvelopeWithSlowAttack;
import core.Visuals.WaveformPanel;
import core.WaveformStrategy.*;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class Tone extends JFrame implements KeyListener {
    private final SourceDataLine line;
    private final CopyOnWriteArrayList<Voice> activeVoices = new CopyOnWriteArrayList<>();
    private final Thread mixerThread;
    private double octave = 1;
    private final WaveformPanel waveformPanel;
    private WaveformStrategy waveformStrategy;
    private int strategySwitcher = 1;

    public Tone(SourceDataLine line) {
        this.line = line;
        this.addKeyListener(this);
        setTitle("core.SynthLogic.Tone Player with Waveform Analyzer");
        setSize(1800, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        waveformPanel = new WaveformPanel();
        add(waveformPanel);

        setVisible(true);

        // Mixer thread continuously combines audio from all active voices
        mixerThread = new Thread(() -> {
            float[] mixBuffer = new float[ConstantValues.BUFFER_SIZE];

            while (true) {
                // Clear the mix buffer
                Arrays.fill(mixBuffer, 0);

                // Mix all active voices
                int activeVoiceCount = activeVoices.size();
                if (activeVoiceCount > 0) {
                    for (Voice voice : activeVoices) {
                        float[] voiceBuffer = voice.generateAudio();

                        for (int i = 0; i < ConstantValues.BUFFER_SIZE; i++) {
                            mixBuffer[i] += voiceBuffer[i] / activeVoiceCount; // Average the voices
                        }
                    }

                    // Clamp the mixed buffer to the range [-1.0, 1.0]
                    for (int i = 0; i < mixBuffer.length; i++) {
                        mixBuffer[i] = Math.max(Math.min(mixBuffer[i], 1.0f), -1.0f);
                    }
                }

                // Update the waveform panel with the mixed buffer
                waveformPanel.updateWaveform(mixBuffer);

                // Convert the mix buffer to byte[] for the SourceDataLine
                byte[] byteBuffer = new byte[ConstantValues.BUFFER_SIZE * 2];
                for (int i = 0, j = 0; i < mixBuffer.length; i++, j += 2) {
                    int intSample = (int) (mixBuffer[i] * 32767); // Convert float to 16-bit PCM

                    // Pack into two bytes (big-endian)
                    byteBuffer[j] = (byte) ((intSample >> 8) & 0xFF);
                    byteBuffer[j + 1] = (byte) (intSample & 0xFF);
                }

                // Write byteBuffer to the SourceDataLine
                line.write(byteBuffer, 0, byteBuffer.length);

                // Remove stopped voices
                activeVoices.removeIf(Voice::isStopped);
            }
        });
        mixerThread.start();
    }

    private void play(Note note, char keyChar) {
        if (activeVoices.stream().anyMatch(v -> v.getKeyChar() == keyChar)) return;

        switch (strategySwitcher % 6) {
            case 1 -> waveformStrategy = new SineStrategy();
            case 2 -> waveformStrategy = new SquareStrategy();
            case 3 -> waveformStrategy = new SawStrategy();
            case 4 -> waveformStrategy = new TriangleStrategy();
            case 5 -> {
                waveformStrategy = new DnBWaveStrategy();
                strategySwitcher = 0;
            }
        }

        EnvelopeStrategy envelopeStrategy = new EnvelopeWithSlowAttack(); // Use ADSR envelope, just test implementation, not final.
        Voice voice = new Voice(note, keyChar, waveformStrategy);
        activeVoices.add(voice);
    }

    private void stop(char keyChar) {
        activeVoices.stream()
                .filter(v -> v.getKeyChar() == keyChar)
                .forEach(Voice::stopVoice);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == '+') {
            this.octave = octave * 2;
        }
        if(e.getKeyChar() == '$'){
            strategySwitcher++;
        }
        if (e.getKeyChar() == '-') {
            this.octave = octave / 2;
        }

        Note note = switch (e.getKeyChar()) {
            case 'q' -> new Note(Note.C4.getFrequency() * octave); // C
            case '2' -> new Note(Note.Csharp4.getFrequency() * octave); // C#
            case 'w' -> new Note(Note.D4.getFrequency() * octave); // D
            case '3' -> new Note(Note.Dsharp4.getFrequency() * octave); // D#
            case 'e' -> new Note(Note.E4.getFrequency() * octave); // E
            case 'r' -> new Note(Note.F4.getFrequency() * octave); // F
            case '5' -> new Note(Note.Fsharp4.getFrequency() * octave); // F#
            case 't' -> new Note(Note.G4.getFrequency() * octave); // G
            case '6' -> new Note(Note.Gsharp4.getFrequency() * octave); // G#
            case 'y' -> new Note(Note.A4.getFrequency() * octave); // A
            case '7' -> new Note(Note.Asharp4.getFrequency() * octave); // A#
            case 'u' -> new Note(Note.B4.getFrequency() * octave); // B
            case 'i' -> new Note(Note.C4.getFrequency() * 2 * octave); // C
            case '9' -> new Note(Note.Csharp4.getFrequency() * 2 * octave); // C#
            case 'o' -> new Note(Note.D4.getFrequency() * 2 * octave); // D
            case '0' -> new Note(Note.D4.getFrequency() * 2 * octave); // D#
            case 'p' -> new Note(Note.E4.getFrequency() * 2 * octave); // E
            default -> null;
        };

        if (note != null) {
            play(note, e.getKeyChar());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        stop(e.getKeyChar()); // stops the voices associated with the key that has been released
    }

    private byte[] convertFloatToByte(float[] floatBuffer) {
        byte[] byteBuffer = new byte[floatBuffer.length * 2];
        for (int i = 0; i < floatBuffer.length; i++) {
            int intSample = (int) (floatBuffer[i] * 32767); // Scale to 16-bit range
            byteBuffer[i * 2] = (byte) ((intSample >> 8) & 0xFF);
            byteBuffer[i * 2 + 1] = (byte) (intSample & 0xFF);
        }
        return byteBuffer;
    }

}
