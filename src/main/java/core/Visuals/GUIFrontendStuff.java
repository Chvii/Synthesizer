package core.Visuals;

import core.SynthLogic.Effects.DelayVerb;
import core.SynthLogic.Effects.EffectController;
import core.SynthLogic.Effects.EffectPicker;
import core.SynthLogic.Effects.FilterEffect;
import core.SynthLogic.Mixer;
import core.SynthLogic.Tone;
import core.SynthLogic.Voice;
import core.WaveformStrategy.WaveformStrategyPicker;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Filter;

public class GUIFrontendStuff extends JFrame {
    private WaveformStrategyPicker waveformStrategyPicker;
    private JLabel strategyLabel;
    private JLabel octaveStateLabel;
    private Tone tone;
    private EffectController effectController;
    private double attackValueOnGraph;
    private double decayValueOnGraph;
    private double sustainValueOnGraph = 1;
    private double releaseValueOnGraph;
    public GUIFrontendStuff(Mixer mixer, WaveformStrategyPicker waveformStrategyPicker, Tone tone, EffectController effectController) {
        this.waveformStrategyPicker = waveformStrategyPicker;
        this.tone = tone;
        this.effectController = effectController;
        // Frame Setup
        setTitle("core.SynthLogic.Flagzisizer VST");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Absolute layout for custom positioning
        setResizable(false);

        // Background Panel
        JPanel backgroundPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(45, 45, 48), getWidth(), getHeight(), new Color(25, 25, 28));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setBounds(0, 0, 1000, 800);
        add(backgroundPanel);

        // Waveform Display
        JPanel waveformPanelWithControls = createWaveformPanelWithControls(mixer);
        waveformPanelWithControls.setBounds(20, 20, 240, 150);
        backgroundPanel.add(waveformPanelWithControls);

        // ADSR Panel
        JPanel adsrPanel = createADSRPanel();
        adsrPanel.setBounds(20, 300, 400, 350);
        backgroundPanel.add(adsrPanel);

        // Octave buttons and text
        JPanel octaveChangerPanel = createOctaveButtons();
        octaveChangerPanel.setBounds(400,20,200,100);
        backgroundPanel.add(octaveChangerPanel);

        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        footerPanel.setBounds(0, 750, 1000, 50);
        backgroundPanel.add(footerPanel);

        // Effect panel
        JPanel effectPanel = createEffectPanel(effectController);
        effectPanel.setBounds(450,300,500,300);
        backgroundPanel.add(effectPanel);

        setVisible(true);
    }

    private JPanel createWaveformPanelWithControls(Mixer mixer) {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(30, 30, 30));

        // Waveform Display
        WaveformPanel waveformPanel = new WaveformPanel();
        mixer.addWaveformUpdateListener(waveformPanel);
        waveformPanel.setBounds(20, 10, 200, 100);
        panel.add(waveformPanel);

        // Waveform Strategy Controls
        JButton prevButton = new JButton("<");
        prevButton.setBounds(20, 110, 50, 30);
        prevButton.addActionListener(e -> {
            waveformStrategyPicker.previousWaveform();
            strategyLabel.setText(waveformStrategyPicker.StringifyWaveformSelector());
        });
        prevButton.setForeground(Color.BLACK);
        panel.add(prevButton);

        strategyLabel = new JLabel(waveformStrategyPicker.StringifyWaveformSelector(), SwingConstants.CENTER); // Placeholder text
        repaint();
        strategyLabel.setBounds(80, 110, 80, 30);
        strategyLabel.setForeground(Color.WHITE);
        strategyLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(strategyLabel);

        JButton nextButton = new JButton(">");
        nextButton.setBounds(170, 110, 50, 30);
        nextButton.addActionListener(e -> {
            waveformStrategyPicker.nextWaveform();
            strategyLabel.setText(waveformStrategyPicker.StringifyWaveformSelector());
        });
        nextButton.setForeground(Color.BLACK);
        panel.add(nextButton);

        return panel;
    }
    private JPanel createOctaveButtons() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  // Set vertical layout for buttons


        JButton octaveUpButton = new JButton("^");
        octaveUpButton.addActionListener(e -> {
            Voice.increaseOctave();
            octaveStateLabel.setText(Voice.getOctaveString());
        });
        panel.add(octaveUpButton);


        JButton octaveDownButton = new JButton("v");
        octaveDownButton.addActionListener(e -> {
            Voice.decreaseOctave();
            octaveStateLabel.setText(Voice.getOctaveString());
        });
        panel.add(octaveDownButton);


        JLabel octaveLabel = new JLabel("Change Octave");
        octaveLabel.setForeground(Color.WHITE);
        octaveLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(octaveLabel);

        octaveStateLabel = new JLabel(String.valueOf(Voice.getOctave()));
        octaveStateLabel.setForeground(Color.WHITE);
        octaveStateLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        panel.add(octaveStateLabel);

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
                addDelayVerbKnobs(panel);
            } else if (selectedEffect == EffectPicker.EffectEnums.FILTER) {
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
        cutoffKnob.setDefaultPosition(false,false,true);
        cutoffKnob.addKnobListener(value ->{
            FilterEffect filterEffect = (FilterEffect) effectController.getCurrentEffect();
            filterEffect.setCutoff((double) value);
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
            filterEffect.setResonance((double) value);
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
            delayVerb.setDelayTimeInSeconds((double) value);
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
            delayVerb.setFeedback((double) value);
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
            delayVerb.setMix((double) value);
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

    private void setAttackValueOnGraph(double attackValueOnGraph){
        this.attackValueOnGraph = attackValueOnGraph;
    }
    private double getAttackValueOnGraph(){
        return attackValueOnGraph;
    }
    private void setDecayValueOnGraph(double decayValueOnGraph){
        this.decayValueOnGraph = decayValueOnGraph;
    }
    private double getDecayValueOnGraph(){
        return decayValueOnGraph;
    }
    private void setSustainValueOnGraph(double sustainValueOnGraph){
        this.sustainValueOnGraph = sustainValueOnGraph;
    }
    private double getSustainValueOnGraph(){
        return (1-sustainValueOnGraph)*200;
    }
    private void setReleaseValueOnGraph(double releaseValueOnGraph){
        this.releaseValueOnGraph = releaseValueOnGraph;
    }
    private double getReleaseValueOnGraph(){
        return releaseValueOnGraph;
    }

    private JPanel createADSRPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));

        // ADSR Graph
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GREEN);
                g2.setStroke(new BasicStroke(2));

                int[] xPoints = {
                        0, /* initial pos*/
                        (int) (getAttackValueOnGraph()/50), /* attack time */
                        (int) (getAttackValueOnGraph()/50) + (int) (getDecayValueOnGraph()/50), /* decay time */
                        280, /* release time */
                        280 + (int) (getReleaseValueOnGraph()/50)};
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
        graphPanel.setBounds(10, 10, 380, 200);
        graphPanel.setBackground(new Color(20, 20, 20));
        panel.add(graphPanel);

        // Attack Knob
        JKnob attackKnob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        attackKnob.setRange(1, 5000);
        attackKnob.setRadius(25);
        attackKnob.addKnobListener(value -> Voice.setAttackTime(value));
        attackKnob.addKnobGraphListener(value -> setAttackValueOnGraph(value));
        attackKnob.setBounds(20, 220, 150, 150);
        panel.add(attackKnob);

        JLabel attackLabel = new JLabel("Attack");
        attackLabel.setForeground(Color.WHITE);
        attackLabel.setBounds(25, 270, 80, 20);
        panel.add(attackLabel);

        // Decay Knob
        JKnob decayKnob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        decayKnob.setRange(1, 5000);
        decayKnob.setRadius(25);
        decayKnob.addKnobListener(value -> Voice.setDecayTime(value));
        decayKnob.addKnobGraphListener(value -> setDecayValueOnGraph(value));
        decayKnob.setBounds(120, 220, 80, 80);
        panel.add(decayKnob);

        JLabel decayLabel = new JLabel("Decay");
        decayLabel.setForeground(Color.WHITE);
        decayLabel.setBounds(125, 270, 80, 20);
        panel.add(decayLabel);

        // Sustain Knob
        JKnob sustainKnob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        sustainKnob.setRange(0.0, 1.0);
        sustainKnob.setDefaultPosition(false,false,true);
        sustainKnob.setRadius(25);
        sustainKnob.addKnobListener(value -> Voice.setSustainLevel(value));
        sustainKnob.addKnobGraphListener(value -> setSustainValueOnGraph(value));
        sustainKnob.setBounds(220, 220, 80, 80);
        panel.add(sustainKnob);

        JLabel sustainLabel = new JLabel("Sustain");
        sustainLabel.setForeground(Color.WHITE);
        sustainLabel.setBounds(225, 270, 80, 20);
        panel.add(sustainLabel);

        // Release Knob
        JKnob releaseKnob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        releaseKnob.setRange(1, 5000);
        releaseKnob.setRadius(25);
        releaseKnob.addKnobListener(value -> Voice.setReleaseTime(value));
        releaseKnob.addKnobGraphListener(value -> setReleaseValueOnGraph(value));
        releaseKnob.setBounds(320, 220, 80, 80);
        panel.add(releaseKnob);

        JLabel releaseLabel = new JLabel("Release");
        releaseLabel.setForeground(Color.WHITE);
        releaseLabel.setBounds(325, 270, 80, 20);
        panel.add(releaseLabel);

        return panel;
    }



    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 20, 20));

        JLabel branding = new JLabel("core.SynthLogic.Flagzisizer VST by Flagz", SwingConstants.CENTER);
        branding.setForeground(new Color(200, 200, 200));
        branding.setFont(new Font("SansSerif", Font.ITALIC, 16));
        panel.add(branding);

        return panel;
    }
}
