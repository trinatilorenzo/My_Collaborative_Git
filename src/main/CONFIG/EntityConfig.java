package main.CONFIG;

import main.CONFIG.enu.Direction;
import main.CONFIG.enu.DynamiteState;
import main.CONFIG.enu.MonkState;
import main.CONFIG.enu.TNTState;

import java.util.ArrayList;


/**
 * ALL'ENTITY SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record EntityConfig(ScreenConfig screenConfig,
                           SpawnPoint playerSpawnPoint,
                           SpawnPoint monkSpawnPoint,
                           ArrayList<SpawnPoint> TntSpawnPoint,
                           ArrayList<SpawnPoint> DynamiteSpawnPoint ) {

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

    public int SCREEN_POSX(){ return screenConfig.SCREEN_WIDTH() / 2;}
    public int SCREEN_POSY(){ return screenConfig.SCREEN_HEIGHT() / 2;}
    //-------------------------------------------------------------

    //NPCConfig
    //-------------------------------------------------------------
    public static MonkState MONK_DEFAULT_STATE = MonkState.IDLE;
    public int MONK_START_X(){return monkSpawnPoint().x();}
    public int MONK_START_Y(){return monkSpawnPoint().y();}
    public int MONK_START_LAYER(){return monkSpawnPoint().layer();}

    public static String[] MONK_DIALOUGES = new String[] {
            "Benvenuto nell'isola delle Piccole Spade, giovane eroe.",
            "Da quando i goblin hanno invaso l'isola, la pace è stata spezzata e il tesoro dell'isola è stato rubato.",
            "Recupera il tesoro e riporta l'armonia. Buona fortuna!"
    };

    public static final double  MONK_DISAPPEAR_DURATION_MS = 1650.0;
    public static final int MONK_ACTIVATION_RADIUS = 160;
    public static final double MONK_RESPAWN_DURATION_MS = 10000.0;

    //-------------------------------------------------------------
    //TNTConfig
    public static TNTState TNT_DEFAULT_STATE = TNTState.WANDER;
    public static final int START_TNT_SPEED = 128; // pixels per second
    public static final int TNT_MAX_LIFE = 1;
    public static final int TNT_DETECTION_RADIUS = 80;
    public static final int TNT_EXPLOSION_RADIUS = 100;

    public static final long TNT_EXPLOSION_DELAY = 1000;
    public static final int TNT_EXPLOSION_DURATION = 300;
    public static final double TNT_MOVEINTERVAL = 1000; // Change direction every 1 second

    public static final int TNT_SPRITE_WIDTH = 128;
    public static final int TNT_SPRITE_HEIGHT = 128;

    public static final int TNT_HITBOX_WIDTH = 55;
    public static final int TNT_HITBOX_HEIGHT = 35;

    public static final int TNT_FOR_SPAWNPOINT = 5;
    public ArrayList<SpawnPoint> TNT_SPAWNPOINT() {return TntSpawnPoint;}

    //-------------------------------------------------------------
    //EnemyDynamiteConfig
    public static final DynamiteState DYNAMITE_DEFAULT_STATE = DynamiteState.WANDER;
    public static final int START_DYNAMITE_SPEED = 256; // pixels per second
    public static final int DYNAMITE_MAX_LIFE = 3;

    public static final int DYNAMITE_DETECTION_RADIUS = 400;
    public static final int DYNAMITE_ATTACKING_RADIUS = 150;
    public static final int DYNAMITE_ATTACK_INTERVAL = 500; // time between attacks
    public static final double DYNAMITE_MOVEINTERVAL = 1000; // Change random direction every 1 second

    public static final int DYNAMITE_SPRITE_WIDTH = 192;
    public static final int DYNAMITE_SPRITE_HEIGHT = 192;

    public static final int DYNAMITE_HITBOX_WIDTH = 55;
    public static final int DYNAMITE_HITBOX_HEIGHT = 35;

    public static final int DYNAMITE_FOR_SPAWNPOINT = 1;
    public ArrayList<SpawnPoint> DYNAMITE_SPAWNPOINT() {return DynamiteSpawnPoint;}
    //-------------------------------------------------------------


    //Dynamite projectile Config
    public static final int PROJECTILE_THROW_SPEED = 100;
    public static final int PROJECTILE_THROW_HEIGHT = 300;
    public static final int PROJECTILE_AIR_TIME = 1500; // ms
    public static final int PROJECTILE_EXPLOSION_RADIUS = 50;
    public static final int PROJECTILE_SPRITE_WIDTH = 64;
    public static final int PROJECTILE_SPRITE_HEIGHT = 64; 
    public static final int PROJECTILE_SIZE = 16;


    // EnemyTorch
    public static final int    TORCH_MAX_LIFE                = 6;
    public static final int    TORCH_BASE_SPEED              = 120;       // px/s
    public static final int    TORCH_HITBOX_WIDTH            = 32;
    public static final int    TORCH_HITBOX_HEIGHT           = 32;
    public static final double TORCH_DETECTION_RADIUS        = 400.0;
    public static final double TORCH_ATTACK_RADIUS           = 160.0;
    public static final double TORCH_CHARGE_DURATION_MS      = 900.0;
    public static final double TORCH_COOLDOWN_DURATION_MS    = 1800.0;
    public static final double TORCH_STUN_DURATION_MS        = 1200.0;
    public static final double TORCH_COOLDOWN_SPEED_FACTOR   = 0.35;     // fraction of normal speed
    public static final double TORCH_JITTER_INTERVAL_MS      = 300.0;
    public static final double TORCH_JITTER_MAX_RADIANS      = 0.3;      // ~17°
    public static final double TORCH_PATROL_INTERVAL         = 1200.0;
    
    // TorchVortex
    public static final double VORTEX_EXPAND_SPEED           = 200.0;    // px/s
    public static final double VORTEX_MAX_RADIUS             = 350.0;

}
