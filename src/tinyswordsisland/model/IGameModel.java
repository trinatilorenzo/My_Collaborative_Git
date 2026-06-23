package tinyswordsisland.model;

import java.util.List;

import tinyswordsisland.controller.InputState;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.enu.GameState;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.model.entity.Player;
import tinyswordsisland.model.event.IGameListener;

public interface IGameModel {

    // states
    void update(InputState input, double deltaMs);

    GameState getGameState();
    boolean isDebugMode();
    void setDebugMode(boolean debug);

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
    //List<GameEvent> consumeGameEvents();

    void addGameListener(IGameListener listener);

    // config (for load)
    GameConfig getGameConfig();

    // getter for rendering
    GameMap getWorldMap();
    List<IRenderable> getAllRenderables();
    String getCurrentDialogue();
    String getCurrentMessage();
    Player getPlayer();
}