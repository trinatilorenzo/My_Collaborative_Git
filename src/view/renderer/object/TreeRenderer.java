package view.renderer.object;

import model.object.OBJ_Tree;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static main.CONFIG.EntityConfig.*;
import static main.CONFIG.ObjConfig.*;
import static main.CONFIG.enu.TreeState.*;
/**
 * The TREE RENDERER CLASS is responsible for rendering the visual representation of the tree objects, managing their animations and states.
 */
public class TreeRenderer extends ObjectRender<OBJ_Tree> {

    private final Map<OBJ_Tree, AnimationManager> managerByTree = new HashMap<>();
    private final BufferedImage[] leavesFrames;
    private final BufferedImage choppedFrame;

    // TODO caricare le informazioni dal file
    public TreeRenderer(){
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree1.png");

        // Extract the tree sprite from the sprite sheet
        leavesFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 8, TREE_03_WIDTH, TREE_03_HEIGHT);
        
        // Fallback frame used when the tree is chopped
        choppedFrame = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_1.png");
    }

    private AnimationManager getManager(OBJ_Tree tree) {
        return managerByTree.computeIfAbsent(tree, t -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("tree_idle", new Animation(leavesFrames, 100, true));
            manager.addAnimation("tree_chopped", new Animation(new BufferedImage[]{choppedFrame}, 1000, false));
            return manager;
        });
    }

   @Override
    public void update(OBJ_Tree tree, double deltaMs) {
        AnimationManager animationManager = getManager(tree);
        animationManager.playAnimation("tree_idle");
   
        // Se l'albero viene colpito, aggiorna più velocemente
        if (tree.getState() == CHOPPING) {
            animationManager.update(deltaMs * 2.5); 
        } else {
            animationManager.update(deltaMs); 

        }
        
        tree.update(deltaMs);

    }

   @Override
    public void draw(Graphics2D g2, OBJ_Tree tree, int screenX, int screenY) {
        AnimationManager animationManager = getManager(tree);

        int drawX = screenX + tree.getShakeOffsetX();
        int drawY = screenY + tree.getShakeOffsetY();

        switch (tree.getState()) {

            case CHOPPED -> {
                g2.drawImage(choppedFrame, drawX, drawY, tree.getWidth(), tree.getHeight(), null);
            }

            default -> {
                BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
                g2.drawImage(frame, drawX, drawY, tree.getWidth(), tree.getHeight(), null);
            }
        }
    }

    
}
