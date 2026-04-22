package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.DynamiteState;
import model.entity.EnemyDynamite;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

public class DynamiteRender {
    private final ConcurrentHashMap<EnemyDynamite, AnimationManager> managerByEnemy;
    private final EntityConfig entityConfig;

    private BufferedImage[] wanderFrames;
    private BufferedImage[] attackFrames;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public DynamiteRender(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
        managerByEnemy = new ConcurrentHashMap<>();
    }

    //-------------------------------------------------------------
    private void loadAnimations() {
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Dynamite/TNT_Red.png");
        wanderFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);
        attackFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);
    }

    //--------------------------------------------------------------
    private AnimationManager getManager(EnemyDynamite dynamite) {
        return managerByEnemy.computeIfAbsent(dynamite, k -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("wander", new Animation(wanderFrames, 90, true));
            manager.addAnimation("attack", new Animation(attackFrames, 100, true));
            manager.playAnimation("wander"); // Stato iniziale
            return manager;
        });
    }

    //-------------------------------------------------------------
    public void update(EnemyDynamite enemy, double deltaMs) {
        AnimationManager manager = getManager(enemy);
        if (enemy.getState() == DynamiteState.WANDER || enemy.getState() == DynamiteState.CHASING) {
            manager.playAnimation("wander");
        } else if (enemy.getState() == DynamiteState.ATTACKING) {
            manager.playAnimation("attack");
        }
        manager.update(deltaMs);
    }

    //-------------------------------------------------------------
    public void draw(Graphics2D g2, EnemyDynamite enemy, int screenX, int screenY) {

        AnimationManager manager = getManager(enemy);
        BufferedImage frame = manager.getCurrent().getCurrentFrame();

        g2.drawImage(
                frame,
                screenX,
                screenY,
                entityConfig.DYNAMITE_SPRITE_WIDTH,
                entityConfig.DYNAMITE_SPRITE_HEIGHT,
                null
        );
    }

    //-------------------------------------------------------------
    public void removeEnemy(EnemyDynamite enemy) {
        managerByEnemy.remove(enemy);
    }

    
}
