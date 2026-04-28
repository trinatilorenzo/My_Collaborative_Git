package controller;

import model.GameModel;
import view.GameView;
import main.CONFIG.enu.GameState;
import view.UI.UI;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * ALL THE CONTROLLER STAFF HERE
 * input, game loop, system ...
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameController {
    private static final int MENU_ITEM_COUNT = 3;
    private static final int MENU_NEW_GAME_INDEX = 0;

    private final GameModel model;
    private final GameView view;
    private final KeyHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameLoop loop;
    private boolean renderOnceOnPause = true; // flag to control rendering when paused

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;

        this.keyHandler = new KeyHandler();
        this.mouseHandler = new MouseHandler();
        this.loop = new GameLoop(this);
        
        view.addKeyListener(keyHandler); // add key listener to the view to capture keyboard input
        view.addMouseListener(mouseHandler);
        view.addMouseMotionListener(mouseHandler);
        view.setFocusable(true); // ensure the view can receive keyboard focus
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

    /**
     * Update the model status and view rendering
     */
    public void update(double deltaMs) { //called by the game loop every frame with a fixed delta time

        InputState input = keyHandler.getInputState();

        if (model.getGameState() == GameState.MENU) {
            updateMainMenu(input);
            model.setDebugMode(input.debug());
            return;
        }

        // pause toggle edge-triggered
        if (input.pause()) { 
            model.setGameState(GameState.PAUSED);
        } else {
            model.setGameState(GameState.PLAYING);
            renderOnceOnPause = true;
        }
            

        // debug mode toggle edge-triggered
        model.setDebugMode(input.debug());

        model.update(input, deltaMs);

        if (model.getGameState() == GameState.PLAYING) {
            view.updateAnimations(deltaMs); // update animations only when playing
        }

    }
    //-------------------------------------------------------------
    private void updateMainMenu(InputState input) {
        UI.MainMenuLayout layout = view.getMainMenuLayout();
        Point mousePosition = mouseHandler.getMousePosition();
        int selection = model.getMainMenuSelection();
        selection = selectionFromMouse(layout, mousePosition, selection);
        model.setHoveredRibbon(hoveredRibbonFromMouse(layout, mousePosition));

        if (input.menuPrevious()) {
            selection = (selection - 1 + MENU_ITEM_COUNT) % MENU_ITEM_COUNT;
        }
        if (input.menuNext()) {
            selection = (selection + 1) % MENU_ITEM_COUNT;
        }

        model.setMainMenuSelection(selection);

        boolean leftClicked = mouseHandler.consumeLeftClick();
        if (leftClicked && layout.newGameBounds().contains(mousePosition)) {
            startNewGame();
            return;
        }
        if (leftClicked && layout.continueBounds().contains(mousePosition)) {
            model.setMainMenuSelection(1);
            return;
        }
        if (leftClicked && layout.settingsBounds().contains(mousePosition)) {
            model.setMainMenuSelection(2);
            return;
        }
        if (leftClicked && layout.ribbonYellowBounds().contains(mousePosition)) {
            model.setActiveRibbon(0);
            return;
        }
        if (leftClicked && layout.ribbonRedBounds().contains(mousePosition)) {
            model.setActiveRibbon(1);
            return;
        }
        if (leftClicked && layout.ribbonBlueBounds().contains(mousePosition)) {
            model.setActiveRibbon(2);
            return;
        }

        if (input.menuConfirm() && selection == MENU_NEW_GAME_INDEX) {
            startNewGame();
        }
    }

    private int selectionFromMouse(UI.MainMenuLayout layout, Point mousePosition, int fallback) {
        if (contains(layout.newGameBounds(), mousePosition)) {
            return 0;
        }
        if (contains(layout.continueBounds(), mousePosition)) {
            return 1;
        }
        if (contains(layout.settingsBounds(), mousePosition)) {
            return 2;
        }
        return fallback;
    }

    private boolean contains(Rectangle bounds, Point mousePosition) {
        return bounds != null && mousePosition != null && bounds.contains(mousePosition);
    }

    private int hoveredRibbonFromMouse(UI.MainMenuLayout layout, Point mousePosition) {
        if (contains(layout.ribbonYellowBounds(), mousePosition)) {
            return 0;
        }
        if (contains(layout.ribbonRedBounds(), mousePosition)) {
            return 1;
        }
        if (contains(layout.ribbonBlueBounds(), mousePosition)) {
            return 2;
        }
        return -1;
    }

    private void startNewGame() {
        model.setMainMenuSelection(0);
        if (model.getGameState() == GameState.MENU) {
            model.setGameState(GameState.PLAYING);
        }
    }
    //-------------------------------------------------------------
    // CONTROLL GAME VIEW
    public void render() { // called by the game loop every frame to render the view
        if (model.getGameState() == GameState.PLAYING || model.getGameState() == GameState.MENU) {
            view.repaint();
        } else if (renderOnceOnPause) { // render once when paused to show the pause screen
            view.repaint();
            renderOnceOnPause = false; 

        }
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------
