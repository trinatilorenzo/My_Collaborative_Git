package input;

/**
 * Represents the state of player input, encapsulating the status of directional keys (up, down, left, right).
 */

//TODO farlo meglio
public record InputState(
        boolean up,
        boolean down,
        boolean left,
        boolean right,
        boolean attack,
        boolean interact, // reserved for future use
        boolean pause,
        boolean debug
) {}