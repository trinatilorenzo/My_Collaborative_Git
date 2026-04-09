package main;


import controller.GameController;
import main.CONFIG.GameConfig;
import model.GameModel;
import view.GameView;

import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {
        // MVC structure

        // Define the tre basic object Model View Controller
        // ------------------------------------------------------------------------------------------------------------
        GameConfig GS = new GameConfig();

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

        //window.addKeyListener(controller);

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        // ------------------------------------------------------------------------------------------------------------

        // start the game-loop in a separated thread
        controller.startGame();

        
    }// end of main method

}//end of main class
