package model;

import controller.InputState;
import main.CONFIG.EntityConfig;
import main.CONFIG.GameConfig;
import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.UIConfig;
import main.CONFIG.enu.DynamiteState;
import main.CONFIG.enu.GameState;
import main.CONFIG.enu.MonkState;
import main.CONFIG.enu.PlayerState;
import main.CONFIG.enu.TNTState;
import model.entity.*;
import model.event.AudioEventType;
import model.object.GameObject;
import model.object.OBJ_Tree;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * ALL THE GAME MODEL STAFF HERE
 * world map, entity, combat, AI, events ...
*/
//-------------------------------------------------------------------------------------------------------------------
public class GameModel {
    private final GameConfig gameConfig;
    //-------------------------------------------------------------

    // Game status
    private GameState gameState;
    private boolean debugMode;
    //-------------------------------------------------------------

    // Collision
    private final CollisionChecker collisionChecker;
    //-------------------------------------------------------------

    // Map & OBJ
    private final GameMap worldGameMap;
    private List<GameObject> objects;

    // Player & NPC
    //-------------------------------------------------------------
    private Player player;
    private Monk monk;
    private List<EnemyTNT> tntEnemies;
    private List<EnemyDynamite> dynamiteEnemies;
    private List<DynamiteProjectile> projectiles;
    //-------------------------------------------------------------

    // Dialogue
    private String currentDialogue; // dialogue currently displayed to the player
    //-------------------------------------------------------------

    private double deadStateElapsedMs;

    //Audio events
    private final List<AudioEventType> pendingAudioEvents = new ArrayList<>();

    /**
     * COSTRUCTOR
      */
    //-------------------------------------------------------------
    public GameModel(GameConfig GS) {
        gameConfig = GS;
        worldGameMap = new GameMap(GS.mapConfig(), GS.mapDoc());
        collisionChecker = new CollisionChecker(this);

        gameState = GameState.MENU; // Default game state is the menu state
        debugMode = false; // Default debug mode is off

        currentDialogue = "";
        deadStateElapsedMs = 0.0;

    }
    //-------------------------------------------------------------

    /**
     * Start a new game from scratch
     */
    //-------------------------------------------------------------
    public void initializeNewGame(){
        player = new Player(gameConfig.entityConfig());

        //initialize NPC
        EntityConfig entityConfig = gameConfig.entityConfig();
        monk = new Monk(entityConfig.MONK_START_X(), entityConfig.MONK_START_Y(), entityConfig);
        tntEnemies = spawnTntEnemies(entityConfig);
        projectiles = new ArrayList<>();
        dynamiteEnemies = spawnDynamiteEnemies(entityConfig, projectiles);

        //initialize Objects
        ObjConfig objC = gameConfig.ObjConfig();
        objects = new ArrayList<>();

        // first level tree
        spawnTrees(objC.TREES_03_SPAWNPOINT(), objC.TREE_TAG_03(), objC.TREE_03_WIDTH, objC.TREE_03_HEIGHT, objC.TREE_03_HITBOX_OFFSET_Y, objC);
        // second level tree
        spawnTrees(objC.TREES_02_SPAWNPOINT(), objC.TREE_TAG_02(), objC.TREE_02_WIDTH, objC.TREE_02_HEIGHT, objC.TREE_02_HITBOX_OFFSET_Y, objC);
        // third level tree
        spawnTrees(objC.TREES_01_SPAWNPOINT(), objC.TREE_TAG_01(), objC.TREE_01_WIDTH, objC.TREE_01_HEIGHT, objC.TREE_01_HITBOX_OFFSET_Y, objC);

        // START THE GAME
        gameState = GameState.PLAYING;
    }
    /**
     * HELPERS METHOD
     */
    private List<EnemyTNT> spawnTntEnemies(EntityConfig entityConfig) {
        List<EnemyTNT> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.TNT_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.TNT_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyTNT(spawnPoint, entityConfig));
            }
        }
        return enemies;
    }
    private List<EnemyDynamite> spawnDynamiteEnemies(EntityConfig entityConfig, List<DynamiteProjectile> projectileStore) {
        List<EnemyDynamite> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.DYNAMITE_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.DYNAMITE_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyDynamite(spawnPoint, entityConfig, projectileStore));
            }
        }
        return enemies;
    }
    private void spawnTrees(List<SpawnPoint> spawnPoints, String treeTag, int treeWidth, int treeHeight, int hitboxOffsetY, ObjConfig objConfig) {
        for (SpawnPoint spawnPoint : spawnPoints) {
            objects.add(new OBJ_Tree(objConfig, treeTag, spawnPoint, treeWidth, treeHeight,
                    createTreeSolidArea(treeWidth, hitboxOffsetY, objConfig)
            ));
        }
    }
    private Rectangle createTreeSolidArea(int treeWidth, int hitboxOffsetY, ObjConfig objConfig) {
        return new Rectangle(
                treeWidth / 2 - (objConfig.TREE_HITBOX_WIDTH / 2),
                hitboxOffsetY,
                objConfig.TREE_HITBOX_WIDTH,
                objConfig.TREE_HITBOX_HEIGHT
        );
    }
    //end helpers -------------------------------------------------
    //-------------------------------------------------------------


    /**
     * MAIN MATHOD OF THE CLASS
     * Update the model status, Called by the controller every frame
     */
    //-------------------------------------------------------------
    public void update(InputState input, double deltaMs) {

        switch (gameState) {
            case MENU:
                // no update for menu state
                break;
            case PLAYING:
                updatePlayingState(input, deltaMs);
                break;
            case PAUSED:
                updateState(input);
                break;
            case GAME_OVER:
                // no update for game over state
                break;
        }

    }
    //-------------------------------------------------------------

    // ALL THE UPDATE METHOD
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void updatePlayingState(InputState input, double deltaMs) {

        int lifeBeforeUpdate = player.getLife();
        updatePlayer(input, deltaMs);
        updateEnemies(deltaMs);
        updateMonk(input);

        if (player.getState() == PlayerState.WALKING) {
            player.move();
        }

        monk.update(player, deltaMs);
        updateInteractions();

        updateEvents(lifeBeforeUpdate);

    }
    //-------------------------------------------------------------
    private void updatePlayer(InputState input, double deltaMs) {
        PlayerState playerStateBeforeUpdate = player.getState();

        // DEATH
        if (player.isDying() || player.isDead()) {
            updateGameOverCountdown(deltaMs);
            updateDeathSequence(deltaMs);
            return;
        }

        player.update(input, deltaMs);

        collisionChecker.checkTile(player);
        collisionChecker.checkObjects(player);

        //Audio ----------------------
        if (playerStateBeforeUpdate != PlayerState.ATTACKING && player.getState() == PlayerState.ATTACKING) {
            emitAudioEvent(AudioEventType.PLAYER_ATTACK);
        }
        //----------------------------
    }
    //-------------------------------------------------------------
    private void updateEnemies(double deltaMs) {
        // Update TNT
        for (EnemyTNT tnt : tntEnemies) {
            TNTState previousState = tnt.getState();
            if (tnt.getState() != TNTState.EXPLODED) {

                tnt.update(player, deltaMs);
                collisionChecker.checkEntity(player, tnt);
                collisionChecker.checkTile(tnt);
                collisionChecker.checkObjects(tnt);
                tnt.move();
            }

            //Audio ----------------------
            if (previousState != tnt.getState() && tnt.getState() == TNTState.EXPLODING) {
                emitAudioEvent(AudioEventType.TNT_EXPLOSION);
            }
            if (previousState != tnt.getState() && tnt.getState() == TNTState.TRIGGERED) {
                emitAudioEvent(AudioEventType.TNT_TRIGGERED);
            }
            //----------------------------
        }
        tntEnemies.removeIf(EnemyTNT::isExploded);

        // Update Dynamite
        for (EnemyDynamite dynamite : dynamiteEnemies) {
            DynamiteState previousState = dynamite.getState();
            if (dynamite.getState() != DynamiteState.DEAD){

                dynamite.update(player, deltaMs);
                collisionChecker.checkEntity(player, dynamite);
                collisionChecker.checkTile(dynamite);
                collisionChecker.checkObjects(dynamite);
                dynamite.move();

                //Audio ----------------------
                if (previousState != DynamiteState.ATTACKING && dynamite.getState() == DynamiteState.ATTACKING) {
                    emitAudioEvent(AudioEventType.PROJECTILE_LAUNCHED);
                }
                //----------------------------
            }

        }
        dynamiteEnemies.removeIf(EnemyDynamite::isDead);

        // Dynamite projectiles
        for (DynamiteProjectile proj : projectiles) {
            proj.update(deltaMs);
            collisionChecker.checkTile(proj);

            if (collisionChecker.intersects(player, proj)) {
                player.takeDamage();
                proj.explode();
            }

            //Audio ----------------------
            if (proj.isExploded()) {
                emitAudioEvent(AudioEventType.PROJECTILE_EXPLODED);
            }
            //----------------------------
        }
        projectiles.removeIf(DynamiteProjectile::isExploded);
    }
    //-------------------------------------------------------------
    private void updateInteractions() {

        //Player attack with sword
        if (player.getState() == PlayerState.ATTACKING) {
            Rectangle attackArea = player.getAttackArea();

            // TNT -------------------
            for (EnemyTNT tnt : tntEnemies) {
                if (!player.isAttackDamageApplied() && attackArea.intersects(tnt.getSolidWorldArea())) {
                    tnt.takeDamage();
                    player.setAttackDamageApplied(true);
                    //Audio ----------------------
                    emitAudioEvent(AudioEventType.ENEMY_HIT);
                }
            }
            // -------------------

            // Dynamite -------------------
            for (EnemyDynamite dynamite : dynamiteEnemies) {
                if (!player.isAttackDamageApplied() && attackArea.intersects(dynamite.getSolidWorldArea())) {
                    dynamite.takeDamage();
                    player.setAttackDamageApplied(true);
                    //Audio ----------------------
                    emitAudioEvent(AudioEventType.ENEMY_HIT);
                }
            }
            // -------------------

            //Tree -----------
            for (GameObject obj : objects) {
                if (obj.isRemoved()) continue;
                if (obj instanceof OBJ_Tree tree
                        && !player.isAttackDamageApplied()
                        && attackArea.intersects(tree.getSolidWorldArea())) {

                    player.setAttackDamageApplied(true);
                    tree.interact();

                    //Audio ----------------------
                    emitAudioEvent(AudioEventType.TREE_HIT);
                }
            }
            // -------------------
        }
    }
    //-------------------------------------------------------------
    private void updateMonk(InputState input) {
        // Monk Talking ----------------------

        if (monk.getState() == MonkState.IDLE) {
            currentDialogue = "";
        }

        if (monk.getState() == MonkState.TALKING && currentDialogue.isEmpty()) {
            currentDialogue = monk.getCurrentDialogue();
            // Audio ----------
            emitAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
        }

        if (monk.getState() == MonkState.TALKING && input.interact()) {
            monk.nextDialogue();

            if (!monk.hasFinishedDialogue()) {
                currentDialogue = monk.getCurrentDialogue();
                // Audio ----------
                emitAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
            } else {
                currentDialogue = "";
                monk.setState(MonkState.DISAPPEARING);
                // Audio ----------
                emitAudioEvent(AudioEventType.DIALOGUE_CLOSE);
            }
        }
        // ----------------------------------
    }
    //-------------------------------------------------------------
    private void updateEvents(int lifeBeforeUpdate) {
        if (player.getLife() < lifeBeforeUpdate) {
            emitAudioEvent(AudioEventType.PLAYER_DAMAGED);
        }
    }
    //-------------------------------------------------------------
    private void updateDeathSequence(double deltaMs) {
        monk.update(player, deltaMs);
        // Keep only finite transitions running; do not start new gameplay logic.
        for (EnemyTNT tnt : tntEnemies) {
            if (tnt.getState() == TNTState.TRIGGERED || tnt.getState() == TNTState.EXPLODING) {
                tnt.update(player, deltaMs);
            }
        }
        tntEnemies.removeIf(EnemyTNT::isExploded);

        for (DynamiteProjectile proj : projectiles) {
            proj.update(deltaMs);
            collisionChecker.checkTile(proj);
        }
        projectiles.removeIf(DynamiteProjectile::isExploded);
    }
    //-------------------------------------------------------------
    private void updateGameOverCountdown(double deltaMs) {
        boolean readyForGameOver = player.isDeathAnimationCompleted() && !hasPendingTransientAnimations();
        if (readyForGameOver) {
            deadStateElapsedMs += deltaMs;
        } else {
            deadStateElapsedMs = 0.0;
        }
        if (deadStateElapsedMs >= UIConfig.GAME_OVER_DELAY_MS) {
            gameState = GameState.GAME_OVER;
            deadStateElapsedMs = 0.0;
        }
    }
    //-------------------------------------------------------------
    private void updateState(InputState input) {
        if (player.isDying() || player.isDead()) {
            gameState = GameState.PLAYING;
            return;
        }
        if(input.pause()){
            gameState = GameState.PAUSED;
        }else {
            gameState = GameState.PLAYING;
        }
    }
    //-------------------------------------------------------------
    //end updates method ------------------------------------------


    /**
     * Returns true while finite transition animations are still running.
     */
    //-------------------------------------------------------------
    private boolean hasPendingTransientAnimations() {

        for (EnemyTNT tnt : tntEnemies) {
            if (tnt.getState() == TNTState.TRIGGERED || tnt.getState() == TNTState.EXPLODING) {
                return true;
            }
        }

        if (!projectiles.isEmpty()) {
            return true;
        }

        return false;
    }
    //-------------------------------------------------------------

    /**
     * Audio event emitter
      */
    //-------------------------------------------------------------
    private void emitAudioEvent(AudioEventType audioEventType) {
        pendingAudioEvents.add(audioEventType);
    }
    public List<AudioEventType> consumeAudioEvents() {
        if (pendingAudioEvents.isEmpty()) {
            return List.of();
        }
        List<AudioEventType> snapshot = List.copyOf(pendingAudioEvents);
        pendingAudioEvents.clear();
        return snapshot;
    }
    //-------------------------------------------------------------


    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    public GameState getGameState() { return gameState; }
    public List<GameObject> getObjects() { return objects; }
    public boolean isDebugMode() { return debugMode; }
    public int getTILE_SIZE(){ return gameConfig.screenConfig().TILE_SIZE(); }
    public Monk getMonk() {
        return monk;
    }
    public List<EnemyTNT> getTntEnemies() { return tntEnemies; }
    public List<EnemyDynamite> getDynamiteEnemies() { return dynamiteEnemies; }
    public String getCurrentDialogue() { return currentDialogue; }
    public List<DynamiteProjectile> getProjectiles(){
        return projectiles;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    //---------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
