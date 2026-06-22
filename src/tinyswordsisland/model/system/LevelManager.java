package tinyswordsisland.model.system;

import tinyswordsisland.config.enu.PowerUpType;
import tinyswordsisland.model.entity.EnemyDynamite;
import tinyswordsisland.model.entity.EnemyTNT;
import tinyswordsisland.model.entity.EnemyTorch;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.model.object.OBJ_Tree;

public final class LevelManager {

    public void checkProgression(GameplayContext ctx) {
        boolean enemiesDefeated = allEnemiesDefeated(ctx);

        if (enemiesDefeated) {
            if (ctx.getCurrentLevel() < 3) {
                updateFlashingEffect(ctx, true);
                ctx.addAudioEvent(AudioEventType.LEVEL_UP);
                ctx.setCurrentLevel(ctx.getCurrentLevel() + 1);
                ctx.setCurrentLevelPowerUpCollected(false);
                ctx.setCurrentMessage("Livello " + ctx.getCurrentLevel() + " completato! Scale sbloccate");
                ctx.getWorldMap().unlockStairsLevel(ctx.getPlayer().getCurrentLayer());
            } else {
                ctx.setLevelCompleted(true);
                ctx.setCurrentMessage("Livello " + ctx.getCurrentLevel() + " completato. Vai alla miniera!");
            }
        }
    }

    public void updateFlashingEffect(GameplayContext ctx, boolean enemiesDefeated) {
        PowerUpType targetType = switch (ctx.getCurrentLevel()) {
            case 0 -> PowerUpType.SHIELD;
            case 1 -> PowerUpType.HEALTH_RESTORE;
            case 2 -> PowerUpType.SPEED_BOOST;
            default -> null;
        };
        for (GameObject obj : ctx.getObjects()) {
            if (obj instanceof OBJ_Tree tree) {
                tree.setFlashingActive(enemiesDefeated && tree.getHiddenPowerUp() == targetType);
            }
        }
    }

    public boolean allEnemiesDefeated(GameplayContext ctx) {
        return switch (ctx.getCurrentLevel()) {
            case 0 -> ctx.getTntEnemies().isEmpty();
            case 1 -> ctx.getDynamiteEnemies().isEmpty();
            case 2 -> ctx.getTorchEnemies().isEmpty();
            case 3 -> true;
            default -> false;
        };
    }

    boolean isPowerUpForCurrentLevel(GameplayContext ctx, PowerUpType type) {
        int level = ctx.getCurrentLevel();
        return (level == 0 || level == 1 && type == PowerUpType.SHIELD)
                || (level == 1 || level == 2 && type == PowerUpType.HEALTH_RESTORE)
                || (level == 2 || level == 3 && type == PowerUpType.SPEED_BOOST);
    }
}
