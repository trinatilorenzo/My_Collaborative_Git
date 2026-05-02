package model.object;
import main.CONFIG.ObjConfig;
import main.CONFIG.enu.TreeState;
import java.awt.Rectangle;

/**
 * The OBJ_TREE CLASS represents a tree object in the game world, which can be interacted with by the player (e.g., chopped down for resources).
 * It extends the base GameObject class and includes properties specific to trees.
 */
//TODO usare la stessa sintassi dei commenti
    //TODO controlare funzionameto metodi + risoluzione bug del hit

public class OBJ_Tree extends GameObject {
    private ObjConfig objConfig;

    private double chopTimer = 0; // Timer to track chopping animation progress

    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;

    private TreeState state = TreeState.IDLE;

    private int health = objConfig.TREE_HEALTH;
    // COSTRUCTOR
    //---------------------------------------------------------------------------------------------
    public OBJ_Tree(String name, int worldX, int worldY, int layer, int width, int height, Rectangle solidArea, ObjConfig objConfig) {
        this.objConfig = objConfig;
        this.worldX = worldX;
        this.worldY = worldY;
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.name = name;

        // hitbox
        this.solidArea = solidArea;
        this.solid = objConfig.TREE_SOLID;
    }

    @Override
    public void interact(){
        // This method can be called by the player when interacting with the tree (e.g., pressing an action key while near it)
        hit();
    }

    /**
     * Called when the player hits the tree.
     */
    public void hit() {

        if(state == TreeState.CHOPPED || state == TreeState.CHOPPING) return; // If already chopped, no further interaction
        health -= 1;
        System.out.println("Remaining tree health: "+ health);
        if(health <= 0) { // If health is depleted, start chopping animation
            startChopping();
        }
        return;
    }

    public void startChopping() {
        state = TreeState.CHOPPING;
        chopTimer = objConfig.CHOP_ANIMATION_DURATION_MS;
        return;
    }
    
    public void update(double deltaMs) { 

        switch(state) {
            case IDLE:
                // No update needed in idle state
                break;
            case CHOPPING:
                chopTimer -= deltaMs;

                // Shake effect during chopping
                shakeOffsetX = (int)(Math.random() * 5 - 2);
                shakeOffsetY = (int)(Math.random() * 5 - 2);

                if(chopTimer <= 0) {
                    shakeOffsetX = 0;
                    shakeOffsetY = 0;
                    solid = false;
                    state = TreeState.CHOPPED;
                }
                System.out.println(state);
                break; 

            case CHOPPED:
                // No update needed in chopped state
                break;
        }
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
    public TreeState getState() {
        return state;
    }

}
