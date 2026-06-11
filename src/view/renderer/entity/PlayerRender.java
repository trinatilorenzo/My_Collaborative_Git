package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.PlayerColor;
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
    private PlayerState previousState;
    private PlayerColor currentColor;

    private long speedEffectStart = -1;
    private long healthEffectStart = -1;


    /**
     * COSTRUCTOR
     */
    //-------------------------------------------------------------
    public PlayerRender(EntityConfig entityConfig, PlayerColor PlayerColor) {
        this.entityConfig = entityConfig;
        this.previousState = EntityConfig.PLAYER_DEFAULT_STATE;
        loadAnimations(PlayerColor);
    }
    //-------------------------------------------------------------

    /**
     * Load animations for the player
     */
    //-------------------------------------------------------------
    private void loadAnimations(PlayerColor PlayerColor) {
        currentColor = PlayerColor;
        //Select the sheet image

        BufferedImage sheetImage = resolveSheetImage(PlayerColor);


        // Select the frames from the sheet image
        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackRightFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 2, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackDownFrames = SpriteLoader.getAnimationFrames(sheetImage, 4, 2, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackUpFrames = SpriteLoader.getAnimationFrames(sheetImage, 6, 2, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
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
    //-------------------------------------------------------------
    public void setPlayerColor(PlayerColor playerColor) {
        if (playerColor != currentColor) {
            previousState = EntityConfig.PLAYER_DEFAULT_STATE;
            loadAnimations(playerColor);
        }
    }
    //-------------------------------------------------------------
    private BufferedImage resolveSheetImage(PlayerColor color) {

        switch (color){
            case RED -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Red.png");
            }
            case YELLOW -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Yellow.png");
            }
            case PURPLE -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Purple.png");
            }
            case BLUE -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Blue.png");
            }
        }
        return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Blue.png"); //default BLUE
    }

    //-------------------------------------------------------------
    private BufferedImage[] loadDeathFrames() {
        BufferedImage deadSheet = SpriteLoader.loadSpriteSheet("/res/player/Dead.png");
        BufferedImage[] firstRow = SpriteLoader.getAnimationFrames(deadSheet, 0, 1, 7, EntityConfig.DEATH_FRAME_SIZE, EntityConfig.DEATH_FRAME_SIZE);
        BufferedImage[] secondRow = SpriteLoader.getAnimationFrames(deadSheet, 1, 1, 4, EntityConfig.DEATH_FRAME_SIZE, EntityConfig.DEATH_FRAME_SIZE);

        BufferedImage[] deathFrames = new BufferedImage[10];
        System.arraycopy(firstRow, 1, deathFrames, 0, 6);
        System.arraycopy(secondRow, 0, deathFrames, 6, 4);
        return deathFrames;
    }
    //-------------------------------------------------------------



    /**
     * Change the player animation based on his state and direction
     */
    //-------------------------------------------------------------
    public void update(Player player, double deltaMs) {

        if (player.isSpeedBoosted() && speedEffectStart==-1){
            speedEffectStart = System.currentTimeMillis();
        }

        if (player.isHealthRestored() && healthEffectStart == -1) {
            healthEffectStart = System.currentTimeMillis();
        }
    
        PlayerState currentState = player.getState();
        boolean attackJustStarted = currentState == PlayerState.ATTACKING && previousState != PlayerState.ATTACKING;

        switch (currentState) {

            case IDLE -> animationManager.playAnimation("idle");
            case WALKING -> animationManager.playAnimation("walk");
            case ATTACKING -> {
                //restart the animation
                if (attackJustStarted) {
                    animationManager.getCurrent().reset();
                }

                if (player.getDirection() == Direction.DOWN) {
                    animationManager.playAnimation("attack_down");
                } else if (player.getDirection() == Direction.UP) {
                    animationManager.playAnimation("attack_up");
                } else {
                    animationManager.playAnimation("attack_right");
                }

                if (animationManager.getCurrent().isFinished()) {
                    player.completeAttackAnimation(); //to know when the attack is ended
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
    //-------------------------------------------------------------

    /**
     * Draws the player on the screen
     */
    //-------------------------------------------------------------
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

        // Save original graphics settings
        Composite originalComposite = g2.getComposite();
        Stroke originalStroke = g2.getStroke();

        if (player.isSpeedBoosted()) {
            if (System.currentTimeMillis() - speedEffectStart < EntityConfig.VISUAL_EFFECT_DURATION_MS){
                long blinkTime = System.currentTimeMillis() / 200; // Change color every 200ms
                if (blinkTime%2==0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); // Semi-transparent
                }
            }
            
        }

        if (player.isHealthRestored()){
            if (System.currentTimeMillis() - healthEffectStart < EntityConfig.VISUAL_EFFECT_DURATION_MS){
                long blinkTime = System.currentTimeMillis() / 200; // Change color every 200ms
                if (blinkTime%2==0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); // Semi-transparent
                }
            }
        }

        // Draw the player sprite 
        if (player.getFacing() != Direction.RIGHT) {
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }

        g2.setComposite(originalComposite); // Restore original composite for drawing the player
        
        if (player.isShielded()) {
            double shieldPulse = 1.0 + 0.05 * Math.sin(System.currentTimeMillis() / 200.0);
            int shieldRadius = (int) ((Math.max(width, height) / 2 + 5) * shieldPulse);
            
            // Outer glow
            g2.setColor(new Color(0, 180, 255, 30));
            g2.fillOval(screenX - shieldRadius - 8, screenY - shieldRadius - 8, (shieldRadius + 8) * 2, (shieldRadius + 8) * 2);

            // Shield core
            g2.setColor(new Color(0, 220, 255, 50));
            g2.fillOval( screenX - shieldRadius, screenY - shieldRadius, shieldRadius * 2, shieldRadius * 2);
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(new Color(120, 255, 255, 180));
            g2.drawOval( screenX - shieldRadius, screenY - shieldRadius, shieldRadius * 2, shieldRadius * 2);
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new Color(255, 255, 255, 100));
            g2.drawOval(screenX - shieldRadius + 4, screenY - shieldRadius + 4, (shieldRadius - 4) * 2, (shieldRadius - 4) * 2);
        }

        // Restore original graphics settings before drawing the player
        g2.setStroke(originalStroke);
        g2.setComposite(originalComposite);


    }
    //-------------------------------------------------------------

    /**
     * Debug method to draw the player's solid area.
     */
    //-------------------------------------------------------------
    public void drawSolidArea(Graphics2D g2, Player player, int screenX, int screenY) {
        Rectangle worldSolid = player.getSolidWorldArea();
        int drawX = worldSolid.x - player.getWorldX() + screenX;
        int drawY = worldSolid.y - player.getWorldY() + screenY;

        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, worldSolid.width, worldSolid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, worldSolid.width, worldSolid.height);

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
    //-------------------------------------------------------------

}
