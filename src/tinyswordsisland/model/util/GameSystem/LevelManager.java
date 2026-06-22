package tinyswordsisland.model.util.GameSystem;

import tinyswordsisland.config.enu.PowerUpType;
import tinyswordsisland.model.GameModel;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.model.object.OBJ_Tree;
import tinyswordsisland.model.event.AudioEventType;

public final class LevelManager {

    /**
     * Update level progression
     */
    public void checkProgression(GameModel model) {
        boolean enemiesDefeated = allEnemiesDefeated(model);

        if (enemiesDefeated) {
            if (model.getCurrentLevel() < 3) {
                updateFlashingEffect(model, true);
                model.addAudioEvent(AudioEventType.LEVEL_UP);
                model.setCurrentLevel(model.getCurrentLevel() + 1);
                model.setCurrentLevelPowerUpCollected(false);
                model.showMessage("Livello " + model.getCurrentLevel() + " completato! Scale sbloccate");
                model.getWorldMap().unlockStairsLevel(model.getPlayer().getCurrentLayer());
            } else {
                model.setLevelCompleted(true);
                model.showMessage("Livello " + model.getCurrentLevel() + " completato. Vai alla miniera!");
            }
        }
    }

    public void updateFlashingEffect(GameModel model, boolean enemiesDefeated) {
        PowerUpType targetType = switch (model.getCurrentLevel()) {
            case 0 -> PowerUpType.SHIELD;
            case 1 -> PowerUpType.HEALTH_RESTORE;
            case 2 -> PowerUpType.SPEED_BOOST;
            default -> null;
        };

        for (GameObject obj : model.getObjects()) {
            if (obj instanceof OBJ_Tree tree) {
                tree.setFlashingActive(enemiesDefeated && tree.getHiddenPowerUp() == targetType);
            }
        }
    }

    /**
     * Check if all enemies of current level are defeated
     */
    public boolean allEnemiesDefeated(GameModel model) {
        return switch (model.getCurrentLevel()) {
            case 0 -> model.getTntEnemies().isEmpty();
            case 1 -> model.getDynamiteEnemies().isEmpty();
            case 2 -> model.getTorchEnemies().isEmpty();
            case 3 -> true;
            default -> false;
        };
    }

    public boolean isPowerUpForCurrentLevel(GameModel model, PowerUpType type) {
        int level = model.getCurrentLevel();
        return (level == 0 || level == 1 && type == PowerUpType.SHIELD)
                || (level == 1 || level == 2 && type == PowerUpType.HEALTH_RESTORE)
                || (level == 2 || level == 3 && type == PowerUpType.SPEED_BOOST);
    }
}