package main;

import java.awt.Color;

public class GameSetting {

    // SCREEN SETTINGS
    //-----------------------------------------------------------------------
    public static final int ORIGINAL_TILE_SIZE = 64;
    public static final int SCALE = 1;
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;

    public static final int MAX_SCREEN_COL = 20;
    public static final int MAX_SCREEN_ROW = 12;

    public static final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    public static final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;
    //-----------------------------------------------------------------------

    // WORLD SETTINGS
    //-----------------------------------------------------------------------
    public static final int MAX_WORLD_COL = 20;
    public static final int MAX_WORLD_ROW = 20;
    public static final int MAP_LAYER_NUM = 5;
    public static final Color GAME_BG_COLOR = new Color(71,171,169);
    //-----------------------------------------------------------------------

    //TILESET
    //-----------------------------------------------------------------------
    public static final int MAX_TILESET_RAW = 40;
    public static final int MAX_TILESET_COL = 20;
    public static final int TILESNUM = MAX_TILESET_COL*MAX_TILESET_RAW ;
    //-----------------------------------------------------------------------

    // ASSETT LOCATION
    //-----------------------------------------------------------------------
    public static final String MAP_PATH = "/res/maps/test_map2__"; // no number and .csv
    public static final String TILESET_PATH = "/res/tiles/tileSet1.png";
    //-----------------------------------------------------------------------

    // FPS
    public static final int FPS = 60;


}
