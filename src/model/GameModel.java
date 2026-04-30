package model;

import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.DynamiteState;
import main.CONFIG.enu.GameState;
import main.CONFIG.enu.PlayerState;
import main.CONFIG.enu.MonkState;
import main.CONFIG.enu.TNTState;
import main.CONFIG.enu.TreeState;
import main.CONFIG.GameConfig;
import model.CollisionChecker;
import model.entity.Player;
import model.entity.Monk;
import model.object.ObjectManager;
import model.object.GameObject;
import model.entity.EnemyDynamite;
import model.entity.EnemyTNT;
import model.entity.DynamiteProjectile;
import java.util.ArrayList;
import java.util.List;

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

    private final CollisionChecker collisionChecker;

    private final GameMap worldGameMap;
    private final ObjectManager objectManager;

    private final Player player;
    private final Monk monk;

    private GameState gameState;
    private boolean debugMode = false;
    private String currentDialogue = "";
    private int mainMenuSelection = 0;
    private int hoveredRibbon = -1;
    private int activeRibbon = -1;
    private boolean hoveredGameOverButton = false;

    // TODO: TNT from file
    private List<EnemyTNT> tntEnemies = new ArrayList<>();
    private List<EnemyDynamite> dynamiteEnemies = new ArrayList<>();
    private List<DynamiteProjectile> projectiles = new ArrayList<>();

    /**
     * COSTRUCTOR
      */
    //-------------------------------------------------------------
    public GameModel(GameConfig GS) {
        gameConfig = GS;
        worldGameMap = new GameMap(GS.mapConfig(), GS.mapDoc());
        player = new Player(GS.entityConfig());

        collisionChecker = new CollisionChecker(this);
        objectManager = new ObjectManager(GS.ObjConfig(), GS.mapDoc());

        monk = new Monk(GS.entityConfig().MONK_START_X(), GS.entityConfig().MONK_START_Y(), GS.entityConfig());

        initializeEnemies();

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
                return;
            }

            player.update(input, deltaMs);

            collisionChecker.checkTile(player);
            collisionChecker.checkObjects(player);

            boolean monkCollision = collisionChecker.checkMonk(player, monk);
            
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

                if (collisionChecker.checkEntity(player, proj)){
                    player.takeDamage();
                    proj.explode();
                }
            }
            // Remove exploded projectiles
            projectiles.removeIf(DynamiteProjectile::isExploded);


            if (player.getState() == PlayerState.WALKING) {
                player.move();
            }
            updateInteractions(input, monkCollision);

            objectManager.update(deltaMs);



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

        objectManager.update(deltaMs);
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

        for (GameObject obj : objectManager.getObjects()) {
            if (obj instanceof OBJ_Tree tree && tree.getState() == TreeState.CHOPPING) {
                return true;
            }
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
        objectManager.reset(gameConfig.mapDoc());
        initializeEnemies();
        gameState = GameState.PLAYING;
    }

    private void initializeEnemies() {
        projectiles = new ArrayList<>();
        tntEnemies = new ArrayList<>();
        dynamiteEnemies = new ArrayList<>();

        for (SpawnPoint sp : gameConfig.entityConfig().TNT_SPAWNPOINT()) {
            for (int i = 0; i < gameConfig.entityConfig().NPC_FOR_SPAWNPOINT; i++) {
                tntEnemies.add(new EnemyTNT(sp, gameConfig.entityConfig()));
            }
        }

        dynamiteEnemies.add(new EnemyDynamite(new SpawnPoint(60 * 64, 40 * 64, 2), gameConfig.entityConfig(), projectiles));
        dynamiteEnemies.add(new EnemyDynamite(new SpawnPoint(60 * 64, 40 * 64, 2), gameConfig.entityConfig(), projectiles));
    }
    //TODO controllare bene

    /**
     * Interactions with objects
     */
    //-------------------------------------------------------------
    //TODO better timing and animation
    private void updateInteractions(InputState input, boolean monkCollision) {

        for (GameObject obj : objectManager.getObjects()) {

            if (obj.isRemoved()) continue; // Skip removed objects

            // oggetti che richiedono attacco
            Rectangle attackArea = player.getAttackArea();
            if (player.getState() == PlayerState.ATTACKING) {
                if (obj instanceof OBJ_Tree) {
                    OBJ_Tree tree = (OBJ_Tree) obj;

                    // Se collide con l'area di attacco → colpisci
                    if (attackArea.intersects(tree.getSolidWorldArea())) {
                        tree.interact(); // qui hit() viene chiamato → chopped = true se health <= 0
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
    public List<GameObject> getObjects() { return objectManager.getObjects(); }
    public ObjectManager getObjectManager() { return objectManager; }
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
