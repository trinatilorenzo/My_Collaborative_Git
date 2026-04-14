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

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = true;
            case KeyEvent.VK_S -> down = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_SPACE -> attack = true;
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
        InputState state = new InputState(up, down, left, right, attack, pauseToggle, debugToggle, interact);
        interact = false; // Reset interact after returning the state, so it only triggers once per key press
        return state;
    }
}
//-------------------------------------------------------------------------------------------------------------------
