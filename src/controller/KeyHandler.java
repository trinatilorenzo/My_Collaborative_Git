package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// - KEY HANDLER CLASS
//  Manage input from keyboard
//-------------------------------------------------------------------------------------------------------------------
public class KeyHandler implements KeyListener {
    private boolean up, down, left, right ;
    private boolean attack;
    private boolean interact = false; 

    private boolean debugToggle = false;
    private boolean pauseToggle = false;
    private boolean menuPrevious = false;
    private boolean menuNext = false;
    private boolean menuConfirm = false;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> {
                up = true;
                menuPrevious = true;
            }
            case KeyEvent.VK_S -> {
                down = true;
                menuNext = true;
            }
            case KeyEvent.VK_A -> {
                left = true;
                menuPrevious = true;
            }
            case KeyEvent.VK_D -> {
                right = true;
                menuNext = true;
            }
            case KeyEvent.VK_UP, KeyEvent.VK_LEFT -> menuPrevious = true;
            case KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT -> menuNext = true;
            case KeyEvent.VK_SPACE -> {
                attack = true;
                menuConfirm = true;
            }
            case KeyEvent.VK_ENTER -> menuConfirm = true;
            case KeyEvent.VK_P -> pauseToggle = !pauseToggle;
            case KeyEvent.VK_F3 -> debugToggle = !debugToggle;
            case KeyEvent.VK_M -> interact = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = false;
            case KeyEvent.VK_S -> down = false;
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
            case KeyEvent.VK_SPACE -> attack = false;
            case KeyEvent.VK_M -> interact = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public InputState getInputState() { // translates the keyboard state into the game's input state, it's a bridge between controller and model
        InputState state = new InputState(
                up,
                down,
                left,
                right,
                attack,
                pauseToggle,
                debugToggle,
                interact,
                menuPrevious,
                menuNext,
                menuConfirm
        );
        interact = false; // Reset interact after returning the state, so it only triggers once per key press
        menuPrevious = false;
        menuNext = false;
        menuConfirm = false;
        return state;
    }
}
//-------------------------------------------------------------------------------------------------------------------
