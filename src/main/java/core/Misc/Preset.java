package core.Misc;

import core.SynthLogic.Effects.EffectChain;
import core.SynthLogic.Effects.EffectFactory;
import core.SynthLogic.Effects.EffectRack;
import core.SynthLogic.Effects.ParameterizedEffect;
import core.SynthLogic.Oscillator;
import core.SynthLogic.Tone;
import core.WaveformStrategy.WaveformStrategyPicker;

import java.io.*;
import java.util.*;
import java.util.function.DoubleConsumer;

public class Preset implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<OscillatorState> oscillatorStates = new ArrayList<>();
    private List<EffectState> effectStates = new ArrayList<>();
    private double attack, decay, sustain, release;

    public void saveState(
            WaveformStrategyPicker[] waveformPickers,
            Tone tone,
            EffectChain effectChain,
            double attack,
            double decay,
            double sustain,
            double release
    ) {
        // Save oscillator states
        for (int i = 0; i < 3; i++) {
            Oscillator osc = tone.getOscillator(i);
            oscillatorStates.add(new OscillatorState(
                    waveformPickers[i].getCurrentWaveformIndex(),
                    osc.getDetune(),
                    osc.getGain(),
                    osc.getOctaveShift()
            ));
        }

        // Save effect chain
        for (EffectRack effect : effectChain.getEffects()) {
            if (effect instanceof ParameterizedEffect) {
                effectStates.add(new EffectState(
                        effect.getClass(),
                        ((ParameterizedEffect) effect).getAllParameters()
                ));
            } else {
                effectStates.add(new EffectState(effect.getClass(), null));
            }
        }

        // Save ADSR
        this.attack = attack;
        this.decay = decay;
        this.sustain = sustain;
        this.release = release;
    }

    public void applyState(
            WaveformStrategyPicker[] waveformPickers,
            Tone tone,
            EffectChain effectChain,
            DoubleConsumer attackSetter,
            DoubleConsumer decaySetter,
            DoubleConsumer sustainSetter,
            DoubleConsumer releaseSetter
    ) {
        // Restore oscillators
        for (int i = 0; i < 3; i++) {
            OscillatorState state = oscillatorStates.get(i);
            waveformPickers[i].setCurrentWaveformIndex(state.waveformIndex);
            Oscillator osc = tone.getOscillator(i);
            osc.setDetune(state.detune);
            osc.setGain(state.gain);
            osc.setOctaveShift(state.octaveShift);
            tone.updateOscillatorWaveforms(i);
        }

        // Restore effects
        effectChain.clear();
        for (EffectState state : effectStates) {
            try {
                EffectRack effect = state.effectClass.getDeclaredConstructor().newInstance();
                if (effect instanceof ParameterizedEffect && state.parameters != null) {
                    ((ParameterizedEffect) effect).setAllParameters(state.parameters);
                }
                effectChain.addEffect(effect);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Restore ADSR
        attackSetter.accept(attack);
        decaySetter.accept(decay);
        sustainSetter.accept(sustain);
        releaseSetter.accept(release);
    }

    // Helper classes for serialization
    class WaveformState implements Serializable {
        int waveformIndex;
        double detune, gain, octaveShift;
        WaveformState(int index, double detune, double gain, double octave) {
            this.waveformIndex = index;
            this.detune = detune;
            this.gain = gain;
            this.octaveShift = octave;
        }
    }

    class OscillatorState implements Serializable {
        int waveformIndex;
        double detune, gain, octaveShift;

        OscillatorState(int index, double detune, double gain, double octave) {
            this.waveformIndex = index;
            this.detune = detune;
            this.gain = gain;
            this.octaveShift = octave;
        }
    }

    class EffectState implements Serializable {
        Class<? extends EffectRack> effectClass;
        Map<Enum<?>, Double> parameters;  // Changed to store enum instances

        EffectState(Class<? extends EffectRack> cls,
                    Map<Enum<?>, Double> params) {
            this.effectClass = cls;
            this.parameters = params != null ? new HashMap<>(params) : null;
        }
    }

    // Preset Manager
    class PresetManager {
        private static final File PRESET_DIR = new File(
                System.getProperty("user.home") + "/.synthpresets"
        );

        static {
            PRESET_DIR.mkdirs();
        }

        public static void savePreset(String name, Preset preset) throws IOException {
            File file = new File(PRESET_DIR, name + ".synth");
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(file))) {
                oos.writeObject(preset);
            }
        }

        public static Preset loadPreset(String name)
                throws IOException, ClassNotFoundException {
            File file = new File(PRESET_DIR, name + ".synth");
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file))) {
                return (Preset) ois.readObject();
            }
        }
    }
}