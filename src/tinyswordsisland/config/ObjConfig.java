package tinyswordsisland.config;

import java.util.ArrayList;

/**
 * OBJ SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record ObjConfig(String TREE_TAG_03,
                        String TREE_TAG_02,
                        String TREE_TAG_01,
                        String CASTLE_TAG,
                        String TOWER_TAG,
                        String GOLDMINE_TAG,
                        String GOBLIN_HOME_TAG,
                        ArrayList<SpawnPoint> tree03SpawnPoint,
                        ArrayList<SpawnPoint> tree02SpawnPoint,
                        ArrayList<SpawnPoint> tree01SpawnPoint,
                        ArrayList<SpawnPoint> castleSpawnPoint,
                        ArrayList<SpawnPoint> towerSpawnPoint,
                        ArrayList<SpawnPoint> goldMineSpawnPoint,
                        ArrayList<SpawnPoint> goblinHomeSpawnPoint) {

    //TREE (COMMON)
    //-------------------------------------------------------------
    public static final Boolean TREE_SOLID = true;

    public static final int TREE_HEALTH = 1;
    public static final double TREE_IDLE_FRAME_MS = 100.0;
    public static final double CHOP_ANIMATION_DURATION_MS = 500;
    //-------------------------------------------------------------

    // TREE 03

    public static final int TREE_03_WIDTH = 192;
    public static final int TREE_03_HEIGHT = 256;
    public static final int TREE_03_HITBOX_WIDTH = 40;
    public static final int TREE_03_HITBOX_HEIGHT = 25;
    public static final int TREE_03_HITBOX_OFFSET_Y = 195;

    public ArrayList<SpawnPoint> TREES_03_SPAWNPOINT() {return tree03SpawnPoint;}
    //-------------------------------------------------------------

    // TREE 02
    public static final int TREE_02_WIDTH = 192;
    public static final int TREE_02_HEIGHT = 192;
    public static final int TREE_02_HITBOX_WIDTH = 20;
    public static final int TREE_02_HITBOX_HEIGHT = 25;
    public static final int TREE_02_HITBOX_OFFSET_Y = 135;

    public ArrayList<SpawnPoint> TREES_02_SPAWNPOINT() {return tree02SpawnPoint;}
    //-------------------------------------------------------------

    // TREE 01
    public static final int TREE_01_WIDTH = 192;
    public static final int TREE_01_HEIGHT = 192;
    public static final int TREE_01_HITBOX_WIDTH = 20;
    public static final int TREE_01_HITBOX_HEIGHT = 25;
    public static final int TREE_01_HITBOX_OFFSET_Y = 135;

    public ArrayList<SpawnPoint> TREES_01_SPAWNPOINT() {return tree01SpawnPoint;}
    //-------------------------------------------------------------

    //BUILDING
    //-------------------------------------------------------------
    public static final Boolean BUILDING_SOLID = true;

    // castel
    public static final int CASTLE_WIDTH = 320;
    public static final int CASTLE_HEIGHT = 256;
    public static final int CASTLE_HITBOX_WIDTH = 210;
    public static final int CASTLE_HITBOX_HEIGHT = 70;
    public static final int CASTLE_HITBOX_OFFSET_Y = 180;
    public ArrayList<SpawnPoint> CASTLE_SPAWNPOINT() {return castleSpawnPoint;}

    //tower
    public static final int TOWER_WIDTH = 128;
    public static final int TOWER_HEIGHT = 256;
    public static final int TOWER_HITBOX_WIDTH = 80;
    public static final int TOWER_HITBOX_HEIGHT = 60;
    public static final int TOWER_HITBOX_OFFSET_Y = 190;
    public ArrayList<SpawnPoint> TOWER_SPAWNPOINT() {return towerSpawnPoint;}

    //goldmine
    public static final int GOLDMINE_WIDTH = 192;
    public static final int GOLDMINE_HEIGHT = 128;
    public static final int GOLDMINE_HITBOX_WIDTH = 130;
    public static final int GOLDMINE_HITBOX_HEIGHT = 55;
    public static final int GOLDMINE_HITBOX_OFFSET_Y = 70;
    public ArrayList<SpawnPoint> GOLDMINE_SPAWNPOINT() {return goldMineSpawnPoint;}

    //goblin home
    public static final int GOBLIN_HOME_WIDTH = 128;
    public static final int GOBLIN_HOME_HEIGHT = 192;
    public static final int GOBLIN_HOME_HITBOX_WIDTH = 90;
    public static final int GOBLIN_HOME_HITBOX_HEIGHT = 55;
    public static final int GOBLIN_HOME_HITBOX_OFFSET_Y = 130;
    public ArrayList<SpawnPoint> GOBLIN_HOME_SPAWNPOINT() {return goblinHomeSpawnPoint;}
    //-------------------------------------------------------------

    // POWER-UP
    public static final int POWER_UP_SIZE = 64;
    public static final int POWER_UP_HITBOX_SIZE = 48;
    public static final long PICKUP_DURATION_MS = 800; // Duration of the power-up effect after being picked up
    public static final long POWER_UP_LIGHTING_DURATION = 200; 

    //-------------------------------------------------------------


}
