package view.renderer.object;

import main.CONFIG.PlayerConfig;
import model.object.OBJ_Monk;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;



public class MonkRenderer extends ObjectRender<OBJ_Monk> {

    private final AnimationManager animationManager;

    public MonkRenderer(PlayerConfig playerCnfg) {
        BufferedImage idleSheetImage = SpriteLoader.loadSpriteSheet("/res/object/monk/Idle.png");
        BufferedImage talkSheetImage = SpriteLoader.loadSpriteSheet("/res/object/monk/Heal.png");
        BufferedImage disappearSheetImage = SpriteLoader.loadSpriteSheet("/res/object/monk/Heal_effect.png");


        // Estrazione dei frame (Esempio: riga 0 Idle, riga 1 Talking/Action)
        BufferedImage[] idleFrames = SpriteLoader.getAnimationFrames(idleSheetImage, 0, 1, 6, playerCnfg.SPRITE_WIDTH, playerCnfg.SPRITE_HEIGHT);
        BufferedImage[] talkingFrames = SpriteLoader.getAnimationFrames(talkSheetImage, 0, 1, 10, playerCnfg.SPRITE_WIDTH, playerCnfg.SPRITE_HEIGHT);
        BufferedImage[] disappearFrames = SpriteLoader.getAnimationFrames(disappearSheetImage, 0, 1, 11, playerCnfg.SPRITE_WIDTH, playerCnfg.SPRITE_HEIGHT);

        animationManager = new AnimationManager();
        animationManager.addAnimation("monk_idle", new Animation(idleFrames, 200, true));
        animationManager.addAnimation("monk_talking", new Animation(talkingFrames, 200, false));
        animationManager.addAnimation("monk_disappear", new Animation(disappearFrames, 150, false));
    }

    @Override
    public void update(OBJ_Monk monk, double deltaMs) {
        if (monk.getState() == OBJ_Monk.MonkState.DISAPPEARED) return;

        // Scegli quale animazione far girare
        if (monk.getState() == OBJ_Monk.MonkState.IDLE) {
            animationManager.playAnimation("monk_idle");
        } else if (monk.getState() == OBJ_Monk.MonkState.TALKING) {
            animationManager.playAnimation("monk_talking");
        } else if (monk.getState() == OBJ_Monk.MonkState.DISAPPEARING) {
            animationManager.playAnimation("monk_disappear");
        }

        animationManager.update(deltaMs);
        if (monk.getState() == OBJ_Monk.MonkState.DISAPPEARING && animationManager.getCurrent().isFinished()) {
            monk.updateDisappearing();
        }
    }

    @Override
    public void draw(Graphics2D g2, OBJ_Monk monk, int screenX, int screenY) {
        // Se è scomparso, non disegniamo nulla
        if (monk.getState() == OBJ_Monk.MonkState.DISAPPEARED) return;

        BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
        
        // Disegniamo il frame
        g2.drawImage(frame, screenX, screenY, monk.getWidth(), monk.getHeight(), null);
    }
}