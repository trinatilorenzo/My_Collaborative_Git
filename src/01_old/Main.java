package old;

import old.CONTROLLER.GameController;
import old.MODEL.GameModel;
import old.VIEW.GameView;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // MVC structute

        // Define the tre basic object Model View Controller
        // ------------------------------------------------------------------------------------------------------------
        GameSetting gs = new GameSetting();

        GameModel model = new GameModel();
        GameController controller = new GameController(model, view, gs);
        GameView view = new GameView(model, controller, gs);




        // ------------------------------------------------------------------------------------------------------------

        // main application window: contains the whole game
        // ------------------------------------------------------------------------------------------------------------
        JFrame window = new JFrame("Nome Del Gioco (Da decidere)");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        window.add(view);
        window.pack();

        window.addKeyListener(controller);

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        // ------------------------------------------------------------------------------------------------------------

        // start the game-loop in a separated thread
        controller.startGameLoop();

        // --- end of main method ---
    }

}
