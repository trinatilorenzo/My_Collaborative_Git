package controller;

import main.CONFIG.GameConfig;
import main.CONFIG.UIConfig;
import main.CONFIG.enu.ButtonValue;
import main.CONFIG.enu.PlayerColor;
import model.GameModel;
import model.event.AudioEventType;
import view.GameView;
import main.CONFIG.enu.GameState;
import view.UI.*;

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

    private ButtonValue.MainMenu mainMenuSelection;
    private ButtonValue.MainMenu mainMenuSelectionMouse;
    private ButtonValue.PauseMenu pauseMenuMenuSelection;
    private ButtonValue.PauseMenu pauseMenuMenuSelectionMouse;
    private ButtonValue.SettingsMenu settingsMenuSelection;
    private ButtonValue.SettingsMenu settingsMenuSelectionMouse;
    private ButtonValue.GameOverMenu gameOverMenuSelection;
    private ButtonValue.GameOverMenu gameOverMenuSelectionMouse;
    private ButtonValue.WinMenu winSelection;
    private ButtonValue.WinMenu winSelectionMouse;

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
        this.mainMenuSelectionMouse = UIConfig.MENU_DEFAULT_SELECTION;
        this.pauseMenuMenuSelection = UIConfig.PAUSE_MENU_DEFAULT_SELECTION;
        this.pauseMenuMenuSelectionMouse = UIConfig.PAUSE_MENU_DEFAULT_SELECTION;
        this.settingsMenuSelection = UIConfig.SETTINGS_MENU_DEFAULT_SELECTION;
        this.settingsMenuSelectionMouse = UIConfig.SETTINGS_MENU_DEFAULT_SELECTION;
        this.gameOverMenuSelection = UIConfig.GAME_OVER_DEFAULT_SELECTION;
        this.gameOverMenuSelectionMouse = UIConfig.GAME_OVER_DEFAULT_SELECTION;
        this.winSelection = UIConfig.WIN_DEFAULT_SELECTION;
        this.winSelectionMouse = UIConfig.WIN_DEFAULT_SELECTION;
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

            case PLAYING -> view.updateAnimations(deltaMs);

            case SETTINGS -> updateSettings(input);

            case WIN -> {
                updateWinScreen(input);
                view.updateAnimations(deltaMs);
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


        // Keyboard
        if (input.menuPrevious()) {
            mainMenuSelection = previousMainMenuItem(mainMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setMainMenuHover(mainMenuSelection);

        }
        if (input.menuNext()) {
            mainMenuSelection = nextMainMenuItem(mainMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setMainMenuHover(mainMenuSelection);
        }
        if (input.menuConfirm()) {
            performMainMenuAction(mainMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            view.setMainMenuSelected(mainMenuSelection);
            return;
        }

        // Mouse
        mainMenuMouseUpdate();
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
    private void mainMenuMouseUpdate(){

        MainMenuLayout layout = view.getMainMenuLayout();
        Point mouse = mouseHandler.getMousePosition();

        ButtonValue.MainMenu mainMenuHover = mainMenuButtonFromPoint(layout, mouse);

        if (mainMenuHover != mainMenuSelectionMouse) {
            mainMenuSelectionMouse = mainMenuHover;

            if (mainMenuHover != null) {
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                mainMenuSelection = mainMenuHover;
            } else {
                mainMenuSelection = null;
            }
        }

        if (mainMenuSelection != null) {
            view.setMainMenuHover(mainMenuSelection);
        } else {
            view.resetMainMenuHover();
        }


        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.MainMenu clicked = mainMenuButtonFromPoint(layout, click);

        if (clicked != null) {
            mainMenuSelectionMouse = clicked;
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            performMainMenuAction(clicked);
        }
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

                    model.forcePlayingState();          
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

        // Keyboard
        if (input.menuPrevious()) {
            pauseMenuMenuSelection = previousPauseMenuItem(pauseMenuMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setPauseHover(pauseMenuMenuSelection);
        }
        if (input.menuNext()) {
            pauseMenuMenuSelection = nextPauseMenuItem(pauseMenuMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setPauseHover(pauseMenuMenuSelection);
        }
        if (input.menuConfirm()) {
            performPauseMenuAction(pauseMenuMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            view.setPauseSelected(pauseMenuMenuSelection);
            return;
        }

        // Mouse
        pauseMenuMouseUpdate();
    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — pause
     */
    //-------------------------------------------------------------
    private ButtonValue.PauseMenu previousPauseMenuItem(ButtonValue.PauseMenu current) {
        ButtonValue.PauseMenu[] items = ButtonValue.PauseMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.PauseMenu.RESUME;
    }
    private ButtonValue.PauseMenu nextPauseMenuItem(ButtonValue.PauseMenu current) {
        ButtonValue.PauseMenu[] items = ButtonValue.PauseMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.PauseMenu.RESUME;
    }
    private ButtonValue.PauseMenu pauseButtonFromPoint(PauseMenuLayout layout, Point mouse) {
        if (contains(layout.resumeBounds(), mouse)) return ButtonValue.PauseMenu.RESUME;
        if (contains(layout.settingsBounds(), mouse)) return ButtonValue.PauseMenu.PAUSE_SETTINGS;
        if (contains(layout.saveBounds(), mouse)) return ButtonValue.PauseMenu.SAVE;
        return null;
    }
    private void pauseMenuMouseUpdate(){
        PauseMenuLayout layout = view.getPauseMenuLayout();
        Point mouse = mouseHandler.getMousePosition();

        ButtonValue.PauseMenu pauseMenuMenuHover = pauseButtonFromPoint(layout, mouse);

        if (pauseMenuMenuHover != pauseMenuMenuSelectionMouse) {
            pauseMenuMenuSelectionMouse = pauseMenuMenuHover;

            if (pauseMenuMenuHover != null) {
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                pauseMenuMenuSelection = pauseMenuMenuHover;
            } else {
                pauseMenuMenuSelection = null;
            }
        }

        if (pauseMenuMenuSelection != null) {
            view.setPauseHover(pauseMenuMenuSelection);
        } else {
            view.resetPauseHover();
        }


        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.PauseMenu clicked = pauseButtonFromPoint(layout, click);

        if (clicked != null) {
            pauseMenuMenuSelection = clicked;
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            performPauseMenuAction(clicked);
        }

    }
    //-------------------------------------------------------------
    private void performPauseMenuAction(ButtonValue.PauseMenu selection) {
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
     * Control the settings screen
     */
    //-------------------------------------------------------------
    private void updateSettings(InputState input) {

        // Keyboard
        if (input.menuPrevious()) {
            settingsMenuSelection = previousSettingsItem(settingsMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setSettingsHover(settingsMenuSelection);
        }
        if (input.menuNext()) {
            settingsMenuSelection = nextSettingsItem(settingsMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setSettingsHover(settingsMenuSelection);
        }
        if (input.menuConfirm()) {
            performSettingsAction(settingsMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setSettingsHover(settingsMenuSelection);
            return;
        }


        // Mouse
        settingsMouseUpdate();

    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — SettingsMenu
     */
    //-------------------------------------------------------------
    private ButtonValue.SettingsMenu previousSettingsItem(ButtonValue.SettingsMenu current) {
        ButtonValue.SettingsMenu[] items = ButtonValue.SettingsMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.SettingsMenu.MUSIC;
    }
    private ButtonValue.SettingsMenu nextSettingsItem(ButtonValue.SettingsMenu current) {
        ButtonValue.SettingsMenu[] items = ButtonValue.SettingsMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.SettingsMenu.MUSIC;
    }
    private ButtonValue.SettingsMenu settingsButtonFromPoint(SettingsLayout layout, Point mouse) {
        if (contains(layout.settingsIconBounds(), mouse)) return ButtonValue.SettingsMenu.SETTINGS_ICON;
        if (contains(layout.musicBounds(), mouse)) return ButtonValue.SettingsMenu.MUSIC;
        if (contains(layout.soundBounds(), mouse)) return ButtonValue.SettingsMenu.SOUND;
        if (contains(layout.resFullBounds(), mouse)) return ButtonValue.SettingsMenu.RES_FULL;
        if (contains(layout.resHalfBounds(), mouse)) return ButtonValue.SettingsMenu.RES_MID;
        if (contains(layout.resMinBounds(), mouse)) return ButtonValue.SettingsMenu.RES_MIN;
        if (contains(layout.quitBounds(), mouse)) return ButtonValue.SettingsMenu.QUIT;

        return null;
    }
    private void settingsMouseUpdate(){
        SettingsLayout layout = view.getSettingsLayout();
        Point mouse = mouseHandler.getMousePosition();

        ButtonValue.SettingsMenu settingsMenuMenuHover = settingsButtonFromPoint(layout, mouse);

        if (settingsMenuMenuHover != settingsMenuSelectionMouse) {
            settingsMenuSelectionMouse = settingsMenuMenuHover;

            if (settingsMenuMenuHover != null) {
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                settingsMenuSelection = settingsMenuMenuHover;
            } else {
                settingsMenuSelection = null;
            }
        }

        if (settingsMenuSelection != null) {
            view.setSettingsHover(settingsMenuSelection);
        } else {
            view.resetSettingsHover();
        }


        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.SettingsMenu clicked = settingsButtonFromPoint(layout, click);

        if (clicked != null) {
            settingsMenuSelection = clicked;
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            performSettingsAction(clicked);
        }
    }
    //-------------------------------------------------------------
    private void performSettingsAction(ButtonValue.SettingsMenu selection) {

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
            case QUIT ->{
                System.exit(0);
            }
        }
    }
    //-------------------------------------------------------------
    // end helpers -----------------------------------------------------



    /**
     * Control the GameOverMenu Menu
     */
    //-------------------------------------------------------------
    private void updateGameOver(InputState input) {

        // Keyboard
        if (input.menuPrevious()) {
            gameOverMenuSelection = previousGameOverItem(gameOverMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setGameOverHover(gameOverMenuSelection);
        }
        if (input.menuNext()) {
            gameOverMenuSelection = nextGameOverItem(gameOverMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setGameOverHover(gameOverMenuSelection);
        }
        if (input.menuConfirm()) {
            performGameOverAction(gameOverMenuSelection);
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            view.setGameOverSelected(gameOverMenuSelection);
            return;
        }


        // Mouse
        gameOverMouseUpdate();

    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — SettingsMenu
     */
    //-------------------------------------------------------------
    private ButtonValue.GameOverMenu previousGameOverItem(ButtonValue.GameOverMenu current) {
        ButtonValue.GameOverMenu[] items = ButtonValue.GameOverMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.GameOverMenu.HOME_OVER;
    }
    private ButtonValue.GameOverMenu nextGameOverItem(ButtonValue.GameOverMenu current) {
        ButtonValue.GameOverMenu[] items = ButtonValue.GameOverMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.GameOverMenu.HOME_OVER;
    }
    private ButtonValue.GameOverMenu gameOverButtonFromPoint(GameOverLayout layout, Point mouse) {
        if (contains(layout.homeButtonBounds(), mouse)) return ButtonValue.GameOverMenu.HOME_OVER;
        if (contains(layout.quitButtonBounds(), mouse)) return ButtonValue.GameOverMenu.QUIT_OVER;
        return null;
    }
    private void gameOverMouseUpdate(){
        GameOverLayout layout = view.getGameOverLayout();
        Point mouse = mouseHandler.getMousePosition();


        ButtonValue.GameOverMenu gameOverMenuHover = gameOverButtonFromPoint(layout, mouse);

        if (gameOverMenuHover != gameOverMenuSelectionMouse) {
            gameOverMenuSelectionMouse = gameOverMenuHover;

            if (gameOverMenuHover != null) {
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                gameOverMenuSelection = gameOverMenuHover;
            } else {
                gameOverMenuSelection = null;
            }
        }

        if (gameOverMenuSelection != null) {
            view.setGameOverHover(gameOverMenuSelection);
        } else {
            view.resetGameOverHover();
        }

        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.GameOverMenu clicked = gameOverButtonFromPoint(layout, click);

        if (clicked != null) {
            gameOverMenuSelection = clicked;
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            performGameOverAction(clicked);
        }
    }
    //-------------------------------------------------------------
    private void performGameOverAction(ButtonValue.GameOverMenu selection) {
        switch (selection) {
            case HOME_OVER    -> model.returnToMenu();
            case QUIT_OVER    -> System.exit(1);
        }
    }
    //-------------------------------------------------------------


    /**
     * Control the GameOverMenu Menu
     */
    //-------------------------------------------------------------
    private void updateWinScreen(InputState input) {

        // Keyboard
        if (input.menuPrevious()) {
            winSelection = previousWinItem(winSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setWinHover(winSelection);
        }
        if (input.menuNext()) {
            winSelection = nextWinItem(winSelection);
            model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            view.setWinHover(winSelection);
        }
        if (input.menuConfirm()) {
            performWinAction(winSelection);
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            view.setWinSelected(winSelection);
            return;
        }


        // Mouse
        winMouseUpdate();

    }
    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — SettingsMenu
     */
    //-------------------------------------------------------------
    private ButtonValue.WinMenu previousWinItem(ButtonValue.WinMenu current) {
        ButtonValue.WinMenu[] items = ButtonValue.WinMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return ButtonValue.WinMenu.HOME_WIN;
    }
    private ButtonValue.WinMenu nextWinItem(ButtonValue.WinMenu current) {
        ButtonValue.WinMenu[] items = ButtonValue.WinMenu.values();
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return ButtonValue.WinMenu.HOME_WIN;
    }
    private ButtonValue.WinMenu winButtonFromPoint(WinLayout layout, Point mouse) {
        if (contains(layout.homeButtonBounds(), mouse)) return ButtonValue.WinMenu.HOME_WIN;
        if (contains(layout.quitButtonBounds(), mouse)) return ButtonValue.WinMenu.QUIT_WIN;
        return null;
    }
    private void winMouseUpdate(){
        WinLayout layout = view.getWinLayout();
        Point mouse = mouseHandler.getMousePosition();


        ButtonValue.WinMenu winMenuHover = winButtonFromPoint(layout, mouse);

        if (winMenuHover !=  winSelectionMouse) {
            winSelectionMouse = winMenuHover;

            if (winMenuHover != null) {
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                winSelection = winMenuHover;
            } else {
                winSelection = null;
            }
        }

        if (winSelection != null) {
            view.setWinHover(winSelection);
        } else {
            view.resetWinHover();
        }

        Point click = mouseHandler.consumeLeftClick();
        ButtonValue.WinMenu clicked = winButtonFromPoint(layout, click);

        if (clicked != null) {
            winSelection = clicked;
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            performWinAction(clicked);
        }
    }
    //-------------------------------------------------------------
    private void performWinAction(ButtonValue.WinMenu selection) {
        switch (selection) {
            case HOME_WIN    -> model.returnToMenu();
            case QUIT_WIN    -> System.exit(1);
        }
    }
    //-------------------------------------------------------------



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