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

    private static final int DEATH_FRAME_SIZE = 128;

    private AnimationManager animationManager;
    private final EntityConfig entityConfig;
    private PlayerState previousState = PlayerState.IDLE;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public PlayerRender(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadAnimations() {

        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/player/Warrior_blue.png");

        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackRightFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 2, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackDownFrames = SpriteLoader.getAnimationFrames(sheetImage, 4, 2, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackUpFrames = SpriteLoader.getAnimationFrames(sheetImage, 6, 2, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] deathFrames = loadDeathFrames();

        animationManager = new AnimationManager();
        // frame duration in milliseconds
        animationManager.addAnimation("idle", new Animation(idleFrames, 120, true));
        animationManager.addAnimation("walk", new Animation(walkFrames, 90, true));
        animationManager.addAnimation("attack_right", new Animation(attackRightFrames, 60, false));
        animationManager.addAnimation("attack_down", new Animation(attackDownFrames, 60, false));
        animationManager.addAnimation("attack_up", new Animation(attackUpFrames, 60, false));
        animationManager.addAnimation("death", new Animation(deathFrames, 80, false));
    }

    private BufferedImage[] loadDeathFrames() {
        BufferedImage deadSheet = SpriteLoader.loadSpriteSheet("/res/player/Dead.png");
        BufferedImage[] firstRow = SpriteLoader.getAnimationFrames(deadSheet, 0, 1, 7, DEATH_FRAME_SIZE, DEATH_FRAME_SIZE);
        BufferedImage[] secondRow = SpriteLoader.getAnimationFrames(deadSheet, 1, 1, 4, DEATH_FRAME_SIZE, DEATH_FRAME_SIZE);

        BufferedImage[] deathFrames = new BufferedImage[10];
        System.arraycopy(firstRow, 1, deathFrames, 0, 6);
        System.arraycopy(secondRow, 0, deathFrames, 6, 4);
        return deathFrames;
    }
    //-------------------------------------------------------------
    public void update(Player player, double deltaMs) {
        PlayerState currentState = player.getState();
        boolean attackJustStarted = currentState == PlayerState.ATTACKING && previousState != PlayerState.ATTACKING;

        switch (currentState) {
            case IDLE -> animationManager.playAnimation("idle");
            case WALKING -> animationManager.playAnimation("walk");
            case ATTACKING -> {
                if (player.getDirection() == Direction.DOWN) {
                    animationManager.playAnimation("attack_down");
                } else if (player.getDirection() == Direction.UP) {
                    animationManager.playAnimation("attack_up");
                } else {
                    animationManager.playAnimation("attack_right");
                }

                if (attackJustStarted) {
                    animationManager.getCurrent().reset();
                }

                if (animationManager.getCurrent().isFinished()) {
                    player.completeAttackAnimation();
                }
            }
            case DYING -> {
                animationManager.playAnimation("death");
                if (animationManager.getCurrent().isFinished()) {
                    player.completeDeathAnimation();
                }
            }
            case DEAD -> animationManager.playAnimation("death");
        }
        animationManager.update(deltaMs);
        previousState = player.getState();
    }

    public void draw(Graphics2D g2, Player player, int screenX, int screenY) {
        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();

        int width = EntityConfig.PLAYER_RENDER_WIDTH;
        int height = EntityConfig.PLAYER_RENDER_HEIGHT;
        int drawX = screenX - width / 2;
        int drawY = screenY - height / 2;

        if (player.getState() == PlayerState.DYING || player.getState() == PlayerState.DEAD) {
            g2.drawImage(frame, drawX, drawY, width, height, null);
            return;
        }

        if (player.getFacing() != Direction.RIGHT) {
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }
    }

    public void drawSolidArea(Graphics2D g2, Player player, int screenX, int screenY) {
        Rectangle solid = player.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        if (player.getState() == PlayerState.ATTACKING) {
            Rectangle attackArea = player.getAttackArea();
            int attackDrawX = attackArea.x - player.getWorldX() + screenX;
            int attackDrawY = attackArea.y - player.getWorldY() + screenY;

            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillRect(attackDrawX, attackDrawY, attackArea.width, attackArea.height);
            g2.setColor(Color.RED);
            g2.drawRect(attackDrawX, attackDrawY, attackArea.width, attackArea.height);
        }
    }
}

