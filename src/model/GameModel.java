package model;

import main.ENUM.GameState;
import main.ENUM.PlayerState;
import main.ENUM.MonkState;
import main.ENUM.TNTState;
import main.CONFIG.GameConfig;
import model.collision.CollisionChecker;
import model.entity.Player;
import model.entity.Monk;
import model.object.ObjectManager;
import model.world.GameMap;
import model.object.GameObject;
import model.entity.EnemyTNT;
import java.util.ArrayList;
import java.util.List;

import model.object.OBJ_Tree;

import input.InputState;

import java.awt.Rectangle;
import java.util.List;

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

    // TODO: TNT from file
    private List<EnemyTNT> tntEnemies = new ArrayList<>();
    
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

        gameState = GameState.PLAYING;

        //DEBUG
        tntEnemies.add(new EnemyTNT(49*64, 22*64, gameConfig.entityConfig()));
        tntEnemies.add(new EnemyTNT(54*64, 20*64, gameConfig.entityConfig()));
 
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
            if (player.getState() == PlayerState.WALKING) {
                player.move();
            }
            updateInteractions(input, monkCollision);

            objectManager.update(deltaMs);

            //TODO: da valutare se spostare 
            
            for (EnemyTNT tnt : tntEnemies) {

                tnt.update(player, deltaMs);

                collisionChecker.checkTile(tnt);
                collisionChecker.checkObjects(tnt);
                System.out.println("collisionX=" + tnt.isCollisionX() + 
                   " collisionY=" + tnt.isCollisionY());

                tnt.move();
            }


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
    public String getCurrentDialogue() { return currentDialogue; }
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    //---------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
