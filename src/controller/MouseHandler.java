package controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * MOUSE HANDLER CLASS
 * Manage input from the Mouse
 */
//-------------------------------------------------------------------------------------------------------------------
public class MouseHandler implements MouseListener, MouseMotionListener {

    // variable to set the state of the keys
    private volatile int mouseX;
    private volatile int mouseY;
    private volatile boolean leftClick;
    private volatile boolean rightClick;

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseX = e.getX();
            mouseY = e.getY();
            leftClick = true;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            mouseX = e.getX();
            mouseY = e.getY();
            rightClick = true;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public Point getMousePosition() {
        return new Point(mouseX, mouseY);
    }

    public synchronized boolean consumeLeftClick() {
        boolean clicked = leftClick;
        leftClick = false;
        return clicked;
    }
    public synchronized boolean consumeRightClick() {
        boolean clicked = rightClick;
        rightClick = false;
        return clicked;
    }

    // Not used
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
//-------------------------------------------------------------------------------------------------------------------