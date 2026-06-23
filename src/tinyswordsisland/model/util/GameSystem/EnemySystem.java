package tinyswordsisland.model.util.GameSystem;

import tinyswordsisland.model.GameModel;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.enu.DynamiteState;
import tinyswordsisland.model.enu.TNTState;
import tinyswordsisland.model.enu.TorchState;


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
                tnt.update(deltaMs);
                model.getCollisionChecker().checkEntity(model.getPlayer(), tnt);
                model.getCollisionChecker().checkTile(tnt);
                model.getCollisionChecker().checkObjects(tnt);
                tnt.move();
            }

            if (previousState != tnt.getState() && tnt.getState() == TNTState.EXPLODING) {
                model.getEventDispatcher().notifyTntExploded();
            }
            if (previousState != tnt.getState() && tnt.getState() == TNTState.TRIGGERED) {
                model.getEventDispatcher().notifyTntTriggered();
            }
        }

        model.getTntEnemies().removeIf(EnemyTNT::isExploded);
    }

    private void updateDynamite(GameModel model, double deltaMs) {
        for (EnemyDynamite dynamite : model.getDynamiteEnemies()) {
            DynamiteState previousState = dynamite.getState();

            if (dynamite.getState() != DynamiteState.DEAD) {
                dynamite.update(deltaMs);
                model.getCollisionChecker().checkEntity(model.getPlayer(), dynamite);
                model.getCollisionChecker().checkTile(dynamite);
                model.getCollisionChecker().checkObjects(dynamite);
                dynamite.move();

                if (previousState != DynamiteState.ATTACKING && dynamite.getState() == DynamiteState.ATTACKING) {
                    model.getEventDispatcher().notifyProjectileLaunched();
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
                model.getEventDispatcher().notifyProjectileExploded();
            }
        }

        model.getProjectiles().removeIf(DynamiteProjectile::isExploded);
    }

    private void updateTorch(GameModel model, double deltaMs) {
        for (EnemyTorch torch : model.getTorchEnemies()) {
            if (torch.getState() != TorchState.DEAD) {
                torch.update(deltaMs);
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