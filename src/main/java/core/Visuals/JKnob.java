package core.Visuals;

import core.Misc.FunctionalValueSetter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.function.DoubleConsumer;

import static java.awt.event.MouseEvent.BUTTON2;
import static java.awt.event.MouseEvent.BUTTON3;

public class JKnob extends JComponent implements MouseListener, MouseMotionListener {

    private int radius = 50; // Radius of the knob
    private int spotRadius = 5; // Spot radius for visualization
    private double minAngle = Math.PI + Math.PI / 4; // Bottom-left
    private double maxAngle = 3 * Math.PI - Math.PI / 4; // Bottom-right

    private double theta; // Current angle of the knob
    private double minValue = 0.0; // Minimum value for the knob
    private double maxValue = 1.0; // Maximum value for the knob
    private double currentValue; // Current value of the knob
    private double defaultPositionValue; // Default position (angle) to reset to
    private Color knobColor;
    private Color spotColor;
    private DoubleConsumer valueSetter;
    private FunctionalValueSetter graphValueSetter;
    private boolean isLogarithmic = false;

    private boolean pressedOnKnob; // Track if the user clicked on the knob
    private int lastY; // Track the last y-coordinate of the mouse

    public JKnob(Color knobColor, Color spotColor) {
        this.theta = minAngle;
        this.defaultPositionValue = theta;
        this.knobColor = knobColor;
        this.spotColor = spotColor;
        this.pressedOnKnob = false;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public void setRange(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        repaint();
    }
    public void setLogarithmic(){
        isLogarithmic = true;
    }

    public double getMinValue() {
        return this.minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public String getCurrentValue() {
        DecimalFormat numberFormat = new DecimalFormat("0.0");
        return String.valueOf(numberFormat.format(currentValue)); // Corrected to return currentValue
    }

    public void setToDefaultPosition() {
        theta = defaultPositionValue;
        updateCurrentValueFromTheta(); // Update currentValue based on theta
        currentValue = 0;
        getCurrentValue(); // to fix tooltip on knob resets
        repaint();
    }

    private void updateCurrentValueFromTheta() {
        double normalizedValue = (theta - minAngle) / (maxAngle - minAngle);
        if (isLogarithmic) {
            // Logarithmic scaling: currentValue = min * (max/min)^normalized
            currentValue = minValue * Math.pow(maxValue / minValue, normalizedValue);
        } else {
            currentValue = minValue + normalizedValue * (maxValue - minValue);
        }
    }

    public void setDefaultPosition(boolean counterclockwise, boolean middle, boolean clockwise) {
        if (counterclockwise) {
            theta = minAngle;
            defaultPositionValue = theta;
        } else if (middle) {
            theta = (minAngle + maxAngle) / 2;
            defaultPositionValue = theta;
        } else if (clockwise) {
            theta = maxAngle;
            defaultPositionValue = theta;
        }
        repaint();
    }

    public void setRadius(int i) {
        this.radius = i;
        this.spotRadius = i / 10;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public void paint(Graphics g) {
        // Draw the knob
        g.setColor(knobColor);
        g.fillOval(0, 0, 2 * radius, 2 * radius);

        // Draw the spot
        Point pt = getSpotCenter();
        g.setColor(spotColor);
        g.fillOval(pt.x - spotRadius, pt.y - spotRadius, 2 * spotRadius, 2 * spotRadius);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(2 * radius, 2 * radius);
    }

    private Point getSpotCenter() {
        int r = radius - spotRadius;
        int x = (int) (radius + r * Math.sin(theta));
        int y = (int) (radius - r * Math.cos(theta));
        return new Point(x, y);
    }

    private boolean isOnKnob(Point pt) {
        int dx = pt.x - radius;
        int dy = pt.y - radius;
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point mouseLoc = e.getPoint();
        if (isOnKnob(mouseLoc)) {
            pressedOnKnob = true;
            lastY = e.getY();
            if (e.getButton() == BUTTON3) {
                setToDefaultPosition();
                // Pass currentValue instead of defaultPositionValue (angle)
                if (valueSetter != null) {
                    valueSetter.accept(currentValue);
                }
                if (graphValueSetter != null) {
                    graphValueSetter.accept(currentValue);
                }
                repaint();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != BUTTON3) {
            pressedOnKnob = false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressedOnKnob) {

            int currentY = e.getY();
            int deltaY = lastY - currentY;

            if (deltaY > 0) {
                theta += 0.05 * Math.abs(deltaY) / 4; // multiplying by deltaY to make it react to movement speed
            } else if (deltaY < 0) {
                theta -= 0.05 * Math.abs(deltaY) / 4;
            }
            theta = clampAngle(theta);
            lastY = currentY;

            updateCurrentValueFromTheta(); // Update currentValue

            if (valueSetter != null) {
                valueSetter.accept(currentValue);
            }
            if (graphValueSetter != null) {
                graphValueSetter.accept(currentValue);
            }

            repaint();
        }
    }

    public void addKnobListener(DoubleConsumer valueSetter) {
        this.valueSetter = valueSetter;
    }

    public void addKnobGraphListener(FunctionalValueSetter graphValueSetter) {
        this.graphValueSetter = graphValueSetter;
    }

    private double clampAngle(double angle) {
        return Math.max(minAngle, Math.min(angle, maxAngle));
    }

    private double normalizeAngle(double angle) {
        angle %= 2 * Math.PI;
        return angle < 0 ? angle + 2 * Math.PI : angle;
    }

    public void setMinAngle(double angle) {
        this.minAngle = normalizeAngle(angle);
    }

    public void setMaxAngle(double angle) {
        this.maxAngle = normalizeAngle(angle);
    }

    public double getModifierParameter() {
        return 0;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    public void setValue(double value) {
        currentValue = Math.max(minValue, Math.min(maxValue, value));
        double normalizedValue;
        if (isLogarithmic) {
            // Invert logarithmic scaling to get normalizedValue
            normalizedValue = Math.log(currentValue / minValue) / Math.log(maxValue / minValue);
        } else {
            normalizedValue = (currentValue - minValue) / (maxValue - minValue);
        }
        theta = minAngle + normalizedValue * (maxAngle - minAngle);
        defaultPositionValue = theta;
        repaint();
    }

    public double currentValueNormalized() {
        return (currentValue - minValue) / (maxValue - minValue);
    }
}