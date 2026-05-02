package view.renderer.object;

import model.object.OBJ_Tree;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static main.CONFIG.ObjConfig.*;
import static main.CONFIG.enu.TreeState.*;

public class TreeRenderer extends ObjectRender<OBJ_Tree> {

    private final Map<OBJ_Tree, AnimationManager> managerByTree = new HashMap<>();

    private final BufferedImage[] leavesFrames01;
    private final BufferedImage[] leavesFrames02;
    private final BufferedImage[] leavesFrames03;

    private final BufferedImage choppedFrame01;
    private final BufferedImage choppedFrame02;
    private final BufferedImage choppedFrame03;

    public TreeRenderer() {
        BufferedImage tree01Sheet = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree1.png");
        BufferedImage tree02Sheet = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree2.png");
        BufferedImage tree03Sheet = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree3.png");

        leavesFrames01 = SpriteLoader.getAnimationFrames(tree01Sheet, 0, 1, 8, TREE_01_WIDTH, TREE_01_HEIGHT);
        leavesFrames02 = SpriteLoader.getAnimationFrames(tree02Sheet, 0, 1, 8, TREE_02_WIDTH, TREE_02_HEIGHT);
        leavesFrames03 = SpriteLoader.getAnimationFrames(tree03Sheet, 0, 1, 8, TREE_03_WIDTH, TREE_03_HEIGHT);

        choppedFrame01 = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_1.png");
        choppedFrame02 = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_2.png");
        choppedFrame03 = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_3.png");
    }

    private BufferedImage[] getLeavesFramesByName(String name) {
        if (name == null) return leavesFrames03;
        return switch (name.trim().toUpperCase()) {
            case "TREE_01", "TREES_01" -> leavesFrames01;
            case "TREE_02", "TREES_02" -> leavesFrames02;
            case "TREE_03", "TREES_03" -> leavesFrames03;
            default -> leavesFrames03;
        };
    }

    private BufferedImage getChoppedFrameByName(String name) {
        if (name == null) return choppedFrame03;
        return switch (name.trim().toUpperCase()) {
            case "TREE_01", "TREES_01" -> choppedFrame01;
            case "TREE_02", "TREES_02" -> choppedFrame02;
            case "TREE_03", "TREES_03" -> choppedFrame03;
            default -> choppedFrame03;
        };
    }

    private AnimationManager getManager(OBJ_Tree tree) {
        return managerByTree.computeIfAbsent(tree, t -> {
            AnimationManager manager = new AnimationManager();

            BufferedImage[] leavesFrames = getLeavesFramesByName(t.getName());
            BufferedImage choppedFrame = getChoppedFrameByName(t.getName());

            manager.addAnimation("tree_idle", new Animation(leavesFrames, TREE_IDLE_FRAME_MS, true));
            manager.addAnimation("tree_chopped", new Animation(new BufferedImage[]{choppedFrame}, 1000, false));

            return manager;
        });
    }

    @Override
    public void update(OBJ_Tree tree, double deltaMs) {
        AnimationManager animationManager = getManager(tree);
        animationManager.playAnimation("tree_idle");

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
                BufferedImage choppedFrame = getChoppedFrameByName(tree.getName());
                g2.drawImage(choppedFrame, drawX, drawY, tree.getWidth(), tree.getHeight(), null);
            }

            default -> {
                BufferedImage frame = animationManager.getCurrent().getCurrentFrame();
                g2.drawImage(frame, drawX, drawY, tree.getWidth(), tree.getHeight(), null);
            }
        }
    }
}
