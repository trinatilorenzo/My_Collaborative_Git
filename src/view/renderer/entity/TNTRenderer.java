package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.ENUM.Direction;
import main.ENUM.PlayerState;
import main.ENUM.TNTState;
import model.entity.EnemyTNT;
import model.entity.Player;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TNTRenderer {

    private AnimationManager animationManager;
    private final EntityConfig entityConfig;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public TNTRenderer(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadAnimations() {

        //TODO: make the loading form diffferent source

        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Barrel_Purple.png");

        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 1, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);
        BufferedImage[] walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.TNT_SPRITE_WIDTH, entityConfig.TNT_SPRITE_HEIGHT);


        animationManager = new AnimationManager();
        animationManager.addAnimation("idle", new Animation(idleFrames, 120, true));
        animationManager.addAnimation("walk", new Animation(walkFrames, 90, true));

        // frame duration in milliseconds
       /*
        animationManager.addAnimation("walk", new Animation(walkFrames, 90, true));
        animationManager.addAnimation("attack_right", new Animation(attackRightFrames, 60, false));
        animationManager.addAnimation("attack_down", new Animation(attackDownFrames, 60, false));
        animationManager.addAnimation("attack_up", new Animation(attackUpFrames, 60, false));
        */
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void draw(Graphics2D g2, EnemyTNT enemy, int screenX, int screenY) {
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

    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void updateAnimations(Player player, double deltaMs) {
        switch (player.getState()) {
            case IDLE:
                animationManager.playAnimation("idle");
                break;
            case WALKING:
                animationManager.playAnimation("walk");
                break;
            case ATTACKING:
                // Attack animation takes priority even while moving
                if (player.getDirection() == Direction.DOWN) {
                    animationManager.playAnimation("attack_down");
                } else if (player.getDirection() == Direction.UP) {
                    animationManager.playAnimation("attack_up");
                } else {
                    animationManager.playAnimation("attack_right");
                }

                // When attack animation finishes, release the attack state
                if (animationManager.getCurrent().isFinished()) {
                    player.stopAttack();
                }
                break;
        }
        animationManager.update(deltaMs);
    }
    //-------------------------------------------------------------


    //DEBUG METOD
    //-------------------------------------------------------------
    public void drawSolidArea(Graphics2D g2, EnemyTNT enemy, Player player) {
        Rectangle solid = enemy.getSolidArea();
        int drawX = enemy.getWorldX() - player.getWorldX() + player.getScreenX() + solid.x;
        int drawY = enemy.getWorldY() - player.getWorldY() + player.getScreenY() + solid.y;

        // Semi-transparent red fill
        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);

        // Solid red border
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

    }
}
