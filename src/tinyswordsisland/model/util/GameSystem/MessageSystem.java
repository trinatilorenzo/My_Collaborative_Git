package tinyswordsisland.model.util.GameSystem;

import tinyswordsisland.controller.InputState;
import tinyswordsisland.config.UIConfig;
import tinyswordsisland.model.enu.MonkState;
import tinyswordsisland.model.GameModel;


import java.io.Serial;
import java.io.Serializable;

public final class MessageSystem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String currentDialogue = "";
    private String currentMessage = "";
    private double messageTimer = 0.0;

    public void update(GameModel model, InputState input, double deltaMs) {
        updateDialogue(model, input);
        updateTimedMessage(deltaMs);
    }

    private void updateDialogue(GameModel model, InputState input) {
        if (model.getMonk() == null) {
            currentDialogue = "";
            return;
        }

        if (model.getMonk().getState() == MonkState.IDLE) {
            currentDialogue = "";
        }

        if (model.getMonk().getState() == MonkState.TALKING && currentDialogue.isEmpty()) {
            currentDialogue = model.getMonk().getCurrentDialogue();
            model.getEventDispatcher().notifyDialogueAdvanced();
        }

        if (model.getMonk().getState() == MonkState.TALKING && input.interact()) {
            model.getMonk().nextDialogue();

            if (!model.getMonk().hasFinishedDialogue()) {
                currentDialogue = model.getMonk().getCurrentDialogue();
                model.getEventDispatcher().notifyDialogueAdvanced();
            } else {
                currentDialogue = "";
                model.getMonk().setState(MonkState.DISAPPEARING);
                model.getEventDispatcher().notifyDialogueClosed();
            }
        }
    }

    private void updateTimedMessage(double deltaMs) {
        if (currentMessage == null || currentMessage.isEmpty()) {
            return;
        }

        messageTimer += deltaMs;
        if (messageTimer >= UIConfig.MESSAGE_TIMER_MS) {
            currentMessage = "";
            messageTimer = 0.0;
        }
    }

    public void showMessage(String message) {
        this.currentMessage = message;
        this.messageTimer = 0.0;
    }

    public void clearDialogue() {
        this.currentDialogue = "";
    }

    public void clearMessage() {
        this.currentMessage = "";
        this.messageTimer = 0.0;
    }

    public void clearAll() {
        this.currentDialogue = "";
        this.currentMessage = "";
        this.messageTimer = 0.0;
    }

    public String getCurrentDialogue() {
        return currentDialogue;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public void afterLoad() {
        if (currentDialogue == null) currentDialogue = "";
        if (currentMessage == null) currentMessage = "";
    }
}