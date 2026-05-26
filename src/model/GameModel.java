package model;

import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.EntityConfig;
import main.CONFIG.enu.DynamiteState;
import main.CONFIG.enu.GameState;
import main.CONFIG.enu.PlayerState;
import main.CONFIG.enu.MonkState;
import main.CONFIG.enu.TNTState;
import main.CONFIG.GameConfig;
import model.entity.Player;
import model.entity.Monk;
import model.object.GameObject;
import model.entity.EnemyDynamite;
import model.entity.EnemyTNT;
import model.entity.DynamiteProjectile;
import model.event.AudioEventType;

import java.util.*;

import model.object.OBJ_Tree;

import controller.InputState;

import java.awt.Rectangle;

/**
 * ALL THE GAME MODEL STAFF HERE
 * world map, entity, combat, AI, events ...
*/
//-------------------------------------------------------------------------------------------------------------------
public class GameModel {
    private static final double GAME_OVER_DELAY_MS = 150.0;
    private static final int MAIN_MENU_ITEM_COUNT = 3;

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

    // System Status
    private String currentDialogue; // diaologue currently displayed to the player
    private String statusMessage; // status message to be displayed for a short time
    private double statusMessageTimerMs;
    //-------------------------------------------------------------

    private double deadStateElapsedMs;

    private final List<AudioEventType> pendingAudioEvents = new ArrayList<>();
    private int mainMenuSelection;
    private int hoveredRibbon;
    private int activeRibbon;
    private boolean hoveredGameOverButton;

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
        statusMessage = "";
        statusMessageTimerMs = 0.0;

        deadStateElapsedMs = 0.0;
        resetMenuUiState();

    }
    //-------------------------------------------------------------

    /**
     * Start a new game from scratch
     */
    //-------------------------------------------------------------
    public void initializeNewGame(){
        resetMenuUiState();

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
            objects.add(new OBJ_Tree(
                    treeTag,
                    spawnPoint.x(), spawnPoint.y(), spawnPoint.layer(),
                    treeWidth, treeHeight,
                    createTreeSolidArea(treeWidth, hitboxOffsetY, objConfig),
                    objConfig
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

        // TODO scriverlo più leggibile
        // MENU
        if (gameState == GameState.MENU){
            return; // --> No model update
        }
        //GAME OVER
        if (gameState == GameState.GAME_OVER) {
            return; // --> No model update
        }

        // PAUSE
        updateState(input); // TODO serve ?

        if (gameState != GameState.PLAYING) {
            updateRuntimeMessages(deltaMs);
            return;
        }

        // DEATH
        if (player.isDying() || player.isDead()) {
            updateDeathSequence(deltaMs);
            updateRuntimeMessages(deltaMs);
            updateGameOverCountdown(deltaMs);
            return;
        }

        // PLAYING
        updatePlayingState(input, deltaMs);
    }
    //-------------------------------------------------------------


    // ALL THE UPDATE METHOD
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void updatePlayingState(InputState input, double deltaMs) {

        //save condition before update
        int lifeBeforeUpdate = player.getLife();
        int projectileCountBeforeUpdate = projectiles.size();

        updatePlayerPhase(input, deltaMs);

        updateEnemiesPhase(deltaMs);
        updateProjectilesPhase(deltaMs);

        if (player.getState() == PlayerState.WALKING) {
            player.move();
        }
        monk.update(player, deltaMs);
        updateInteractions(input);

        updateRuntimeMessages(deltaMs);
        updateGameOverCountdown(deltaMs);
        updatePostFrameEvents(lifeBeforeUpdate, projectileCountBeforeUpdate);

    }
    //-------------------------------------------------------------
    private void updatePlayerPhase(InputState input, double deltaMs) {

        PlayerState playerStateBeforeUpdate = player.getState();
        player.update(input, deltaMs);

        if (playerStateBeforeUpdate != PlayerState.ATTACKING && player.getState() == PlayerState.ATTACKING) {
            emitAudioEvent(AudioEventType.PLAYER_ATTACK);
        }

        collisionChecker.checkTile(player);
        collisionChecker.checkObjects(player);
    }
    //-------------------------------------------------------------
    private void updateEnemiesPhase(double deltaMs) {
        for (EnemyTNT tnt : tntEnemies) {
            TNTState previousState = tnt.getState();
            if (tnt.getState() != TNTState.EXPLODED) {
                collisionChecker.checkEntity(player, tnt);
            }
            tnt.update(player, deltaMs);
            if (previousState != tnt.getState() && tnt.getState() == TNTState.EXPLODING) {
                emitAudioEvent(AudioEventType.TNT_EXPLOSION);
            }
            if (previousState != tnt.getState() && tnt.getState() == TNTState.TRIGGERED) {
                emitAudioEvent(AudioEventType.TNT_TRIGGERED);
            }

            collisionChecker.checkTile(tnt);
            collisionChecker.checkObjects(tnt);
            if (tnt.getState() == TNTState.WANDER) {
                collisionChecker.checkEntity(tnt, player);
            }
            tnt.move();
        }
        tntEnemies.removeIf(EnemyTNT::isExploded);

        for (EnemyDynamite dynamite : dynamiteEnemies) {
            DynamiteState previousState = dynamite.getState();
            collisionChecker.checkEntity(player, dynamite);
            dynamite.update(player, deltaMs);
            if (previousState != DynamiteState.ATTACKING && dynamite.getState() == DynamiteState.ATTACKING) {
                emitAudioEvent(AudioEventType.ENEMY_ALERT);
            }

            collisionChecker.checkTile(dynamite);
            collisionChecker.checkObjects(dynamite);
            if (dynamite.getState() == DynamiteState.WANDER) {
                collisionChecker.checkEntity(dynamite, player);
            }
            dynamite.move();
        }
        dynamiteEnemies.removeIf(EnemyDynamite::isDead);
    }
    //-------------------------------------------------------------
    private void updateInteractions(InputState input) {
        if (player.getState() == PlayerState.ATTACKING) {
            Rectangle attackArea = player.getAttackArea();

            for (GameObject obj : objects) {
                if (obj.isRemoved()) continue;
                if (obj instanceof OBJ_Tree tree && attackArea.intersects(tree.getSolidWorldArea())) {
                    tree.interact();
                    emitAudioEvent(AudioEventType.TREE_HIT);
                }
            }

            for (EnemyDynamite enemy : dynamiteEnemies) {
                if (!attackArea.intersects(enemy.getSolidWorldArea())) continue;
                DynamiteState previousState = enemy.getState();
                enemy.takeDamage();
                emitAudioEvent(AudioEventType.ENEMY_HIT);
                if (previousState != DynamiteState.DEAD && enemy.getState() == DynamiteState.DEAD) {
                    emitAudioEvent(AudioEventType.ENEMY_DEFEATED);
                }
            }

            for (EnemyTNT tnt : tntEnemies) {
                if (!attackArea.intersects(tnt.getSolidWorldArea())) continue;
                TNTState previousState = tnt.getState();
                tnt.takeDamage();
                emitAudioEvent(AudioEventType.ENEMY_HIT);
                if (previousState != TNTState.EXPLODED && tnt.getState() == TNTState.EXPLODED) {
                    emitAudioEvent(AudioEventType.ENEMY_DEFEATED);
                }
            }
        }

        if (monk.getState() == MonkState.IDLE) {
            currentDialogue = "";
        }

        if (monk.getState() == MonkState.TALKING && currentDialogue.isEmpty()) {
            currentDialogue = monk.getCurrentDialogue();
            emitAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
        }

        if (monk.getState() == MonkState.TALKING && input.interact()) {
            monk.nextDialogue();

            if (!monk.hasFinishedDialogue()) {
                currentDialogue = monk.getCurrentDialogue();
                emitAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
            } else {
                currentDialogue = "";
                monk.setState(MonkState.DISAPPEARING);
                emitAudioEvent(AudioEventType.DIALOGUE_CLOSE);
            }
        }

        //TODO: gestione attacco player danno

    }
    //-------------------------------------------------------------
    private void updateProjectilesPhase(double deltaMs) {
        for (DynamiteProjectile proj : projectiles) {
            proj.update(deltaMs);
            collisionChecker.checkTile(proj);

            if (collisionChecker.intersects(player, proj)) {
                player.takeDamage();
                proj.explode();
            }
        }
        projectiles.removeIf(DynamiteProjectile::isExploded);
    }
    //-------------------------------------------------------------
    private void updatePostFrameEvents(int lifeBeforeUpdate, int projectileCountBeforeUpdate) {
        if (player.getLife() < lifeBeforeUpdate) {
            emitAudioEvent(AudioEventType.PLAYER_DAMAGED);
        }
        if (projectiles.size() > projectileCountBeforeUpdate) {
            emitAudioEvent(AudioEventType.PROJECTILE_LAUNCHED);
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
    private void updateGameOverCountdown(double deltaMs) {
        boolean readyForGameOver = player.isDeathAnimationCompleted() && !hasPendingTransientAnimations();
        if (readyForGameOver) {
            deadStateElapsedMs += deltaMs;
        } else {
            deadStateElapsedMs = 0.0;
        }
        if (deadStateElapsedMs >= GAME_OVER_DELAY_MS) {
            gameState = GameState.GAME_OVER;
            deadStateElapsedMs = 0.0;
        }
    }
    //-------------------------------------------------------------
    private void updateRuntimeMessages(double deltaMs) {
        if (statusMessageTimerMs > 0) {
            statusMessageTimerMs -= deltaMs;
            if (statusMessageTimerMs <= 0) {
                statusMessage = "";
                statusMessageTimerMs = 0.0;
            }
        }
    }
    //-------------------------------------------------------------
    //end updates method ------------------------------------------


    /**
     * Load a saved game from file
     */
    //-------------------------------------------------------------
    private void loadSavedGame(){
        //TODO load saved game
    }
    //-------------------------------------------------------------










    public boolean hasPendingTransientAnimations() {
        if (player.isDying()) {
            return true;
        }
        if (monk.getState() == MonkState.DISAPPEARING) {
            return true;
        }

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





    private void showStatusMessage(String message, double durationMs) {
        statusMessage = message;
        statusMessageTimerMs = durationMs;
    }

    private void clearStatusMessage() {
        statusMessage = "";
        statusMessageTimerMs = 0.0;
    }


    public void selectMainMenuItem(int selection) {
        mainMenuSelection = Math.max(0, Math.min(MAIN_MENU_ITEM_COUNT - 1, selection));
    }

    public void selectPreviousMainMenuItem() {
        mainMenuSelection = (mainMenuSelection - 1 + MAIN_MENU_ITEM_COUNT) % MAIN_MENU_ITEM_COUNT;
    }

    public void selectNextMainMenuItem() {
        mainMenuSelection = (mainMenuSelection + 1) % MAIN_MENU_ITEM_COUNT;
    }

    public void confirmMainMenuSelection() {
        if (mainMenuSelection == 0) {
            initializeNewGame();
        }
    }

    public void handleMainMenuButtonClick(int buttonIndex) {
        selectMainMenuItem(buttonIndex);
        if (buttonIndex == 0) {
            initializeNewGame();
        }
    }

    public void handleMainMenuConfirm() {
        confirmMainMenuSelection();
    }

    public void requestNewGame() {
        initializeNewGame();
    }

    public void setHoveredRibbon(int hoveredRibbon) {
        this.hoveredRibbon = hoveredRibbon;
    }

    public void setActiveRibbon(int activeRibbon) {
        this.activeRibbon = activeRibbon;
    }

    public void setHoveredGameOverButton(boolean hoveredGameOverButton) {
        this.hoveredGameOverButton = hoveredGameOverButton;
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
    public String getStatusMessage() { return statusMessage; }
    public int getMainMenuSelection() { return mainMenuSelection; }
    public int getHoveredRibbon() { return hoveredRibbon; }
    public int getActiveRibbon() { return activeRibbon; }
    public boolean isHoveredGameOverButton() { return hoveredGameOverButton; }

    public List<AudioEventType> consumeAudioEvents() {
        if (pendingAudioEvents.isEmpty()) {
            return List.of();
        }
        List<AudioEventType> snapshot = List.copyOf(pendingAudioEvents);
        pendingAudioEvents.clear();
        return snapshot;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    //---------------------------------


    private void emitAudioEvent(AudioEventType audioEventType) {
        pendingAudioEvents.add(audioEventType);
    }

    private void resetMenuUiState() {
        mainMenuSelection = 0;
        hoveredRibbon = -1;
        activeRibbon = -1;
        hoveredGameOverButton = false;
    }

}
//-------------------------------------------------------------------------------------------------------------------
