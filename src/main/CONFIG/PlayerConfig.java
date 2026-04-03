package main.CONFIG;

import main.ENUM.Direction;


/**
 * PLAYER SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record PlayerConfig(ScreenConfig screenConfig) {
    private static final int START_COL = 62;
    private static final int START_ROW = 19;
    public static final int START_PLAYER_SPEED = 6 * 60;
    public static final int START_WORLD_LAYER = 3;
    public static final int SPRITE_WIDTH = 192;
    public static final int SPRITE_HEIGHT = 192;
    public static final int PLAYER_SCALE = 1;
    public static final int RENDER_WIDTH = SPRITE_WIDTH * PLAYER_SCALE;
    public static final int RENDER_HEIGHT = SPRITE_HEIGHT * PLAYER_SCALE;
    public static final Direction FACING = Direction.RIGHT;
    public static final int RANGE_ATTACK = 20;

    public static final int PLAYER_HITBOX_WIDTH = 45 * PLAYER_SCALE;
    public static final int PLAYER_HITBOX_HEIGHT = 35 * PLAYER_SCALE;

    public int START_WORLD_X(){return START_COL * screenConfig.TILE_SIZE();}
    public int START_WORLD_Y(){return START_ROW * screenConfig.TILE_SIZE();}
    public int SCREEN_POSX(){ return screenConfig.SCREEN_WIDTH() / 2 - SPRITE_WIDTH / 2;}
    public int SCREEN_POSY(){ return screenConfig.SCREEN_HEIGHT() / 2 - SPRITE_HEIGHT / 2;}
}
