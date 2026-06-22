package tinyswordsisland.model.util.GameSystem;

import tinyswordsisland.config.enu.*;
import tinyswordsisland.model.GameModel;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.event.AudioEventType;

public final class EnemySystem {

    public void update(GameModel model, double deltaMs) {
        updateTnt(model, deltaMs);
        updateDynamite(model, deltaMs);
        updateProjectiles(model, deltaMs);
        updateTorch(model, deltaMs);
    }

    private void updateTnt(GameModel model, double deltaMs) {
        for (EnemyTNT tnt : model.getTntEnemies()) {
            TNTState previousState = tnt.getState();

            if (tnt.getState() != TNTState.EXPLODED) {
                tnt.update(model.getPlayer(), deltaMs);
                model.getCollisionChecker().checkEntity(model.getPlayer(), tnt);
                model.getCollisionChecker().checkTile(tnt);
                model.getCollisionChecker().checkObjects(tnt);
                tnt.move();
            }

            if (previousState != tnt.getState() && tnt.getState() == TNTState.EXPLODING) {
                model.addAudioEvent(AudioEventType.TNT_EXPLOSION);
            }
            if (previousState != tnt.getState() && tnt.getState() == TNTState.TRIGGERED) {
                model.addAudioEvent(AudioEventType.TNT_TRIGGERED);
            }
        }

        model.getTntEnemies().removeIf(EnemyTNT::isExploded);
    }

    private void updateDynamite(GameModel model, double deltaMs) {
        for (EnemyDynamite dynamite : model.getDynamiteEnemies()) {
            DynamiteState previousState = dynamite.getState();

            if (dynamite.getState() != DynamiteState.DEAD) {
                dynamite.update(model.getPlayer(), deltaMs);
                model.getCollisionChecker().checkEntity(model.getPlayer(), dynamite);
                model.getCollisionChecker().checkTile(dynamite);
                model.getCollisionChecker().checkObjects(dynamite);
                dynamite.move();

                if (previousState != DynamiteState.ATTACKING && dynamite.getState() == DynamiteState.ATTACKING) {
                    model.addAudioEvent(AudioEventType.PROJECTILE_LAUNCHED);
                }
            }
        }

        model.getDynamiteEnemies().removeIf(EnemyDynamite::isDead);
    }

    private void updateProjectiles(GameModel model, double deltaMs) {
        for (DynamiteProjectile proj : model.getProjectiles()) {
            proj.update(deltaMs);
            model.getCollisionChecker().checkTile(proj);

            if (model.getCollisionChecker().intersects(model.getPlayer(), proj)) {
                model.getPlayer().takeDamage();
                proj.explode();
            }

            if (proj.isExploded()) {
                model.addAudioEvent(AudioEventType.PROJECTILE_EXPLODED);
            }
        }

        model.getProjectiles().removeIf(DynamiteProjectile::isExploded);
    }

    private void updateTorch(GameModel model, double deltaMs) {
        for (EnemyTorch torch : model.getTorchEnemies()) {
            if (torch.getState() != TorchState.DEAD) {
                torch.update(model.getPlayer(), deltaMs);
                model.getCollisionChecker().checkEntity(torch, model.getPlayer());
                model.getCollisionChecker().checkEntity(model.getPlayer(), torch);
                model.getCollisionChecker().checkTile(torch);
                model.getCollisionChecker().checkObjects(torch);
                torch.move();
            }
        }

        model.getTorchEnemies().removeIf(EnemyTorch::isDead);
    }
}