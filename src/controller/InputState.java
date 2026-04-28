package controller;

/**
 * Represents the state of player input, encapsulating the status of directional keys (up, down, left, right).
 */

public record InputState(
        boolean up,
        boolean down,
        boolean left,
        boolean right,
        boolean attack,
        boolean pause,
        boolean debug,
        boolean interact,
        boolean menuPrevious,
        boolean menuNext,
        boolean menuConfirm
) {}
