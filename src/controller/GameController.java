package controller;

import main.GameSetting;
import model.GameModel;
import view.GameView;

public class GameController {
    // ALL THE CONTROLLER STAFF HERE
    // input, game loop , system ...

    private GameModel model;
    private GameView view;
    private KeyHandler keyHandler;
    private GameLoop loop;
    private GameSetting gs;

    public GameController(GameModel model, GameView view, GameSetting setting) {
        this.model = model;
        this.view = view;
        this.gs = setting;
        this.keyHandler = new KeyHandler();
        this.loop = new GameLoop(this, gs);
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