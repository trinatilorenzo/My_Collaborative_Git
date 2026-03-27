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

    private boolean chopping = false; // Flag to indicate if the tree is currently being chopped (for animation purposes)
    private static final double CHOP_ANIMATION_DURATION = 500; // ms
    private double chopTimer = 0; // Timer to track chopping animation progress 
    private boolean readyToDrop = false; // Flag to indicate if the tree is ready to drop the item after being chopped
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;
    // COSTRUCTOR
    //---------------------------------------------------------------------------------------------
    public OBJ_Tree(int worldX, int worldY) {
        // Qua gestisco posizione, stato e interazione con il player, ma non la logica di disegno che è gestita dalla view 
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = TREE_SPRITE_WIDTH;
        this.height = TREE_SPRITE_HEIGHT;
        this.name = "TREE";
        this.solid = true;

        // hitbox
        this.solidArea = new Rectangle(TREE_SPRITE_WIDTH/2 - (TREE_HITBOX_WIDTH/2), 195, TREE_HITBOX_WIDTH, TREE_HITBOX_HEIGHT);
        this.solid = true;
    }

    /**
     * Called when the player hits the tree.
     */
    public Item hit() {

        if(chopped || chopping) return null; // If already chopped, no further interaction
        health--;
        System.out.println("Tree hit! Remaining health: " + health);
        if(health <= 0){
            startChopping();
            return new Item("WOOD", worldX, worldY, 1);
        }
        return null;
    }

    public void startChopping() {
        this.chopping = true;
        chopTimer = CHOP_ANIMATION_DURATION;
    }
    public boolean isChopped() {
        return chopped;
    }

    public boolean isChopping() {
        return chopping;
    }

    public void updateChop(double deltaMs) {
        if(chopping) {
            chopTimer -= deltaMs;

            // Shake semplice: alterna offset tra -2 e +2
            shakeOffsetX = (int)(Math.random() * 5 - 2);
            shakeOffsetY = (int)(Math.random() * 5 - 2);

            if(chopTimer <= 0) {
                chopping = false;
                chopped = true; // ora l'albero è effettivamente tagliato
                readyToDrop = true;
                shakeOffsetX = 0;
                shakeOffsetY = 0;
                solid = false;
                name = "TREE_CUT";
            }
        }
    }
    @Override
    public void interact(){
        // This method can be called by the player when interacting with the tree (e.g., pressing an action key while near it)
        hit();
    }

    public Rectangle getSolidWorldArea() { //called by game model to check attack collision with the tree, since the solidArea is relative to the object position we need to get the world coordinates of the solid area
        return new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
    }

    public int getShakeOffsetX() {
        return shakeOffsetX;
    }
    public int getShakeOffsetY() {
        return shakeOffsetY;
    }
}
