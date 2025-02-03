package core.Visuals;

import core.Constants.ConstantValues;
import core.SynthLogic.*;
import core.SynthLogic.Controller.KeyboardToSynth;
import core.SynthLogic.Controller.SynthController;
import core.SynthLogic.Effects.*;
import core.WaveformStrategy.*;
import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.WrappedPlainView;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class GUIFrontendStuff extends JFrame {
    private Tone tone;
    private EffectChain effectChain;
    private JLabel[] oscillatorLabels = new JLabel[3];
    private JLabel octaveStateLabel;
    private double attackValue = 0.1;
    private double decayValue = 0.1;
    private double sustainValue = 1.0;
    private double releaseValue = 0.1;
    private JLabel[] octaveStateLabels = new JLabel[3];
    private WaveformStrategyPicker waveformPickers;

    public GUIFrontendStuff(Mixer mixer, WaveformStrategyPicker[] waveformPickers, Tone tone, EffectChain effectChain) {
        this.tone = tone;
        this.effectChain = effectChain;

        // Frame Setup
        setTitle("Flagzisizer VST");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);

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
        backgroundPanel.setBounds(0, 0, 1200, 800);
        add(backgroundPanel);

        // Oscillators Panel
        initializeOscillatorStates();
        JPanel oscillatorsPanel = createOscillatorsPanel(mixer);
        oscillatorsPanel.setBounds(40, 40, 800, 270);
        backgroundPanel.add(oscillatorsPanel);

        // ADSR Panel
        JPanel adsrPanel = createADSRPanel();
        adsrPanel.setBounds(40, 460, 420, 280);
        backgroundPanel.add(adsrPanel);

        // Effect Panel
        JPanel effectPanel = createEffectPanel(effectChain);
        effectPanel.setBounds(500, 360, 550, 370);
        backgroundPanel.add(effectPanel);


        setVisible(true);

    }

    private void initializeOscillatorStates() {
        // Activate only the first oscillator
        for (int i = 0; i < 3; i++) {
            Oscillator osc = tone.getOscillator(i);
            osc.setIsActive(i == 0);
        }
    }

    private JPanel createOscillatorsPanel(Mixer mixer) {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                "OSCILLATORS",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(180, 200, 200)
        ));

        for (int i = 0; i < 3; i++) {
            JPanel oscPanel = createSingleOscillatorPanel(mixer, i);
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


        // ================== WAVEFORM DISPLAY ==================
        WaveformDisplayPanel waveformDisplay = new WaveformDisplayPanel(oscillator);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weightx = 2.0;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(waveformDisplay, gbc);


        // ================== POWER BUTTON ==================
        CustomPowerButton powerButton = new CustomPowerButton();
        powerButton.setSelected(oscillator.getIsActive());
        powerButton.addItemListener(e -> {
            boolean selected = powerButton.isSelected();
            oscillator.setIsActive(selected);
        });
        repaint();


        // ================== OCTAVE CONTROLS ==================
        JPanel octavePanel = new JPanel();
        octavePanel.setLayout(new BoxLayout(octavePanel, BoxLayout.Y_AXIS));
        octavePanel.setBackground(new Color(45, 45, 48));
        octavePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                "OCTAVE",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 7),
                new Color(180, 200, 200)
        ));

        JButton octUp = createIconButton("↑", new Color(100, 200, 150));
        octUp.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel octaveLabel = new JLabel(oscillator.getPrettyOctaveValue() + "x", SwingConstants.CENTER);
        octaveLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        octaveLabel.setForeground(Color.WHITE);
        octaveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton octDown = createIconButton("↓", new Color(200, 100, 100));
        octDown.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components with spacing
        octavePanel.add(Box.createVerticalGlue());
        octavePanel.add(octUp);
        octavePanel.add(Box.createVerticalStrut(5));
        octavePanel.add(octaveLabel);
        octavePanel.add(Box.createVerticalStrut(5));
        octavePanel.add(octDown);
        octavePanel.add(Box.createVerticalGlue());

        // Action listeners for octave buttons
        octUp.addActionListener(e -> {
            oscillator.octaveUp();
            octaveLabel.setText(oscillator.getPrettyOctaveValue() + "x");
        });
        octDown.addActionListener(e -> {
            oscillator.octaveDown();
            octaveLabel.setText(oscillator.getPrettyOctaveValue() + "x");
        });

        // container for power button and octave controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(45, 45, 48));

        // Adding power button with spacing
        JPanel powerContainer = new JPanel();
        powerContainer.setBackground(new Color(45, 45, 48));
        powerContainer.add(powerButton);
        rightPanel.add(powerContainer);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(octavePanel);

        // GridBagConstraints for right panel
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(rightPanel, gbc);

        // Waveform Selector (below waveform) ---
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

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(controlPanel, gbc);


        // Knobs (bottom row) ---
        JPanel knobPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        knobPanel.setBackground(new Color(45, 45, 48));

        // Info panel showing knob value ---
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
        knobInfoPanel.setLayout(new BoxLayout(knobInfoPanel,BoxLayout.Y_AXIS));

        knobPanel.add(knobInfoPanel);


        // Detune ---
        JKnob detuneKnob = createStyledKnob("Detune", -10, 10, 0, new Color(200, 120, 80));
        detuneKnob.setRadius(10);
        detuneKnob.addKnobListener(e -> {
            oscillator.setDetune(e);
            knobInfoPanel.setTipText(detuneKnob.getCurrentValue());
            knobInfoPanel.setFont(new Font("Segoe UI", Font.BOLD, 10));

            knobInfoPanel.repaint();

        });

        knobPanel.add(createKnobPanel(detuneKnob, "DETUNE"));

        JKnob gainKnob = createStyledKnob("Gain", 0.0, 1.0, 1.0, new Color(80, 160, 200));
        gainKnob.setRadius(10);
        gainKnob.addKnobListener(e -> {
            oscillator.setGain(e);
            knobInfoPanel.setTipText(gainKnob.getCurrentValue());
            knobInfoPanel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            knobInfoPanel.repaint();
        });
        knobPanel.add(createKnobPanel(gainKnob, "GAIN"));


        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
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
        newOctave = oscillator.getPrettyOctaveValue();
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
        octaveStateLabel.setText("Octave: " + oscillator.getPrettyOctaveValue());
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

    private JPanel createEffectPanel(EffectChain effectChain) {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setPreferredSize(new Dimension(550, 370));
        panel.setBackground(new Color(45, 45, 48));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                "FX",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(180, 200, 200)
        ));


        // Active Effects list
        DefaultListModel<EffectRack> listModel = new DefaultListModel<>();
        JList<EffectRack> effectList = new JList<>(listModel);
        effectList.setBackground(new Color(45, 45, 48));
        effectList.setForeground(Color.WHITE);
        effectList.setSelectionBackground(new Color(80, 80, 80));
        effectList.setSelectionForeground(new Color(30,30,30));
        effectList.setFixedCellHeight(40);
        effectList.setCellRenderer(new EffectListRenderer());
        effectList.setDragEnabled(true);
        effectList.setDropMode(DropMode.INSERT);
        effectList.setTransferHandler(new EffectTransferHandler(effectChain, listModel));

        // Hover effect
        effectList.addMouseMotionListener(new MouseAdapter() {
            private int lastHoverIndex = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                int index = effectList.locationToIndex(e.getPoint());
                if (index != lastHoverIndex) {
                    lastHoverIndex = index;
                    effectList.repaint();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(effectList);
        scrollPane.setPreferredSize(new Dimension(160, 260));
        scrollPane.setBackground(new Color(40,40,46));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                "EFFECTS",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(180, 200, 200)
        ));
        scrollPane.getViewport().setBackground(new Color(45, 45, 48));

        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setBackground(new Color(45, 45, 48));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<EffectFactory.EffectType> typeCombo = new JComboBox<>(EffectFactory.EffectType.values());
        typeCombo.setRenderer(new StyledComboRenderer());
        typeCombo.setBackground(new Color(60, 60, 60));
        typeCombo.setForeground(Color.WHITE);
        typeCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1)
                ));
        typeCombo.addActionListener(e -> {
            EffectFactory.EffectType type = (EffectFactory.EffectType) typeCombo.getSelectedItem();
            if (type != null) {
                EffectRack effect = EffectFactory.createEffect(type);
                effectChain.addEffect(effect);
                listModel.addElement(effect);
            }
        });

        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));
        paramPanel.setBackground(new Color(45, 45, 48));
        JScrollPane paramScroll = new JScrollPane(paramPanel) {{
            setPreferredSize(new Dimension(320, 280));
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                    "PARAMETERS",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    new Color(180, 200, 200)
            ));
            setBackground(new Color(40,40,46));
        }};

        // Context menu for deletion
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int index = effectList.getSelectedIndex();
            if (index != -1) {
                listModel.remove(index);
                effectChain.removeEffect(effectChain.getEffects().get(index));
            }
        });
        contextMenu.add(deleteItem);

        effectList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = effectList.locationToIndex(e.getPoint());
                    effectList.setSelectedIndex(index);
                    contextMenu.show(effectList, e.getX(), e.getY());
                }
            }
        });

        effectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Add this check
                paramPanel.removeAll();
                EffectRack selected = effectList.getSelectedValue();
                if (selected instanceof ParameterizedEffect) {
                    createParameterControls((ParameterizedEffect) selected, paramPanel);
                }
                paramPanel.revalidate();
                paramPanel.repaint();
            }
        });

        controlsPanel.add(typeCombo, BorderLayout.NORTH);
        controlsPanel.add(paramScroll, BorderLayout.CENTER);
        panel.add(controlsPanel, BorderLayout.EAST);
        panel.add(scrollPane, BorderLayout.WEST);
        return panel;
    }



    private void createParameterControls(ParameterizedEffect effect, JPanel parent) {
        parent.removeAll();
        if(effect instanceof ParametricEQ) {
            // Special layout for EQ with spectrum
            parent.setLayout(new BorderLayout());

            // Add compact controls below spectrum
            JPanel controlPanel = new JPanel(new GridLayout(5, 3, 5, 5)); // 2 rows, 5 columns
            controlPanel.setBackground(new Color(45, 45, 48));

            for (ParameterizedEffect.Parameter param : effect.getParameters()) {
                if (param == ParametricEQ.Parameter.BAND1_FREQ ||
                        param == ParametricEQ.Parameter.BAND2_FREQ ||
                        param == ParametricEQ.Parameter.BAND3_FREQ || // VERY elegant solution here.
                        param == ParametricEQ.Parameter.BAND4_FREQ ||
                        param == ParametricEQ.Parameter.BAND5_FREQ)
                {
                    JComponent control = createCompactControl(effect, param, true);
                    controlPanel.add(createCompactControlPanel(param.getDisplayName(), control));
                } else {
                JComponent control = createCompactControl(effect, param, false);
                controlPanel.add(createCompactControlPanel(param.getDisplayName(), control));
                }
            }
            parent.add(controlPanel, BorderLayout.SOUTH);

        } else {
            parent.setLayout(new GridLayout(3, 3, 5, 5));

            for (ParameterizedEffect.Parameter param : effect.getParameters()) {
                JPanel controlPanel = createControlPanel(param.getDisplayName());
                JComponent control = switch (param.getControlType()) {
                    case KNOB -> createKnobControl(effect, param);
                    case BUTTON -> createButtonControl(effect, param);
                    case LIST -> createListControl(effect, param);
                };
                controlPanel.add(control);
                parent.add(controlPanel);
            }
            parent.revalidate();
            parent.repaint();
        }
    }

    private JComponent createCompactControl(ParameterizedEffect effect,
                                            ParameterizedEffect.Parameter param, boolean isLogarithmic) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 48));

        JKnob knob = new JKnob(new Color(60, 60, 60), new Color(150, 200, 255)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                // Custom knob painting to match dark theme
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(getBackground());
                g2d.fillOval(0, 0, getWidth(), getHeight());

                // Indicator
                g2d.setColor(getForeground());
                double angle = Math.toRadians(135 + (270 * currentValueNormalized()));
                int cx = getWidth()/2;
                int cy = getHeight()/2;
                int len = cx - 4;
                g2d.drawLine(cx, cy,
                        (int)(cx + Math.cos(angle)*len),
                        (int)(cy - Math.sin(angle)*len));
            }
        };
        if(isLogarithmic) {
            knob.setLogarithmic();
        }

        knob.setRange(param.getMin(), param.getMax());
        knob.setValue(effect.getParameter(param));
        knob.setRadius(15);

        JLabel label = new JLabel(param.getDisplayName(), SwingConstants.CENTER);
        label.setForeground(new Color(200, 200, 200));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 9));

        knob.addKnobListener(value -> {
            effect.setParameter(param, value);
            label.setText(String.format("%s: %.1f", param.getDisplayName(), value));
        });


        panel.add(knob, BorderLayout.CENTER);
        panel.add(label, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCompactControlPanel(String title, JComponent control) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 48));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(control);
        return panel;
    }

    private JPanel createControlPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 48));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel label = new JLabel(title);
        label.setForeground(new Color(200, 200, 200));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

    private JComponent createKnobControl(ParameterizedEffect effect,
                                         ParameterizedEffect.Parameter param) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(45, 45, 48));

        JKnob knob = createStyledKnob(
                param.getDisplayName(),
                param.getMin(),
                param.getMax(),
                effect.getParameter(param),
                new Color(100, 200, 255)
        );
        knob.setRadius(25);
        knob.addKnobListener(value -> effect.setParameter(param, value));
        panel.add(knob);

        return panel;
    }

    private JComponent createButtonControl(ParameterizedEffect effect,
                                           ParameterizedEffect.Parameter param) {
        JToggleButton button = new JToggleButton();
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setSelected(effect.getParameter(param) >= 0.5);
        button.setText(button.isSelected() ? "ON" : "OFF");
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        button.addActionListener(e -> {
            boolean selected = button.isSelected();
            button.setSelected(selected);
            button.setText(selected ? "ON" : "OFF");
            effect.setParameter(param, selected ? 1.0 : 0.0);
        });

        return button;
    }

    private JComponent createListControl(ParameterizedEffect effect, ParameterizedEffect.Parameter param) {
        JComboBox<Enum<?>> combo = new JComboBox<>(param.getOptions());
        combo.setBackground(new Color(60, 60, 60));
        combo.setForeground(Color.WHITE);
        combo.setSelectedIndex((int) Math.round(effect.getParameter(param)));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value.toString().replace("_", " "));
                setBackground(isSelected ? new Color(80, 80, 80) : new Color(60, 60, 60));
                return this;
            }
        });

        combo.addActionListener(e ->
                effect.setParameter(param, combo.getSelectedIndex())
        );

        return combo;
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
        EffectChain effectChain = new EffectChain();
        Mixer mixer = new StandardMixer(line, effectChain);



        Tone tone = new StandardTone(line, waveformPickers, mixer);

        new GUIFrontendStuff(mixer, waveformPickers, tone, effectChain);

        Receiver receiver = null; // Dont need a receiver I guess
        SynthController synthController = new SynthController(receiver, tone);
        new KeyboardToSynth(tone).run();
    }


    class WaveformDisplayPanel extends JPanel {
        private final Oscillator oscillator;
        private final int width = 170;
        private final int height = 100;

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

    class EffectTransferHandler extends TransferHandler {
        private final EffectChain chain;
        private final DefaultListModel<EffectRack> model;
        private int draggedIndex = -1;

        public EffectTransferHandler(EffectChain chain, DefaultListModel<EffectRack> model) {
            this.chain = chain;
            this.model = model;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JList<?> list = (JList<?>) c;
            draggedIndex = list.getSelectedIndex();
            return new StringSelection("");
        }

        @Override
        public boolean canImport(TransferSupport support) {
            // Check if the drop target is a JList
            return support.getComponent() instanceof JList;
        }

        @Override
        public boolean importData(TransferSupport support) {
            try {
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int dropIndex = dl.getIndex();

                if (draggedIndex == -1 || draggedIndex == dropIndex) {
                    return false;
                }

                // Handle downward drag correctly
                if (dropIndex > draggedIndex) {
                    dropIndex--;
                }

                // Update both model and effect chain
                EffectRack moved = model.remove(draggedIndex);
                model.add(dropIndex, moved);
                chain.reorderEffect(draggedIndex, dropIndex);


                // Update selection
                JList<?> list = (JList<?>) support.getComponent();
                list.clearSelection();
                list.setSelectedIndex(dropIndex);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
    }

    class EffectListRenderer extends DefaultListCellRenderer {
        private final Color BACKGROUND = new Color(45, 45, 48);
        private final Color SELECTION_BG = new Color(80, 80, 80);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 80, 80)),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            label.setIcon(new DragHandleIcon());
            label.setBackground(isSelected ? SELECTION_BG : BACKGROUND);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (value instanceof EffectRack) {
                String className = value.getClass().getSimpleName();
                className = className.replace("Effect", "");
                label.setText(className);
            }
            return label;
        }
    }

    // Custom drag handle icon
    class DragHandleIcon implements Icon {
        private final Color COLOR = new Color(150, 150, 150);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR);

            int dotSize = 2;
            int spacing = 4;
            for (int i = 0; i < 3; i++) {
                g2.fillOval(x, y + i * spacing, dotSize, dotSize);
            }

            g2.dispose();
        }

        @Override public int getIconWidth() { return 10; }
        @Override public int getIconHeight() { return 14; }
    }

    // Styled combo box renderer
    class StyledComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            label.setBackground(isSelected ? new Color(80, 80, 80) : new Color(60, 60, 60));
            label.setForeground(Color.WHITE);
            return label;
        }
    }

    class CustomPowerButton extends JToggleButton {
        private static final Color ON_COLOR = new Color(120, 240, 80);
        private static final Color OFF_COLOR = new Color(200, 0, 0);
        private static final Color HOVER_COLOR = new Color(255, 255, 255, 50);
        private static final int SIZE = 24;

        public CustomPowerButton() {
            setPreferredSize(new Dimension(SIZE, SIZE));
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Button background
            Color bgColor = isSelected() ? ON_COLOR : OFF_COLOR;
            if (getModel().isPressed()) {
                bgColor = bgColor.darker();
            } else if (getModel().isRollover()) {
                bgColor = bgColor.brighter();
            }

            // Main circle
            g2.setColor(bgColor);
            g2.fillOval(0, 0, SIZE, SIZE);

            // Inner symbol
            g2.setColor(Color.WHITE);
            if (isSelected()) {
                // "I" shape when on
                g2.fillRect(SIZE/2 - 2, SIZE/4, 4, SIZE/2);
            } else {
                // "O" shape when off
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(SIZE/4, SIZE/4, SIZE/2, SIZE/2);
            }

            // Hover effect
            if (getModel().isRollover()) {
                g2.setColor(HOVER_COLOR);
                g2.fillOval(0, 0, SIZE, SIZE);
            }

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No border
        }
    }
}
