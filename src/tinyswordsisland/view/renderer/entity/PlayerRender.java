package tinyswordsisland.view.renderer.entity;

import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.model.enu.Direction;
import tinyswordsisland.model.enu.PlayerColor;
import tinyswordsisland.model.enu.PlayerState;
import tinyswordsisland.view.ViewEvent;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.view.SpriteLoader;
import tinyswordsisland.view.animation.Animation;
import tinyswordsisland.view.animation.AnimationManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
    private final List<ViewEvent> pendingViewEvents = new ArrayList<>();

    private boolean firstAttackHalf = true;
    private boolean playerRender = true;

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
        BufferedImage shieldSheetImage = resolveSheetImageShield(PlayerColor);


        // Select the frames from the sheet image
        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);

        BufferedImage[] attackRightFrames1 = SpriteLoader.getAnimationFrames(sheetImage, 2, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackDownFrames1 = SpriteLoader.getAnimationFrames(sheetImage, 4, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackUpFrames1 = SpriteLoader.getAnimationFrames(sheetImage, 6, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);

        BufferedImage[] attackRightFrames2 = SpriteLoader.getAnimationFrames(sheetImage, 3, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackDownFrames2 = SpriteLoader.getAnimationFrames(sheetImage, 5, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackUpFrames2 = SpriteLoader.getAnimationFrames(sheetImage, 7, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] deathFrames = loadDeathFrames();

        BufferedImage[] idleFramesShield = SpriteLoader.getAnimationFrames(shieldSheetImage, 0, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] walkFramesShield = SpriteLoader.getAnimationFrames(shieldSheetImage, 1, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackRightFramesShield1 = SpriteLoader.getAnimationFrames(shieldSheetImage, 2, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackDownFramesShield1 = SpriteLoader.getAnimationFrames(shieldSheetImage, 4, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackUpFramesShield1 = SpriteLoader.getAnimationFrames(shieldSheetImage, 6, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);

        BufferedImage[] attackRightFramesShield2 = SpriteLoader.getAnimationFrames(shieldSheetImage, 3, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackDownFramesShield2 = SpriteLoader.getAnimationFrames(shieldSheetImage, 5, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] attackUpFramesShield2 = SpriteLoader.getAnimationFrames(shieldSheetImage, 7, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);

        animationManager = new AnimationManager();
        // frame duration in milliseconds
        animationManager.addAnimation("idle", new Animation(idleFrames, 120, true));
        animationManager.addAnimation("walk", new Animation(walkFrames, 90, true));
        animationManager.addAnimation("attack_right1", new Animation(attackRightFrames1, 60, false));
        animationManager.addAnimation("attack_down1", new Animation(attackDownFrames1, 60, false));
        animationManager.addAnimation("attack_up1", new Animation(attackUpFrames1, 60, false));
        animationManager.addAnimation("attack_right2", new Animation(attackRightFrames2, 60, false));
        animationManager.addAnimation("attack_down2", new Animation(attackDownFrames2, 60, false));
        animationManager.addAnimation("attack_up2", new Animation(attackUpFrames2, 60, false));
        animationManager.addAnimation("death", new Animation(deathFrames, 80, false));

        animationManager.addAnimation("idle_shield", new Animation(idleFramesShield, 120, true));
        animationManager.addAnimation("walk_shield", new Animation(walkFramesShield, 90, true));
        animationManager.addAnimation("attack_right1_shield", new Animation(attackRightFramesShield1, 60, false));
        animationManager.addAnimation("attack_down1_shield", new Animation(attackDownFramesShield1, 60, false));
        animationManager.addAnimation("attack_up1_shield", new Animation(attackUpFramesShield1, 60, false));
        animationManager.addAnimation("attack_right2_shield", new Animation(attackRightFramesShield1, 60, false));
        animationManager.addAnimation("attack_down2_shield", new Animation(attackDownFramesShield1, 60, false));
        animationManager.addAnimation("attack_up2_shield", new Animation(attackUpFramesShield1, 60, false));

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
    private BufferedImage resolveSheetImageShield(PlayerColor color) {

        switch (color){
            case RED -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Red_Shield.png");
            }
            case YELLOW -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Yellow_Shield.png");
            }
            case PURPLE -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Purple_Shield.png");
            }
            case BLUE -> {
                return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Blue_Shield.png");
            }
        }
        return SpriteLoader.loadSpriteSheet("/res/player/Warrior_Blue_Shield.png"); //default BLUE
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
    public void update(IRenderable player, double deltaMs) {

        if (player.isSpeedBoostedRender() && speedEffectStart==-1){
            speedEffectStart = System.currentTimeMillis();
        }

        if (player.isHealthRestoredRender() && healthEffectStart == -1) {
            healthEffectStart = System.currentTimeMillis();
        }

        PlayerState currentState = PlayerState.values()[player.getRenderState()];
        boolean attackJustStarted = currentState == PlayerState.ATTACKING && previousState != PlayerState.ATTACKING;

        if (!player.isShieldedRender()) {
            switch (currentState) {

                case IDLE ->{
                    playerRender = true;
                    animationManager.playAnimation("idle");
                }
                case WALKING -> animationManager.playAnimation("walk");
                case ATTACKING -> {
                    Direction direction = Direction.values()[player.getRenderDirection()];

                    if (attackJustStarted) {
                        String attackName;

                        if (direction == Direction.DOWN) {
                            attackName = firstAttackHalf ? "attack_down1" : "attack_down2";
                        } else if (direction == Direction.UP) {
                            attackName = firstAttackHalf ? "attack_up1" : "attack_up2";
                        } else {
                            attackName = firstAttackHalf ? "attack_right1" : "attack_right2";
                        }

                        animationManager.playAnimation(attackName);
                        animationManager.getCurrent().reset();

                        firstAttackHalf = !firstAttackHalf;
                    }

                    if (animationManager.getCurrent().isFinished()) {
                        pendingViewEvents.add(ViewEvent.PLAYER_ATTACK_ANIMATION_COMPLETED);
                    }
                }
                case DYING -> {
                    animationManager.playAnimation("death");
                    if (animationManager.getCurrent().isFinished()) {
                        pendingViewEvents.add(ViewEvent.PLAYER_DEATH_ANIMATION_COMPLETED);
                    }
                }

                case DEAD -> animationManager.playAnimation("death");
                case WIN -> playerRender = false;
            }
        }else {
            switch (currentState) {

                case IDLE -> animationManager.playAnimation("idle_shield");
                case WALKING -> animationManager.playAnimation("walk_shield");
                case ATTACKING -> {
                    Direction direction = Direction.values()[player.getRenderDirection()];

                    if (attackJustStarted) {
                        String attackName;

                        if (direction == Direction.DOWN) {
                            attackName = firstAttackHalf ? "attack_down1_shield" : "attack_down2_shield";
                        } else if (direction == Direction.UP) {
                            attackName = firstAttackHalf ? "attack_up1_shield" : "attack_up2_shield";
                        } else {
                            attackName = firstAttackHalf ? "attack_right1_shield" : "attack_right2_shield";
                        }

                        animationManager.playAnimation(attackName);
                        animationManager.getCurrent().reset();

                        firstAttackHalf = !firstAttackHalf;
                    }

                    if (animationManager.getCurrent().isFinished()) {
                        pendingViewEvents.add(ViewEvent.PLAYER_ATTACK_ANIMATION_COMPLETED);
                    }
                }
            }
        }


        animationManager.update(deltaMs);
        previousState = currentState;
    }
    public List<ViewEvent> consumeViewEvents() {
        List<ViewEvent> events = new ArrayList<>(pendingViewEvents);
        pendingViewEvents.clear();
        return events;
    }
    //-------------------------------------------------------------

    /**
     * Draws the player on the screen
     */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2, IRenderable player, int screenX, int screenY) {
        if (!playerRender) return;
        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();

        int width = EntityConfig.PLAYER_RENDER_WIDTH;
        int height = EntityConfig.PLAYER_RENDER_HEIGHT;
        int drawX = screenX - width / 2;
        int drawY = screenY - height / 2;

        PlayerState state = PlayerState.values()[player.getRenderState()];
        if (state == PlayerState.DYING || state == PlayerState.DEAD) {
            g2.drawImage(frame, drawX, drawY, width, height, null);
            return;
        }

        // Save original graphics settings
        Composite originalComposite = g2.getComposite();
        Stroke originalStroke = g2.getStroke();

        if (player.isSpeedBoostedRender()) {
            if (System.currentTimeMillis() - speedEffectStart < EntityConfig.VISUAL_EFFECT_DURATION_MS){
                long blinkTime = System.currentTimeMillis() / 200; // Change color every 200ms
                if (blinkTime%2==0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); // Semi-transparent
                }
            }

        }

        if (player.isHealthRestoredRender()){
            if (System.currentTimeMillis() - healthEffectStart < EntityConfig.VISUAL_EFFECT_DURATION_MS){
                long blinkTime = System.currentTimeMillis() / 200; // Change color every 200ms
                if (blinkTime%2==0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); // Semi-transparent
                }
            }
        }

        // Draw the player sprite
        if (!player.isFacingRightRender()) {
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }

        g2.setComposite(originalComposite); // Restore original composite for drawing the player


        // Restore original graphics settings before drawing the player
        g2.setStroke(originalStroke);
        g2.setComposite(originalComposite);


    }
    //-------------------------------------------------------------

    /**
     * Debug method to draw the player's solid area.
     */
    //-------------------------------------------------------------
    public void drawSolidArea(Graphics2D g2, IRenderable player, int screenX, int screenY) {
        Rectangle solid = player.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        if (PlayerState.values()[player.getRenderState()] == PlayerState.ATTACKING) {
            int attackDrawX = player.getAttackAreaX() - player.getWorldX() + screenX;
            int attackDrawY = player.getAttackAreaY() - player.getWorldY() + screenY;

            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillRect(attackDrawX, attackDrawY, player.getAttackAreaWidth(), player.getAttackAreaHeight());
            g2.setColor(Color.RED);
            g2.drawRect(attackDrawX, attackDrawY, player.getAttackAreaWidth(), player.getAttackAreaHeight());
        }
    }
    //-------------------------------------------------------------

}
