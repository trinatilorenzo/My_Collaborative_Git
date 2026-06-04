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

/**
 * The TNTRenderer CLASS is responsible for rendering the TNT entity onto the game screen.
 */
//-------------------------------------------------------------------------------------------------------------------
public class TNTRenderer {

    private final ConcurrentHashMap<EnemyTNT, AnimationManager> managerByTNT;
    private final EntityConfig entityConfig;

    private BufferedImage[] wanderFrames;
    private BufferedImage[] triggeredFrames;
    private BufferedImage[] explosionFrames;

    /**
     * CONSTRUCTOR
     */
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

        wanderFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, EntityConfig.TNT_SPRITE_WIDTH, EntityConfig.TNT_SPRITE_HEIGHT);
        triggeredFrames = SpriteLoader.getAnimationFrames(sheetImage, 5, 1, 3, EntityConfig.TNT_SPRITE_WIDTH, EntityConfig.TNT_SPRITE_HEIGHT);
        explosionFrames = SpriteLoader.getAnimationFrames(explosionSheet, 0, 1, 9, EntityConfig.PLAYER_RENDER_WIDTH, EntityConfig.PLAYER_RENDER_HEIGHT);
    }
    //-------------------------------------------------------------

    /**
     * An animation Manger for each TNT entity
     */
    //-------------------------------------------------------------
    private AnimationManager getManager(EnemyTNT tnt) {
        // if is not in the map, create a new one

        return managerByTNT.computeIfAbsent(tnt, k -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("wander", new Animation(wanderFrames, 90, true));
            manager.addAnimation("triggered", new Animation(triggeredFrames, 100, true));
            manager.addAnimation("explosion", new Animation(explosionFrames, 100, false));
            manager.playAnimation("wander");
            return manager;
        });
    }
    //-------------------------------------------------------------

    /**
     * Change the TNT animation based on his state
     */
    //-------------------------------------------------------------
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
    //-------------------------------------------------------------

    /**
     * Draw the TNT on the screen
     */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2, EnemyTNT tnt, int screenX, int screenY) {
        AnimationManager animationManager = getManager(tnt);
        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();

        if (tnt.getState() == TNTState.EXPLODING) {
            int drawW = EntityConfig.TNT_SPRITE_WIDTH * 3;
            int drawH = EntityConfig.TNT_SPRITE_HEIGHT * 3;
            g2.drawImage(frame, screenX - drawW / 2, screenY - drawH / 2, drawW, drawH, null);
        } else if (tnt.getState() != TNTState.EXPLODED) {
            int drawX = screenX - EntityConfig.TNT_SPRITE_WIDTH / 2;
            int drawY = screenY - EntityConfig.TNT_SPRITE_HEIGHT / 2;
            g2.drawImage(frame, drawX, drawY, EntityConfig.TNT_SPRITE_WIDTH, EntityConfig.TNT_SPRITE_HEIGHT, null);

            // DYNAMIC HEALTH BAR (Appears only after taking the first hit)
            if (tnt.getLife() < tnt.getMaxLife()) {
                int barWidth = EntityConfig.TNT_SPRITE_WIDTH/2;
                int barHeight = 5;
                int barX = screenX - barWidth / 2;
                int barY = screenY - barHeight - 20; // Positions bar safely above the head

                double lifePercent = (double) tnt.getLife() / tnt.getMaxLife();
                if (lifePercent < 0) lifePercent = 0;

                int currentBarWidth = (int) (barWidth * lifePercent);
                int roundness = 5;
                // Background (Black outline box)
                g2.setColor(Color.gray);
                g2.fillRoundRect(barX, barY, barWidth, barHeight, roundness, roundness);

                // Health fill (Fiery Orange/Red color)
                g2.setColor(new Color(255, 69, 0));
                g2.fillRoundRect(barX, barY, currentBarWidth, barHeight, roundness, roundness);

                // Border trim
                g2.setColor(new Color(30, 30, 30));
                g2.drawRoundRect(barX, barY, barWidth, barHeight, roundness, roundness);
            }

        }


    }
    //-------------------------------------------------------------

    /**
     * Use to be sure to remove a tnt from render when it's exploded
     */
    //-------------------------------------------------------------
    public void removeTNT(EnemyTNT tnt) {
        managerByTNT.remove(tnt);
    }
    //-------------------------------------------------------------

    /**
     * Debug method to draw the tnt's solid area and interaction radius.
     */
    //-------------------------------------------------------------
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
        int r = EntityConfig.TNT_EXPLOSION_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.GREEN);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);

        // Detection Area
        g2.setColor(new Color(0, 84, 255, 80));
        r = EntityConfig.TNT_DETECTION_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.BLUE);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);
    }
    //-------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
    