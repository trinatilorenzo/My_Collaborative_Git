package tinyswordsisland.model.system;

import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.enu.GameState;
import tinyswordsisland.model.CollisionChecker;
import tinyswordsisland.model.GameMap;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.model.object.GameObject;

import java.util.List;

public interface GameplayContext {
    GameConfig getGameConfig();
    Player getPlayer();
    Monk getMonk();
    List<EnemyTNT> getTntEnemies();
    List<EnemyDynamite> getDynamiteEnemies();
    List<EnemyTorch> getTorchEnemies();
    List<GameObject> getObjects();
    GameMap getWorldMap();
    CollisionChecker getCollisionChecker();
    int getCurrentLevel();
    void setCurrentLevel(int level);
    boolean isLevelCompleted();
    void setLevelCompleted(boolean completed);
    boolean isCurrentLevelPowerUpCollected();
    void setCurrentLevelPowerUpCollected(boolean collected);
    void setCurrentMessage(String message);
    void setGameState(GameState state);
    void addAudioEvent(AudioEventType event);
}
