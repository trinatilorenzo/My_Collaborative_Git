package controller;

import main.CONFIG.GameConfig;
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
import java.io.IOException;


/**
 * ALL THE CONTROLLER STAFF HERE
 * input, game loop, system ...
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameController {

    private GameModel model;
    private final GameView view;
    private final KeyHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameLoop loop;

    private GameState lastKnownState;

    private ButtonValue.MainMenu  mainMenuSelection;
    private ButtonValue.Pause pauseMenuSelection;
    private ButtonValue.Settings settingsSelection;
    private ButtonValue.GameOver gameOverSelection;

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

        resetSelection();

        view.addKeyListener(keyHandler); // add key listener to the view to capture keyboard input
        view.addMouseListener(mouseHandler);
        view.addMouseMotionListener(mouseHandler);
        view.setFocusable(true); // ensure the view can receive keyboard focus
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void resetSelection(){
        this.mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
        this.pauseMenuSelection = UIConfig.PAUSE_DEFAULT_SELECTION;
        this.settingsSelection = UIConfig.SETTINGS_DEFAULT_SELECTION;
        this.gameOverSelection = UIConfig.GAME_OVER_DEFAULT_SELECTION;
    };
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
        resetSelection();
        MainMenuLayout layout = view.getMainMenuLayout();
        Point mouse = mouseHandler.getMousePosition();

        // Keyboard
        if (input.menuPrevious()) {
            mainMenuSelection = previousMainMenuItem(mainMenuSelection);
        }
        if (input.menuNext()) {
            mainMenuSelection = nextMainMenuItem(mainMenuSelection);
        }
        if (input.menuConfirm()) {
            performMainMenuAction(mainMenuSelection);
            return;
        }

        // Mouse
        ButtonValue.MainMenu mainMenuHover = mainMenuButtonFromPoint(layout, mouse);
        if (mainMenuHover != null) {
            mainMenuSelection = mainMenuHover;
            view.setMainMenuHover(mainMenuHover);
        }else {
            view.resetMainMenuHover();
        }

        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.MainMenu clicked = mainMenuButtonFromPoint(layout, click);

        if (clicked != null) {
            mainMenuSelection = clicked;
            performMainMenuAction(clicked);
        }
    }
    //-------------------------------------------------------------

    /**
     * HELPERS METHOD
     */
    //-------------------------------------------------------------
    private ButtonValue.MainMenu previousMainMenuItem(ButtonValue.MainMenu current) {
        ButtonValue.MainMenu[] items = ButtonValue.MainMenu.values();

        for (int i = 0; i < items.length; i++) {
            if (items[i] == current) {
                return items[(i - 1 + items.length) % items.length];
            }
        }
        return ButtonValue.MainMenu.NEW_GAME;
    }
    private ButtonValue.MainMenu nextMainMenuItem(ButtonValue.MainMenu current) {
        ButtonValue.MainMenu[] items = ButtonValue.MainMenu.values();
        for (int i = 0; i < items.length; i++) {
            if (items[i] == current) {
                return items[(i + 1) % items.length];
            }
        }
        return ButtonValue.MainMenu.NEW_GAME;
    }
    private ButtonValue.MainMenu mainMenuButtonFromPoint(MainMenuLayout layout, Point point) {
        if (contains(layout.newGameBounds(), point)) return ButtonValue.MainMenu.NEW_GAME;
        if (contains(layout.continueBounds(), point)) return ButtonValue.MainMenu.LOAD_GAME;
        if (contains(layout.settingsBounds(), point)) return ButtonValue.MainMenu.SETTINGS;
        if (contains(layout.toggleBlueBounds(), point)) return ButtonValue.MainMenu.TOGGLE_BLUE;
        if (contains(layout.toggleYellowBounds(), point)) return ButtonValue.MainMenu.TOGGLE_YELLOW;
        if (contains(layout.toggleRedBounds(), point)) return ButtonValue.MainMenu.TOGGLE_RED;
        if (contains(layout.togglePurpleBounds(), point)) return ButtonValue.MainMenu.TOGGLE_PURPLE;

        return null;
    }
    //-------------------------------------------------------------
    private void performMainMenuAction(ButtonValue.MainMenu selection) {
        if (selection == null) return;

        switch (selection) {
            case NEW_GAME:
                model.initializeNewGame();
                break;

            case LOAD_GAME:
                try {
                    GameConfig config = model.getGameConfig();
                    GameModel loaded = SaveManager.loadLatestGame();

                    loaded.restoreTransientState(config);
                    model.copyFrom(loaded);

                    model.forcePlayingState();          // vedi sotto
                    keyHandler.resetPauseToggle();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case SETTINGS:
                model.toggleSetingsFormMenu();
                break;

            case TOGGLE_BLUE:
                model.setPlayerColor(PlayerColor.BLUE);
                view.updatePlayerColor();
                break;

            case TOGGLE_YELLOW:
                model.setPlayerColor(PlayerColor.YELLOW);
                view.updatePlayerColor();
                break;

            case TOGGLE_RED:
                model.setPlayerColor(PlayerColor.RED);
                view.updatePlayerColor();
                break;

            case TOGGLE_PURPLE:
                model.setPlayerColor(PlayerColor.PURPLE);
                view.updatePlayerColor();
                break;
        }
    }
    //-------------------------------------------------------------
    //end helpers -------------------------------------------------------------



    /**
     * Control the pause menu
     */
    //-------------------------------------------------------------
    private void updatePauseMenu(InputState input) {
        resetSelection();
        PauseMenuLayout layout = view.getPauseMenuLayout();
        Point mouse = mouseHandler.getMousePosition();

        // Keyboard
        if (input.menuPrevious()) {
            pauseMenuSelection = previousPauseMenuItem(pauseMenuSelection);
        }
        if (input.menuNext()) {
            pauseMenuSelection = nextPauseMenuItem(pauseMenuSelection);
        }
        if (input.menuConfirm()) {
            performPauseMenuAction(pauseMenuSelection);
            return;
        }

        // Mouse
        ButtonValue.Pause pauseMenuHover = pauseButtonFromPoint(layout, mouse);
        if (pauseMenuHover != null) {
            pauseMenuSelection = pauseMenuHover;
            view.setPauseHover(pauseMenuHover);
        }else{
            view.resetPauseHover();
        }

        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.Pause clicked = pauseButtonFromPoint(layout, click);

        if (clicked != null) {
            pauseMenuSelection = clicked;
            performPauseMenuAction(clicked);
        }
    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — pause
     */
    //-------------------------------------------------------------
    private ButtonValue.Pause previousPauseMenuItem(ButtonValue.Pause current) {
        ButtonValue.Pause[] items = ButtonValue.Pause.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.Pause.RESUME;
    }
    private ButtonValue.Pause nextPauseMenuItem(ButtonValue.Pause current) {
        ButtonValue.Pause[] items = ButtonValue.Pause.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.Pause.RESUME;
    }
    private ButtonValue.Pause pauseButtonFromPoint(PauseMenuLayout layout, Point mouse) {
        if (contains(layout.resumeBounds(), mouse)) return ButtonValue.Pause.RESUME;
        if (contains(layout.settingsBounds(), mouse)) return ButtonValue.Pause.PAUSE_SETTINGS;
        if (contains(layout.saveBounds(), mouse)) return ButtonValue.Pause.SAVE;
        return null;
    }
    //-------------------------------------------------------------
    private void performPauseMenuAction(ButtonValue.Pause selection) {
        if (selection == null) return;

        switch (selection) {
            case RESUME:
                model.resumeFromPause();
                keyHandler.resetPauseToggle();
                break;

            case SAVE:
                try {
                    SaveManager.saveGame(model);
                    System.out.println("Partita salvata.");
                    model.returnToMenu();
                    keyHandler.resetPauseToggle();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;

            case PAUSE_SETTINGS:
                model.toggleSetingsFormPause();
                break;
        }
    }
    //-------------------------------------------------------------
    //end helpers -------------------------------------------------------------


    /**
     * Control the GameOver Menu
     */
    //-------------------------------------------------------------
    private void updateGameOver(InputState input) {
        resetSelection();
        GameOverLayout layout = view.getGameOverLayout();
        Point mouse = mouseHandler.getMousePosition();

        // Keyboard
        if (input.menuPrevious()) {
            gameOverSelection = previousGameOverItem(gameOverSelection);
        }
        if (input.menuNext()) {
            gameOverSelection = nextGameOverItem(gameOverSelection);
        }
        if (input.menuConfirm()) {
            performGameOverAction(gameOverSelection);
            return;
        }

        // Mouse
        ButtonValue.GameOver gameOverHover = gameOverButtonFromPoint(layout, mouse);
        if (gameOverHover != null) {
            gameOverSelection = gameOverHover;
            view.setGameOverHover(gameOverHover);
        }else{
            view.resetGameOverHover();
        }

        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.GameOver clicked = gameOverButtonFromPoint(layout, click);

        if (clicked != null) {
            gameOverSelection = clicked;
            performGameOverAction(clicked);
        }
    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — Settings
     */
    //-------------------------------------------------------------
    private ButtonValue.GameOver previousGameOverItem(ButtonValue.GameOver current) {
        ButtonValue.GameOver[] items = ButtonValue.GameOver.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.GameOver.RESTART;
    }
    private ButtonValue.GameOver nextGameOverItem(ButtonValue.GameOver current) {
        ButtonValue.GameOver[] items = ButtonValue.GameOver.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.GameOver.RESTART;
    }
    private ButtonValue.GameOver gameOverButtonFromPoint(GameOverLayout layout, Point mouse) {
        if (contains(layout.newGameBounds(), mouse)) return ButtonValue.GameOver.RESTART;
        return null;
    }
    //-------------------------------------------------------------
    private void performGameOverAction(ButtonValue.GameOver selection) {
        switch (selection) {
            case RESTART    -> {
                model.initializeNewGame();
            }
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

        // Keyboard
        if (input.menuPrevious()) {
            settingsSelection = previousSettingsItem(settingsSelection);
        }
        if (input.menuNext()) {
            settingsSelection = nextSettingsItem(settingsSelection);
        }
        if (input.menuConfirm()) {
            performSettingsAction(settingsSelection);
            return;
        }

        // Mouse
        ButtonValue.Settings settingsMenuHover = settingsButtonFromPoint(layout, mouse);
        if (settingsMenuHover != null) {
            settingsSelection = settingsMenuHover;
            view.setSettingsHover(settingsMenuHover);
        }else{
            view.resetSettingsHover();
        }

        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.Settings clicked = settingsButtonFromPoint(layout, click);

        if (clicked != null) {
            settingsSelection = clicked;
            performSettingsAction(clicked);
        }

    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — Settings
     */
    //-------------------------------------------------------------
    private ButtonValue.Settings previousSettingsItem(ButtonValue.Settings current) {
        ButtonValue.Settings[] items = ButtonValue.Settings.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.Settings.MUSIC;
    }
    private ButtonValue.Settings nextSettingsItem(ButtonValue.Settings current) {
        ButtonValue.Settings[] items = ButtonValue.Settings.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.Settings.MUSIC;
    }
    private ButtonValue.Settings settingsButtonFromPoint(SettingsLayout layout, Point mouse) {
        if (contains(layout.settingsIconBounds(), mouse)) return ButtonValue.Settings.SETTINGS_ICON;
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
    private void performSettingsAction(ButtonValue.Settings selection) {

        switch (selection) {
            case SETTINGS_ICON -> {
                model.closeSettings();
                System.out.println("settings");
            }
            case MUSIC    -> {
                model.toggleMusic();
                System.out.println("music");
            }
            case SOUND    -> {
                model.toggleSound();
                System.out.println("sound");
            }
            case RES_FULL -> {
                model.setMaxResolution();
                view.setResolution();
                System.out.println("full");
            }
            case RES_MID  -> {
                model.setMidResolution();
                view.setResolution();
                System.out.println("mid");
            }
            case RES_MIN  -> {
                model.setMinResolution();
                view.setResolution();
                System.out.println("small");
            }
            case FPS_60   -> {
                model.setLowFps();
                System.out.println("60");
            }
            case FPS_120  -> {
                model.setMediumFps();
                System.out.println("120");
            }
            case FPS_240  -> {
                model.setHighFps();
                System.out.println("240");
            }
        }
    }
    //-------------------------------------------------------------
    // end helpers -----------------------------------------------------



    /**
     * Checks if a rectangle contains a point
     * (used to check mouse hovers and clicks)
     */
    //-------------------------------------------------------------
    private boolean contains(Rectangle bounds, Point mousePosition) {
        return bounds != null && mousePosition != null && bounds.contains(mousePosition);
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
        view.repaint();
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------