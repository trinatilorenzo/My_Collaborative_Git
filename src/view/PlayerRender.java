package view;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import model.entity.Player;
import model.entity.Entity.Direction;
import view.Animation.Animation;
import view.Animation.AnimationManager;

import static main.GameSetting.*;

public class PlayerRender {

    private AnimationManager animationManager;
    private final int spriteWidth = SPRITE_FRAME_WIDTH;
    private final int spriteHeight = SPRITE_FRAME_HEIGHT;

    public PlayerRender(){
        loadAnimations();
    }

    private void loadAnimations(){
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/player/Warrior_blue.png");

        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 6, spriteWidth, spriteHeight);
        BufferedImage[] walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, spriteWidth, spriteHeight);
        BufferedImage[] attackRightFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 2, 6, spriteWidth, spriteHeight);
        BufferedImage[] attackDownFrames = SpriteLoader.getAnimationFrames(sheetImage, 4, 2, 6, spriteWidth, spriteHeight);
        BufferedImage[] attackUpFrames = SpriteLoader.getAnimationFrames(sheetImage, 6, 2, 6, spriteWidth, spriteHeight);

        animationManager = new AnimationManager();
        animationManager.addAnimation("idle", new Animation(idleFrames, 6, true));
        animationManager.addAnimation("walk", new Animation(walkFrames, 5, true));
        animationManager.addAnimation("attack_right", new Animation(attackRightFrames, 3, false));
        animationManager.addAnimation("attack_down", new Animation(attackDownFrames, 3, false));
        animationManager.addAnimation("attack_up", new Animation(attackUpFrames, 3, false));
    }// end loadSpriteSheet method

    public void draw(Graphics2D g2, Player player){
        updateAnimation(player);
        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
        //System.out.println("X: "+ player.getWorldX()/TILE_SIZE + "Y: "+ player.getWorldY()/TILE_SIZE);

        if (player.getFacingRight() == -1) {
            // Flip the image horizontally for left direction
            g2.drawImage(frame,
                         player.getScreenX() + PLAYER_RENDER_WIDTH, 
                         player.getScreenY(), -PLAYER_RENDER_WIDTH, 
                         PLAYER_RENDER_HEIGHT, 
                         null);
        } else {
            // Draw normally for right direction
            g2.drawImage(frame, 
                         player.getScreenX(), 
                         player.getScreenY(), 
                         PLAYER_RENDER_WIDTH, 
                         PLAYER_RENDER_HEIGHT, 
                         null);
        }
    }

    private void updateAnimation(Player player){
        switch (player.getState()) {
            case IDLE:
                animationManager.playAnimation("idle");
                break;
            case WALKING:
                animationManager.playAnimation("walk");
                break;
            case ATTACKING:
                if (player.getDirection() == Direction.DOWN) {
                    animationManager.playAnimation("attack_down");
                } else if (player.getDirection() == Direction.UP) {
                    animationManager.playAnimation("attack_up");
                } else {
                    animationManager.playAnimation("attack_right");
                }
                 
                // Check if the attack animation has finished to reset the attacking state
                if (animationManager.getCurrent().isFinished()) {
                    player.stopAttack();
                }
                break;
            
        }
        animationManager.update();
    }
}
