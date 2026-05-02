package main.CONFIG;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * OBJ SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record ObjConfig(ArrayList<SpawnPoint> tree03SpawnPoint,
                        ArrayList<SpawnPoint> tree02SpawnPoint,
                        ArrayList<SpawnPoint> tree01SpawnPoint) {
    //TREE (COMMON)
    //-------------------------------------------------------------
    public static final Boolean TREE_SOLID = true;
    public static final int TREE_HITBOX_WIDTH = 40;
    public static final int TREE_HITBOX_HEIGHT = 25;
    public static final int TREE_HEALTH = 4;
    public static final double TREE_IDLE_FRAME_MS = 100.0;
    public static final double CHOP_ANIMATION_DURATION_MS = 500;
    //-------------------------------------------------------------

    // TREE 03
    public static final String TREE_TAG_03 = "trees_03";
    public static final int TREE_03_WIDTH = 192; //TODO from file
    public static final int TREE_03_HEIGHT = 256;

    public ArrayList<SpawnPoint> TREES_03_SPAWNPOINT() {return tree03SpawnPoint;}
    //-------------------------------------------------------------

    // TREE 02
    public static final String TREE_TAG_02 = "trees_02";
    public static final int TREE_02_WIDTH = 192;
    public static final int TREE_02_HEIGHT = 256;

    public ArrayList<SpawnPoint> TREES_02_SPAWNPOINT() {return tree02SpawnPoint;}
    //-------------------------------------------------------------

    // TREE 01
    public static final String TREE_TAG_01 = "trees_01";
    public static final int TREE_01_WIDTH = 192;
    public static final int TREE_01_HEIGHT = 256;

    public ArrayList<SpawnPoint> TREES_01_SPAWNPOINT() {return tree01SpawnPoint;}
    //-------------------------------------------------------------

    //BUILDING
    //-------------------------------------------------------------

    //-------------------------------------------------------------


}