package controller;

import main.CONFIG.UIConfig;
import main.CONFIG.enu.PlayerColor;
import model.GameModel;
import view.GameView;
import main.CONFIG.enu.GameState;
import view.UI.GameOverLayout;
import view.UI.MainMenuLayout;
import view.UI.PauseMenuLayout;

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

    private boolean renderOnceOnPause; // flag to control rendering when paused

    private GameState lastKnownState;
    private int mainMenuSelection;
    private int pauseMenuSelection;
    private PlayerColor selectedPlayerColor;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameController(GameModel model, GameView view) {
        this.model = model;
        this.view = view;

        this.keyHandler = new KeyHandler();
        this.mouseHandler = new MouseHandler();
        this.loop = new GameLoop(this);
        this.lastKnownState = model.getGameState();
        this.mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        this.pauseMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;

        view.addKeyListener(keyHandler); // add key listener to the view to capture keyboard input
        view.addMouseListener(mouseHandler);
        view.addMouseMotionListener(mouseHandler);
        view.setFocusable(true); // ensure the view can receive keyboard focus

        this.renderOnceOnPause = true;
        this.selectedPlayerColor = main.CONFIG.EntityConfig.DEFAULT_COLOR;
    }
    //-------------------------------------------------------------

    /**
     * START the game-loop thread
     */
    //------------------------------------------------------------
    public void startGame() {
        loop.start();
    }
    //-------------------------------------------------------------
    /**
     *  STOP the game-loop thread
     */
    //------------------------------------------------------------
    public void stopGame(){
        loop.stopGameLoop();
        view.shutdownAudio();
    }
    //-------------------------------------------------------------

    /**
     * Update the model status and view rendering
     * called by the game loop every frame with a fixed delta time
     */
    //------------------------------------------------------------
    public void update(double deltaMs) {

        InputState input = keyHandler.getInputState();
        GameState currentState = model.getGameState();

        model.setDebugMode(input.debug());
        model.update(input, deltaMs);

        if (currentState == GameState.PAUSED && lastKnownState != GameState.PAUSED) {
            pauseMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
            view.setPauseMenuSelection(pauseMenuSelection);
        }

        switch (currentState) {
            case MENU -> updateMainMenu(input);

            case GAME_OVER -> updateGameOver(input);

            case PAUSED -> updatePauseMenu(input);

            case PLAYING -> {
                view.updateAnimations(deltaMs);
                renderOnceOnPause = true;
            }
        }

        syncAudio();

    }
    //-------------------------------------------------------------

    /**
     * Control the main menu
     */
    //-------------------------------------------------------------
    private void updateMainMenu(InputState input) {

        //to control the main menu, the GameController needs to know
        // the mouse position relative to the layout of the main menu

        MainMenuLayout layout = view.getMainMenuLayout();
        Point mousePosition = mouseHandler.getMousePosition();

        // selection with keyboard
        if (input.menuPrevious()) {
            selectPreviousMainMenuItem();
        }
        if (input.menuNext()) {
            selectNextMainMenuItem();
        }
        view.setMainMenuSelection(mainMenuSelection);
        if (input.menuConfirm()) {
            confirmMainMenuSelection();
            return;
        }

        //selection with mouse
        mainMenuSelection = selectionFromMouse(layout, mousePosition);
        view.setHoveredRibbon(hoveredRibbonFromMouse(layout, mousePosition));

        if(mouseHandler.consumeLeftClick()){
            if (contains(layout.ribbonBlueBounds(), mousePosition)) {
                setActiveRibbon(0);
                selectedPlayerColor = PlayerColor.BLUE;
                return;
            }
            if (contains(layout.ribbonYellowBounds(), mousePosition)) {
                setActiveRibbon(1);
                selectedPlayerColor = PlayerColor.YELLOW;
                return;
            }
            if (contains(layout.ribbonRedBounds(), mousePosition)) {
                setActiveRibbon(2);
                selectedPlayerColor = PlayerColor.RED;
                return;
            }
            if (contains(layout.ribbonPurpleBounds(), mousePosition)) {
                setActiveRibbon(3);
                selectedPlayerColor = PlayerColor.PURPLE;
                return;
            }
            confirmMainMenuSelection();
            return;
        }

    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD
     */
    //-------------------------------------------------------------
    private int selectionFromMouse(MainMenuLayout layout, Point mousePosition) {
        if (contains(layout.newGameBounds(), mousePosition)) {
            return 0;
        }
        if (contains(layout.continueBounds(), mousePosition)) {
            return 1;
        }
        if (contains(layout.settingsBounds(), mousePosition)) {
            return 2;
        }
        return UIConfig.MENU_NO_SELECTION;
    }
    //-------------------------------------------------------------
    private boolean contains(Rectangle bounds, Point mousePosition) {
        return bounds != null && mousePosition != null && bounds.contains(mousePosition);
    }
    //-------------------------------------------------------------
    private int hoveredRibbonFromMouse(MainMenuLayout layout, Point mousePosition) {
        if (contains(layout.ribbonBlueBounds(), mousePosition)) {
            return 0;
        }
        if (contains(layout.ribbonYellowBounds(), mousePosition)) {
            return 1;
        }
        if (contains(layout.ribbonRedBounds(), mousePosition)) {
            return 2;
        }
        if (contains(layout.ribbonPurpleBounds(), mousePosition)) {
            return 3;
        }
        return UIConfig.MENU_NO_SELECTION;
    }
    //-------------------------------------------------------------
    private void selectPreviousMainMenuItem() {
        mainMenuSelection = (mainMenuSelection - 1 + UIConfig.MAIN_MENU_ITEM_COUNT) % UIConfig.MAIN_MENU_ITEM_COUNT;
    }
    //-------------------------------------------------------------
    private void selectNextMainMenuItem() {
        mainMenuSelection = (mainMenuSelection + 1) % UIConfig.MAIN_MENU_ITEM_COUNT;
    }
    //-------------------------------------------------------------
    private void confirmMainMenuSelection() {

        switch (mainMenuSelection){
            case 0 -> { startNewGame();}
            case 1 -> {System.out.println("continue");}
            case 2 -> {System.out.println("settings");}
        }

    }
    //-------------------------------------------------------------
    private void setActiveRibbon(int activeRibbon) {
        view.setActiveRibbon(activeRibbon);
    }
    //-------------------------------------------------------------
    //end helpers -------------------------------------------------------------

    /**
     * Initialize a new game
     */
    //-------------------------------------------------------------
    private void startNewGame() {
        model.initializeNewGame();
        view.setPlayerRenderColor(selectedPlayerColor);
        mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        view.setMainMenuSelection(mainMenuSelection);
        view.setHoveredRibbon(UIConfig.MENU_NO_SELECTION);
        view.setActiveRibbon(UIConfig.MENU_NO_SELECTION);
        renderOnceOnPause = true;
    }
    //-------------------------------------------------------------

    /**
     * Control the pause menu
     */
    //-------------------------------------------------------------
    private void updatePauseMenu(InputState input) {

        PauseMenuLayout layout = view.getPauseMenuLayout();
        Point mousePosition = mouseHandler.getMousePosition();

        if (input.menuPrevious()) {
            selectPreviousPauseMenuItem();
        }
        if (input.menuNext()) {
            selectNextPauseMenuItem();
        }
        view.setPauseMenuSelection(pauseMenuSelection);

        if (input.menuConfirm()) {
            confirmPauseMenuSelection();
            return;
        }

        pauseMenuSelection = pauseSelectionFromMouse(layout, mousePosition);
        view.setPauseMenuSelection(pauseMenuSelection);

        if (mouseHandler.consumeLeftClick()) {
            confirmPauseMenuSelection();
        }
    }
    //-------------------------------------------------------------

    private int pauseSelectionFromMouse(PauseMenuLayout layout, Point mousePosition) {
        if (contains(layout.resumeBounds(), mousePosition)) {
            return 0;
        }
        if (contains(layout.settingsBounds(), mousePosition)) {
            return 1;
        }
        if (contains(layout.saveBounds(), mousePosition)) {
            return 2;
        }
        return UIConfig.MENU_NO_SELECTION;
    }

    private void selectPreviousPauseMenuItem() {
        pauseMenuSelection = (pauseMenuSelection - 1 + UIConfig.PAUSE_MENU_ITEM_COUNT) % UIConfig.PAUSE_MENU_ITEM_COUNT;
    }

    private void selectNextPauseMenuItem() {
        pauseMenuSelection = (pauseMenuSelection + 1) % UIConfig.PAUSE_MENU_ITEM_COUNT;
    }

    private void confirmPauseMenuSelection() {
        switch (pauseMenuSelection) {
            case 0 -> resumeFromPause();
            case 1 -> System.out.println("settings");
            case 2 -> quitToMainMenu();
        }
    }

    private void resumeFromPause() {
        model.resumeFromPause();
        keyHandler.resetPauseToggle();
        pauseMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        view.setPauseMenuSelection(pauseMenuSelection);
        renderOnceOnPause = true;
    }

    private void quitToMainMenu() {
        model.returnToMenu();
        keyHandler.resetPauseToggle();
        pauseMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        view.setPauseMenuSelection(pauseMenuSelection);
        mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        view.setMainMenuSelection(mainMenuSelection);
        renderOnceOnPause = true;
    }

    /**
     * Control the GameOver Menu
     */
    //-------------------------------------------------------------
    private void updateGameOver(InputState input) {
        GameOverLayout layout = view.getGameOverLayout();
        Point mousePosition = mouseHandler.getMousePosition();

        boolean hoveredGameOverButton = contains(layout.newGameBounds(), mousePosition);
        view.setHoveredGameOverButton(hoveredGameOverButton);

        boolean leftClicked = mouseHandler.consumeLeftClick();
        if ((leftClicked && hoveredGameOverButton) || input.menuConfirm()) {
            startNewGame();
        }
    }
    //-------------------------------------------------------------

    /**
     * Sync the audio with the game state
     */
    //-------------------------------------------------------------
    private void syncAudio() {
        GameState currentState = model.getGameState();
        if (currentState != lastKnownState) {
            view.onGameStateChanged(currentState);
            lastKnownState = currentState;
        }
        view.processGameEvents();
    }
    //-------------------------------------------------------------

    /**
     * Control the view rendering
     * called by the game loop every frame to render the view
     */
    //-------------------------------------------------------------
    public void render() {
        if (model.getGameState() == GameState.PLAYING
                || model.getGameState() == GameState.MENU
                || model.getGameState() == GameState.GAME_OVER
                || model.getGameState() == GameState.PAUSED) {
            view.repaint();
        } else if (renderOnceOnPause) { // render once when paused to show the pause screen
            view.repaint();
            renderOnceOnPause = false;

        }
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------
