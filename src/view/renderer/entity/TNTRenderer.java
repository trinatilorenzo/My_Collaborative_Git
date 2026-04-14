package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.ENUM.TNTState;
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

        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Barrel_Purple.png");
        BufferedImage explosionSheet = SpriteLoader.loadSpriteSheet("/res/npc/Explosions.png");

        //BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 1, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);
        wanderFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);
        triggeredFrames = SpriteLoader.getAnimationFrames(sheetImage, 4, 1, 3, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);
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
    /*public void draw(Graphics2D g2, EnemyTNT enemy, int screenX, int screenY) {
        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();

        //System.out.println("X: "+ player.getWorldX()/TILE_SIZE + "Y: "+ player.getWorldY()/TILE_SIZE);

        if (enemy.getDirection()!= Direction.RIGHT) {
            // Flip the image horizontally for left direction
            g2.drawImage(frame,
                    screenX + EntityConfig.PLAYER_RENDER_WIDTH,
                    screenY, -EntityConfig.PLAYER_RENDER_HEIGHT,
                    EntityConfig.PLAYER_RENDER_HEIGHT,
                    null);
        } else {
            // Draw normally for right direction
            g2.drawImage(frame,
                    screenX,
                    screenY,
                    EntityConfig.PLAYER_RENDER_WIDTH,
                    EntityConfig.PLAYER_RENDER_HEIGHT,
                    null);
        }

    }*/
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
                    break;
            }

            animationManager.update(deltaMs);
        }

        //-------------------------------------------------------------
        public void draw(Graphics2D g2, EnemyTNT tnt, int screenX, int screenY) {

            if (tnt.getState() == TNTState.EXPLODED) return;
            AnimationManager animationManager = getManager(tnt);
            BufferedImage frame = animationManager.getCurrent().getCurrentFrame();

            g2.drawImage(
                    frame,
                    screenX,
                    screenY,
                    entityConfig.TNT_SPRITE_WIDTH,
                    entityConfig.TNT_SPRITE_HEIGHT,
                    null
            );
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
    }

}