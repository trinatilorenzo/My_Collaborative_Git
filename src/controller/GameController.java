package controller;

import main.GameSetting;
import model.GameModel;
import view.GameView;

import static main.GameSetting.*;

public class GameController {
    // ALL THE CONTROLLER STAFF HERE
    // input, game loop , system ...

    private GameModel model;
    private GameView view;
    private KeyHandler keyHandler;
    private GameLoop loop;


    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;

        this.keyHandler = new KeyHandler();
        this.loop = new GameLoop(this);
        view.addKeyListener(keyHandler);
        view.setFocusable(true);
    }

    public void startGame() {
        loop.start();
    }
    public void stopGame(){
        loop.stopGameLoop();
    }

    public void update() {
        model.update(keyHandler);
    }

    public void render() {
        view.repaint();
    }
}