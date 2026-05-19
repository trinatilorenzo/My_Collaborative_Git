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
    private GameState lastKnownState;
    private int mainMenuSelection = 0;
    private int hoveredRibbon = -1;
    private int activeRibbon = -1;
    private boolean hoveredGameOverButton = false;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;

        this.keyHandler = new KeyHandler();
        this.mouseHandler = new MouseHandler();
        this.loop = new GameLoop(this);
        this.lastKnownState = model.getGameState();
        
        view.addKeyListener(keyHandler); // add key listener to the view to capture keyboard input
        view.addMouseListener(mouseHandler);
        view.addMouseMotionListener(mouseHandler);
        view.setFocusable(true); // ensure the view can receive keyboard focus
        publishMenuUiState();
        publishGameOverUiState();
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

        InputState input = keyHandler.getInputState();

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
    private void updateMainMenu(InputState input) {
        UI.MainMenuLayout layout = view.getMainMenuLayout();
        Point mousePosition = mouseHandler.getMousePosition();
        int selection = mainMenuSelection;
        selection = selectionFromMouse(layout, mousePosition, selection);
        hoveredRibbon = hoveredRibbonFromMouse(layout, mousePosition);

        if (input.menuPrevious()) {
            selection = (selection - 1 + MENU_ITEM_COUNT) % MENU_ITEM_COUNT;
        }
        if (input.menuNext()) {
            selection = (selection + 1) % MENU_ITEM_COUNT;
        }

        mainMenuSelection = selection;

        boolean leftClicked = mouseHandler.consumeLeftClick();
        if (leftClicked && layout.newGameBounds().contains(mousePosition)) {
            startNewGame();
            return;
        }
        if (leftClicked && layout.continueBounds().contains(mousePosition)) {
            mainMenuSelection = 1;
            publishMenuUiState();
            return;
        }
        if (leftClicked && layout.settingsBounds().contains(mousePosition)) {
            mainMenuSelection = 2;
            publishMenuUiState();
            return;
        }
        if (leftClicked && layout.ribbonYellowBounds().contains(mousePosition)) {
            activeRibbon = 0;
            publishMenuUiState();
            return;
        }
        if (leftClicked && layout.ribbonRedBounds().contains(mousePosition)) {
            activeRibbon = 1;
            publishMenuUiState();
            return;
        }
        if (leftClicked && layout.ribbonBlueBounds().contains(mousePosition)) {
            activeRibbon = 2;
            publishMenuUiState();
            return;
        }

        if (input.menuConfirm() && selection == MENU_NEW_GAME_INDEX) {
            startNewGame();
            return;
        }

        publishMenuUiState();
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
        mainMenuSelection = 0;
        hoveredRibbon = -1;
        activeRibbon = -1;
        hoveredGameOverButton = false;
        publishMenuUiState();
        publishGameOverUiState();
        model.initializeNewGame();
        renderOnceOnPause = true;
    }
    //-------------------------------------------------------------
    private void updateGameOver(InputState input) {
        UI.GameOverLayout layout = view.getGameOverLayout();
        Point mousePosition = mouseHandler.getMousePosition();

        hoveredGameOverButton = contains(layout.newGameBounds(), mousePosition);
        publishGameOverUiState();

        boolean leftClicked = mouseHandler.consumeLeftClick();
        if ((leftClicked && hoveredGameOverButton) || input.menuConfirm()) {
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

    private void publishMenuUiState() {
        view.setMainMenuSelection(mainMenuSelection);
        view.setHoveredRibbon(hoveredRibbon);
        view.setActiveRibbon(activeRibbon);
    }

    private void publishGameOverUiState() {
        view.setHoveredGameOverButton(hoveredGameOverButton);
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
