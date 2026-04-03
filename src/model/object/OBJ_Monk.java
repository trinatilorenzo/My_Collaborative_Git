package model.object;

import java.awt.Rectangle;
import static main.GameSetting.*;

public class OBJ_Monk extends GameObject {

    public enum MonkState { IDLE, TALKING, DISAPPEARING, DISAPPEARED }

    private MonkState state = MonkState.IDLE;
    private String[] dialogues;
    private int dialogueIndex = 0;
    
    // Raggio di attivazione (usato dal GameModel per chiamare activate())
    public static final int DETECTION_RADIUS = 150;

    public OBJ_Monk(int worldX, int worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.name = "MONK";
        this.solid = false;
        this.width = PLAYER_SPRITE_WIDTH;
        this.height = PLAYER_SPRITE_HEIGHT;
        // Hitbox standard
        this.solidArea = new Rectangle(this.width * 1/4, this.height * 1/4, this.width / 2, this.height / 2);
        this.dialogues = new String[] {
            "Benvenuto nell'isola delle Piccole Spade, giovane eroe.",
            "Da quando i goblin hanno invaso l'isola, la pace è stata spezzata e il tesoro dell'isola è stato rubato.",
            "Recupera il tesoro e riporta l'armonia. Buona fortuna!"
        };
    }

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

    @Override
    /*
    public void interact() {
        if (state == MonkState.DISAPPEARED || state == MonkState.DISAPPEARING) return;

        // Se non è ancora TALKING, forziamo l'attivazione
        if (state == MonkState.IDLE){
            state = MonkState.TALKING;
        };

        if (dialogueIndex < dialogues.length) {
            System.out.println("Monaco: " + dialogues[dialogueIndex]);
            dialogueIndex++;
        } else { // end dialogue
            state = MonkState.DISAPPEARING; 
            this.solid = false;
        }
    }*/

    public void interact() {
        if (state == MonkState.IDLE){
            state = MonkState.TALKING;
        };
    }

    public String getCurrentDialogue() {
        if (dialogueIndex < dialogues.length) {
            return dialogues[dialogueIndex];
        } else {
            return null; // No more dialogues
        }
    }

    public boolean hasFinishedDialogue() {
        return dialogueIndex >= dialogues.length;
    }

    public void advanceDialogue() {
        dialogueIndex++;
    }

    public void updateDisappearing() {
        if (state == MonkState.DISAPPEARING) {
            state = MonkState.DISAPPEARED;
        }
    }

    public void reset() {
        state = MonkState.IDLE;
        dialogueIndex = 0;
    }

    // GETTERS
    public MonkState getState() { return state; }
    public void setState(MonkState state) { this.state = state; }

}