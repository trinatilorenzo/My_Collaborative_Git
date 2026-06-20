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
    public static final int MAX_FRAME_SKIP = 20;

    //SCREEN SIZE
    public static final int SCALE = 1;

    public static final int MIN_SCREEN_COL = 18; //min 15
    public static final int MIN_SCREEN_ROW = 11; //min 11

    public int TILE_SIZE() {return ORIGINAL_TILESIZE * SCALE;}

    public int MIN_SCREEN_WIDTH() {
        return TILE_SIZE() * MIN_SCREEN_COL;
    }
    public int MIN_SCREEN_HEIGHT() {return TILE_SIZE() * MIN_SCREEN_ROW;}
}
