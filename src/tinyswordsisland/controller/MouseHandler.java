package tinyswordsisland.controller;

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

    // Posizione corrente del cursore (aggiornata da mouseMoved/mouseDragged)
    private volatile int mouseX;
    private volatile int mouseY;

    // Posizione esatta al momento del click (separata dal move, per evitare drift tra eventi)
    private volatile int clickX;
    private volatile int clickY;

    private volatile boolean leftClick;
    private volatile boolean rightClick;

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            clickX = e.getX();
            clickY = e.getY();
            mouseX = e.getX();
            mouseY = e.getY();
            leftClick = true;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            clickX = e.getX();
            clickY = e.getY();
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

    /** Posizione corrente del cursore (per hover). */
    public Point getMousePosition() {
        return new Point(mouseX, mouseY);
    }

    /**
     * Consuma il click sinistro e restituisce la posizione esatta in cui è avvenuto.
     * Restituisce null se non c'è nessun click pendente.
     */
    public synchronized Point consumeLeftClick() {
        if (!leftClick) return null;
        leftClick = false;
        return new Point(clickX, clickY);
    }

    /**
     * Consuma il click destro e restituisce la posizione esatta in cui è avvenuto.
     * Restituisce null se non c'è nessun click pendente.
     */
    public synchronized Point consumeRightClick() {
        if (!rightClick) return null;
        rightClick = false;
        return new Point(clickX, clickY);
    }

    // Not used
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
//-------------------------------------------------------------------------------------------------------------------