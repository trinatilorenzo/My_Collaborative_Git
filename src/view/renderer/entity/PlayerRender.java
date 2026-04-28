package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.PlayerState;
import model.entity.Player;
import view.SpriteLoader;
import view.Animation.Animation;
import view.Animation.AnimationManager;

import java.awt.*;
import java.awt.image.BufferedImage;



/**
 * The PLAYERRENDER CLASS is responsible for rendering the player entity onto the game screen.
 */
//-------------------------------------------------------------------------------------------------------------------
public class PlayerRender {

    private AnimationManager animationManager;
    private final EntityConfig entityConfig;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public PlayerRender(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadAnimations() {

        //TODO: make the loading form diffferent source

        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/player/Warrior_blue.png");

        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackRightFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 2, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackDownFrames = SpriteLoader.getAnimationFrames(sheetImage, 4, 2, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackUpFrames = SpriteLoader.getAnimationFrames(sheetImage, 6, 2, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);

        animationManager = new AnimationManager();
        // frame duration in milliseconds
        animationManager.addAnimation("idle", new Animation(idleFrames, 120, true));
        animationManager.addAnimation("walk", new Animation(walkFrames, 90, true));
        animationManager.addAnimation("attack_right", new Animation(attackRightFrames, 60, false));
        animationManager.addAnimation("attack_down", new Animation(attackDownFrames, 60, false));
        animationManager.addAnimation("attack_up", new Animation(attackUpFrames, 60, false));
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void draw(Graphics2D g2, Player player) {
        //TODO controllare se ci sono altri modi per le animazioni nella corsa
        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
        //System.out.println("X: "+ player.getWorldX()/TILE_SIZE + "Y: "+ player.getWorldY()/TILE_SIZE);

        if (player.getFacing()!= Direction.RIGHT) {
            // Flip the image horizontally for left direction
            g2.drawImage(frame,
                    player.getScreenX() + EntityConfig.PLAYER_RENDER_WIDTH/2,
                    player.getScreenY() - EntityConfig.PLAYER_RENDER_HEIGHT/2 ,
                    -EntityConfig.PLAYER_RENDER_WIDTH,
                    EntityConfig.PLAYER_RENDER_HEIGHT,
                    null);
        } else {
            // Draw normally for right directionc
            g2.drawImage(frame,
                    player.getScreenX() - EntityConfig.PLAYER_RENDER_WIDTH/2,
                    player.getScreenY() - EntityConfig.PLAYER_RENDER_HEIGHT/2,
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
    public void drawSolidArea(Graphics2D g2, Player player) {
        Rectangle solid = player.getSolidArea();
        int drawX = player.getScreenX() - player.getSolidArea().width/2;
        int drawY = player.getScreenY() - player.getSolidArea().height/2;

        // Semi-transparent red fill
        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);

        // Solid red border
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        // Draw attack area if attacking
        if (player.getState() == PlayerState.ATTACKING) {
            Rectangle attackArea = player.getAttackArea();
            int attackDrawX = attackArea.x - player.getWorldX() + player.getScreenX();
            int attackDrawY = attackArea.y - player.getWorldY() + player.getScreenY();

            // Semi-transparent red fill for attack area
            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillRect(attackDrawX, attackDrawY, attackArea.width, attackArea.height);

            // Solid red border for attack area
            g2.setColor(Color.RED);
            g2.drawRect(attackDrawX, attackDrawY, attackArea.width, attackArea.height);
        }
    }
    //-------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
