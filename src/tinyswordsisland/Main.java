package tinyswordsisland;


import tinyswordsisland.controller.GameController;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.model.GameModel;
import tinyswordsisland.view.GameView;

import java.awt.Cursor;
import java.awt.Taskbar;
import javax.swing.*;

/**
 * This is the main class of the game.
 * The game java app uses an MVC structure. So to work need to create a GameController, a GameModel, and a GameView.
 */

public class Main {


    public static void main(String[] args) {


        // load the game configuration from the XML file
        // this class is specific for our game and contains all the game's constants'
        GameConfig GS = new GameConfig("MappaGiocoV4.tmx", "tileSet1.tsx");

        // MVC structure
        // Define the tre basic object Model View Controller
        // ------------------------------------------------------------------------------------------------------------
        GameModel model = new GameModel(GS);
        GameController controller = new GameController(model);
        GameView view = new GameView(GS, controller);
        controller.setView(view);
        // ------------------------------------------------------------------------------------------------------------

        // main application window: contains the whole game
        // ------------------------------------------------------------------------------------------------------------
        JFrame window = new JFrame("Tiny Swords Island");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // load the logo
        ImageIcon logo = new ImageIcon(Main.class.getResource("/res/UI/Icons/logo_gioco.png"));
        window.setIconImage(logo.getImage());
        try {Taskbar.getTaskbar().setIconImage(logo.getImage());} catch (Exception ignored) {}


        window.add(view);
        window.pack();
        Cursor customCursor = view.getCustomGameCursor();
        if (customCursor != null) {
            view.setCursor(customCursor);
            window.setCursor(customCursor);
        }

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        view.requestFocusInWindow(); // force active state directly on the tinyswordsisland.view
        view.setResolution();

        // ------------------------------------------------------------------------------------------------------------

        // start the game-loop in a separated thread
        controller.startGame();

        
    }// end of main method

}//end of main class
