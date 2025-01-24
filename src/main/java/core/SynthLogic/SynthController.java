package core.SynthLogic;

import core.Visuals.GUIFrontendStuff;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SynthController {
    private Tone tone;
    private static final ArrayList<Character> listOfKeys = new ArrayList<>();
    private final Map<Character, Note> keyNoteMap = new HashMap<>();

    public SynthController(Tone tone, GUIFrontendStuff gui) {
        this.tone = tone;
        setupKeyBindings();
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
            if(tone.getOctave()!=1){
                note = note.modulateFrequency(tone.getOctave()); //horrible solution but it works for now
            }
            tone.play(note, key);
        }
    }

    private void handleKeyRelease(char key) {
        if (keyNoteMap.containsKey(key)) {
            tone.stop(key);
            listOfKeys.removeIf(k -> k == key);
        }
    }
}
