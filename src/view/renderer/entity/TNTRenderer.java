package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.TNTState;
import model.entity.EnemyTNT;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.HashMap;

public class TNTRenderer {

    private final Map<EnemyTNT, AnimationManager> managerByTNT;
    private final EntityConfig entityConfig;

    private BufferedImage[] wanderFrames;
    private BufferedImage[] triggeredFrames;
    private BufferedImage[] explosionFrames;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public TNTRenderer(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
        managerByTNT = new HashMap<>();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadAnimations() {

        //TODO: make the loading form diffferent source

        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_TNT/Barrel_Purple.png");
        BufferedImage explosionSheet = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_TNT/Explosions.png");

        //BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 1, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);
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

    //-------------------------------------------------------------
    public void update(EnemyTNT tnt, double deltaMs) {
            AnimationManager animationManager = getManager(tnt);
            switch (tnt.getState()) {

                case WANDER:
                    animationManager.playAnimation("wander");
                    break;
                case TRIGGERED:
                    animationManager.playAnimation("triggered");
                    break;
                case EXPLODING:
                    animationManager.playAnimation("explosion");
                    break;
                case EXPLODED:
                    removeTNT(tnt);
                    break;
            }

            animationManager.update(deltaMs);
        }

        //-------------------------------------------------------------
        public void draw(Graphics2D g2, EnemyTNT tnt, int screenX, int screenY) {
            AnimationManager animationManager = getManager(tnt);
            BufferedImage frame = animationManager.getCurrent().getCurrentFrame();

            //TODO migliorare la sintassi e la dismesione (forse)
            if (tnt.getState() == TNTState.EXPLODING) {
                int drawW = entityConfig.TNT_SPRITE_WIDTH * 3;
                int drawH = entityConfig.TNT_SPRITE_HEIGHT * 3;
                int drawX = screenX - entityConfig.TNT_SPRITE_WIDTH;
                int drawY = screenY - entityConfig.TNT_SPRITE_HEIGHT ;
                g2.drawImage(frame, drawX, drawY, drawW, drawH, null);
            } else if (tnt.getState() != TNTState.EXPLODED) {
                g2.drawImage(frame, screenX, screenY, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT, null);
            }
        }

        public void removeTNT(EnemyTNT tnt) {
            managerByTNT.remove(tnt);
        }

        //DEBUG METOD
        //-------------------------------------------------------------
        public void drawSolidArea(Graphics2D g2, EnemyTNT tnt, int screenX, int screenY) {

            Rectangle solid = tnt.getSolidArea();

            int drawX = screenX + solid.x;
            int drawY = screenY + solid.y;

            // semi-trasparente rosso
            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillRect(drawX, drawY, solid.width, solid.height);

            // bordo rosso
            g2.setColor(Color.RED);
            g2.drawRect(drawX, drawY, solid.width, solid.height);

            // draw explosion area (centered on sprite center, consistent with explode())
            int centerX = screenX + entityConfig.TNT_SPRITE_WIDTH / 2;
            int centerY = screenY + entityConfig.TNT_SPRITE_HEIGHT / 2;

            g2.setColor(new Color(93, 255, 0, 80));
            int r = entityConfig.TNT_EXPLOSION_RADIUS;
            g2.fillOval(centerX - r, centerY - r, 2 * r, 2 * r);

            g2.setColor(Color.GREEN);
            g2.drawOval(centerX - r, centerY - r, 2 * r, 2 * r);

            // draw detection area (centered on sprite center, consistent with checkPlayerProximity())
            g2.setColor(new Color(0, 84, 255, 80));
            r = entityConfig.TNT_DETECTION_RADIUS;
            g2.fillOval(centerX - r, centerY - r, 2 * r, 2 * r);

            g2.setColor(Color.BLUE);
            g2.drawOval(centerX - r, centerY - r, 2 * r, 2 * r);
        }
}

