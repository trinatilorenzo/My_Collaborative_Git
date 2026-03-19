package view.renderer.object;

import model.object.OBJ_Tree;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static main.GameSetting.*;
/**
 * The TREE RENDERER CLASS is responsible for rendering the visual representation of the tree objects, managing their animations and states.
 */
public class TreeRenderer extends ObjectRender<OBJ_Tree> {

    private final AnimationManager animationManager;
    //private final BufferedImage choppedFrame;

    public TreeRenderer(){
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree1.png");

        // Extract the tree sprite from the sprite sheet
        BufferedImage[] leavesFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 8, TREE_SPRITE_WIDTH, TREE_SPRITE_HEIGHT);
        
        // Initialize the animation manager and add the idle animation
        animationManager = new AnimationManager();
        animationManager.addAnimation("tree_idle", new Animation(leavesFrames, 1000, true));

        // Fallback frame used when the tree is chopped (no dedicated stump sprite available)

        //choppedFrame = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump1.png");
    }

    @Override
    public void update(OBJ_Tree tree, double deltaMs) {
        if (!tree.isChopped()) {
            animationManager.playAnimation("tree_idle");
            animationManager.update(deltaMs);
        }
    }

    @Override
    public void draw(Graphics2D g2, OBJ_Tree tree, int screenX, int screenY) {
        if (tree.isChopped()) {
            //g2.drawImage(choppedFrame, screenX, screenY, tree.getWidth(), tree.getHeight(), null);
        } else {
            BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
            g2.drawImage(frame, screenX, screenY, tree.getWidth(), tree.getHeight(), null);
        }
    }

    
}
