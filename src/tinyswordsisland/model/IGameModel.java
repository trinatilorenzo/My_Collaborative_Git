package tinyswordsisland.model;

import java.util.List;

import tinyswordsisland.input.InputState;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.enu.GameState;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.model.entity.Player;
import tinyswordsisland.model.event.AudioEventType;

public interface IGameModel {

    GameState getGameState();
    boolean isDebugMode();
    void setDebugMode(boolean debug);
    void update(InputState input, double deltaMs);

    void initializeNewGame();
    void forcePlayingState();
    void restoreTransientState(GameConfig config);
    void beforeSave();

    void resumeFromPause();
    void returnToMenu();
    void toggleSettingsFromMenu();
    void toggleSettingsFromPause();
    void closeSettings();

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

    void addAudioEvent(AudioEventType event);

    GameConfig getGameConfig();

    GameMap getWorldMap();
    List<IRenderable> getAllRenderables();
    String getCurrentDialogue();
    String getCurrentMessage();
    Player getPlayer();
}
