package controller;

import main.CONFIG.UIConfig;
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
    private int mainMenuSelection;

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
        this.mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        
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
        mainMenuSelection = selectionFromMouse(layout, mousePosition, mainMenuSelection);
        view.setHoveredRibbon(hoveredRibbonFromMouse(layout, mousePosition));

        if (input.menuPrevious()) {
            selectPreviousMainMenuItem();
        }
        if (input.menuNext()) {
            selectNextMainMenuItem();
        }
        view.setMainMenuSelection(mainMenuSelection);

        boolean leftClicked = mouseHandler.consumeLeftClick();
        if (leftClicked && contains(layout.newGameBounds(), mousePosition)) {
            selectMainMenuItem(0);
            view.setMainMenuSelection(mainMenuSelection);
            startNewGame();
            return;
        }
        if (leftClicked && contains(layout.continueBounds(), mousePosition)) {
            selectMainMenuItem(1);
            view.setMainMenuSelection(mainMenuSelection);
            return;
        }
        if (leftClicked && contains(layout.settingsBounds(), mousePosition)) {
            selectMainMenuItem(2);
            view.setMainMenuSelection(mainMenuSelection);
            return;
        }
        if (leftClicked && contains(layout.ribbonYellowBounds(), mousePosition)) {
            setActiveRibbon(0);
            return;
        }
        if (leftClicked && contains(layout.ribbonRedBounds(), mousePosition)) {
            setActiveRibbon(1);
            return;
        }
        if (leftClicked && contains(layout.ribbonBlueBounds(), mousePosition)) {
            setActiveRibbon(2);
            return;
        }

        if (input.menuConfirm()) {
            confirmMainMenuSelection();
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
        return UIConfig.MENU_NO_SELECTION;
    }

    private void selectMainMenuItem(int selection) {
        mainMenuSelection = Math.max(0, Math.min(UIConfig.MAIN_MENU_ITEM_COUNT - 1, selection));
    }

    private void selectPreviousMainMenuItem() {
        mainMenuSelection = (mainMenuSelection - 1 + UIConfig.MAIN_MENU_ITEM_COUNT) % UIConfig.MAIN_MENU_ITEM_COUNT;
    }

    private void selectNextMainMenuItem() {
        mainMenuSelection = (mainMenuSelection + 1) % UIConfig.MAIN_MENU_ITEM_COUNT;
    }

    private void confirmMainMenuSelection() {
        if (mainMenuSelection == 0) {
            startNewGame();
        }
    }

    private void setActiveRibbon(int activeRibbon) {
        view.setActiveRibbon(activeRibbon);
    }

    private void startNewGame() {
        model.initializeNewGame();
        mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        view.setMainMenuSelection(mainMenuSelection);
        view.setHoveredRibbon(UIConfig.MENU_NO_SELECTION);
        view.setActiveRibbon(UIConfig.MENU_NO_SELECTION);
        renderOnceOnPause = true;
    }
    //-------------------------------------------------------------
    private void updateGameOver(InputState input) {
        UI.GameOverLayout layout = view.getGameOverLayout();
        Point mousePosition = mouseHandler.getMousePosition();

        boolean hoveredGameOverButton = contains(layout.newGameBounds(), mousePosition);
        view.setHoveredGameOverButton(hoveredGameOverButton);

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
