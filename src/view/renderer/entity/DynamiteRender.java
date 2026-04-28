package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.DynamiteState;
import model.entity.DynamiteProjectile;
import model.entity.EnemyDynamite;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DynamiteRender {
    private final ConcurrentHashMap<EnemyDynamite, AnimationManager> managerByEnemy;
    private final EntityConfig entityConfig;

    private BufferedImage[] wanderFrames;
    private BufferedImage[] attackFrames;

    private BufferedImage[] projectileFrames;

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
        BufferedImage projectileImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Dynamite/Dynamite.png");
        wanderFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);
        attackFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);
        
        projectileFrames = SpriteLoader.getAnimationFrames(projectileImage, 0, 1, 6, entityConfig.PROJECTILE_SPRITE_WIDTH, entityConfig.PROJECTILE_SPRITE_HEIGHT);
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
        if (!enemy.isFacingRight()){
            // Flip the image
            g2.drawImage(frame,
                    screenX + EntityConfig.DYNAMITE_SPRITE_WIDTH / 2,
                    screenY - EntityConfig.DYNAMITE_SPRITE_HEIGHT / 2,
                    -EntityConfig.DYNAMITE_SPRITE_WIDTH,
                    EntityConfig.DYNAMITE_SPRITE_HEIGHT,
                    null);
        }else{
            g2.drawImage(
                    frame,
                    screenX - entityConfig.DYNAMITE_SPRITE_WIDTH / 2,
                    screenY - entityConfig.DYNAMITE_SPRITE_HEIGHT / 2,
                    entityConfig.DYNAMITE_SPRITE_WIDTH,
                    entityConfig.DYNAMITE_SPRITE_HEIGHT,
                    null
            );
        }
    }

    //-------------------------------------------------------------
    public void removeEnemy(EnemyDynamite enemy) {
        managerByEnemy.remove(enemy);
    }

    //-------------------------------------------------------------
    public void drawProjectile(Graphics2D g2, DynamiteProjectile proj, int screenX, int screenY) {

        BufferedImage frame = projectileFrames[0]; // oppure animazione futura

        int w = entityConfig.PROJECTILE_SPRITE_WIDTH;
        int h = entityConfig.PROJECTILE_SPRITE_HEIGHT;

        double angle = proj.getAngle();

        // salva trasformazione corrente
        java.awt.geom.AffineTransform old = g2.getTransform();

        // trasla al centro del proiettile
        g2.translate(screenX, screenY);

        // ruota
        g2.rotate(angle);

        // disegna centrato
        g2.drawImage(
            frame,
            -w / 2,
            -h / 2,
            w,
            h,
            null
        );

        // ripristina trasformazione
        g2.setTransform(old);
    }

    //DEBUG
    //TODO: drawSolidArea

}
