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
    private final BufferedImage choppedFrame;

    public TreeRenderer(){
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree1.png");

        // Extract the tree sprite from the sprite sheet
        BufferedImage[] leavesFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 8, TREE_SPRITE_WIDTH, TREE_SPRITE_HEIGHT);
        
        // Initialize the animation manager and add the idle animation
        animationManager = new AnimationManager();
        animationManager.addAnimation("tree_idle", new Animation(leavesFrames, 1000, true));

        // Fallback frame used when the tree is chopped (no dedicated stump sprite available)

        choppedFrame = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_1.png");
        animationManager.addAnimation("tree_chopped", new Animation(new BufferedImage[]{choppedFrame}, 1000, false));
    }

    /*@Override
    public void update(OBJ_Tree tree, double deltaMs) {
        if (!tree.isChopped()) {
            animationManager.playAnimation("tree_idle");
            animationManager.update(deltaMs);
        } else {
            // Stop animations when the tree is chopped to show the static stump image
            animationManager.playAnimation("tree_chopped");
            animationManager.update(deltaMs);
        }
    }*/
   @Override
    public void update(OBJ_Tree tree, double deltaMs) {
        // Aggiorna sempre l'animazione idle, ma non la usiamo se l'albero è chopped
        animationManager.playAnimation("tree_idle");

        // Se l'albero viene colpito, aggiorna più velocemente
        if (tree.isChopping()) {
            animationManager.update(deltaMs * 2.5); 
        } else {
            animationManager.update(deltaMs); 

        }
        
        tree.updateChop(deltaMs);
    }
    /*
    @Override
    public void draw(Graphics2D g2, OBJ_Tree tree, int screenX, int screenY) {
        if (tree.isChopped()) {
            g2.drawImage(choppedFrame, screenX, screenY, tree.getWidth(), tree.getHeight(), null);
        } else {
            BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
            g2.drawImage(frame, screenX, screenY, tree.getWidth(), tree.getHeight(), null);
        }
    }*/
   @Override
    public void draw(Graphics2D g2, OBJ_Tree tree, int screenX, int screenY) {
        int drawX = screenX + tree.getShakeOffsetX();
        int drawY = screenY + tree.getShakeOffsetY();

        if (tree.isChopped()) {
            // Disegna lo stump
            g2.drawImage(choppedFrame, drawX, drawY, tree.getWidth(), tree.getHeight(), null);
        } else {
            // Disegna il frame corrente
            BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
            g2.drawImage(frame, drawX, drawY, tree.getWidth(), tree.getHeight(), null);
        }
    }

    
}
