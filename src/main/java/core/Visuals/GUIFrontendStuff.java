package core.Visuals;

import core.SynthLogic.Mixer;
import core.SynthLogic.Tone;
import core.SynthLogic.Voice;
import core.WaveformStrategy.WaveformStrategyPicker;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class GUIFrontendStuff extends JFrame {
    private WaveformStrategyPicker waveformStrategyPicker;
    private JLabel strategyLabel;
    private JLabel octaveStateLabel;
    private Tone tone;
    public GUIFrontendStuff(Mixer mixer, WaveformStrategyPicker waveformStrategyPicker, Tone tone) {
        this.waveformStrategyPicker = waveformStrategyPicker;
        this.tone = tone;
        // Frame Setup
        setTitle("Synthesizer VST");
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
        adsrPanel.setBounds(20, 300, 500, 300);
        backgroundPanel.add(adsrPanel);

        // Octave buttons and text
        JPanel octaveChangerPanel = createOctaveButtons();
        octaveChangerPanel.setBounds(400,20,200,100);
        backgroundPanel.add(octaveChangerPanel);

        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        footerPanel.setBounds(0, 750, 1000, 50);
        backgroundPanel.add(footerPanel);

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
            tone.increaseOctave();
            octaveStateLabel.setText(tone.getOctaveString());
        });
        panel.add(octaveUpButton);


        JButton octaveDownButton = new JButton("v");
        octaveDownButton.addActionListener(e -> {
            tone.decreaseOctave();
            octaveStateLabel.setText(tone.getOctaveString());
        });
        panel.add(octaveDownButton);


        JLabel octaveLabel = new JLabel("Change Octave");
        octaveLabel.setForeground(Color.WHITE);
        octaveLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(octaveLabel);

        octaveStateLabel = new JLabel(String.valueOf(tone.getOctave()));
        octaveStateLabel.setForeground(Color.WHITE);
        octaveStateLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        panel.add(octaveStateLabel);

        return panel;
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
                int[] xPoints = {0, (int) (Voice.getAttackTime()), 150, 250, 300};
                int[] yPoints = {200, 50, 100, 100, 200};
                g2.drawPolyline(xPoints, yPoints, xPoints.length);
            }
        };
        graphPanel.setBounds(10, 10, 480, 200);
        graphPanel.setBackground(new Color(20, 20, 20));
        panel.add(graphPanel);

        // ADSR Knobs
        JPanel attackKnob = createKnobPanel("Attack", Voice::setAttackTime);
        attackKnob.setBounds(20, 220, 80, 60);

        JPanel decayKnob = createKnobPanel("Decay", Voice::setDecayTime);
        decayKnob.setBounds(120, 220, 80, 60);

        JPanel sustainKnob = createKnobPanel("Sustain", Voice::setSustainLevel);
        sustainKnob.setBounds(220, 220, 80, 60);

        JPanel releaseKnob = createKnobPanel("Release", Voice::setReleaseTime);
        releaseKnob.setBounds(320, 220, 80, 60);

        panel.add(attackKnob);
        panel.add(decayKnob);
        panel.add(sustainKnob);
        panel.add(releaseKnob);

        return panel;
    }


    private JPanel createKnobPanel(String label, java.util.function.DoubleConsumer valueSetter) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        // JKnob
        JKnob knob = new JKnob(new Color(70, 70, 70), Color.BLACK);
        knob.setRadius(20);

        // Add listener to update ADSR value
        knob.addKnobListener(valueSetter);


        // Knob Label
        JLabel knobLabel = new JLabel(label, SwingConstants.LEFT);
        knobLabel.setForeground(Color.WHITE);
        knobLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Add JKnob and Label
        panel.add(knob, BorderLayout.CENTER);
        panel.add(knobLabel, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 20, 20));

        JLabel branding = new JLabel("Synthesizer VST by Flagz", SwingConstants.CENTER);
        branding.setForeground(new Color(200, 200, 200));
        branding.setFont(new Font("SansSerif", Font.ITALIC, 16));
        panel.add(branding);

        return panel;
    }
}
