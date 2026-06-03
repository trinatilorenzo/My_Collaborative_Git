package view.renderer;

import main.CONFIG.ScreenConfig;
import model.object.GameObject;
import model.object.OBJ_Tree;
import model.entity.Player;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.CONFIG.ObjConfig.*;
import static main.CONFIG.enu.TreeState.CHOPPED;
import static main.CONFIG.enu.TreeState.CHOPPING;

/**
 * Renderer for all map objects: animated trees and static buildings.
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameObjectRenderer {

    private static final double CHOPPING_ANIMATION_SPEED = 2.5;

    // One animation manager for each tree in the map.
    private final Map<OBJ_Tree, AnimationManager> treeAnimations = new HashMap<>();

    // Tree sprites
    private BufferedImage[] tree01Frames;
    private BufferedImage[] tree02Frames;
    private BufferedImage[] tree03Frames;
    private BufferedImage stump01;
    private BufferedImage stump02;
    private BufferedImage stump03;

    // Building sprites
    private BufferedImage castle;
    private BufferedImage tower;
    private BufferedImage goldMine;
    private BufferedImage goblinHouse;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameObjectRenderer() {
        loadTreeSprites();
        loadBuildingSprites();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadTreeSprites() {
        BufferedImage tree01Sheet = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree1.png");
        BufferedImage tree02Sheet = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree2.png");
        BufferedImage tree03Sheet = SpriteLoader.loadSpriteSheet("/res/object/tree/Tree3.png");

        tree01Frames = SpriteLoader.getAnimationFrames(tree01Sheet, 0, 1, 8, TREE_01_WIDTH, TREE_01_HEIGHT);
        tree02Frames = SpriteLoader.getAnimationFrames(tree02Sheet, 0, 1, 8, TREE_02_WIDTH, TREE_02_HEIGHT);
        tree03Frames = SpriteLoader.getAnimationFrames(tree03Sheet, 0, 1, 8, TREE_03_WIDTH, TREE_03_HEIGHT);

        stump01 = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_1.png");
        stump02 = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_2.png");
        stump03 = SpriteLoader.loadSpriteSheet("/res/object/tree/Stump_3.png");
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadBuildingSprites() {
        castle = SpriteLoader.loadSpriteSheet("/res/object/buildings/Castle_Blue.png");
        tower = SpriteLoader.loadSpriteSheet("/res/object/buildings/Tower_Blue.png");
        goldMine = SpriteLoader.loadSpriteSheet("/res/object/buildings/GoldMine_Active.png");
        goblinHouse = SpriteLoader.loadSpriteSheet("/res/object/buildings/Goblin_House.png");
    }
    //-------------------------------------------------------------


    /**
     * Updates animated objects.
     */
    //-------------------------------------------------------------
    public void update(GameObject obj, double deltaMs) {
        if (obj instanceof OBJ_Tree tree) {
            updateTree(tree, deltaMs);
        }
    }
    //-------------------------------------------------------------
    private void updateTree(OBJ_Tree tree, double deltaMs) {
        if (tree.isRemoved()) {
            treeAnimations.remove(tree);
            return;
        }

        AnimationManager animation = getTreeAnimation(tree);

        if (tree.getState() == CHOPPING) {
            animation.update(deltaMs * CHOPPING_ANIMATION_SPEED);
        } else {
            animation.update(deltaMs);
        }
    }
    //-------------------------------------------------------------

    /**
     * Draws a map object.
     */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2, GameObject obj, int screenX, int screenY) {
        if (obj instanceof OBJ_Tree tree) {
            drawTree(g2, tree, screenX, screenY);
        } else {
            drawBuilding(g2, obj, screenX, screenY);
        }
    }
    //-------------------------------------------------------------
    private void drawTree(Graphics2D g2, OBJ_Tree tree, int screenX, int screenY) {
        int drawX = screenX;
        int drawY = screenY;

        if (tree.getState() == CHOPPING) {
            drawX += (int) (Math.random() * 5 - 2);
            drawY += (int) (Math.random() * 5 - 2);
        }

        BufferedImage sprite;
        if (tree.getState() == CHOPPED) {
            sprite = getTreeStump(tree.getName());
        } else {
            sprite = getTreeAnimation(tree).getCurrent().getCurrentFrame();
        }

        g2.drawImage(sprite, drawX, drawY, tree.getWidth(), tree.getHeight(), null);
    }
    //-------------------------------------------------------------
    private void drawBuilding(Graphics2D g2, GameObject obj, int screenX, int screenY) {
        BufferedImage sprite = getBuildingSprite(obj.getName());
        g2.drawImage(sprite, screenX, screenY, obj.getWidth(), obj.getHeight(), null);
    }
    //-------------------------------------------------------------

    /**
     * Draws object hitboxes in debug mode.
     */
    //-------------------------------------------------------------
    public void drawDebugSolidAreas(Graphics2D g2, List<GameObject> objects, Player player, ScreenConfig screenCfg) {

        int pScreenX = screenCfg.SCREEN_WIDTH() / 2 - (screenCfg.TILE_SIZE() / 2);
        int pScreenY = screenCfg.SCREEN_HEIGHT() / 2 - (screenCfg.TILE_SIZE() / 2);

        Color previousColor = g2.getColor();
        Stroke previousStroke = g2.getStroke();

        g2.setStroke(new BasicStroke(2));

        for (GameObject object : objects) {
            if (object == null || object.isRemoved() || object.getSolidArea() == null) {
                continue;
            }

            Rectangle solidArea = object.getSolidArea();
            int screenX = object.getWorldX() + solidArea.x - player.getWorldX() + pScreenX;
            int screenY = object.getWorldY() + solidArea.y - player.getWorldY() + pScreenY;

            if (screenX + solidArea.width < 0 || screenX > screenCfg.SCREEN_WIDTH() ||
                    screenY + solidArea.height < 0 || screenY > screenCfg.SCREEN_HEIGHT()) {
                continue;
            }

            boolean sameLayer = object.getLayer() == player.getCurrentLayer();
            g2.setColor(sameLayer ? new Color(255, 40, 40, 90) : new Color(255, 180, 0, 55));
            g2.fillRect(screenX, screenY, solidArea.width, solidArea.height);

            g2.setColor(sameLayer ? new Color(255, 40, 40, 220) : new Color(255, 180, 0, 160));
            g2.drawRect(screenX, screenY, solidArea.width, solidArea.height);
        }

        g2.setStroke(previousStroke);
        g2.setColor(previousColor);
    }
    //-------------------------------------------------------------

    /**
     * UTILITY METHOD
     */
    //-------------------------------------------------------------
    private String normalize(String name) {
        if (name == null) {
            return "";
        }

        return name.trim().toLowerCase();
    }
    //-------------------------------------------------------------

    //GETTERS
    //-------------------------------------------------------------
    private AnimationManager getTreeAnimation(OBJ_Tree tree) {
        AnimationManager animation = treeAnimations.get(tree);

        if (animation == null) {
            animation = new AnimationManager();
            animation.addAnimation("idle", new Animation(getTreeFrames(tree.getName()), TREE_IDLE_FRAME_MS, true));
            animation.playAnimation("idle");
            treeAnimations.put(tree, animation);
        }

        return animation;
    }
    //-------------------------------------------------------------
    private BufferedImage[] getTreeFrames(String name) {
        String normalizedName = normalize(name);

        return switch (normalizedName) {
            case "tree_01", "trees_01" -> tree01Frames;
            case "tree_02", "trees_02" -> tree02Frames;
            default -> tree03Frames;
        };
    }
    //-------------------------------------------------------------
    private BufferedImage getTreeStump(String name) {
        String normalizedName = normalize(name);

        return switch (normalizedName) {
            case "tree_01", "trees_01" -> stump01;
            case "tree_02", "trees_02" -> stump02;
            default -> stump03;
        };
    }
    //-------------------------------------------------------------
    private BufferedImage getBuildingSprite(String name) {
        String normalizedName = normalize(name);

        return switch (normalizedName) {
            case "tower" -> tower;
            case "goldmine" -> goldMine;
            case "goblin_home", "goblin_house" -> goblinHouse;
            default -> castle;
        };
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
