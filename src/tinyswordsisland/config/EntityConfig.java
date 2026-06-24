package tinyswordsisland.config;

import tinyswordsisland.model.enu.*;

import java.util.ArrayList;


/**
 * ALL'ENTITY SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record EntityConfig(ScreenConfig screenConfig,
                           SpawnPoint playerSpawnPoint,
                           SpawnPoint monkSpawnPoint,
                           ArrayList<SpawnPoint> TntSpawnPoint,
                           ArrayList<SpawnPoint> DynamiteSpawnPoint,
                           ArrayList<SpawnPoint> TorchSpawnPoint
                           ) {

        //TODO load from somwere
        public static final int SPRITE_WIDTH = 192;
        public static final int SPRITE_HEIGHT = 192;

        public static final int DEATH_FRAME_SIZE = 128;

        public static final int HITBOX_WIDTH = 45;
        public static final int HITBOX_HEIGHT = 35;

        //PlayerConfig
        //-------------------------------------------------------------
        public static final int PLAYER_MAX_LIFE = 6;
        public static final int START_PLAYER_SPEED = 384; // pixels per second
        public static final int PLAYER_SCALE = 1;
        public static final int PLAYER_RENDER_WIDTH = SPRITE_WIDTH * PLAYER_SCALE;
        public static final int PLAYER_RENDER_HEIGHT = SPRITE_HEIGHT * PLAYER_SCALE;
        public static final Direction START_FACING = Direction.RIGHT;
        public static final PlayerState PLAYER_DEFAULT_STATE = PlayerState.IDLE;
        public static final PlayerColor DEFAULT_COLOR = PlayerColor.BLUE;
        public static final int RANGE_ATTACK = 20;

        public static final int PLAYER_HITBOX_WIDTH = 45 * PLAYER_SCALE;
        public static final int PLAYER_HITBOX_HEIGHT = 35 * PLAYER_SCALE;
        public static final int PLAYER_OFFSET_HEIGHT = 15;

        public static final double SHIELD_DURATION_MS = 25000.0; // 20 seconds
        public static final int SPEED_BOOST_AMOUNT = 128; 
        public static final double VISUAL_EFFECT_DURATION_MS = 3000.0; // 3 seconds


        public int START_WORLD_X(){return playerSpawnPoint().x();}
        public int START_WORLD_Y(){return playerSpawnPoint().y();}
        public int START_WORLD_LAYER(){return playerSpawnPoint().layer();}


        //-------------------------------------------------------------

        //NPCConfig
        //-------------------------------------------------------------
        public static MonkState MONK_DEFAULT_STATE = MonkState.IDLE;
        public int MONK_START_X(){return monkSpawnPoint().x();}
        public int MONK_START_Y(){return monkSpawnPoint().y();}
        public int MONK_START_LAYER(){return monkSpawnPoint().layer();}

        public static String[] MONK_DIALOUGES = new String[] {

                "Benvenuto nell'isola delle Piccole Spade, giovane eroe. \n Premi per andare avanti (M)",
                "Da quando i goblin hanno invaso l'isola, la pace è stata spezzata. Il tesoro dell'isola è stato rubato. (M)",
                "Se sei qui è perche devi aiutarci a sconfigerli, se ci riuscirai potrai tenerti il bottino. (M)",
                "Potrai trovare sotto qualche albero degli aiuti per la tua missione. Buona fortuna! (M)",
                "Non potrai scendere le scale finché non sarai abbastanza forte. Ripulisci l'isola dai goblin e riporta l'armonia! (M)"
        };

        public static final double  MONK_DISAPPEAR_DURATION_MS = 1650.0;
        public static final int MONK_ACTIVATION_RADIUS = 160;


        //-------------------------------------------------------------
        //TNTConfig
        public static TNTState TNT_DEFAULT_STATE = TNTState.WANDER;
        public static final int START_TNT_SPEED = 128; // pixels per second
        public static final int TNT_MAX_LIFE = 2;
        public static final int TNT_DETECTION_RADIUS = 80;
        public static final int TNT_EXPLOSION_RADIUS = 100;

        public static final long TNT_EXPLOSION_DELAY = 1000;
        public static final int TNT_EXPLOSION_DURATION = 300;
        public static final double TNT_MOVEINTERVAL = 1000; // Change direction every 1 second

        public static final int TNT_SPRITE_WIDTH = 128;
        public static final int TNT_SPRITE_HEIGHT = 128;

        public static final int TNT_HITBOX_WIDTH = 55;
        public static final int TNT_HITBOX_HEIGHT = 35;

        public static final int TNT_FOR_SPAWNPOINT = 10;
        public ArrayList<SpawnPoint> TNT_SPAWNPOINT() {return TntSpawnPoint;}

        //-------------------------------------------------------------
        //EnemyDynamiteConfig
        public static final DynamiteState DYNAMITE_DEFAULT_STATE = DynamiteState.WANDER;
        public static final int START_DYNAMITE_SPEED = 128; // pixels per second
        public static final int DYNAMITE_MAX_LIFE = 5;

        public static final int DYNAMITE_DETECTION_RADIUS = 400;
        public static final int DYNAMITE_ATTACKING_RADIUS = 150;
        public static final int DYNAMITE_ATTACK_INTERVAL = 800; // time between attacks
        public static final double DYNAMITE_MOVEINTERVAL = 1000; // Change random direction every 1 second

        public static final int DYNAMITE_SPRITE_WIDTH = 192;
        public static final int DYNAMITE_SPRITE_HEIGHT = 192;

        public static final int DYNAMITE_HITBOX_WIDTH = 55;
        public static final int DYNAMITE_HITBOX_HEIGHT = 35;

        public static final int DYNAMITE_FOR_SPAWNPOINT = 2;
        public ArrayList<SpawnPoint> DYNAMITE_SPAWNPOINT() {return DynamiteSpawnPoint;}
        //-------------------------------------------------------------


        //Dynamite projectile Config
        public static final int PROJECTILE_THROW_SPEED = 80;
        public static final int PROJECTILE_THROW_HEIGHT = 300;
        public static final int PROJECTILE_AIR_TIME = 1800; // ms
        public static final int PROJECTILE_EXPLOSION_RADIUS = 50;
        public static final int PROJECTILE_SPRITE_WIDTH = 64;
        public static final int PROJECTILE_SPRITE_HEIGHT = 64; 
        public static final int PROJECTILE_SIZE = 16;


        // Enemy Torch Config
        public static final int TORCH_MAX_LIFE = 10;
        public static final int TORCH_START_SPEED = 128;      // px/s
        public static final double TORCH_SCALE = 1.5;       
        public static final int TORCH_HITBOX_WIDTH = (int) (45 * TORCH_SCALE);       
        public static final int TORCH_HITBOX_HEIGHT = (int) (35 * TORCH_SCALE);       
        public static final int TORCH_SPRITE_WIDTH = 192;      
        public static final int TORCH_SPRITE_HEIGHT = 192;
        public static final int TORCH_FOR_SPAWNPOINT = 1;

        public static final int TORCH_ATTACK_DURATION = 400;
        public static final int TORCH_APPROACH_TIME = 500;
        public static final int TORCH_COOLDOWN_TIME = 2000;
        public static final int FIRE_SPRITE_WIDTH = 128;
        public static final int FIRE_SPRITE_HEIGHT = 128;
        public ArrayList<SpawnPoint> TORCH_SPAWNPOINT() {return TorchSpawnPoint;}

}
