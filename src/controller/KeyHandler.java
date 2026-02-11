package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// - KEY HANDLER CLASS
//  Manage input from keyboard
//-------------------------------------------------------------------------------------------------------------------
public class KeyHandler implements KeyListener {
    private boolean up, down, left, right;
    private boolean attack;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = true;
            case KeyEvent.VK_S -> down = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_SPACE -> attack = true;
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

    // Getters for key states
    public boolean isAttack() { return attack; }
    public boolean isUp() { return up; }
    public boolean isDown() { return down; }
    public boolean isLeft() { return left; }
    public boolean isRight() { return right; }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
//-------------------------------------------------------------------------------------------------------------------