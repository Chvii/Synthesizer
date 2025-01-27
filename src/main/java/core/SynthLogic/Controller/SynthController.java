package core.SynthLogic.Controller;

import core.SynthLogic.Note;
import core.SynthLogic.Tone;
import core.Visuals.GUIFrontendStuff;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SynthController implements Receiver {
    private Tone tone;
    private Receiver receiver;
    boolean isSystemExclusiveData = false;
    private static final ArrayList<Character> listOfKeys = new ArrayList<>();
    private final Map<Character, Note> keyNoteMap = new HashMap<>();
    private final Map<Integer, Note> midiNoteMap = new HashMap<>();

    public SynthController(Receiver receiver,Tone tone) {
        this.receiver = receiver;
        this.tone = tone;
        setupKeyBindings();
        setupMidiBindings();
        setupGlobalKeyListener();
    }

    private void setupKeyBindings() {
        // Map notes to keys
        keyNoteMap.put('q', new Note(Note.C4.getFrequency()));
        keyNoteMap.put('2', new Note(Note.Csharp4.getFrequency()));
        keyNoteMap.put('w', new Note(Note.D4.getFrequency()));
        keyNoteMap.put('3', new Note(Note.Dsharp4.getFrequency()));
        keyNoteMap.put('e', new Note(Note.E4.getFrequency()));
        keyNoteMap.put('r', new Note(Note.F4.getFrequency()));
        keyNoteMap.put('5', new Note(Note.Fsharp4.getFrequency()));
        keyNoteMap.put('t', new Note(Note.G4.getFrequency()));
        keyNoteMap.put('6', new Note(Note.Gsharp4.getFrequency()));
        keyNoteMap.put('y', new Note(Note.A4.getFrequency()));
        keyNoteMap.put('7', new Note(Note.Asharp4.getFrequency()));
        keyNoteMap.put('u', new Note(Note.B4.getFrequency()));
        keyNoteMap.put('i', new Note(Note.C4.getFrequency() * 2));
        keyNoteMap.put('9', new Note(Note.Csharp4.getFrequency()*2));
        keyNoteMap.put('o', new Note(Note.D4.getFrequency()*2));
        keyNoteMap.put('0', new Note(Note.Dsharp4.getFrequency()*2));
        keyNoteMap.put('p', new Note(Note.E4.getFrequency()*2));
        keyNoteMap.put('z', new Note(Note.C4.getFrequency()/2));
        keyNoteMap.put('s', new Note(Note.Csharp4.getFrequency()/2));
        keyNoteMap.put('x', new Note(Note.D4.getFrequency()/2));
        keyNoteMap.put('d', new Note(Note.Dsharp4.getFrequency()/2));
        keyNoteMap.put('c', new Note(Note.E4.getFrequency()/2));
        keyNoteMap.put('v', new Note(Note.F4.getFrequency()/2));
        keyNoteMap.put('g', new Note(Note.Fsharp4.getFrequency()/2));
        keyNoteMap.put('b', new Note(Note.G4.getFrequency()/2));
        keyNoteMap.put('h', new Note(Note.Gsharp4.getFrequency()/2));
        keyNoteMap.put('n', new Note(Note.A4.getFrequency()/2));
        keyNoteMap.put('j', new Note(Note.Asharp4.getFrequency()/2));
        keyNoteMap.put('m', new Note(Note.B4.getFrequency()/2));
    }

    /**
     * I was REAL excited about this solution to mapping all possible MIDI keys (from 0 to 127)
     * to individual notes
     * :)!
     */
    private void setupMidiBindings() {
        double frequencyCalculator = 0;
        for(int n = 0; n < 127 ; n++){
            frequencyCalculator = 440*Math.pow(2,((double)n-69)/12);
            midiNoteMap.put(n, new Note(frequencyCalculator));
        }
    }


    private void setupGlobalKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            char key = e.getKeyChar();

            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED -> handleKeyPress(key);
                case KeyEvent.KEY_RELEASED -> handleKeyRelease(key);
            }
            return false; // Allow other components to process the key event
        });
    }

    private void handleKeyPress(char key) {
        if (keyNoteMap.containsKey(key) && !listOfKeys.contains(key)) {
            listOfKeys.add(key);
            Note note = keyNoteMap.get(key);

            tone.play(note,100);
        }
    }
    private void handleKeyRelease(char key) {
        if (keyNoteMap.containsKey(key)) {
            Note note = keyNoteMap.get(key);
            tone.stop(note);
            listOfKeys.removeIf(k -> k == key);
        }
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        displayMessage(message, timeStamp);
        receiver.send(message, timeStamp);
    }

    @Override
    public void close() {
        receiver.close();
    }

    public void handleMidiNoteOn(int key, int velocity) {
        Note note = midiNoteMap.get(key);
        if (note != null) {
            System.out.println("PLAYING KEY: " + key);
            tone.play(note, velocity);
        }
    }

    public void handleMidiNoteOff(int key) {
        Note note = midiNoteMap.get(key);
        if (note != null) {
            System.out.println("STOPPING KEY: " + key);
            tone.stop(note);
        }
    }

    private void displayMessage(MidiMessage message, long timeStamp) {

        // Check: Are we printing system exclusive data?
        if (isSystemExclusiveData) {
            displayRawData(message);
            return;
        }

        int status = message.getStatus();

        // These statuses clutter the display
        if ( status == 0xf8 ) { return; } // ignore timing messages
        if ( status == 0xfe ) { return; } // ignore status active

        System.out.printf("%d - Status: 0x%s",
                timeStamp, Integer.toHexString(status));

        // Strip channel number out of status
        int leftNibble = status & 0xf0;

        // These statuses have MIDI channel numbers and data (except
        // 0xf0 thru 0xff)
        switch (leftNibble) {
            case 0x80: displayNoteOff(message); break;
            case 0x90: displayNoteOn(message); break;
            case 0xa0: displayKeyPressure(message); break;
            case 0xb0: displayControllerChange(message); break;
            case 0xc0: displayProgramChange(message); break;
            case 0xd0: displayChannelPressure(message); break;
            case 0xe0: displayPitchBend(message); break;
            case 0xf0: displaySystemMessage(message); break;
            default:
                System.out.println(" Unknown status");
                displayRawData(message);
        }
    }
    // Displays raw data as integers, if any
    private void displayRawData(MidiMessage message) {
        byte[] bytes = message.getMessage();

        if (message.getLength() > 1) {
            System.out.print("\tRaw data: ");

            for (int i = 1; i < bytes.length; i++) {
                System.out.print(byteToInt(bytes[i]) + " ");
            }

            System.out.println();
        }
    }

    // Display status and data of a NoteOn message.  Data may come
    // in pairs after the status byte.
    //
    // Note that a NoteOn with a velocity of 0 is synonymous with
    // a NoteOff message.
    private void displayNoteOn(MidiMessage message) {
        if (message.getLength() < 3 || message.getLength() % 2 == 0) {
            System.out.println(" Bad MIDI message");
            return;
        }

        byte[] bytes = message.getMessage();

        // Zero velocity
        if ( bytes[2] == 0 ) {
            System.out.print(" = Note off");
        } else {
            System.out.print(" = Note on");
        }

        System.out.print(", Channel " + midiChannelToInt(message));

        if ( bytes[2] == 0 ) {
            System.out.println(", Note " + byteToInt(bytes[1]));


            return;
        }

        System.out.print("\n\t");

        for (int i = 1; i < message.getLength(); i += 2) {
            if ( i > 1 ) {
                System.out.print("; ");
            }
            System.out.printf( "Number %d, Velocity %d",
                    byteToInt(bytes[i]) , byteToInt(bytes[i + 1]) );

            // Sending the MIDI ON signal to the SynthController
            handleMidiNoteOn(byteToInt(bytes[i]),byteToInt(bytes[i + 1]));
        }

        System.out.println();
    }

    // Display status and data of a NoteOff message.
    private void displayNoteOff(MidiMessage message) {
        if (message.getLength() < 3 || message.getLength() % 2 == 0) {
            System.out.println(" Bad MIDI message");
        } else {
            byte[] bytes = message.getMessage();
            System.out.printf(" = Note off, Channel %d, Note %d%n",
                    midiChannelToInt(message), byteToInt(bytes[1]));

            // Sending the MIDI OFF signal to the SynthController
            handleMidiNoteOff(byteToInt(bytes[1]));
            System.out.println();
        }
    }

    // Display status and data of a ControllerChange message.  Data may come
    // in pairs after the status byte.
    private void displayControllerChange(MidiMessage message) {
        if (message.getLength() < 3 || message.getLength() % 2 == 0) {
            System.out.println(" Bad MIDI message");
            return;
        }

        System.out.print(" = Controller Change, Channel "
                + midiChannelToInt(message) + "\n\t");

        byte[] bytes = message.getMessage();
        for (int i = 1; i < message.getLength(); i += 2) {
            if ( i > 1 ) {
                System.out.print("; ");
            }
            System.out.printf( "Controller %d, Value %d",
                    byteToInt(bytes[i]), byteToInt(bytes[i + 1]) );
        }

        System.out.println();
    }

    // Display status and data of a KeyPressure message.  Data may come
    // in pairs after the status byte.
    private void displayKeyPressure(MidiMessage message) {
        if (message.getLength() < 3 || message.getLength() % 2 == 0) {
            System.out.println(" Bad MIDI message");
            return;
        }

        System.out.print(" = Key Pressure, Channel "
                + midiChannelToInt(message) + "\n\t");

        byte[] bytes = message.getMessage();
        for (int i = 1; i < message.getLength(); i += 2) {
            if ( i > 1 ) {
                System.out.print("; ");
            }
            System.out.printf( "Note Number %d, Pressure %d",
                    byteToInt(bytes[i]), byteToInt(bytes[i + 1]) );
        }

        System.out.println();
    }

    // Display status and data of a PitchBend message.  Data may come
    // in pairs after the status byte.
    private void displayPitchBend(MidiMessage message) {
        if (message.getLength() < 3 || message.getLength() % 2 == 0) {
            System.out.println(" Bad MIDI message");
            return;
        }

        System.out.print(" = Pitch Bend, Channel "
                + midiChannelToInt(message) + "\n\t");

        byte[] bytes = message.getMessage();
        for (int i = 1; i < message.getLength(); i += 2) {
            if ( i > 1 ) {
                System.out.print("; ");
            }
            System.out.printf( "Value %d",
                    bytesToInt(bytes[i], bytes[i + 1]) );
        }

        System.out.println();
    }

    // Display status and data of a ProgramChange message
    private void displayProgramChange(MidiMessage message) {
        if (message.getLength() < 2) {
            System.out.println(" Bad MIDI message");
            return;
        }

        System.out.print(" = Program Change, Channel "
                + midiChannelToInt(message) + "\n\t");

        byte[] bytes = message.getMessage();
        for (int i = 1; i < message.getLength(); i++) {
            if ( i > 1 ) {
                System.out.print(", ");
            }
            System.out.println("Program Number " + byteToInt(bytes[i]));
        }
    }

    // Display status and data of a ChannelPressure message
    private void displayChannelPressure(MidiMessage message) {
        if (message.getLength() < 2) {
            System.out.println(" Bad MIDI message");
            return;
        }

        System.out.print(" = Channel Pressure, Channel "
                + midiChannelToInt(message) + "\n\t");

        byte[] bytes = message.getMessage();
        for (int i = 1; i < message.getLength(); i++) {
            if ( i > 1 ) {
                System.out.print(", ");
            }
            System.out.println("Pressure " + byteToInt(bytes[i]));
        }
    }

    // Display system messages.  Some may have data.
    //
    // "Begin System Exclusive" stops data interpretation, "End of
    // System Exclusive" starts it again
    private void displaySystemMessage(MidiMessage message) {
        byte[] bytes = message.getMessage();

        switch (message.getStatus()) {
            case 0xf0:
                System.out.println(" = Begin System Exclusive");
                isSystemExclusiveData = true;
                break;
            case 0xf1:
                if (bytes.length < 2) {
                    System.out.println(" Bad Data");
                } else {
                    System.out.println(" = MIDI Time Code 1/4 Frame, Time Code "
                            + byteToInt(bytes[1]));
                }
                break;
            case 0xf2:
                if (bytes.length < 3) {
                    System.out.println(" Bad Data");
                } else {
                    System.out.println(" = Song Position, Pointer "
                            + bytesToInt(bytes[1], bytes[2]));
                }
            case 0xf3:
                if (bytes.length < 2) {
                    System.out.println(" Bad Data");
                } else {
                    System.out.println(" = Song Select, Song "
                            + byteToInt(bytes[1]));
                }
                break;
            case 0xf6:
                System.out.println(" = Tune Request");
                break;
            case 0xf7:
                System.out.println(" = End of System Exclusive");
                isSystemExclusiveData = false;
                break;
            case 0xf8:
                System.out.println(" = Timing Clock"); // ignored
                break;
            case 0xfa:
                System.out.println(" = Start");
                break;
            case 0xfb:
                System.out.println(" = Continue");
                break;
            case 0xfc:
                System.out.println(" = Stop");
                break;
            case 0xfe:
                System.out.println(" = Active Sensing"); // ignored
                break;
            case 0xff:
                System.out.println(" = System Reset");
                break;
            default:
                System.out.println(" Unknow System Message");
                displayRawData(message);
        }
    }

    private int byteToInt(byte b) {
        return b & 0xff;
    }

    // Two 7-bit bytes
    private int bytesToInt(byte msb, byte lsb) {
        return byteToInt(msb) * 128 + byteToInt(lsb);
    }

    private int midiChannelToInt(MidiMessage message) {
        return (message.getStatus() & 0x0) + 1;
    }
}
