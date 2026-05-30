package model.object;

import java.awt.Rectangle;
import main.CONFIG.ObjConfig;
import main.CONFIG.enu.TreeState;

/**
 * Represents an interactive tree that can be chopped by the player.
 */
public class OBJ_Tree extends GameObject {

    private final ObjConfig objConfig;
    private TreeState state = TreeState.IDLE;
    private int health;
    private double chopTimer = 0;

    public OBJ_Tree(String name, int worldX, int worldY, int layer, int width, int height, Rectangle solidArea, ObjConfig objConfig) {
        super(name, worldX, worldY, width, height, solidArea);
        this.layer = layer;
        this.objConfig = objConfig;
        this.solid = objConfig.TREE_SOLID;
        this.health = objConfig.TREE_HEALTH;
    }

    public void hit() {
        if (state == TreeState.CHOPPED || state == TreeState.CHOPPING) return;

        health--;
        if (health <= 0) {
            state = TreeState.CHOPPING;
            chopTimer = objConfig.CHOP_ANIMATION_DURATION_MS;
        }
    }

    @Override
    public void update(double deltaMs) {
        if (state != TreeState.CHOPPING) return;

        chopTimer -= deltaMs;
        if (chopTimer <= 0) {
            this.solid = false;
            this.state = TreeState.CHOPPED;
        }
    }

    /**
     * Visual shake effect offset during the chopping phase.
     */
    public int getShakeOffsetX() {
        return (state == TreeState.CHOPPING) ? (int) (Math.random() * 5 - 2) : 0;
    }

    public int getShakeOffsetY() {
        return (state == TreeState.CHOPPING) ? (int) (Math.random() * 5 - 2) : 0;
    }

    public TreeState getState() { return state; }
}