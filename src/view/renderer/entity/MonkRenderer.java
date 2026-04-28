package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.MonkState;
import model.entity.Monk;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;



public class MonkRenderer {

    private AnimationManager animationManager;
    private final EntityConfig entityConfig;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public MonkRenderer(EntityConfig entityConfig) {

        this.entityConfig = entityConfig;
        loadAnimations();
        animationManager.playAnimation("monk_idle");
    }
    //-------------------------------------------------------------

    private void loadAnimations() {
        //TODO carica da file
        BufferedImage idleSheetImage = SpriteLoader.loadSpriteSheet("/res/npc/monk/Idle.png");
        BufferedImage talkSheetImage = SpriteLoader.loadSpriteSheet("/res/npc/monk/Heal.png");
        BufferedImage disappearSheetImage = SpriteLoader.loadSpriteSheet("/res/npc/monk/Heal_Effect.png");


        // Estrazione dei frame (Esempio: riga 0 Idle, riga 1 Talking/Action)
        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(idleSheetImage, 0, 1, 6, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] talkingFrames = SpriteLoader.getAnimationFrames(talkSheetImage, 0, 1, 9, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);
        BufferedImage[] disappearFrames = SpriteLoader.getAnimationFrames(disappearSheetImage, 0, 1, 11, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT);

        animationManager = new AnimationManager();
        animationManager.addAnimation("monk_idle", new Animation(idleFrames, 200, true));
        animationManager.addAnimation("monk_talking", new Animation(talkingFrames, 200, true));
        animationManager.addAnimation("monk_disappear", new Animation(disappearFrames, 150, false));
    }
//TODO make it better
    public void update(Monk monk, double deltaMs) {
        if (monk.getState() == MonkState.DISAPPEARED) return;

        // Scegli quale animazione far girare
        if (monk.getState() == MonkState.IDLE) {
            animationManager.playAnimation("monk_idle");
        } else if (monk.getState() == MonkState.TALKING) {
            animationManager.playAnimation("monk_talking");
        } else if (monk.getState() == MonkState.DISAPPEARING) {
            animationManager.playAnimation("monk_disappear");
        }

        animationManager.update(deltaMs);
        if (monk.getState() == MonkState.DISAPPEARING && animationManager.getCurrent().isFinished()) {
            monk.updateDisappearing();
        }
    }


    public void draw(Graphics2D g2, Monk monk, int screenX, int screenY) {
        if (monk.getState() == MonkState.DISAPPEARED) return;

        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
        int drawX = screenX - entityConfig.SPRITE_WIDTH / 2;
        int drawY = screenY - entityConfig.SPRITE_HEIGHT / 2;
        g2.drawImage(frame, drawX, drawY, entityConfig.SPRITE_WIDTH, entityConfig.SPRITE_HEIGHT, null);
    }
}
