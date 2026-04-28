package main.CONFIG;

import main.CONFIG.enu.Direction;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.MonkState;

import java.util.ArrayList;


/**
 * PLAYER SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record EntityConfig(ScreenConfig screenConfig,
                           SpawnPoint playerSpawnPoint,
                          SpawnPoint monkSpawnPoint,
                          ArrayList<SpawnPoint> TntSpawnPoint) {

    //TODO load from somwere
    public static final int SPRITE_WIDTH = 192;
    public static final int SPRITE_HEIGHT = 192;

    public static final int HITBOX_WIDTH = 45;
    public static final int HITBOX_HEIGHT = 35;

    //PlayerConfig
    //-------------------------------------------------------------
    public static final int START_WORLD_LAYER = 3;

    public static final int START_PLAYER_SPEED = 384; // pixels per second
    public static final int PLAYER_SCALE = 1;
    public static final int PLAYER_RENDER_WIDTH = SPRITE_WIDTH * PLAYER_SCALE;
    public static final int PLAYER_RENDER_HEIGHT = SPRITE_HEIGHT * PLAYER_SCALE;
    public static final Direction START_FACING = Direction.RIGHT;
    public static final int RANGE_ATTACK = 20;

    public static final int PLAYER_HITBOX_WIDTH = 45 * PLAYER_SCALE;
    public static final int PLAYER_HITBOX_HEIGHT = 35 * PLAYER_SCALE;


    public int START_WORLD_X(){return playerSpawnPoint().x();}
    public int START_WORLD_Y(){return playerSpawnPoint().y();}
    public int START_WORLD_LAYER(){return playerSpawnPoint().layer();}
    public int SCREEN_POSX(){ return screenConfig.SCREEN_WIDTH() / 2 - SPRITE_WIDTH / 2;}
    public int SCREEN_POSY(){ return screenConfig.SCREEN_HEIGHT() / 2 - SPRITE_HEIGHT / 2;}
    //-------------------------------------------------------------

    //NPCConfig
    //-------------------------------------------------------------
    public static MonkState MONK_DEFAULT_STATE = MonkState.IDLE;
    public static String MONK_TAG = "Monk";
    public int MONK_START_X(){return monkSpawnPoint().x();}
    public int MONK_START_Y(){return monkSpawnPoint().y();}
    public int MONK_START_LAYER(){return monkSpawnPoint().layer();}

    public static String[] MONK_DIALOUGES = new String[] {
            "Benvenuto nell'isola delle Piccole Spade, giovane eroe.",
            "Da quando i goblin hanno invaso l'isola, la pace è stata spezzata e il tesoro dell'isola è stato rubato.",
            "Recupera il tesoro e riporta l'armonia. Buona fortuna!"
    };

    //-------------------------------------------------------------
    //TNTConfig
    public static final int START_TNT_SPEED = 128;
    public static final int TNT_DETECTION_RADIUS = 80;
    public static final int TNT_EXPLOSION_RADIUS = 100;

    public static final int TNT_SPRITE_WIDTH = 128;
    public static final int TNT_SPRITE_HEIGHT = 128;

    public static final int TNT_HITBOX_WIDTH = 55;
    public static final int TNT_HITBOX_HEIGHT = 35;

    public static final int NPC_FOR_SPAWNPOINT = 10;
    public ArrayList<SpawnPoint> TNT_SPAWNPOINT() {return TntSpawnPoint;}

    //-------------------------------------------------------------
    //EnemyDynamiteConfig
    public static final int DYNAMITE_SPEED = 128;
    public static final int DYNAMITE_DETECTION_RADIUS = 300;
    public static final int DYNAMITE_ATTACKING_RADIUS = 80;
    public static final int DYNAMITE_ATTACK_INTERVAL = 2000; // 2 seconds

    public static final int DYNAMITE_SPRITE_WIDTH = 192;
    public static final int DYNAMITE_SPRITE_HEIGHT = 192;

    public static final int DYNAMITE_HITBOX_WIDTH = 55;
    public static final int DYNAMITE_HITBOX_HEIGHT = 35;
    //Dynamite projectile Config
    public static final int PROJECTILE_THROW_SPEED = 200;
    public static final int PROJECTILE_THROW_HEIGHT = 300;
    public static final int PROJECTILE_FUSE_TIME = 2000; // ms
    public static final int PROJECTILE_EXPLOSION_RADIUS = 50;
    public static final int PROJECTILE_SPRITE_WIDTH = 64;
    public static final int PROJECTILE_SPRITE_HEIGHT = 64; 
    public static final int PROJECTILE_SIZE = 16;
 
}
