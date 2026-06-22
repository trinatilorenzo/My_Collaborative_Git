package tinyswordsisland.controller;

import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.UIConfig;
import tinyswordsisland.config.enu.ButtonValue;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.model.GameMap;
import tinyswordsisland.model.IGameModel;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.view.IGameView;
import tinyswordsisland.config.enu.GameState;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;


/**
 * ALL THE CONTROLLER STAFF HERE
 * input, game loop, system ...
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameController implements IController{

    private IGameModel model;
    private IGameView view;
    private final KeyHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameLoop loop;

    private GameState lastKnownState;

    private Enum<?> currentKeyboardSelection;
    private Enum<?> currentMouseSelection;

    private boolean keyboardNavigationActive = false;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameController(IGameModel model) {
        this.model = model;

        this.keyHandler = new KeyHandler();
        this.mouseHandler = new MouseHandler();
        this.loop = new GameLoop(this);
        this.lastKnownState = model.getGameState();

        resetSelection();
    }
    //-------------------------------------------------------------
    public void setView(IGameView view) {
        this.view = view;
        view.addKeyListener(keyHandler);
        view.addMouseListener(mouseHandler);
        view.addMouseMotionListener(mouseHandler);
        view.setFocusable(true);
    }
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

        model.setDebugMode(input.debug());
        model.update(input, deltaMs);

        GameState state = model.getGameState();

        if (state == GameState.PLAYING || state == GameState.WIN) {
            view.updateAnimations(deltaMs);
        }

        if (state != GameState.PLAYING) {

            if (input.menuPrevious()) {
                keyboardNavigationActive = true;
                currentKeyboardSelection = previousItem(state, currentKeyboardSelection);
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                view.applyMenuState(state, currentKeyboardSelection, null);

            } else if (input.menuNext()) {
                keyboardNavigationActive = true;
                currentKeyboardSelection = nextItem(state, currentKeyboardSelection);
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
                view.applyMenuState(state, currentKeyboardSelection, null);

            } else if (input.menuConfirm()) {
                keyboardNavigationActive = true;
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
            case MENU      -> ButtonValue.MainMenu.NEW_GAME;
            case PAUSED    -> ButtonValue.PauseMenu.RESUME;
            case SETTINGS  -> ButtonValue.SettingsMenu.SETTINGS_ICON;
            case GAME_OVER -> ButtonValue.GameOverMenu.HOME_OVER;
            case WIN       -> ButtonValue.WinMenu.HOME_WIN;
            default        -> null;
        };
    }

    //-------------------------------------------------------------

    private void updateMenuMouse(GameState screen) {
        Point mouse = mouseHandler.getMousePosition();
        Enum<?> hovered = view.getButtonAtPoint(screen, mouse);

        if (hovered != currentMouseSelection) {
            currentMouseSelection = hovered;

            if (hovered != null) {
                keyboardNavigationActive = false;
                currentKeyboardSelection = hovered;
                model.addAudioEvent(AudioEventType.BUTTON_HOVER);
            }
        }

        Point click = mouseHandler.consumeLeftClick();
        if (click != null) {
            Enum<?> clicked = view.getButtonAtPoint(screen, click);
            if (clicked != null) {
                keyboardNavigationActive = false;
                currentMouseSelection = clicked;
                currentKeyboardSelection = clicked;
                model.addAudioEvent(AudioEventType.BUTTON_CLICKED);
                performAction(screen, clicked);
                return;
            }
        }

        Enum<?> visualSelection;
        if (keyboardNavigationActive) {
            visualSelection = currentKeyboardSelection;
        } else {
            visualSelection = currentMouseSelection; // può essere null ed è giusto così
        }

        view.applyMenuState(screen, visualSelection, null);
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
                    IGameModel loaded = SaveManager.loadLatestGame();
                    loaded.restoreTransientState(model.getGameConfig());
                    loaded.forcePlayingState();
                    this.model = loaded;  // sostituisci il model
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
        view.processGameEvents(model.consumeAudioEvents());
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

    // GETTER
    @Override public GameState getGameState()                  { return model.getGameState(); }
    @Override public boolean isDebugMode()                     { return model.isDebugMode(); }
    @Override public int getPlayerWorldX()                     { return model.getPlayer().getWorldX(); }
    @Override public int getPlayerWorldY()                     { return model.getPlayer().getWorldY(); }
    @Override public int getPlayerCurrentLayer()               { return model.getPlayer().getCurrentLayer(); }
    @Override public int getPlayerLife()                       { return model.getPlayer().getLife(); }
    @Override public int getPlayerMaxLife()                    { return model.getPlayer().getMaxLife(); }
    @Override public boolean playerHasShield()                 { return model.getPlayer().hasShield(); }
    @Override public double getPlayerShieldTimerMs()           { return model.getPlayer().getShieldTimerMs(); }
    @Override public Rectangle getPlayerSolidArea()            { return model.getPlayer().getSolidArea(); }
    @Override public GameMap getWorldMap()                     { return model.getWorldMap(); }
    @Override public List<IRenderable> getAllRenderables()     { return model.getAllRenderables(); }
    @Override public String getCurrentDialogue()               { return model.getCurrentDialogue(); }
    @Override public String getCurrentMessage()                { return model.getCurrentMessage(); }
    @Override public boolean isSoundEnabled()                  { return model.isSoundEnabled(); }
    @Override public boolean isMusicEnabled()                  { return model.isMusicEnabled(); }
    @Override public int getResolutionValue()                  { return model.getResolutionValue(); }
    @Override public PlayerColor getPlayerColor()              { return model.getPlayerColor(); }
    @Override public List<AudioEventType> consumeAudioEvents() { return model.consumeAudioEvents(); }
}

//-------------------------------------------------------------------------------------------------------------------          case SETTINGS  -> performSettingsAction((ButtonValue.SettingsMenu) selection);
