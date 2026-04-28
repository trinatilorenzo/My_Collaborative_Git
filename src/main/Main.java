package main;


import controller.GameController;
import main.CONFIG.GameConfig;
import model.GameModel;
import view.GameView;

import java.awt.Cursor;
import javax.swing.JFrame;

/**
 * This is the main class of the game.
 * The game java app uses an MVC structure. So to work need to create a GameController, a GameModel, and a GameView.
 */

public class Main {

    public static void main(String[] args) {

        // load the game configuration from the XML file
        // this class is specific for our game and contains all the game's constants'
        GameConfig GS = new GameConfig();

        // MVC structure
        // Define the tre basic object Model View Controller
        // ------------------------------------------------------------------------------------------------------------
        GameModel model = new GameModel(GS);
        GameView view = new GameView(GS, model);
        GameController controller = new GameController(model, view);
        // ------------------------------------------------------------------------------------------------------------

        // main application window: contains the whole game
        // ------------------------------------------------------------------------------------------------------------
        JFrame window = new JFrame("Tiny Swords Island");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //TODO: FINESTRA RIDIMENSIONABILE
        window.setResizable(true);

        window.add(view);
        window.pack();
        Cursor customCursor = view.getCustomGameCursor();
        if (customCursor != null) {
            view.setCursor(customCursor);
            window.setCursor(customCursor);
        }

        //window.addKeyListener(controller);

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        // ------------------------------------------------------------------------------------------------------------

        // start the game-loop in a separated thread
        controller.startGame();

        
    }// end of main method

}//end of main class
