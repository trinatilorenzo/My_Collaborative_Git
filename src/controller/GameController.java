package controller;

import main.CONFIG.UIConfig;
import main.CONFIG.enu.ButtonValue;
import main.CONFIG.enu.PlayerColor;
import model.GameModel;
import view.GameView;
import main.CONFIG.enu.GameState;
import view.UI.GameOverLayout;
import view.UI.MainMenuLayout;
import view.UI.PauseMenuLayout;
import view.UI.SettingsLayout;

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

    private GameState lastKnownState;
    private PlayerColor selectedPlayerColor;

    private ButtonValue.MainMenu  mainMenuSelection;
    private ButtonValue.Pause pauseMenuSelection;
    private ButtonValue.Settings settingsSelection;

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
        this.pauseMenuSelection = UIConfig.PAUSE_DEFAULT_SELECTION;
        this.settingsSelection = UIConfig.SETTINGS_DEFAULT_SELECTION;

        view.addKeyListener(keyHandler); // add key listener to the view to capture keyboard input
        view.addMouseListener(mouseHandler);
        view.addMouseMotionListener(mouseHandler);
        view.setFocusable(true); // ensure the view can receive keyboard focus

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

        switch (currentState) {
            case MENU -> updateMainMenu(input);

            case GAME_OVER -> updateGameOver(input);

            case PAUSED -> updatePauseMenu(input);

            case PLAYING -> {
                view.updateAnimations(deltaMs);
            }

            case SETTINGS -> updateSettings(input);
        }

        syncAudio();

    }
    //-------------------------------------------------------------

    /**
     * Control the main menu
     */
    //-------------------------------------------------------------
    private void updateMainMenu(InputState input) {

        MainMenuLayout layout = view.getMainMenuLayout();
        Point mouse = mouseHandler.getMousePosition();

        // Keyboard
        if (input.menuPrevious()) {
            mainMenuSelection = previousMainMenuItem(mainMenuSelection);
        }
        if (input.menuNext()){
            mainMenuSelection = nextMainMenuItem(mainMenuSelection);
        }

        view.setMainMenuSelected(mainMenuSelection);
        view.setMainMenuHover(null);

        if (input.menuConfirm()) {
            confirmMainMenuSelection();
            return;
        }

        //Mouse
        view.setMainMenuHover(mainMenuHoverFromMouse(layout, mouse));
        ButtonValue.MainMenu mouseSelection = mainMenuSelectionFromMouse(layout, mouse);

        if (mouseSelection != null) {
            view.setMainMenuSelected(mouseSelection);
        }

        Point click = mouseHandler.consumeLeftClick();
        if (click != null) {

            if (contains(layout.ribbonBlueBounds(), click)) {
                view.setRibbonSelected(ButtonValue.MainMenu.TOGGLE_BLUE);
                selectedPlayerColor = PlayerColor.BLUE;
                return; }
            if (contains(layout.ribbonYellowBounds(), click)) {
                view.setRibbonSelected(ButtonValue.MainMenu.TOGGLE_YELLOW);
                selectedPlayerColor = PlayerColor.YELLOW;
                return; }
            if (contains(layout.ribbonRedBounds(), click)) {
                view.setRibbonSelected(ButtonValue.MainMenu.TOGGLE_RED);
                selectedPlayerColor = PlayerColor.RED;
                return; }
            if (contains(layout.ribbonPurpleBounds(), click)) {
                view.setRibbonSelected(ButtonValue.MainMenu.TOGGLE_PURPLE);
                selectedPlayerColor = PlayerColor.PURPLE;
                return; }
        }
        ButtonValue.MainMenu clickSelection = mainMenuSelectionFromMouse(layout, click);
        if (clickSelection != null) {
            mainMenuSelection = clickSelection;
            confirmMainMenuSelection();
        }

    }
    //-------------------------------------------------------------

    /**
     * HELPERS METHOD
     */
    //-------------------------------------------------------------
    private ButtonValue.MainMenu mainMenuSelectionFromMouse(MainMenuLayout layout, Point mouse) {
        if (contains(layout.newGameBounds(), mouse)) return ButtonValue.MainMenu.NEW_GAME;
        if (contains(layout.continueBounds(), mouse)) return ButtonValue.MainMenu.LOAD_GAME;
        if (contains(layout.settingsBounds(), mouse)) return ButtonValue.MainMenu.SETTINGS;
        return null;
    }
    //-------------------------------------------------------------
    private boolean contains(Rectangle bounds, Point mousePosition) {
        return bounds != null && mousePosition != null && bounds.contains(mousePosition);
    }
    //-------------------------------------------------------------
    private ButtonValue.MainMenu mainMenuHoverFromMouse(MainMenuLayout layout, Point mouse) {
        if (contains(layout.ribbonBlueBounds(), mouse)) return ButtonValue.MainMenu.TOGGLE_BLUE;
        if (contains(layout.ribbonYellowBounds(), mouse)) return ButtonValue.MainMenu.TOGGLE_YELLOW;
        if (contains(layout.ribbonRedBounds(), mouse)) return ButtonValue.MainMenu.TOGGLE_RED;
        if (contains(layout.ribbonPurpleBounds(), mouse)) return ButtonValue.MainMenu.TOGGLE_PURPLE;
        return null;
    }
    //-------------------------------------------------------------
    private ButtonValue.MainMenu previousMainMenuItem(ButtonValue.MainMenu current) {
        ButtonValue.MainMenu[] items = { ButtonValue.MainMenu.NEW_GAME, ButtonValue.MainMenu.LOAD_GAME, ButtonValue.MainMenu.SETTINGS };
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.MainMenu.NEW_GAME;
    }
    //-------------------------------------------------------------
    private ButtonValue.MainMenu nextMainMenuItem(ButtonValue.MainMenu current) {
        ButtonValue.MainMenu[] items = { ButtonValue.MainMenu.NEW_GAME, ButtonValue.MainMenu.LOAD_GAME, ButtonValue.MainMenu.SETTINGS };
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.MainMenu.NEW_GAME;
    }
    //-------------------------------------------------------------
    private void confirmMainMenuSelection() {
        if (mainMenuSelection == ButtonValue.MainMenu.NEW_GAME)  { startNewGame(); return; }
        if (mainMenuSelection == ButtonValue.MainMenu.LOAD_GAME)    { System.out.println("continue"); return; }
        if (mainMenuSelection == ButtonValue.MainMenu.SETTINGS)  { model.openSettings(); }
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
    }
    //-------------------------------------------------------------



    /**
     * Control the pause menu
     */
    //-------------------------------------------------------------
    private void updatePauseMenu(InputState input) {

        PauseMenuLayout layout = view.getPauseMenuLayout();
        Point mouse = mouseHandler.getMousePosition();

        // Navigazione tastiera
        if (input.menuPrevious()) pauseMenuSelection = previousPauseMenuItem(pauseMenuSelection);
        if (input.menuNext())     pauseMenuSelection = nextPauseMenuItem(pauseMenuSelection);
        view.setPauseSelected(pauseMenuSelection);
        view.setPauseHover(null);

        if (input.menuConfirm()) { confirmPauseMenuSelection(); return; }

        // Hover da mouse
        view.setPauseHover(pauseSelectionFromMouse(layout, mouse));

        // Selected da mouse: aggiorna solo se il cursore è sopra un bottone
        ButtonValue.Pause mouseSelection = pauseSelectionFromMouse(layout, mouse);
        if (mouseSelection != null) {
            view.setPauseSelected(mouseSelection);
        }

        // Click: usa la posizione esatta del click
        Point click = mouseHandler.consumeLeftClick();
        if (click != null) {
            ButtonValue.Pause clickSelection = pauseSelectionFromMouse(layout, click);
            if (clickSelection != null) {
                pauseMenuSelection = clickSelection;
                confirmPauseMenuSelection();
            }
        }
    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — pause
     */
    private ButtonValue.Pause pauseSelectionFromMouse(PauseMenuLayout layout, Point mouse) {
        if (contains(layout.resumeBounds(), mouse)) return ButtonValue.Pause.RESUME;
        if (contains(layout.settingsBounds(), mouse)) return ButtonValue.Pause.PAUSE_SETTINGS;
        if (contains(layout.saveBounds(), mouse)) return ButtonValue.Pause.SAVE;
        return null;
    }
    //-------------------------------------------------------------
    private ButtonValue.Pause previousPauseMenuItem(ButtonValue.Pause current) {
        ButtonValue.Pause[] items = ButtonValue.Pause.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.Pause.RESUME;
    }
    //-------------------------------------------------------------
    private ButtonValue.Pause nextPauseMenuItem(ButtonValue.Pause current) {
        ButtonValue.Pause[] items = ButtonValue.Pause.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.Pause.RESUME;
    }
    //-------------------------------------------------------------
    private void confirmPauseMenuSelection() {
        if (pauseMenuSelection == ButtonValue.Pause.RESUME)   { resumeFromPause(); return; }
        if (pauseMenuSelection == ButtonValue.Pause.PAUSE_SETTINGS) { System.out.println("settings"); return; }
        if (pauseMenuSelection == ButtonValue.Pause.SAVE)     { quitToMainMenu(); }
    }
    //-------------------------------------------------------------
    private void resumeFromPause() {
        model.resumeFromPause();
        keyHandler.resetPauseToggle();
        pauseMenuSelection = UIConfig.PAUSE_DEFAULT_SELECTION;
        view.setPauseHover(null);
        view.setPauseSelected(null);
    }
    //-------------------------------------------------------------
    private void quitToMainMenu() {
        model.returnToMenu();
        keyHandler.resetPauseToggle();
        pauseMenuSelection = UIConfig.PAUSE_DEFAULT_SELECTION;
        view.setPauseHover(null);
        view.setPauseSelected(null);
    }
    //-------------------------------------------------------------
    // end helpers ------------------------------------------------------


    /**
     * Control the GameOver Menu
     */
    //-------------------------------------------------------------
    private void updateGameOver(InputState input) {
        GameOverLayout layout = view.getGameOverLayout();
        Point mouse = mouseHandler.getMousePosition();

        // Hover da mouse
        view.setGameOverHover(contains(layout.newGameBounds(), mouse)
                ? ButtonValue.GameOver.RESTART : null);

        // Tastiera: RESTART è l'unico bottone, sempre selezionato
        view.setGameOverSelected(ButtonValue.GameOver.RESTART);

        Point click = mouseHandler.consumeLeftClick();
        if ((click != null && contains(layout.newGameBounds(), click)) || input.menuConfirm()) {
            startNewGame();
        }
    }
    //-------------------------------------------------------------



    /**
     * Control the settings screen
     */
    //-------------------------------------------------------------
    private void updateSettings(InputState input) {

        SettingsLayout layout = view.getSettingsLayout();
        Point mouse = mouseHandler.getMousePosition();

        if (input.menuPrevious()) settingsSelection = previousSettingsItem(settingsSelection);
        if (input.menuNext())     settingsSelection = nextSettingsItem(settingsSelection);
        view.setSettingsHover(settingsSelection);

        if (input.menuConfirm()) { confirmSettingsSelection(); return; }

        settingsSelection = settingsSelectionFromMouse(layout, mouse);
        view.setSettingsHover(settingsSelection);

        Point click = mouseHandler.consumeLeftClick();
        if (click != null) {
            if (!contains(layout.settingsBounds(), click)) { /* model.closeSettings(); */ return; }
            ButtonValue.Settings clickSelection = settingsSelectionFromMouse(layout, click);
            if (clickSelection != null) {
                settingsSelection = clickSelection;
                confirmSettingsSelection();
            }
        }

    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — Settings
     */
    //-------------------------------------------------------------
    private ButtonValue.Settings settingsSelectionFromMouse(SettingsLayout layout, Point mouse) {
        if (contains(layout.musicBounds(), mouse)) return ButtonValue.Settings.MUSIC;
        if (contains(layout.soundBounds(), mouse)) return ButtonValue.Settings.SOUND;
        if (contains(layout.resFullBounds(), mouse)) return ButtonValue.Settings.RES_FULL;
        if (contains(layout.resHalfBounds(), mouse)) return ButtonValue.Settings.RES_MID;
        if (contains(layout.resMinBounds(), mouse)) return ButtonValue.Settings.RES_MIN;
        if (contains(layout.fpsBounds1(), mouse)) return ButtonValue.Settings.FPS_60;
        if (contains(layout.fpsBounds2(), mouse)) return ButtonValue.Settings.FPS_120;
        if (contains(layout.fpsBounds3(), mouse)) return ButtonValue.Settings.FPS_240;
        return null;
    }
    //-------------------------------------------------------------
    private ButtonValue.Settings previousSettingsItem(ButtonValue.Settings current) {
        ButtonValue.Settings[] items = ButtonValue.Settings.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.Settings.MUSIC;
    }
    //-------------------------------------------------------------
    private ButtonValue.Settings nextSettingsItem(ButtonValue.Settings current) {
        ButtonValue.Settings[] items = ButtonValue.Settings.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.Settings.MUSIC;
    }
    //-------------------------------------------------------------
    private void confirmSettingsSelection() {

        switch (settingsSelection) {
            case MUSIC    -> {
                view.setSettingsMusicSelected(ButtonValue.Settings.MUSIC);
                System.out.println("music");
            }
            case SOUND    -> {
                view.setSettingsSoundSelected(ButtonValue.Settings.SOUND);
                System.out.println("sound");
            }
            case RES_FULL -> {
                view.setSettingsSelected(ButtonValue.Settings.RES_FULL);
                System.out.println("full");
            }
            case RES_MID  -> {
                view.setSettingsSelected(ButtonValue.Settings.RES_MID);
                System.out.println("mid");
            }
            case RES_MIN  -> {
                view.setSettingsSelected(ButtonValue.Settings.RES_MIN);
                System.out.println("small");
            }
            case FPS_60   -> {
                view.setSettingsSelected(ButtonValue.Settings.FPS_60);
                System.out.println("60");
            }
            case FPS_120  -> {
                view.setSettingsSelected(ButtonValue.Settings.FPS_120);
                System.out.println("120");
            }
            case FPS_240  -> {
                view.setSettingsSelected(ButtonValue.Settings.FPS_240);
                System.out.println("240");
            }
        }
    }
    //-------------------------------------------------------------
    // end helpers -----------------------------------------------------

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
        view.repaint();
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------