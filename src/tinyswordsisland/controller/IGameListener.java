package tinyswordsisland.controller;

public interface IGameListener {
    // Eventi di movimento e combattimento Player
    void onPlayerWalkStart();
    void onPlayerWalkStop();
    void onPlayerAttackStart();
    void onPlayerAttackStop();
    void onPlayerDamaged(int currentLife, int maxLife);

    // Eventi Mondo e Nemici
    void onEnemyHit();
    void onEnemyDefeated();
    void onTreeHit();
    void onTreeDestroyed();
    void onStairsUnlocked();
    void onStairsLocked();

    // Eventi Esplosivi
    void onTntTriggered();
    void onTntExploded();
    void onProjectileLaunched();
    void onProjectileExploded();

    // Eventi UI-Logic (Gestiti dal model, es. avanzamento livello)
    void onLevelUp();
    void onPowerUpCollected();
    void onDialogueAdvanced();
    void onDialogueClosed();
;
}
