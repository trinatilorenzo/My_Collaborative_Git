package controller;

import model.GameModel;
import view.GameView;
import main.GameSetting.GameState;

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
    private boolean renderOnceOnPause = true;

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
    public void update(double deltaMs) {

        // pause toggle edge-triggered
        if (keyHandler.isPauseToggle()) {
            model.setGameState(GameState.PAUSED);
        } else {
            model.setGameState(GameState.PLAYING);
            renderOnceOnPause = true;
        }

        // debug flag stored in model
        model.setDebugMode(keyHandler.isDebugToggle());

        model.update(keyHandler, deltaMs);

        if (model.getGameState() == GameState.PLAYING) {
            view.updateAnimations(deltaMs);
        }

    }
    //-------------------------------------------------------------
    // CONTROLL GAME VIEW
    public void render() {
        if (model.getGameState() == GameState.PLAYING) {
            view.repaint();
        } else if (renderOnceOnPause) {
            view.repaint();
            renderOnceOnPause = false;

        }
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------
