package main;

import javax.swing.*;

public class main {
    // This is the main calas of MONTELAGO GAME

    public static void main (String[] args) {

        JFrame window = new JFrame() ;
        window.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE) ;
        window.setResizable(true) ;
        window.setTitle("My game") ; 

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.setupGame(); // the geme is being setup with all object and music

        // the game loop start from here
        gamePanel.startGameThread();
    }

}
