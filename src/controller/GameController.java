package controller;

import model.GameModel;
import view.GameView;

/**
 * ALL THE CONTROLLER STAFF HERE
 * input, game loop , system ...
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameController {

    private final GameModel model;
    private final GameView view;
    private final KeyHandler keyHandler;
    private final GameLoop loop;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;

        this.keyHandler = new KeyHandler();
        this.loop = new GameLoop(this);
        view.addKeyListener(keyHandler);
        view.setFocusable(true);
    }
    //-------------------------------------------------------------

    // START the game-loop thread
    public void startGame() {
        loop.start();
    }
    //-------------------------------------------------------------
    // STOP the game-loop thread
    public void stopGame(){
        loop.stopGameLoop();
    }
    //-------------------------------------------------------------

    // CONTROLL GAME MODEL
    public void update() {

        // debug mode controll
        if (keyHandler.isDebugToggle()) {
            view.setDebugModeON();
        }else {
            view.setDebugModeOFF();
        }
        //-------------------------

        model.update(keyHandler);

    }
    //-------------------------------------------------------------
    // CONTROLL GAME VIEW
    public void render() {
        view.repaint();
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------