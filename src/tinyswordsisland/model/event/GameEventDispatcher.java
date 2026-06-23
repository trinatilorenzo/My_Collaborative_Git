package tinyswordsisland.model.event;

import tinyswordsisland.controller.IGameListener;

import java.util.ArrayList;
import java.util.List;

public class GameEventDispatcher {

    private transient List<IGameListener> listeners = new ArrayList<>();

    public void addListener(IGameListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void resetListeners() {
        this.listeners = new ArrayList<>();
    }

    /**
     * event emitter
     */
    //-------------------------------------------------------------
    public void notifyPlayerAttackStart() {
        if (listeners != null) for (IGameListener l : listeners) l.onPlayerAttackStart();
    }
    public void notifyPlayerAttackStop() {
        if (listeners != null) for (IGameListener l : listeners) l.onPlayerAttackStop();
    }
    public void notifyPlayerWalkStart() {
        if (listeners != null) for (IGameListener l : listeners) l.onPlayerWalkStart();
    }
    public void notifyPlayerWalkStop() {
        if (listeners != null) for (IGameListener l : listeners) l.onPlayerWalkStop();
    }
    public void notifyPlayerDamaged(int currentLife, int maxLife) {
        if (listeners != null) for (IGameListener l : listeners) l.onPlayerDamaged(currentLife, maxLife);
    }
    public void notifyEnemyHit() {
        if (listeners != null) for (IGameListener l : listeners) l.onEnemyHit();
    }
    public void notifyEnemyDefeated() {
        if (listeners != null) for (IGameListener l : listeners) l.onEnemyDefeated();
    }
    public void notifyTreeHit() {
        if (listeners != null) for (IGameListener l : listeners) l.onTreeHit();
    }
    public void notifyTreeDestroyed() {
        if (listeners != null) for (IGameListener l : listeners) l.onTreeDestroyed();
    }
    public void notifyStairsUnlocked() {
        if (listeners != null) for (IGameListener l : listeners) l.onStairsUnlocked();
    }
    public void notifyStairsLocked() {
        if (listeners != null) for (IGameListener l : listeners) l.onStairsLocked();
    }
    public void notifyTntTriggered() {
        if (listeners != null) for (IGameListener l : listeners) l.onTntTriggered();
    }
    public void notifyTntExploded() {
        if (listeners != null) for (IGameListener l : listeners) l.onTntExploded();
    }
    public void notifyProjectileLaunched() {
        if (listeners != null) for (IGameListener l : listeners) l.onProjectileLaunched();
    }
    public void notifyProjectileExploded() {
        if (listeners != null) for (IGameListener l : listeners) l.onProjectileExploded();
    }
    public void notifyLevelUp() {
        if (listeners != null) for (IGameListener l : listeners) l.onLevelUp();
    }
    public void notifyPowerUpCollected() {
        if (listeners != null) for (IGameListener l : listeners) l.onPowerUpCollected();
    }
    public void notifyDialogueAdvanced() {
        if (listeners != null) for (IGameListener l : listeners) l.onDialogueAdvanced();
    }
    public void notifyDialogueClosed() {
        if (listeners != null) for (IGameListener l : listeners) l.onDialogueClosed();
    }


}
