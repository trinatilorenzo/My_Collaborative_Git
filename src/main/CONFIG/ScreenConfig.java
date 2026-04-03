package main.CONFIG;

import java.awt.*;

/**
 * SCREEN SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
// ScreenConfig.java
public record ScreenConfig(int ORIGINAL_TILESIZE, int scale, int MAX_SCREEN_COL, int MAX_SCREEN_ROW, Color GAME_BG_COLOR) {
    public int TILE_SIZE() {return ORIGINAL_TILESIZE * scale;}
    public int SCREEN_WIDTH() {
        return TILE_SIZE() * MAX_SCREEN_COL;
    }
    public int SCREEN_HEIGHT() {return TILE_SIZE() * MAX_SCREEN_ROW;}
}
