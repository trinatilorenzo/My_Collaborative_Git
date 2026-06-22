package tinyswordsisland.model.entity;

import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.config.enu.MonkState;

import java.awt.Rectangle;

/**
 * The MONK CLASS represents a npc character in the game, extending the base Entity class.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Monk extends Entity {

    //state
    private MonkState state;
    private double disappearElapsedMs;
    //dialogues
    private String[] dialogues;
    private int dialogueIndex;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public Monk(int worldX, int worldY, EntityConfig entityConfig) {
        super(entityConfig);

        initializeDefaultValues(worldX, worldY);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void initializeDefaultValues(int worldX, int worldY) {
        this.state = EntityConfig.MONK_DEFAULT_STATE; // state (idle)
        //position on map
        this.worldX = worldX;
        this.worldY = worldY;
        this.currentLayer = entityConfig.MONK_START_LAYER();
        //solid area
        solidArea = new Rectangle(0, 0, EntityConfig.HITBOX_WIDTH, EntityConfig.HITBOX_HEIGHT);
        //dialogues
        this.dialogues = EntityConfig.MONK_DIALOUGES;
        this.dialogueIndex = 0;
        //state
        this.disappearElapsedMs = 0.0;
    }
    //-------------------------------------------------------------

    /**
     * Update the monk's state
     * called every frame
     */
    //-------------------------------------------------------------
    public void update(Player player, double deltaMs) {

        // make the monk disappear
        if (state == MonkState.DISAPPEARING) {
            disappearElapsedMs += deltaMs;
            if (disappearElapsedMs >= EntityConfig.MONK_DISAPPEAR_DURATION_MS) {
                state = MonkState.DISAPPEARED;
            }
            return;
        }

        // respawn after a cooldown
        if (state == MonkState.DISAPPEARED) {

            return;
        }
        checkPlayerProximity(player);
    }
    //-------------------------------------------------------------

    /**
     * Move the monk to a new point on the map and set up new dialogues.
     */
    //-------------------------------------------------------------
    public void moveToNextLocation(int newX, int newY, String[] nextDialogues) {
        this.worldX = newX;
        this.worldY = newY;
        this.dialogues = nextDialogues;
        this.dialogueIndex = 0;
        this.disappearElapsedMs = 0.0;
        this.state = MonkState.IDLE; 
    }


    /**
     * Checks if the player is within the detection radius
     */
    //-------------------------------------------------------------
    private void checkPlayerProximity(Player player) {
        long distanceX = player.worldX - worldX;
        long distanceY = player.worldY - worldY;
        long distanceSq = (distanceX * distanceX + distanceY * distanceY);
        double radius = entityConfig.MONK_ACTIVATION_RADIUS;
        double radiusSq = radius * radius;
        if (distanceSq < radiusSq) {
            talk();
        } else {
            stopTalking();
        }
    }
    //-------------------------------------------------------------

    //SETTER
    //---------------------------------------------------------------------------
    /**
     * Start the monk talking
     */
    //-------------------------------------------------------------
    public void talk() {
        if (state == MonkState.IDLE) {
            state = MonkState.TALKING;
            disappearElapsedMs = 0.0;
        }
    }
    //-------------------------------------------------------------
    /**
     * Start the monk talking from first time
     */
    //-------------------------------------------------------------
    public void stopTalking() {
        if (state == MonkState.TALKING) {
            state = MonkState.IDLE;
        }
    }
    //-------------------------------------------------------------
    /**
     * monk interacts changing his state from idle to talking
     */
    //-------------------------------------------------------------
    public void interact() {
        if (state == MonkState.IDLE){
            state = MonkState.TALKING;
        }
    }
    //-------------------------------------------------------------
    /**
     *  increment the dialogue index
     */
    //-------------------------------------------------------------
    public void nextDialogue() {
        dialogueIndex++;
    }
    //-------------------------------------------------------------
    /**
     * reset the monk's state to the initial state
     */
    //-------------------------------------------------------------
    public void resetDialogue() {
        state = MonkState.IDLE;
        dialogueIndex = 0;
        disappearElapsedMs = 0.0;
    }
    //-------------------------------------------------------------
    public void setState(MonkState state) {
        this.state = state;
        if (state == MonkState.DISAPPEARING) {
            disappearElapsedMs = 0.0;
        }
    }
    //end seter -----------------------------------------------------------------

    //GETTER
    //---------------------------------------------------------------------------
    /**
     * Is true when the monk reaches the end of the dialogue
     */
    //-------------------------------------------------------------
    public boolean hasFinishedDialogue() {
        return dialogues == null || dialogueIndex >= dialogues.length;
    }
    //-------------------------------------------------------------
    /** Return the text of the current dialogue */
    //-------------------------------------------------------------
    public String getCurrentDialogue() {
        if (dialogues != null && dialogueIndex < dialogues.length) {
            return dialogues[dialogueIndex];
        } else {
            return null; // No more dialogues
        }
    }
    //-------------------------------------------------------------
    public MonkState getState() { return state; }
    // end getter ---------------------------------------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
