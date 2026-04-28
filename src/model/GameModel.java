package model;

import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.DynamiteState;
import main.CONFIG.enu.GameState;
import main.CONFIG.enu.PlayerState;
import main.CONFIG.enu.MonkState;
import main.CONFIG.enu.TNTState;
import main.CONFIG.GameConfig;
import model.CollisionChecker;
import model.entity.Player;
import model.entity.Monk;
import model.object.ObjectManager;
import model.object.GameObject;
import model.entity.EnemyDynamite;
import model.entity.EnemyTNT;
import java.util.ArrayList;
import java.util.Iterator;
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

    private List<EnemyTNT> tntEnemies = new ArrayList<>();
    private List<EnemyDynamite> dynamiteEnemies = new ArrayList<>();
    
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

        // create the enemy
        for(SpawnPoint sp : GS.entityConfig().TNT_SPAWNPOINT()){
            for (int i = 0; i < GS.entityConfig().NPC_FOR_SPAWNPOINT; i++) {
                tntEnemies.add(new EnemyTNT(sp, GS.entityConfig()));

            }
        }
        dynamiteEnemies.add(new EnemyDynamite(new SpawnPoint(44*64, 29*64, 2), GS.entityConfig()));
        dynamiteEnemies.add(new EnemyDynamite(new SpawnPoint(44*64, 29*64, 2), GS.entityConfig()));

        gameState = GameState.PLAYING;
 
    }
    //-------------------------------------------------------------

    /**
     * Update the model status
     */
    //-------------------------------------------------------------
    public void update(InputState input, double deltaMs) {

        if (gameState == GameState.PLAYING) {
            player.update(input, deltaMs);

            collisionChecker.checkTile(player);
            collisionChecker.checkObjects(player);

            boolean monkCollision = collisionChecker.checkMonk(player, monk);
            
            Iterator<EnemyTNT> tntIterator = tntEnemies.iterator();
            while (tntIterator.hasNext()) {
                EnemyTNT tnt = tntIterator.next();
                if (tnt.getState() != TNTState.EXPLODED) {
                    collisionChecker.checkEntity(player, tnt);
                }
                tnt.update(player, deltaMs);

                if (tnt.getState() == TNTState.EXPLODED) {
                    tntIterator.remove();
                    continue;
                }

                collisionChecker.checkTile(tnt);
                collisionChecker.checkObjects(tnt);
                if (tnt.getState() == TNTState.WANDER) {
                    collisionChecker.checkEntity(tnt, player);
                }
                tnt.move();
            }

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
            
            if (player.getState() == PlayerState.WALKING) {
                player.move();
            }
            updateInteractions(input, monkCollision);

            objectManager.update(deltaMs);



        }
    }
    //-------------------------------------------------------------
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
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    //---------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
