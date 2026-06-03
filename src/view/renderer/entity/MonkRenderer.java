package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.MonkState;
import model.entity.EnemyTNT;
import model.entity.Monk;
import model.entity.Player;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * The MONKRENDER CLASS is responsible for rendering the monk entity onto the game screen.
 */
//-------------------------------------------------------------------------------------------------------------------
public class MonkRenderer {

    private AnimationManager animationManager;
    private final EntityConfig entityConfig;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public MonkRenderer(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadAnimations() {

        BufferedImage idleSheetImage = SpriteLoader.loadSpriteSheet("/res/npc/monk/Idle.png");
        BufferedImage talkSheetImage = SpriteLoader.loadSpriteSheet("/res/npc/monk/Heal.png");
        BufferedImage disappearSheetImage = SpriteLoader.loadSpriteSheet("/res/npc/monk/Heal_Effect.png");


        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(idleSheetImage, 0, 1, 6, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] talkingFrames = SpriteLoader.getAnimationFrames(talkSheetImage, 0, 1, 9, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);
        BufferedImage[] disappearFrames = SpriteLoader.getAnimationFrames(disappearSheetImage, 0, 1, 11, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT);

        animationManager = new AnimationManager();
        animationManager.addAnimation("monk_idle", new Animation(idleFrames, 200, true));
        animationManager.addAnimation("monk_talking", new Animation(talkingFrames, 200, true));
        animationManager.addAnimation("monk_disappear", new Animation(disappearFrames, 150, false));
    }
    //-------------------------------------------------------------

    /**
     * Change the monk animation based on his state
     */
    //-------------------------------------------------------------
    public void update(Monk monk, double deltaMs) {
        if (monk.getState() == MonkState.DISAPPEARED) return;

        switch (monk.getState()) {
            case IDLE -> animationManager.playAnimation("monk_idle");
            case TALKING -> animationManager.playAnimation("monk_talking");
            case DISAPPEARING -> animationManager.playAnimation("monk_disappear");
        }

        animationManager.update(deltaMs);
    }
    //-------------------------------------------------------------

    /**
     * Draws the monk on the screen
     */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2, Monk monk, int screenX, int screenY) {
        if (monk.getState() == MonkState.DISAPPEARED) return;

        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
        int drawX = screenX - EntityConfig.SPRITE_WIDTH / 2;
        int drawY = screenY - EntityConfig.SPRITE_HEIGHT / 2;
        g2.drawImage(frame, drawX, drawY, EntityConfig.SPRITE_WIDTH, EntityConfig.SPRITE_HEIGHT, null);
    }
    //-------------------------------------------------------------

    /**
     * Debug method to draw the monk's solid area and interaction radius.
     */
    //-------------------------------------------------------------
    public void drawSolidArea(Graphics2D g2, Monk monk, int screenX, int screenY) {

        Rectangle solid = monk.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        // Detection Area
        g2.setColor(new Color(0, 84, 255, 80));
        int r = entityConfig.MONK_ACTIVATION_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.BLUE);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);
    }
    //-------------------------------------------------------------

}
