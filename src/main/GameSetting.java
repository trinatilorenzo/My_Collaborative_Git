package main;

public class GameSetting {

    // SCREEN SETTINGS
    //-----------------------------------------------------------------------
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int SCALE = 3;
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;

    public static final int MAX_SCREEN_COL = 12;
    public static final int MAX_SCREEN_ROW = 16;

    public static final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    public static final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;
    //-----------------------------------------------------------------------

    // WORLD SETTINGS
    //-----------------------------------------------------------------------
    public static final int MAX_WORLD_COL = 12;
    public static final int MAX_WORLD_ROW = 16;

    public static final int TILESNUM = 50;
    //-----------------------------------------------------------------------

    // ASSETT LOCATION
    //-----------------------------------------------------------------------
    public static final String MAP_PATH = "/res/maps/map.txt";

    //-----------------------------------------------------------------------

    // FPS
    public static final int FPS = 60;


}
