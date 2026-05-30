package model.event;

public enum AudioEventType {
    GAME_START,
    PLAYER_ATTACK, //triggered
    PLAYER_DAMAGED, //triggered
    TREE_HIT,
    ENEMY_HIT,
    ENEMY_DEFEATED,
    PROJECTILE_LAUNCHED, //triggered
    PROJECTILE_EXPLODED, //triggered
    TNT_TRIGGERED, //triggered
    TNT_EXPLOSION, //triggered
    DIALOGUE_ADVANCE,
    DIALOGUE_CLOSE,
    STAIRS_LOCKED,
    STAIRS_UNLOCKED
}

