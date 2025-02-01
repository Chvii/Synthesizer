package core.Visuals;

import core.SynthLogic.*;
import core.SynthLogic.Controller.KeyboardToSynth;
import core.SynthLogic.Controller.SynthController;
import core.SynthLogic.Effects.DelayVerb;
import core.SynthLogic.Effects.EffectController;
import core.SynthLogic.Effects.EffectPicker;
import core.SynthLogic.Effects.FilterEffect;
import core.WaveformStrategy.WaveformStrategyPicker;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GUIFrontendStuff extends JFrame {
    private WaveformStrategyPicker[] waveformPickers;
    private Tone tone;
    private EffectController effectController;
    private JLabel[] oscillatorLabels = new JLabel[3];
    private JLabel octaveStateLabel;
    private double attackValue = 0.1;
    private double decayValue = 0.1;
    private double sustainValue = 1.0;
    private double releaseValue = 0.1;
    private JLabel[] octaveStateLabels = new JLabel[3];

    public GUIFrontendStuff(Mixer mixer, WaveformStrategyPicker[] waveformPickers, Tone tone, EffectController effectController) {
        this.waveformPickers = waveformPickers;
        this.tone = tone;
        this.effectController = effectController;

        // Frame Setup
        setTitle("Flagzisizer VST");
        setSize(1500, 1100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);

        // Background Panel
        JPanel backgroundPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(45, 45, 48), getWidth(), getHeight(), new Color(25, 25, 28));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setBounds(0, 0, 1500, 1200);
        add(backgroundPanel);

        // Oscillators Panel
        JPanel oscillatorsPanel = createOscillatorsPanel(mixer);
        oscillatorsPanel.setBounds(20, 20, 910, 300);
        backgroundPanel.add(oscillatorsPanel);

        // ADSR Panel
        JPanel adsrPanel = createADSRPanel();
        adsrPanel.setBounds(20, 300, 400, 350);
        backgroundPanel.add(adsrPanel);

        // Effect Panel
        JPanel effectPanel = createEffectPanel(effectController);
        effectPanel.setBounds(500, 300, 460, 350);
        backgroundPanel.add(effectPanel);

        setVisible(true);
    }

    private JPanel createOscillatorsPanel(Mixer mixer) {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createTitledBorder("Oscillators"));

        for (int i = 0; i < 3; i++) {
            JPanel oscPanel = createSingleOscillatorPanel(mixer, i);
            oscPanel.setBounds(10 + (300 * i), 20, 290, 260);
            panel.add(oscPanel);
        }
        return panel;
    }

    private JPanel createSingleOscillatorPanel(Mixer mixer, int i) {
        Oscillator oscillator = tone.getOscillator(i);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(45, 45, 48));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                "OSC " + (i + 1),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(180, 200, 200)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Column weights for 6-column grid (0-5)
        int[] columnWeights = {0, 1, 1, 1, 1, 0}; // Middle 4 columns expand
        for(int col=0; col<6; col++) {
            gbc.gridx = col;
            gbc.weightx = columnWeights[col];
            panel.add(Box.createHorizontalGlue(), gbc);
        }
        gbc.weightx = 0; // Reset for actual components

        // --- Waveform Display (columns 1-4) ---
        WaveformDisplayPanel waveformDisplay = new WaveformDisplayPanel(oscillator);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4; // Spans 4 middle columns
        gbc.weighty = 0.7; // 70% of vertical space
        panel.add(waveformDisplay, gbc);

        // --- Octave Controls (column 5) ---
        JPanel octavePanel = new JPanel(new GridLayout(3, 1, 0, 5));
        octavePanel.setBackground(new Color(45, 45, 48));
        octavePanel.setPreferredSize(new Dimension(60, 120));

        JButton octUp = createIconButton("↑", new Color(100, 200, 150));
        JLabel octaveLabel = new JLabel(String.valueOf(oscillator.getOctaveShift()), SwingConstants.CENTER);
        octaveLabel.setForeground(Color.WHITE);
        JButton octDown = createIconButton("↓", new Color(200, 100, 100));

        octavePanel.add(octUp);
        octavePanel.add(octaveLabel);
        octavePanel.add(octDown);

        octUp.addActionListener(e -> updateOctave(i, oscillator, octaveLabel, 2));
        octDown.addActionListener(e -> updateOctave(i, oscillator, octaveLabel, 0.5));

        gbc.gridx = 5; // 6th column
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(octavePanel, gbc);

        // --- Waveform Selector (below waveform) ---
        JPanel controlPanel = new JPanel(new BorderLayout(5, 0));
        controlPanel.setBackground(new Color(45, 45, 48));

        JButton prevButton = createIconButton("<", new Color(80, 160, 200));
        JLabel waveformLabel = new JLabel(oscillator.getWaveformStrategy().Stringify(), SwingConstants.CENTER);
        waveformLabel.setForeground(Color.WHITE);
        JButton nextButton = createIconButton(">", new Color(80, 160, 200));

        prevButton.addActionListener(e -> updateWaveform(-1, i, waveformLabel, waveformDisplay));
        nextButton.addActionListener(e -> updateWaveform(1, i, waveformLabel, waveformDisplay));

        controlPanel.add(prevButton, BorderLayout.WEST);
        controlPanel.add(waveformLabel, BorderLayout.CENTER);
        controlPanel.add(nextButton, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weighty = 0.1; // 10% of vertical space
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(controlPanel, gbc);

        // --- Knobs (bottom row) ---
        JPanel knobPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        knobPanel.setBackground(new Color(45, 45, 48));

        // --- Info panel showing knob value ---
        JToolTip knobInfoPanel = knobPanel.createToolTip();
        knobInfoPanel.setBackground(new Color(40, 40, 44));
        knobInfoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),"",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(180, 200, 200)
        ));
        knobInfoPanel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        knobInfoPanel.setForeground(Color.white);

        // --- Detune ---

        JKnob detuneKnob = createStyledKnob("Detune", -10, 10, 0, new Color(200, 120, 80));
        detuneKnob.setRadius(10);
        detuneKnob.addKnobListener(e -> {
            oscillator.setDetune(e);
            knobInfoPanel.setTipText("D: " + detuneKnob.getCurrentValue());
            knobInfoPanel.repaint();

        });
        knobPanel.add(knobInfoPanel);




        knobPanel.add(createKnobPanel(detuneKnob, "DETUNE"));

        JKnob gainKnob = createStyledKnob("Gain", 0.0, 1.0, 1.0, new Color(80, 160, 200));
        gainKnob.setRadius(10); //TODO: gain knob gets reset to 0 instead of 1.
        gainKnob.addKnobListener(e -> {
            oscillator.setGain(e);
            knobInfoPanel.setTipText("G: " + gainKnob.getCurrentValue());
            knobInfoPanel.repaint();
        });
        knobPanel.add(createKnobPanel(gainKnob, "GAIN"));

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weighty = 0.2; // 20% of vertical space
        //gbc.anchor = GridBagConstraints.SOUTH;
        panel.add(knobPanel, gbc);

        return panel;
    }

    private void updateWaveform(int direction, int oscIndex, JLabel label, WaveformDisplayPanel display) {
        if(direction > 0) {
            tone.nextWaveform(oscIndex);
        } else {
            tone.previousWaveform(oscIndex);
        }
        tone.updateOscillatorWaveforms(oscIndex);
        label.setText(tone.getWaveformName(oscIndex));
        display.repaint();
    }
    private void updateOctave(int index, Oscillator oscillator, JLabel octaveLabel, double multiplier) {
        double newOctave = oscillator.getOctaveShift() * multiplier;
        oscillator.setOctaveShift(newOctave);
        octaveLabel.setText(String.format("%.1fx", newOctave));
    }
    private JButton createIconButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        button.setPreferredSize(new Dimension(40, 25));
        button.setMargin(new Insets(2, 5, 2, 5));
        return button;
    }

    private JKnob createStyledKnob(String label, double min, double max, double init, Color color) {
        JKnob knob = new JKnob(new Color(60, 60, 60), color);
        knob.setRange(min, max);
        knob.setValue(init);
        knob.setRadius(20);
        return knob;
    }
    private ArrayList getRange(JKnob knob){
        ArrayList<Double>rangeArray = new ArrayList<>();
        rangeArray.add(knob.getMinValue());
        rangeArray.add(knob.getMaxValue());
        return rangeArray;
    }

    private void updateOctaveLabel(int index, Oscillator oscillator) {
        octaveStateLabel.setText("Octave: " + oscillator.getOctaveShift());
    }

    private double getAttackValueOnGraph() {
        return attackValue * 20; // Convert seconds to milliseconds for visual scale
    }
    private void setAttackValueOnGraph(double attackValue){
        this.attackValue = attackValue;
    }

    private double getDecayValueOnGraph() {
        return decayValue * 20;
    }
    private void setDecayValueOnGraph(double decayValue){
        this.decayValue = decayValue;
    }

    private double getSustainValueOnGraph() {
        return (1-sustainValue) * 200; // Convert 0-1 range to pixel height
    }

    private void setSustainValueOnGraph(double sustainValue) {
        this.sustainValue = sustainValue;
    }

    private double getReleaseValueOnGraph() {
        return releaseValue * 500;
    }
    private void setReleaseValueOnGraph(double releaseValue){
        this.releaseValue = releaseValue;
    }

    private JPanel createADSRPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 48));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 60), 1),
                "ENVELOPE",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(200, 200, 200)
        ));

        // ADSR GRAPH
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.drawString(String.format("A: %.2fs", attackValue), 10, 20);
                g2.drawString(String.format("D: %.2fs", decayValue), 100, 20);
                g2.drawString(String.format("S: %.2f", sustainValue), 190, 20);
                g2.drawString(String.format("R: %.2fs", releaseValue), 280, 20);
                g2.setColor(new Color(100, 200, 255));
                g2.setStroke(new BasicStroke(2));

                int[] xPoints = {
                        0, /* initial pos*/
                        (int) (getAttackValueOnGraph()), /* attack time */
                        (int) (getAttackValueOnGraph()) + (int) (getDecayValueOnGraph()), /* decay time */
                        280, /* release time */
                        280 + (int) (getReleaseValueOnGraph()/250)};
                int[] yPoints = {
                        200, /* 0 volume at start */
                        0, /* max volume after attack time */
                        (int) getSustainValueOnGraph(), /* sustain level */
                        (int) getSustainValueOnGraph(), /* sustain level, release start */
                        200}; /* 0 volume after release */
                g2.drawPolyline( xPoints, yPoints, xPoints.length);
                repaint();
            }
        };
        graphPanel.setBackground(new Color(30, 30, 30));
        panel.add(graphPanel, BorderLayout.CENTER);

        // Knobs panel
        JPanel knobPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        knobPanel.setBackground(new Color(45, 45, 48));



        JKnob attackKnob = createStyledKnob("ATTACK", 0.01, 5.0, 0.1, new Color(200, 120, 80));
        attackKnob.addKnobListener(value ->{
            StandardVoice.setAttackTime(value);
            setAttackValueOnGraph(value);
        });
        JKnob decayKnob = createStyledKnob("DECAY", 0.01, 5.0, 0.1, new Color(120, 200, 80));
        decayKnob.addKnobListener(value ->{
            StandardVoice.setDecayTime(value);
            setDecayValueOnGraph(value);
        });
        JKnob sustainKnob = createStyledKnob("SUSTAIN", 0.01, 1.0, 1.0, new Color(80, 160, 200));
        sustainKnob.addKnobListener(value -> {
            StandardVoice.setSustainLevel(value);
            setSustainValueOnGraph(value);
        });
        JKnob releaseKnob = createStyledKnob("RELEASE", 0.01, 50.0, 0.1, new Color(180, 100, 200));
        releaseKnob.addKnobListener(value -> {
            StandardVoice.setReleaseTime(value);
            setReleaseValueOnGraph(value);
        });
        knobPanel.add(attackKnob);
        knobPanel.add(decayKnob);
        knobPanel.add(sustainKnob);
        knobPanel.add(releaseKnob);
        graphPanel.repaint();

        panel.add(knobPanel, BorderLayout.SOUTH);
        panel.setBackground(new Color(30, 30, 30));
        return panel;
    }


    private JPanel createEffectPanel(EffectController effectController) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));

        JComboBox<EffectPicker.EffectEnums> effectDropdown = new JComboBox<>(EffectPicker.EffectEnums.values());
        effectDropdown.setBounds(20, 20, 150, 30);
        effectDropdown.addActionListener(e -> {
            EffectPicker.EffectEnums selectedEffect = (EffectPicker.EffectEnums) effectDropdown.getSelectedItem();
            effectController.changeEffect(selectedEffect);

            // Show knobs only for DelayVerb
            if (selectedEffect == EffectPicker.EffectEnums.DELAYVERB) {
                panel.removeAll();
                panel.add(effectDropdown);
                addDelayVerbKnobs(panel);
            } else if (selectedEffect == EffectPicker.EffectEnums.FILTER) {
                panel.removeAll();
                panel.add(effectDropdown);
                addFilterKnobs(panel);
            }
            else {
                panel.removeAll();
                panel.add(effectDropdown); // Re-add dropdown
            }
            panel.revalidate();
            panel.repaint();
        });
        panel.add(effectDropdown);

        return panel;
    }

    private void addFilterKnobs(JPanel panel){
        JKnob cutoffKnob = new JKnob(new Color(70, 70, 70),Color.BLACK);
        cutoffKnob.setBounds(34,135,150,150);
        cutoffKnob.setRange(0,1);
        cutoffKnob.setRadius(60);
        cutoffKnob.setValue(1);
        cutoffKnob.addKnobListener(value ->{
            FilterEffect filterEffect = (FilterEffect) effectController.getCurrentEffect();
            filterEffect.setCutoff((float) value);
        });
        panel.add(cutoffKnob);

        JLabel cutoffLabel = new JLabel("Cutoff");
        cutoffLabel.setFont(new Font("Helvetica", Font.ITALIC, 28));
        cutoffLabel.setForeground(Color.white);
        cutoffLabel.setBounds(50,60,100,100);
        panel.add(cutoffLabel);

        JComboBox<FilterEffect.FilterIntensity> filterSlopeMenu = new JComboBox<>(FilterEffect.FilterIntensity.values());
        filterSlopeMenu.setSelectedIndex(1);
        filterSlopeMenu.setFont(new Font("Helvetica", Font.BOLD,16));
        filterSlopeMenu.setBounds(200,90,100,100);
        filterSlopeMenu.addActionListener(value -> {
            FilterEffect filterEffect = (FilterEffect) effectController.getCurrentEffect(); // this whole method is peak Java.
            FilterEffect.FilterIntensity selectedSlope = (FilterEffect.FilterIntensity) filterSlopeMenu.getSelectedItem();
            filterEffect.setIntensity(selectedSlope);
        });
        panel.add(filterSlopeMenu);

        JLabel slopeMenuLabel = new JLabel();
        slopeMenuLabel.setText("SLOPE");
        slopeMenuLabel.setFont(new Font("Helvetica", Font.ITALIC, 16));
        slopeMenuLabel.setForeground(Color.white);
        slopeMenuLabel.setBounds(220,60, 100,100);
        panel.add(slopeMenuLabel);

        JKnob resonanceKnob = new JKnob(new Color(120,120,120),Color.BLACK);
        resonanceKnob.setRange(0.5,1);
        resonanceKnob.setRadius(30);
        resonanceKnob.addKnobListener(value -> {
            FilterEffect filterEffect = (FilterEffect) effectController.getCurrentEffect();
            filterEffect.setResonance( value);
        });
        resonanceKnob.setBounds(220,200,100,100);
        panel.add(resonanceKnob);

        JLabel resonanceLabel = new JLabel("Resonance");
        resonanceLabel.setFont(new Font("Helvetica", Font.ITALIC, 16));
        resonanceLabel.setForeground(Color.white);
        resonanceLabel.setBounds(208,135,100,100);
        panel.add(resonanceLabel);
    }

    private void addDelayVerbKnobs(JPanel panel) {
        // Delay Time Knob
        JKnob delayKnob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        delayKnob.setRange(0.001, 2.0); // Fast to slow delay
        delayKnob.addKnobListener(value -> {
            DelayVerb delayVerb = (DelayVerb) effectController.getCurrentEffect();
            delayVerb.setDelayTimeInSeconds((float) value);
        });
        delayKnob.setBounds(50, 70, 100, 100);
        panel.add(delayKnob);

        JLabel delayLabel = new JLabel("Delay Time");
        delayLabel.setForeground(Color.WHITE);
        delayLabel.setBounds(70, 170, 100, 20);
        panel.add(delayLabel);

        // Feedback Knob
        JKnob feedbackKnob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        feedbackKnob.setRange(0.0, 1.0);
        feedbackKnob.addKnobListener(value -> {
            DelayVerb delayVerb = (DelayVerb) effectController.getCurrentEffect();
            delayVerb.setFeedback((float) value);
        });
        feedbackKnob.setBounds(200, 70, 100, 100);
        panel.add(feedbackKnob);

        JLabel feedbackLabel = new JLabel("Feedback");
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setBounds(220, 170, 100, 20);
        panel.add(feedbackLabel);

        // Mix Knob
        JKnob mixKnob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        mixKnob.setRange(0.0, 1.0);
        mixKnob.addKnobListener(value -> {
            DelayVerb delayVerb = (DelayVerb) effectController.getCurrentEffect();
            delayVerb.setMix((float) value);
        });
        mixKnob.setBounds(350, 70, 100, 100);
        panel.add(mixKnob);

        JLabel mixLabel = new JLabel("Mix");
        mixLabel.setForeground(Color.WHITE);
        mixLabel.setBounds(370, 170, 100, 20);
        panel.add(mixLabel);

        // Stereo mode switch
        JButton stereoButton = new JButton("Wide Mode");
        stereoButton.addActionListener(e -> {
            DelayVerb delayVerb = (DelayVerb) effectController.getCurrentEffect();
            delayVerb.setStereoMode();
        });
        stereoButton.setBounds(200, 200, 120, 30);
        panel.add(stereoButton);
    }
    private JPanel createKnobPanel(JKnob knob, String labelText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 48));

        // Add the knob centered in the panel
        JPanel knobWrapper = new JPanel(new GridBagLayout());
        knobWrapper.setBackground(new Color(45, 45, 48));
        knobWrapper.add(knob);
        panel.add(knobWrapper, BorderLayout.CENTER);

        // Add the label below the knob
        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        panel.add(label, BorderLayout.SOUTH);

        return panel;
    }

    public static void main(String[] args) throws LineUnavailableException, MidiUnavailableException {
        AudioFormat af = new AudioFormat(44100, 16, 2, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open(af, 2500);
        line.start();

        WaveformStrategyPicker[] waveformPickers = new WaveformStrategyPicker[3];
        for (int i = 0; i < waveformPickers.length; i++) {
            waveformPickers[i] = new WaveformStrategyPicker();
        }

        EffectPicker effectPicker = new EffectPicker();
        EffectController effectController = new EffectController(effectPicker);
        Mixer mixer = new StandardMixer(line, effectPicker);
        Tone tone = new StandardTone(line, waveformPickers, mixer);

        new GUIFrontendStuff(mixer, waveformPickers, tone, effectController);

        Receiver receiver = null; // Replace with an actual receiver if needed
        SynthController synthController = new SynthController(receiver, tone);
        new KeyboardToSynth(tone).run();
    }


    class WaveformDisplayPanel extends JPanel {
        private final Oscillator oscillator;
        private final int width = 225;
        private final int height = 140;

        public WaveformDisplayPanel(Oscillator oscillator) {
            this.oscillator = oscillator;
            setPreferredSize(new Dimension(width, height));
            setBackground(new Color(30, 30, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw waveform
            g2d.setColor(new Color(100, 200, 255));
            double[] samples = oscillator.generateWaveformSamples();
            for (int i = 0; i < samples.length - 1; i++) {
                int x1 = (int) (i * (width / (double) samples.length));
                int y1 = (int) ((0.5 - samples[i] * 0.4) * height);
                int x2 = (int) ((i + 1) * (width / (double) samples.length));
                int y2 = (int) ((0.5 - samples[i + 1] * 0.4) * height);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }
}
