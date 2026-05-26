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
    private final GameModel model;
    private final GameView view;
    private final KeyHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameLoop loop;
    private boolean renderOnceOnPause = true; // flag to control rendering when paused
    private GameState lastKnownState;
    private boolean attackLatch;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;

        this.keyHandler = new KeyHandler();
        this.mouseHandler = new MouseHandler();
        this.loop = new GameLoop(this);
        this.lastKnownState = model.getGameState();
        this.attackLatch = false;
        
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
        view.shutdownAudio();
    }
    //-------------------------------------------------------------

    /**
     * Update the model status and view rendering
     */
    public void update(double deltaMs) { //called by the game loop every frame with a fixed delta time

        InputState rawInput = keyHandler.getInputState();
        InputState input = adaptAttackInput(rawInput);

        if (model.getGameState() == GameState.MENU) {
            updateMainMenu(input);
            model.setDebugMode(input.debug());
            syncAudio();
            return;
        }
        if (model.getGameState() == GameState.GAME_OVER) {
            updateGameOver(input);
            model.setDebugMode(input.debug());
            syncAudio();
            return;
        }

        model.setDebugMode(input.debug());

        model.update(input, deltaMs);

        if (model.getGameState() == GameState.PLAYING) {
            view.updateAnimations(deltaMs); // update animations only when playing
            renderOnceOnPause = true;
        }

        syncAudio();

    }
    //-------------------------------------------------------------
    private InputState adaptAttackInput(InputState rawInput) {
        boolean attackPressedThisFrame = rawInput.attack() && !attackLatch;

        if (!rawInput.attack()) {
            attackLatch = false;
        } else if (attackPressedThisFrame) {
            attackLatch = true;
        }

        return new InputState(
                rawInput.up(),
                rawInput.down(),
                rawInput.left(),
                rawInput.right(),
                attackPressedThisFrame,
                rawInput.pause(),
                rawInput.debug(),
                rawInput.interact(),
                rawInput.menuPrevious(),
                rawInput.menuNext(),
                rawInput.menuConfirm()
        );
    }

    private void updateMainMenu(InputState input) {
        UI.MainMenuLayout layout = view.getMainMenuLayout();
        Point mousePosition = mouseHandler.getMousePosition();
        int selection = model.getMainMenuSelection();
        selection = selectionFromMouse(layout, mousePosition, selection);
        model.setHoveredRibbon(hoveredRibbonFromMouse(layout, mousePosition));
        model.selectMainMenuItem(selection);

        if (input.menuPrevious()) {
            model.selectPreviousMainMenuItem();
        }
        if (input.menuNext()) {
            model.selectNextMainMenuItem();
        }

        boolean leftClicked = mouseHandler.consumeLeftClick();
        if (leftClicked && layout.newGameBounds().contains(mousePosition)) {
            model.handleMainMenuButtonClick(0);
            return;
        }
        if (leftClicked && layout.continueBounds().contains(mousePosition)) {
            model.handleMainMenuButtonClick(1);
            return;
        }
        if (leftClicked && layout.settingsBounds().contains(mousePosition)) {
            model.handleMainMenuButtonClick(2);
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

        if (input.menuConfirm()) {
            model.handleMainMenuConfirm();
            return;
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
        model.requestNewGame();
        renderOnceOnPause = true;
    }
    //-------------------------------------------------------------
    private void updateGameOver(InputState input) {
        UI.GameOverLayout layout = view.getGameOverLayout();
        Point mousePosition = mouseHandler.getMousePosition();

        model.setHoveredGameOverButton(contains(layout.newGameBounds(), mousePosition));

        boolean leftClicked = mouseHandler.consumeLeftClick();
        if ((leftClicked && model.isHoveredGameOverButton()) || input.menuConfirm()) {
            startNewGame();
        }
    }

    private void syncAudio() {
        GameState currentState = model.getGameState();
        if (currentState != lastKnownState) {
            view.onGameStateChanged(currentState);
            lastKnownState = currentState;
        }
        view.processGameEvents();
    }

    //-------------------------------------------------------------
    // CONTROLL GAME VIEW
    public void render() { // called by the game loop every frame to render the view
        if (model.getGameState() == GameState.PLAYING
                || model.getGameState() == GameState.MENU
                || model.getGameState() == GameState.GAME_OVER) {
            view.repaint();
        } else if (renderOnceOnPause) { // render once when paused to show the pause screen
            view.repaint();
            renderOnceOnPause = false; 

        }
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------
