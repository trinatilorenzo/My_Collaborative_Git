package model;

import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.object.OBJ_Tree;

import controller.InputState;

import java.awt.Rectangle;

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
    private boolean debugMode = false;
    //-------------------------------------------------------------

    // Collision
    private final CollisionChecker collisionChecker;
    //-------------------------------------------------------------

    // Map & OBJ
    private final GameMap worldGameMap;
    private final List<GameObject> objects = new ArrayList<>();

    // Player & NPC
    //-------------------------------------------------------------
    private final Player player;
    private Monk monk;
    private List<EnemyTNT> tntEnemies;
    private List<EnemyDynamite> dynamiteEnemies;
    private List<DynamiteProjectile> projectiles;
    //-------------------------------------------------------------


    // TODO da rivedere tuttta sta roba
    private static final double STAIR_LOCKED_MSG_DURATION_MS = 2200.0;
    private static final double STAIR_UNLOCKED_MSG_DURATION_MS = 2600.0;
    private static final double STAIR_LOCKED_MSG_COOLDOWN_MS = 1200.0;

    private record StairTile(int col, int row) {}

    private String currentDialogue = "";
    private int mainMenuSelection = 0;
    private int hoveredRibbon = -1;
    private int activeRibbon = -1;
    private boolean hoveredGameOverButton = false;

    // Level -> blocked stair tiles (tile coordinates) for that level.
    // When that level is cleared, the collision tiles on (level - 1) are opened.
    private final Map<Integer, List<StairTile>> lockedStairsByLevel = new HashMap<>();
    private final Set<Integer> unlockedStairsLevels = new HashSet<>();

    private String statusMessage = "";
    private double statusMessageTimerMs = 0.0;
    private double stairsLockedMessageCooldownMs = 0.0;

    /**
     * COSTRUCTOR
      */
    //-------------------------------------------------------------
    public GameModel(GameConfig GS) {
        gameConfig = GS;
        worldGameMap = new GameMap(GS.mapConfig(), GS.mapDoc());
        collisionChecker = new CollisionChecker(this);

        player = new Player(GS.entityConfig());
        initializeNPC();
        initialieOBJ();

        initializeLockedStairsConfig();

        initializeStairLocks();

        gameState = GameState.MENU;
 
    }
    //-------------------------------------------------------------

    /**
     * Update the model status
     */
    //-------------------------------------------------------------
    public void update(InputState input, double deltaMs) {

        if (gameState == GameState.PLAYING) {
            if (player.isDying() || player.isDead()) {
                updateDeathSequence(deltaMs);
                updateRuntimeMessages(deltaMs);
                return;
            }

            player.update(input, deltaMs);

            collisionChecker.checkTile(player);
            collisionChecker.checkObjects(player);

            boolean monkCollision = collisionChecker.intersects(player, monk);
            
            for (EnemyTNT tnt : tntEnemies) {
                if (tnt.getState() != TNTState.EXPLODED) {
                    collisionChecker.checkEntity(player, tnt);
                }
                tnt.update(player, deltaMs);

                collisionChecker.checkTile(tnt);
                collisionChecker.checkObjects(tnt);
                if (tnt.getState() == TNTState.WANDER) {
                    collisionChecker.checkEntity(tnt, player);
                }
                tnt.move();
            }
            tntEnemies.removeIf(EnemyTNT::isExploded);

            for (EnemyDynamite dynamite : dynamiteEnemies){
                collisionChecker.checkEntity(player, dynamite);
                dynamite.update(player, deltaMs);

                collisionChecker.checkTile(dynamite);
                collisionChecker.checkObjects(dynamite);
                if (dynamite.getState() == DynamiteState.WANDER) {
                    collisionChecker.checkEntity(dynamite, player);
                }
                dynamite.move();
            }
            dynamiteEnemies.removeIf(EnemyDynamite::isDead);

            for (DynamiteProjectile proj : projectiles) {
                proj.update(deltaMs);
                collisionChecker.checkTile(proj);

                if (collisionChecker.intersects(player, proj)){
                    player.takeDamage();
                    proj.explode();
                }
            }
            // Remove exploded projectiles
            projectiles.removeIf(DynamiteProjectile::isExploded);

            evaluateLevelUnlocks();

            if (player.getState() == PlayerState.WALKING) {
                player.move();
            }
            updateInteractions(input, monkCollision);

            updateRuntimeMessages(deltaMs);



        }
    }
    //-------------------------------------------------------------

    /**
     * UTILITY METODH initialize NPC by reading the spawn point from config file
     */
    //-------------------------------------------------------------
    private void initializeNPC() {
        //load the monk
        monk = new Monk(gameConfig.entityConfig().MONK_START_X(),
                        gameConfig.entityConfig().MONK_START_Y(),
                        gameConfig.entityConfig());
        //load the tnt
        tntEnemies = new ArrayList<>();
        for (SpawnPoint sp : gameConfig.entityConfig().TNT_SPAWNPOINT()) {
            for (int i = 0; i < gameConfig.entityConfig().TNT_FOR_SPAWNPOINT; i++) {
                tntEnemies.add(new EnemyTNT(sp, gameConfig.entityConfig()));
            }
        }

        //load the dynamite
        dynamiteEnemies = new ArrayList<>();
        projectiles = new ArrayList<>();
        for (SpawnPoint sp : gameConfig.entityConfig().DYNAMITE_SPAWNPOINT()) {
            for (int i = 0; i < gameConfig.entityConfig().DYNAMITE_FOR_SPAWNPOINT; i++) {
                dynamiteEnemies.add(new EnemyDynamite(sp, gameConfig.entityConfig(), projectiles));
            }
        }
    }

    /**
     * UTILITY METODH initialize OBJ by reading the spawn point from config file
     */
    //-------------------------------------------------------------
    private void initialieOBJ(){
        ObjConfig objC = gameConfig.ObjConfig();
        Rectangle solidArea = new Rectangle(objC.TREE_03_WIDTH/2 - (objC.TREE_HITBOX_WIDTH/2),
                195, //TODO better
                objC.TREE_HITBOX_WIDTH,
                objC.TREE_HITBOX_HEIGHT);
        //load the first type of tree
        for (SpawnPoint sp : gameConfig.ObjConfig().TREES_03_SPAWNPOINT()) {
            objects.add(new OBJ_Tree(objC.TREE_TAG_03(),
                            sp.x(), sp.y(), sp.layer(),
                            objC.TREE_03_WIDTH, objC.TREE_03_HEIGHT,solidArea,
                            gameConfig.ObjConfig()));
        }
        Rectangle solidArea2 = new Rectangle(objC.TREE_02_WIDTH/2 - (objC.TREE_HITBOX_WIDTH/2),
                135, //TODO better
                objC.TREE_HITBOX_WIDTH,
                objC.TREE_HITBOX_HEIGHT);
        // load the second type of tree
        for (SpawnPoint sp : gameConfig.ObjConfig().TREES_02_SPAWNPOINT()) {
            objects.add(new OBJ_Tree(objC.TREE_TAG_02(),
                    sp.x(), sp.y(), sp.layer(),
                    objC.TREE_02_WIDTH, objC.TREE_02_HEIGHT,solidArea2,
                    gameConfig.ObjConfig()));
        }
        // load the third type of tree
        for (SpawnPoint sp : gameConfig.ObjConfig().TREES_01_SPAWNPOINT()) {
            objects.add(new OBJ_Tree(objC.TREE_TAG_01(),
                    sp.x(), sp.y(), sp.layer(),
                    objC.TREE_01_WIDTH, objC.TREE_01_HEIGHT,solidArea2,
                    gameConfig.ObjConfig()));
        }

    }
    //-------------------------------------------------------------

    private void updateDeathSequence(double deltaMs) {
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

    public void resetForNewGame() {
        player.resetForNewGame();
        monk.resetDialogue();
        currentDialogue = "";
        hoveredRibbon = -1;
        activeRibbon = -1;
        hoveredGameOverButton = false;
        clearStatusMessage();
        initializeNPC();
        initializeStairLocks();
        gameState = GameState.PLAYING;
    }


    private void initializeLockedStairsConfig() {
        // Layer 2 -> Layer 1 gate (existing map note)
        lockedStairsByLevel.put(2, List.of(
                new StairTile(42, 25),
                new StairTile(43, 25),
                new StairTile(44, 25)
        ));

        // Layer 3 -> Layer 2 gate (existing map note)
        lockedStairsByLevel.put(3, List.of(
                new StairTile(57, 43),
                new StairTile(58, 43),
                new StairTile(59, 43)
        ));
    }

    private void initializeStairLocks() {
        unlockedStairsLevels.clear();

        for (Map.Entry<Integer, List<StairTile>> entry : lockedStairsByLevel.entrySet()) {
            int level = entry.getKey();
            int collisionLayerToClose = level - 1;
            if (collisionLayerToClose < 0 || collisionLayerToClose >= worldGameMap.getGameLayerNum()) continue;

            for (StairTile tile : entry.getValue()) {
                worldGameMap.setCollisionTile(collisionLayerToClose, tile.row(), tile.col(), true);
            }
        }
    }

    private void evaluateLevelUnlocks() {
        List<Integer> levels = new ArrayList<>(lockedStairsByLevel.keySet());
        levels.sort(Comparator.reverseOrder());

        for (int level : levels) {
            if (unlockedStairsLevels.contains(level)) continue;
            if (hasLivingEnemiesOnLayer(level)) continue;

            unlockStairsForLevel(level);
        }
    }

    private boolean hasLivingEnemiesOnLayer(int layer) {
        for (EnemyTNT tnt : tntEnemies) {
            if (!tnt.isExploded() && tnt.getCurrentLayer() == layer) {
                return true;
            }
        }
        for (EnemyDynamite dynamite : dynamiteEnemies) {
            if (!dynamite.isDead() && dynamite.getCurrentLayer() == layer) {
                return true;
            }
        }
        return false;
    }

    private int getLivingEnemiesCountOnLayer(int layer) {
        int count = 0;
        for (EnemyTNT tnt : tntEnemies) {
            if (!tnt.isExploded() && tnt.getCurrentLayer() == layer) {
                count++;
            }
        }
        for (EnemyDynamite dynamite : dynamiteEnemies) {
            if (!dynamite.isDead() && dynamite.getCurrentLayer() == layer) {
                count++;
            }
        }
        return count;
    }

    private void unlockStairsForLevel(int level) {
        List<StairTile> tiles = lockedStairsByLevel.get(level);
        if (tiles == null || tiles.isEmpty()) {
            unlockedStairsLevels.add(level);
            return;
        }

        int collisionLayerToOpen = level - 1;
        if (collisionLayerToOpen < 0 || collisionLayerToOpen >= worldGameMap.getGameLayerNum()) {
            unlockedStairsLevels.add(level);
            return;
        }

        for (StairTile tile : tiles) {
            worldGameMap.setCollisionTile(collisionLayerToOpen, tile.row(), tile.col(), false);
        }

        unlockedStairsLevels.add(level);
        showStatusMessage("Hai sconfitto tutti i mostri del livello " + level + ". Scale sbloccate!", STAIR_UNLOCKED_MSG_DURATION_MS);
    }

    private boolean isLockedStairTile(int level, int row, int col) {
        List<StairTile> tiles = lockedStairsByLevel.get(level);
        if (tiles == null || tiles.isEmpty()) return false;

        for (StairTile tile : tiles) {
            if (tile.row() == row && tile.col() == col) {
                return true;
            }
        }
        return false;
    }

    private void showLockedStairsMessage(int level) {
        if (stairsLockedMessageCooldownMs > 0) return;

        int remaining = getLivingEnemiesCountOnLayer(level);
        showStatusMessage("Scale bloccate: elimina tutti i mostri del livello (" + remaining + " rimasti).", STAIR_LOCKED_MSG_DURATION_MS);
        stairsLockedMessageCooldownMs = STAIR_LOCKED_MSG_COOLDOWN_MS;
    }

    private void showStatusMessage(String message, double durationMs) {
        statusMessage = message;
        statusMessageTimerMs = durationMs;
    }

    private void clearStatusMessage() {
        statusMessage = "";
        statusMessageTimerMs = 0.0;
        stairsLockedMessageCooldownMs = 0.0;
    }

    private void updateRuntimeMessages(double deltaMs) {
        if (statusMessageTimerMs > 0) {
            statusMessageTimerMs -= deltaMs;
            if (statusMessageTimerMs <= 0) {
                statusMessage = "";
                statusMessageTimerMs = 0.0;
            }
        }

        if (stairsLockedMessageCooldownMs > 0) {
            stairsLockedMessageCooldownMs -= deltaMs;
            if (stairsLockedMessageCooldownMs < 0) {
                stairsLockedMessageCooldownMs = 0.0;
            }
        }
    }

    public void onPlayerBlockedByStairs(int currentLayer, int checkRow, int colLeft, int colRight) {
        if (unlockedStairsLevels.contains(currentLayer)) {
            return;
        }

        boolean blockedStairTile =
                isLockedStairTile(currentLayer, checkRow, colLeft)
                || isLockedStairTile(currentLayer, checkRow, colRight);

        if (!blockedStairTile) {
            return;
        }

        if (!hasLivingEnemiesOnLayer(currentLayer)) {
            unlockStairsForLevel(currentLayer);
            return;
        }

        showLockedStairsMessage(currentLayer);
    }
    //TODO controllare bene

    /**
     * Interactions with objects
     */
    //-------------------------------------------------------------
    //TODO better timing and animation
    private void updateInteractions(InputState input, boolean monkCollision) {

        for (GameObject obj : objects) {

            if (obj.isRemoved()) continue; // Skip removed objects

            if (player.getState() == PlayerState.ATTACKING) {

                Rectangle attackArea = player.getAttackArea();

                if (obj instanceof OBJ_Tree) {
                    OBJ_Tree tree = (OBJ_Tree) obj;

                    // Se collide con l'area di attacco → colpisci
                    if (attackArea.intersects(tree.getSolidWorldArea())) {
                        tree.interact(); // qui hit() viene chiamato → chopped = true se health <= 0
                    }
                }

                for (EnemyDynamite enemy : dynamiteEnemies) {
                    if (attackArea.intersects(enemy.getSolidWorldArea())) {
                        enemy.takeDamage();
                    }
                }

                for (EnemyTNT tnt : tntEnemies) {
                    if (attackArea.intersects(tnt.getSolidWorldArea())) {
                        tnt.takeDamage();
                    }
                }

            }

        }

        // Monk interaction triggered by collision
        if (monkCollision && monk.getState() == MonkState.IDLE) {
            monk.activate();
            currentDialogue = monk.getCurrentDialogue();
        }

        if (monk.getState() == MonkState.TALKING && input.interact()) {
            monk.advanceDialogue();

            if (!monk.hasFinishedDialogue()) {
                currentDialogue = monk.getCurrentDialogue();
            } else {
                currentDialogue = "";
                monk.setState(MonkState.DISAPPEARING);
            }
        }

        //TODO: gestione attacco player danno

    }
    //-------------------------------------------------------------

    //TODO ordine
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
    public int getMainMenuSelection() { return mainMenuSelection; }
    public int getHoveredRibbon() { return hoveredRibbon; }
    public int getActiveRibbon() { return activeRibbon; }
    public boolean isHoveredGameOverButton() { return hoveredGameOverButton; }
    public List<DynamiteProjectile> getProjectiles(){
        return projectiles;
    }
    public String getStatusMessage() { return statusMessage; }
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    public void setMainMenuSelection(int mainMenuSelection) { this.mainMenuSelection = mainMenuSelection; }
    public void setHoveredRibbon(int hoveredRibbon) { this.hoveredRibbon = hoveredRibbon; }
    public void setActiveRibbon(int activeRibbon) { this.activeRibbon = activeRibbon; }
    public void setHoveredGameOverButton(boolean hoveredGameOverButton) { this.hoveredGameOverButton = hoveredGameOverButton; }
    //---------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
