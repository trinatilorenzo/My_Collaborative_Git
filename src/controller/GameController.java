package controller;

import main.CONFIG.GameConfig;
import main.CONFIG.UIConfig;
import main.CONFIG.enu.ButtonValue;
import main.CONFIG.enu.PlayerColor;
import model.GameModel;
import model.event.AudioEventType;
import view.IGameView;
import main.CONFIG.enu.GameState;

import java.awt.Point;
import java.io.IOException;


/**
 * ALL THE CONTROLLER STAFF HERE
 * input, game loop, system ...
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameController {

    private GameModel model;
    private final IGameView view;
    private final KeyHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameLoop loop;

    private GameState lastKnownState;

    private Enum<?> currentKeyboardSelection;
    private Enum<?> currentMouseSelection;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameController(GameModel model, IGameView view) {
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
        currentKeyboardSelection = UIConfig.MENU_DEFAULT_SELECTION;
        currentMouseSelection    = UIConfig.MENU_DEFAULT_SELECTION;  
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
        GameState state = model.getGameState();

        model.setDebugMode(input.debug());
        model.update(input, deltaMs);

        if (state == GameState.PLAYING || state == GameState.WIN) {
            view.updateAnimations(deltaMs);
        }
        if (state != GameState.PLAYING) {

            if (input.menuPrevious()) {
                currentKeyboardSelection = previousItem(state, currentKeyboardSelection);
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                view.applyMenuState(state, currentKeyboardSelection, null);
            } else if (input.menuNext()) {
                currentKeyboardSelection = nextItem(state, currentKeyboardSelection);
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                view.applyMenuState(state, currentKeyboardSelection, null);
            } else if (input.menuConfirm()) {
                model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
                view.applyMenuState(state, currentKeyboardSelection, null);
                performAction(state, currentKeyboardSelection);
            } else {
                updateMenuMouse(state);
            } 
        }

        syncAudio();

    }
    //-------------------------------------------------------------
    private Enum<?> previousItem(GameState screen, Enum<?> current) {
        Enum<?>[] items = itemsForScreen(screen);
        if (items == null || current == null) return defaultForScreen(screen);
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i - 1 + items.length) % items.length];
        return defaultForScreen(screen);
    }

    private Enum<?> nextItem(GameState screen, Enum<?> current) {
        Enum<?>[] items = itemsForScreen(screen);
        if (items == null || current == null) return defaultForScreen(screen);
        for (int i = 0; i < items.length; i++)
            if (items[i] == current) return items[(i + 1) % items.length];
        return defaultForScreen(screen);
    }

    private Enum<?>[] itemsForScreen(GameState screen) {
        return switch (screen) {
            case MENU      -> ButtonValue.MainMenu.values();
            case PAUSED    -> ButtonValue.PauseMenu.values();
            case SETTINGS  -> ButtonValue.SettingsMenu.values();
            case GAME_OVER -> ButtonValue.GameOverMenu.values();
            case WIN       -> ButtonValue.WinMenu.values();
            default        -> null;
        };
    }

    private Enum<?> defaultForScreen(GameState screen) {
        return switch (screen) {
            case MENU      -> UIConfig.MENU_DEFAULT_SELECTION;
            case PAUSED    -> UIConfig.PAUSE_MENU_DEFAULT_SELECTION;
            case SETTINGS  -> UIConfig.SETTINGS_MENU_DEFAULT_SELECTION;
            case GAME_OVER -> UIConfig.GAME_OVER_DEFAULT_SELECTION;
            case WIN       -> UIConfig.WIN_DEFAULT_SELECTION;
            default        -> null;
        };
    }
    
    //-------------------------------------------------------------
    
    private void updateMenuMouse(GameState screen) {
        Point mouse = mouseHandler.getMousePosition();
        Enum<?> hovered = view.getButtonAtPoint(screen, mouse);

        // update hover only if changed
        Enum<?> previousMouse = currentMouseSelection;
        if (hovered != previousMouse) {
            currentMouseSelection = hovered;
            if (hovered != null) {
                currentKeyboardSelection = hovered;
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            }
        } 
     
        view.applyMenuState(screen, currentKeyboardSelection, null);

        // click
        Point click = mouseHandler.consumeLeftClick();
        Enum<?> clicked = view.getButtonAtPoint(screen, click);
        if (clicked != null) {
            currentMouseSelection = clicked;
            currentKeyboardSelection = clicked;
            model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
            performAction(screen, clicked);
        }
    }

    private void performAction(GameState screen, Enum<?> selection) {
        if (selection == null) return;
        switch (screen) {
            case MENU      -> performMainMenuAction((ButtonValue.MainMenu) selection);
            case PAUSED    -> performPauseMenuAction((ButtonValue.PauseMenu) selection);
            case SETTINGS  -> performSettingsAction((ButtonValue.SettingsMenu) selection);
            case GAME_OVER -> performGameOverAction((ButtonValue.GameOverMenu) selection);
            case WIN       -> performWinAction((ButtonValue.WinMenu) selection);
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


    //-------------------------------------------------------------
    /**
     * HELPERS METHOD — pause
     */
    //-------------------------------------------------------------
    
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


    //-------------------------------------------------------------
    private void performGameOverAction(ButtonValue.GameOverMenu selection) {
        switch (selection) {
            case HOME_OVER    -> model.returnToMenu();
            case QUIT_OVER    -> System.exit(1);
        }
    }
    //-------------------------------------------------------------
 
    //-------------------------------------------------------------
    private void performWinAction(ButtonValue.WinMenu selection) {
        switch (selection) {
            case HOME_WIN    -> model.returnToMenu();
            case QUIT_WIN    -> System.exit(1);
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
            resetSelection();
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
        view.render();
    }
    //-------------------------------------------------------------


}

//-------------------------------------------------------------------------------------------------------------------