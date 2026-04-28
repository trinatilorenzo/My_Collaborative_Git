package main.CONFIG;

import java.awt.*;

/**
 * SCREEN SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
// ScreenConfig.java
public record ScreenConfig(int ORIGINAL_TILESIZE, Color GAME_BG_COLOR) {

    //GAME LOOP
    public static final int FPS = 120;
    public static final int MAX_FRAME_SKIP = 10;

    //SCREEN SIZE
    public static final int SCALE = 1;
    public static final int MAX_SCREEN_COL = 20;
    public static final int MAX_SCREEN_ROW = 12;

    public int TILE_SIZE() {return ORIGINAL_TILESIZE * SCALE;}
    public int SCREEN_WIDTH() {
        return TILE_SIZE() * MAX_SCREEN_COL;
    }
    public int SCREEN_HEIGHT() {return TILE_SIZE() * MAX_SCREEN_ROW;}
}
