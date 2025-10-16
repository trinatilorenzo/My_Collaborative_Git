package main;


import controller.GameController;
import model.GameModel;
import view.GameView;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // MVC structute

        // Define the tre basic object Model View Controller
        // ------------------------------------------------------------------------------------------------------------
        GameSetting gs = new GameSetting();

        GameModel model = new GameModel(gs);
        GameView view = new GameView(model, gs);

        GameController controller = new GameController(model, view, gs);
        // ------------------------------------------------------------------------------------------------------------

        // main application window: contains the whole game
        // ------------------------------------------------------------------------------------------------------------
        JFrame window = new JFrame("Nome Del Gioco (Da decidere)");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        window.add(view);
        window.pack();

        //window.addKeyListener(controller);

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        // ------------------------------------------------------------------------------------------------------------

        // start the game-loop in a separated thread
        controller.startGame();

        // --- end of main method ---
    }

}
