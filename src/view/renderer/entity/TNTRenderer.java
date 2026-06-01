package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.TNTState;
import model.entity.EnemyTNT;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

public class TNTRenderer {

    private final ConcurrentHashMap<EnemyTNT, AnimationManager> managerByTNT;
    private final EntityConfig entityConfig;

    private BufferedImage[] wanderFrames;
    private BufferedImage[] triggeredFrames;
    private BufferedImage[] explosionFrames;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public TNTRenderer(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
        managerByTNT = new ConcurrentHashMap<>();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadAnimations() {
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_TNT/Barrel_Purple.png");
        BufferedImage explosionSheet = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_TNT/Explosions.png");

        wanderFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);
        triggeredFrames = SpriteLoader.getAnimationFrames(sheetImage, 5, 1, 3, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);
        explosionFrames = SpriteLoader.getAnimationFrames(explosionSheet, 0, 1, 9, entityConfig.PLAYER_RENDER_WIDTH, entityConfig.PLAYER_RENDER_HEIGHT);
    }
    //-------------------------------------------------------------

    private AnimationManager getManager(EnemyTNT tnt) {
        return managerByTNT.computeIfAbsent(tnt, k -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("wander", new Animation(wanderFrames, 90, true));
            manager.addAnimation("triggered", new Animation(triggeredFrames, 100, true));
            manager.addAnimation("explosion", new Animation(explosionFrames, 100, false));
            manager.playAnimation("wander"); // Stato iniziale
            return manager;
        });
    }

    public void update(EnemyTNT tnt, double deltaMs) {
        AnimationManager animationManager = getManager(tnt);
        switch (tnt.getState()) {
            case WANDER -> animationManager.playAnimation("wander");
            case TRIGGERED -> animationManager.playAnimation("triggered");
            case EXPLODING -> animationManager.playAnimation("explosion");
            case EXPLODED -> removeTNT(tnt);
        }
        animationManager.update(deltaMs);
    }

    public void draw(Graphics2D g2, EnemyTNT tnt, int screenX, int screenY) {
        AnimationManager animationManager = getManager(tnt);
        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();

        if (tnt.getState() == TNTState.EXPLODING) {
            int drawW = entityConfig.TNT_SPRITE_WIDTH * 3;
            int drawH = entityConfig.TNT_SPRITE_HEIGHT * 3;
            g2.drawImage(frame, screenX - drawW / 2, screenY - drawH / 2, drawW, drawH, null);
        } else if (tnt.getState() != TNTState.EXPLODED) {
            int drawX = screenX - entityConfig.TNT_SPRITE_WIDTH / 2;
            int drawY = screenY - entityConfig.TNT_SPRITE_HEIGHT / 2;
            g2.drawImage(frame, drawX, drawY, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT, null);
        }
    }

    public void removeTNT(EnemyTNT tnt) {
        managerByTNT.remove(tnt);
    }

    public void drawSolidArea(Graphics2D g2, EnemyTNT tnt, int screenX, int screenY) {
        Rectangle solid = tnt.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        // Explosion Area
        g2.setColor(new Color(93, 255, 0, 80));
        int r = entityConfig.TNT_EXPLOSION_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.GREEN);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);

        // Detection Area
        g2.setColor(new Color(0, 84, 255, 80));
        r = entityConfig.TNT_DETECTION_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.BLUE);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);
    }
}
//-------------------------------------------------------------
    