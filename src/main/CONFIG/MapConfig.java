package main.CONFIG;

import java.awt.*; /**
 * MAP SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record MapConfig(int ORIGINAL_TILESIZE,
                        int MAX_WORLD_COL,
                        int MAX_WORLD_ROW) {

    public static final int MAX_TILESET_ROW = 40;
    public static final int MAX_TILESET_COL = 20;
    public static final int TILES_NUM = MAX_TILESET_COL * MAX_TILESET_ROW;
    public static final String COLL_TAG = "collision";
    public static final int COLL_ID = 2;
}
