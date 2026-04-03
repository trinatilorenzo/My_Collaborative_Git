package model.entity;

import main.CONFIG.EntityConfig;
import main.ENUM.MonkState;

import java.awt.Rectangle;

public class Monk extends Entity {

    //TODO non deve essere un oggetto ma un entity

    private EntityConfig entityConfig;
    private MonkState state;
    private String[] dialogues;
    private int dialogueIndex = 0;


    // COSTRUCTOR
    //-------------------------------------------------------------
    public Monk(int worldX, int worldY, EntityConfig entityConfig) {
        this.entityConfig = entityConfig;

        this.name = entityConfig.MONK_TAG;
        this.state = entityConfig.MONK_DEFAULT_STATE;

        this.worldX = worldX;
        this.worldY = worldY;

        solidArea = new Rectangle((entityConfig.SPRITE_WIDTH / 2) - (entityConfig.HITBOX_WIDTH/2),
                (entityConfig.SPRITE_HEIGHT / 2) ,
                entityConfig.HITBOX_WIDTH,
                entityConfig.HITBOX_HEIGHT);

        this.dialogues = entityConfig.MONK_DIALOUGES;

    }
    //-------------------------------------------------------------


    //-------------------------------------------------------------
    public void activate() {
        if (state == MonkState.IDLE) {
            state = MonkState.TALKING;

            // First dialogue now
            if (dialogueIndex == 0 && dialogueIndex < dialogues.length) {
                System.out.println("Monaco: " + dialogues[dialogueIndex]);
                dialogueIndex++;
            }
        }
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void interact() {
        if (state == MonkState.IDLE){
            state = MonkState.TALKING;
        };
    }
    //-------------------------------------------------------------


    //-------------------------------------------------------------
    public boolean hasFinishedDialogue() {
        return dialogueIndex >= dialogues.length;
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    public void advanceDialogue() {
        dialogueIndex++;
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void updateDisappearing() {
        if (state == MonkState.DISAPPEARING) {
            state = MonkState.DISAPPEARED;
        }
    }
    //-------------------------------------------------------------




    //GETTER
    //-------------------------------------------------------------
    public String getCurrentDialogue() {
        if (dialogueIndex < dialogues.length) {
            return dialogues[dialogueIndex];
        } else {
            return null; // No more dialogues
        }
    }
    public MonkState getState() { return state; }
    public void setState(MonkState state) { this.state = state; }
    //-------------------------------------------------------------

    //SETTER
    //-------------------------------------------------------------
    public void resetDialogue() {
        state = MonkState.IDLE;
        dialogueIndex = 0;
    }
    //-------------------------------------------------------------
}