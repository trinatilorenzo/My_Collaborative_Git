package tinyswordsisland.input;

/**
 * Snapshot of player and menu input for a single game frame.
 */
public record InputState(
        boolean up,
        boolean down,
        boolean left,
        boolean right,
        boolean shield,
        boolean movementRequested,
        boolean attack,
        boolean pause,
        boolean debug,
        boolean interact,
        boolean menuPrevious,
        boolean menuNext,
        boolean menuConfirm
) {}
