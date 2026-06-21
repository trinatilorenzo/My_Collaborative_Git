package model;

import java.io.IOException;
import java.util.List;

import controller.InputState;
import main.CONFIG.GameConfig;
import main.CONFIG.enu.GameState;
import main.CONFIG.enu.PlayerColor;
import model.entity.Player;
import model.event.AudioEventType;

public interface IGameModel {

    // states
    GameState getGameState();
    boolean isDebugMode();
    void setDebugMode(boolean debug);
    void update(InputState input, double deltaMs);

    // game life cycle
    void initializeNewGame();
    void forcePlayingState();
    void restoreTransientState(GameConfig config);

    // states navigation
    void resumeFromPause();
    void returnToMenu();
    void toggleSetingsFormMenu();
    void toggleSetingsFormPause();
    void closeSettings();

    // settings
    void toggleMusic();
    void toggleSound();
    void setMaxResolution();
    void setMidResolution();
    void setMinResolution();
    void setPlayerColor(PlayerColor color);
    boolean isSoundEnabled();
    boolean isMusicEnabled();
    int getResolutionValue();
    PlayerColor getPlayerColor();
    List<AudioEventType> consumeAudioEvents();

    // audio
    void addAudioEvent(AudioEventType event);

    // config (for load)
    GameConfig getGameConfig();

    // getter for rendering
    GameMap getWorldMap();
    List<IRenderable> getAllRenderables();
    String getCurrentDialogue();
    String getCurrentMessage();
    Player getPlayer();
}