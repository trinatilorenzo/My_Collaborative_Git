package main.CONFIG;

/**
 * OBJ SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record ObjConfig() {
    public static final String OBJ_TAG = "objectgroup";

    //TREE
    //----------------------------------------------------------------------------------------------------------------------
    public static final String TREE_TAG = "tree";
    public static final Boolean TREE_SOLID = true;

    public static final int TREE_SPRITE_WIDTH = 192;
    public static final int TREE_SPRITE_HEIGHT = 256;
    public static final int TREE_HITBOX_WIDTH = 40;
    public static final int TREE_HITBOX_HEIGHT = 25;
    public static final int TREE_HEALTH = 4;
    public static final double TREE_IDLE_FRAME_MS = 100.0;
    public static final double CHOP_ANIMATION_DURATION_MS = 500;
    //----------------------------------------------------------------------------------------------------------------------

}
