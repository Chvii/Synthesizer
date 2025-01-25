package core.Visuals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.DoubleConsumer;

class JKnob extends JComponent implements MouseListener, MouseMotionListener {

    private int radius = 50; // Radius of the knob
    private int spotRadius = 5; // Spot radius for visualization
    private double minAngle = Math.PI + Math.PI / 4; // Bottom-left
    private double maxAngle = 3 * Math.PI - Math.PI / 4; // Bottom-right

    private double theta; // Current angle of the knob
    private double minValue = 0.0; // Minimum value for the knob
    private double maxValue = 1.0; // Maximum value for the knob
    private double currentValue; // Current value of the knob
    private Color knobColor;
    private Color spotColor;
    private DoubleConsumer valueSetter;

    private boolean pressedOnKnob; // Track if the user clicked on the knob
    private int lastY; // Track the last y-coordinate of the mouse

    public JKnob(Color knobColor, Color spotColor) {
        this.theta = minAngle;
        this.knobColor = knobColor;
        this.spotColor = spotColor;
        this.pressedOnKnob = false;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public void setRange(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    public void setDefaultPosition(boolean counterclockwise, boolean middle, boolean clockwise){
        if(counterclockwise){
            theta = Math.PI + Math.PI / 4;
        }
        if(middle){
            theta = (3 * Math.PI - Math.PI / 4) - (Math.PI + Math.PI / 4);
        }
        if(clockwise){
            theta = 3 * Math.PI - Math.PI / 4;
        }
    }

    public void setRadius(int i){
        this.radius = i;
        this.spotRadius = i/10;
    }
    public double getMinAngle(){
        return minAngle;
    }
    public double getRadius(){
        return radius;
    }

    /**
     * Paint the knob with a circular surface and spot.
     */
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

    /**
     * Get the preferred size of the knob.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(2 * radius, 2 * radius);
    }

    /**
     * Calculate the center of the spot.
     */
    private Point getSpotCenter() {
        int r = radius - spotRadius;
        int x = (int) (radius + r * Math.sin(theta));
        int y = (int) (radius - r * Math.cos(theta));
        return new Point(x, y);
    }

    /**
     * Determine if a point is within the knob's circular bounds.
     */
    private boolean isOnKnob(Point pt) {
        int dx = pt.x - radius;
        int dy = pt.y - radius;
        return dx * dx + dy * dy <= radius * radius; // Check if within the circle
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point mouseLoc = e.getPoint();
        if (isOnKnob(mouseLoc)) {
            pressedOnKnob = true;
            lastY = e.getY(); // Record the initial y-coordinate
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressedOnKnob = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressedOnKnob) {
            int currentY = e.getY();
            int deltaY = lastY - currentY;

            if (deltaY > 0) {
                theta += 0.05;
            } else if (deltaY < 0) {
                theta -= 0.05;
            }

            theta = clampAngle(theta);
            lastY = currentY;

            double normalizedValue = (theta - minAngle) / (maxAngle - minAngle);
            currentValue = minValue + normalizedValue * (maxValue - minValue);

            if (valueSetter != null) {
                valueSetter.accept(currentValue);
            }

            repaint();
        }
    }

    public void addKnobListener(DoubleConsumer valueSetter) {
        this.valueSetter = valueSetter;
    }

    /**
     * Clamp the angle to ensure it stays between minAngle and maxAngle.
     */
    private double clampAngle(double angle) {
        if (angle < minAngle) {
            return minAngle;
        }
        if (angle > maxAngle) {
            return maxAngle;
        }
        return angle;
    }

    /**
     * Normalize an angle to the range [0, 2π].
     */
    private double normalizeAngle(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    /**
     * Set the minimum angle for the knob.
     * @param angle The minimum angle in radians.
     */
    public void setMinAngle(double angle) {
        this.minAngle = normalizeAngle(angle);
    }

    /**
     * Set the maximum angle for the knob.
     * @param angle The maximum angle in radians.
     */
    public void setMaxAngle(double angle) {
        this.maxAngle = normalizeAngle(angle);
    }
    public double getModifierParameter(){
        //return modifierParameter;
        return 0;
    }

    // Unused mouse event handlers
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}