package model.event;

public enum AudioEventType {
    GAME_START,
    PLAYER_WALK_START,
    PLAYER_WALK_STOP,
    PLAYER_ATTACK, //triggered
    PLAYER_ATTACK_STOP,
    PLAYER_DAMAGED, //triggered
    TREE_HIT,
    TREE_FINAL,
    ENEMY_HIT,
    ENEMY_DEFEATED,
    PROJECTILE_LAUNCHED, //triggered
    PROJECTILE_EXPLODED, //triggered
    TNT_TRIGGERED, //triggered
    TNT_EXPLOSION, //triggered
    DIALOGUE_ADVANCE,
    DIALOGUE_CLOSE,
    STAIRS_LOCKED,
    STAIRS_UNLOCKED,
    BUTTON_CLICKED,
    BUTTON_HOVER,
}

