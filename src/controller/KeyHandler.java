package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * KEY HANDLER CLASS
 * Manage input from the keyboard
 */
//-------------------------------------------------------------------------------------------------------------------
public class KeyHandler implements KeyListener {

    // variable to set the state of the keys
    private volatile boolean up, down, left, right ;
    private volatile boolean attackRequested;
    private volatile boolean interact = false; 

    private volatile boolean debugToggle = false;
    private volatile boolean pauseToggle = false;
    private volatile boolean menuPrevious = false;
    private volatile boolean menuNext = false;
    private volatile boolean menuConfirm = false;

    private boolean pausePressed = false;
    private boolean debugPressed = false;
    private boolean attackPressed = false;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> { up = true; menuPrevious = true; }
            case KeyEvent.VK_S -> { down = true; menuNext = true; }
            case KeyEvent.VK_A -> { left = true; menuPrevious = true; }
            case KeyEvent.VK_D -> { right = true; menuNext = true; }
            case KeyEvent.VK_UP, KeyEvent.VK_LEFT -> menuPrevious = true;
            case KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT -> menuNext = true;
            case KeyEvent.VK_SPACE -> {
                if (!attackPressed) {
                    attackRequested = true;
                    attackPressed = true;
                }
                menuConfirm = true;
            }
            case KeyEvent.VK_ENTER -> menuConfirm = true;
            case KeyEvent.VK_M -> interact = true;

            // Anti repetition toggle
            case KeyEvent.VK_P -> {
                if (!pausePressed) {
                    pauseToggle = !pauseToggle;
                    pausePressed = true;
                }
            }
            case KeyEvent.VK_F3 -> {
                if (!debugPressed) {
                    debugToggle = !debugToggle;
                    debugPressed = true;
                }
            }}
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = false;
            case KeyEvent.VK_S -> down = false;
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
            case KeyEvent.VK_SPACE -> attackPressed = false;
            case KeyEvent.VK_M -> interact = false;
            case KeyEvent.VK_P -> pausePressed = false;
            case KeyEvent.VK_F3 -> debugPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     *  translates the keyboard state into the game's input state, it's a bridge between controller and model
     */
    //-------------------------------------------------------------
    public synchronized InputState getInputState() {
        InputState state = new InputState(
                up,
                down,
                left,
                right,
                attackRequested,
                pauseToggle,
                debugToggle,
                interact,
                menuPrevious,
                menuNext,
                menuConfirm
        );

        attackRequested = false;
        interact = false; // Reset interact after returning the state, so it only triggers once per key press
        menuPrevious = false;
        menuNext = false;
        menuConfirm = false;
        return state;
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
