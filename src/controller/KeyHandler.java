package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import input.InputState;

// - KEY HANDLER CLASS
//  Manage input from keyboard
//-------------------------------------------------------------------------------------------------------------------
public class KeyHandler implements KeyListener {
    private boolean up, down, left, right ;
    private boolean attack;
    private boolean interact = false; // reserved for future use

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
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public InputState getInputState() { // translates the keyboard state into the game's input state, it's a bridge between controller and model
        return new InputState(up, down, left, right, attack, interact, pauseToggle, debugToggle);
    }
}
//-------------------------------------------------------------------------------------------------------------------
