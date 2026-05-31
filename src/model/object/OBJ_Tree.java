package model.object;
import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.TreeState;
import java.awt.Rectangle;

/**
 * The OBJ_TREE CLASS represents a tree object in the game world,
 * it extends the base GameObject class and includes properties specific to trees.
 */
//-------------------------------------------------------------------------------------------------------------------
    //TODO controlare funzionameto metodi + risoluzione bug del hit

public class OBJ_Tree extends GameObject {

    private int health;
    private TreeState state;

    //
    private double chopTimer = 0; // Timer to track chopping progress

    /**
     * COSTRUCTOR
     */
    //-------------------------------------------------------------
    public OBJ_Tree(ObjConfig objConfig, String name, SpawnPoint spawnPoint, int width, int height, Rectangle solidArea) {
        super(objConfig, name, spawnPoint, width, height, solidArea, ObjConfig.TREE_SOLID);

        this.health = ObjConfig.TREE_HEALTH;
        this.state = TreeState.IDLE;
    }
    //-------------------------------------------------------------


    /**
     * Update the tree's state
     * * called every frame *
     */
    //-------------------------------------------------------------
    @Override
    public void update(double deltaMs) {
        // No update needed in other states
        if (state == TreeState.CHOPPING) {
            chopTimer -= deltaMs;

            if(chopTimer <= 0) {
                if(health <= 0){
                    solid = false;
                    state = TreeState.CHOPPED;
                } else {
                    this.state = TreeState.IDLE;
                }
            }
        }
    }
    //-------------------------------------------------------------


    //-------------------------------------------------------------
    @Override
    public void interact(){
        // The player can call this method when interacting with the tree
        hit();
    }
    //-------------------------------------------------------------


    /**
     * Called when the player hits the tree.
     */
    //-------------------------------------------------------------
    private void hit() {
        if(state == TreeState.CHOPPED || state == TreeState.CHOPPING){
            return; // If already chopped, no further interaction
        }

        health --;
        startChopping();

    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void startChopping() {
        state = TreeState.CHOPPING;
        chopTimer = ObjConfig.CHOP_ANIMATION_DURATION_MS;
    }
    //-------------------------------------------------------------



    //GETTER
    //-------------------------------------------------------------
    public TreeState getState() {
        return state;
    }
    public Rectangle getSolidWorldArea() { //called by game model to check attack collision with the tree, since the solidArea is relative to the object position we need to get the world coordinates of the solid area

        return new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);

    }
    //-------------------------------------------------------------

    //SETTER
    //-------------------------------------------------------------

    //-------------------------------------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
