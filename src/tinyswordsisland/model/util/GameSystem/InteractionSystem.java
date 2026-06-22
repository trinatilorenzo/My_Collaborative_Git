package tinyswordsisland.model.util.GameSystem;

import tinyswordsisland.config.enu.*;
import tinyswordsisland.model.GameModel;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.model.object.OBJ_PowerUp;
import tinyswordsisland.model.object.OBJ_Tree;

import java.awt.Rectangle;

public final class InteractionSystem {

    private final LevelManager levelManager = new LevelManager();

    public void update(GameModel model) {
        Player player = model.getPlayer();

        if (player.getState() == PlayerState.ATTACKING) {
            handlePlayerAttack(model, player);
        }

        handleObjectInteractions(model, player);
        levelManager.checkProgression(model);
    }

    private void handlePlayerAttack(GameModel model, Player player) {
        Rectangle attackArea = player.getAttackArea();

        for (EnemyTNT tnt : model.getTntEnemies()) {
            if (!player.isAttackDamageApplied() && attackArea.intersects(tnt.getSolidWorldArea())) {
                tnt.takeDamage();
                player.setAttackDamageApplied(true);
                model.addAudioEvent(AudioEventType.ENEMY_HIT);
            }
        }

        for (EnemyDynamite dynamite : model.getDynamiteEnemies()) {
            if (!player.isAttackDamageApplied() && attackArea.intersects(dynamite.getSolidWorldArea())) {
                dynamite.takeDamage();
                player.setAttackDamageApplied(true);
                model.addAudioEvent(AudioEventType.ENEMY_HIT);

                if (dynamite.getState() == DynamiteState.DEAD) {
                    model.addAudioEvent(AudioEventType.ENEMY_DEFEATED);
                }
            }
        }

        for (EnemyTorch torch : model.getTorchEnemies()) {
            if (!player.isAttackDamageApplied() && attackArea.intersects(torch.getSolidWorldArea())) {
                torch.takeDamage();
                player.setAttackDamageApplied(true);
                model.addAudioEvent(AudioEventType.ENEMY_HIT);
            }
        }

        for (GameObject obj : model.getObjects()) {
            if (obj.isRemoved()) continue;

            if (obj instanceof OBJ_Tree tree
                    && !player.isAttackDamageApplied()
                    && attackArea.intersects(tree.getSolidWorldArea())
                    && tree.isSolid()) {

                player.setAttackDamageApplied(true);
                tree.interact();

                model.addAudioEvent(AudioEventType.TREE_HIT);
                if (tree.isLastHit()) {
                    model.addAudioEvent(AudioEventType.TREE_FINAL);
                }
            }
        }
    }

    private void handleObjectInteractions(GameModel model, Player player) {
        for (GameObject obj : model.getObjects()) {

            if (obj.getName().equals(model.getGameConfig().ObjConfig().GOLDMINE_TAG())) {
                if (player.getSolidWorldArea().intersects(obj.getSolidWorldArea())) {
                    if (model.getCurrentLevel() == model.getGameConfig().getMaxLevel() && model.isLevelCompleted()) {
                        model.setGameState(GameState.WIN);
                        return;
                    }
                }
            }

            if (obj instanceof OBJ_PowerUp powerUp && player.getSolidWorldArea().intersects(powerUp.getSolidWorldArea())) {
                if (!powerUp.isCollectible()) {
                    continue;
                }

                player.applyPowerUpEffect(powerUp.getType());
                powerUp.remove();

                if (levelManager.isPowerUpForCurrentLevel(model, powerUp.getType())) {
                    model.setCurrentLevelPowerUpCollected(true);

                    if (powerUp.getType() == PowerUpType.SHIELD) {
                        model.showMessage("Premi (R) per attivare lo scudo");
                    }
                }

                model.addAudioEvent(AudioEventType.POWERUP_COLLECTED);
            }
        }
    }
}