package view.renderer.object;

import model.object.GameObject;
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
    private final BufferedImage woodSprite;

    public TreeRenderer(){
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/objects/Tree.png");

        // Extract the tree sprite from the sprite sheet
        BufferedImage[] leavesFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 4, TREE_SPRITE_WIDTH, TREE_SPRITE_HEIGHT);
        
        // Initialize the animation manager and add the idle animation
        animationManager = new AnimationManager();
        animationManager.addAnimation("tree_idle", new Animation(leavesFrames, 1000, true));
                
        // Load the tree sprite
        woodSprite = SpriteLoader.loadSpriteSheet("/res/objects/wood.png");
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
            g2.drawImage(woodSprite, screenX, screenY, tree.getWidth(), tree.getHeight(), null);
        } else {
            BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
            g2.drawImage(frame, screenX, screenY, tree.getWidth(), tree.getHeight(), null);
        }
    }

    
}
