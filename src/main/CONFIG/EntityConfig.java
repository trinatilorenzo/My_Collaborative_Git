package main.CONFIG;

import main.ENUM.Direction;
import main.ENUM.MonkState;


/**
 * PLAYER SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record EntityConfig(ScreenConfig screenConfig) {
    public static final int SPRITE_WIDTH = 192;
    public static final int SPRITE_HEIGHT = 192;

    public static final int HITBOX_WIDTH = 45;
    public static final int HITBOX_HEIGHT = 35;

    //PlayerConfig
    //-------------------------------------------------------------
    private static final int START_COL = 62;
    private static final int START_ROW = 19;
    public static final int START_PLAYER_SPEED = 6 * 60;
    public static final int START_WORLD_LAYER = 3;

    public static final int PLAYER_SCALE = 1;
    public static final int PLAYER_RENDER_WIDTH = SPRITE_WIDTH * PLAYER_SCALE;
    public static final int PLAYER_RENDER_HEIGHT = SPRITE_HEIGHT * PLAYER_SCALE;
    public static final Direction FACING = Direction.RIGHT;
    public static final int RANGE_ATTACK = 20;

    public static final int PLAYER_HITBOX_WIDTH = 45 * PLAYER_SCALE;
    public static final int PLAYER_HITBOX_HEIGHT = 35 * PLAYER_SCALE;

    public int START_WORLD_X(){return START_COL * screenConfig.TILE_SIZE();}
    public int START_WORLD_Y(){return START_ROW * screenConfig.TILE_SIZE();}
    public int SCREEN_POSX(){ return screenConfig.SCREEN_WIDTH() / 2 - SPRITE_WIDTH / 2;}
    public int SCREEN_POSY(){ return screenConfig.SCREEN_HEIGHT() / 2 - SPRITE_HEIGHT / 2;}
    //-------------------------------------------------------------

    //NPCConfig
    //-------------------------------------------------------------
    public static MonkState MONK_DEFAULT_STATE = MonkState.IDLE;
    public static String MONK_TAG = "Monk";

    public static String[] MONK_DIALOUGES = new String[] {
            "Benvenuto nell'isola delle Piccole Spade, giovane eroe.",
            "Da quando i goblin hanno invaso l'isola, la pace è stata spezzata e il tesoro dell'isola è stato rubato.",
            "Recupera il tesoro e riporta l'armonia. Buona fortuna!"
    };


    //-------------------------------------------------------------

}
