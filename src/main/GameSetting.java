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
    public static final int MAX_WORLD_COL = 100;
    public static final int MAX_WORLD_ROW = 80;
    public static final int GRAPHIC_LAYER_NUM = 7;
    public static final int GAME_LAYER_NUM = 3;
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
    public static final String MAP_PATH = "/res/maps/MappaGiocoV0_"; // no number and .csv
    public static final String TILESET_PATH = "/res/tiles/tileSet1.png";
    //-----------------------------------------------------------------------


    // PLAYER SETTINGS
    //-----------------------------------------------------------------------
    public static final int PLAYER_SPEED = 4;
    public static final int START_WORLD_X = 50 * TILE_SIZE;
    public static final int START_WORLD_Y = 20 * TILE_SIZE;
    public static final int SPRITE_FRAME_WIDTH =  192;
    public static final int SPRITE_FRAME_HEIGHT =  192;
    public static final int PLAYER_RENDER_WIDTH = (int)(SPRITE_FRAME_WIDTH * 1.0); 
    public static final int PLAYER_RENDER_HEIGHT = (int)(SPRITE_FRAME_HEIGHT * 1.0);
    //-----------------------------------------------------------------------

    // FPS
    public static final int FPS = 60;


}
