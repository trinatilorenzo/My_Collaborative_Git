package model.object;

import static main.GameSetting.*;
import java.awt.Rectangle;
/**
 * The OBJ_TREE CLASS represents a tree object in the game world, which can be interacted with by the player (e.g., chopped down for resources).
 * It extends the base GameObject class and includes properties specific to trees.
 */

public class OBJ_Tree extends GameObject {

    private boolean chopped = false;
    private int health = TREE_HEALTH;
    private static final int width = TREE_SPRITE_WIDTH;
    private static final int height = TREE_SPRITE_HEIGHT; 

    // COSTRUCTOR
    //---------------------------------------------------------------------------------------------
    public OBJ_Tree(int worldX, int worldY) {
        // Qua gestisco posizione, stato e interazione con il player, ma non la logica di disegno che è gestita dalla view 
        this.worldX = worldX;
        this.worldY = worldY;
        this.name = "TREE";
        this.solid = true;

        // hitbox
        this.solidArea = new Rectangle(TREE_SPRITE_WIDTH/2 - (TREE_HITBOX_WIDTH/2), 190, TREE_HITBOX_WIDTH, TREE_HITBOX_HEIGHT);
        this.solid = true;
    }

    /**
     * Called when the player hits the tree.
     */
    public Item hit() {

        if(chopped) return null; // If already chopped, no further interaction
        health--;
        if(health <= 0){
            chopped = true;
            solid = false;
            name = "TREE_CUT";
            return new Item("WOOD", worldX, worldY, 1);
        }

        return null;
    }

    public boolean isChopped() {
        return chopped;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void interact(){
        // This method can be called by the player when interacting with the tree (e.g., pressing an action key while near it)
        hit();
    }

}
