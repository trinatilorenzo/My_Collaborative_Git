package tinyswordsisland.model.system;

import tinyswordsisland.config.enu.*;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.model.object.OBJ_PowerUp;
import tinyswordsisland.model.object.OBJ_Tree;

import java.awt.Rectangle;
import java.util.List;

public final class InteractionSystem {

    private final LevelManager levelManager = new LevelManager();

    public void update(GameplayContext ctx) {
        Player player = ctx.getPlayer();
        if (player.getState() == PlayerState.ATTACKING) {
            handlePlayerAttack(ctx, player);
        }
        handleObjectInteractions(ctx, player);
        levelManager.checkProgression(ctx);
    }

    private void handlePlayerAttack(GameplayContext ctx, Player player) {
        Rectangle attackArea = player.getAttackArea();

        for (EnemyTNT tnt : ctx.getTntEnemies()) {
            if (!player.isAttackDamageApplied() && attackArea.intersects(tnt.getSolidWorldArea())) {
                tnt.takeDamage();
                player.setAttackDamageApplied(true);
                ctx.addAudioEvent(AudioEventType.ENEMY_HIT);
            }
        }

        for (EnemyDynamite dynamite : ctx.getDynamiteEnemies()) {
            if (!player.isAttackDamageApplied() && attackArea.intersects(dynamite.getSolidWorldArea())) {
                dynamite.takeDamage();
                player.setAttackDamageApplied(true);
                ctx.addAudioEvent(AudioEventType.ENEMY_HIT);
                if (dynamite.getState() == DynamiteState.DEAD) {
                    ctx.addAudioEvent(AudioEventType.ENEMY_DEFEATED);
                }
            }
        }

        for (EnemyTorch torch : ctx.getTorchEnemies()) {
            if (!player.isAttackDamageApplied() && attackArea.intersects(torch.getSolidWorldArea())) {
                torch.takeDamage();
                player.setAttackDamageApplied(true);
                ctx.addAudioEvent(AudioEventType.ENEMY_HIT);
            }
        }

        for (GameObject obj : ctx.getObjects()) {
            if (obj.isRemoved()) continue;
            if (obj instanceof OBJ_Tree tree
                    && !player.isAttackDamageApplied()
                    && attackArea.intersects(tree.getSolidWorldArea())
                    && tree.isSolid()) {
                player.setAttackDamageApplied(true);
                tree.interact();
                ctx.addAudioEvent(AudioEventType.TREE_HIT);
                if (tree.isLastHit()) {
                    ctx.addAudioEvent(AudioEventType.TREE_FINAL);
                }
            }
        }
    }

    private void handleObjectInteractions(GameplayContext ctx, Player player) {
        for (GameObject obj : ctx.getObjects()) {
            if (obj.getName().equals(ctx.getGameConfig().ObjConfig().GOLDMINE_TAG())) {
                if (player.getSolidWorldArea().intersects(obj.getSolidWorldArea())) {
                    if (ctx.getCurrentLevel() == ctx.getGameConfig().getMaxLevel() && ctx.isLevelCompleted()) {
                        ctx.setGameState(GameState.WIN);
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
                if (levelManager.isPowerUpForCurrentLevel(ctx, powerUp.getType())) {
                    ctx.setCurrentLevelPowerUpCollected(true);
                    if (powerUp.getType() == PowerUpType.SHIELD) {
                        ctx.setCurrentMessage("Premi (R) per attivare lo scudo");
                    }
                }
            }
        }
    }

    public void updateFlashingEffect(GameplayContext ctx, boolean enemiesDefeated) {
        levelManager.updateFlashingEffect(ctx, enemiesDefeated);
    }
}
