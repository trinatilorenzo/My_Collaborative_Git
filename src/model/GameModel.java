package model;

//import controller.KeyHandler;
import model.collision.CollisionChecker;
import model.entity.Player;
import model.world.GameMap;
import model.object.GameObject;
import model.object.WorldManager;
import model.object.OBJ_Tree;

import input.InputState;
import main.GameSetting.GameState;
import main.GameSetting.PlayerState;
import static main.GameSetting.*;

import java.awt.Rectangle;
import java.util.List;


/**
 * ALL THE GAME MODEL STAFF HERE
 * world map, entity, combat, AI, events ...
*/
//-------------------------------------------------------------------------------------------------------------------
public class GameModel {

    private GameMap worldGameMap;
    private Player player;
    private CollisionChecker collisionChecker;
    private WorldManager worldManager;
    private GameState gameState;
    private boolean debugMode = false;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameModel() {
        worldGameMap = new GameMap(MAP_PATH, MAX_WORLD_ROW, MAX_WORLD_COL, GRAPHIC_LAYER_NUM, GAME_LAYER_NUM);
        player = new Player();
        collisionChecker = new CollisionChecker(this);
        worldManager = new WorldManager();
        spawnStaticObjects();
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
            if (player.getState() == PlayerState.WALKING) {
                player.move();
            }

        // -------------------------
        // NUOVA LOGICA ATTACCO
        // -------------------------
        if (player.getState() == PlayerState.ATTACKING) {
            // Prendi l'area dell'attacco del player
            Rectangle attackArea = player.getAttackArea();

            for (GameObject obj : worldManager.getObjects()) {
                // Verifica solo gli alberi
                if (obj instanceof OBJ_Tree) {
                    OBJ_Tree tree = (OBJ_Tree) obj;

                    // Se collide con l'area di attacco → colpisci
                    if (attackArea.intersects(tree.getSolidWorldArea())) {
                        tree.interact(); // qui hit() viene chiamato → chopped = true se health <= 0
                    }
                }
            }
        }
        // -------------------------

            worldManager.update(deltaMs);
        }
    }
    //-------------------------------------------------------------
    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    public GameState getGameState() { return gameState; }
    public List<GameObject> getObjects() { return worldManager.getObjects(); }
    public WorldManager getWorldManager() { return worldManager; }
    public boolean isDebugMode() { return debugMode; }
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    //---------------------------------

    /**
     * Temporary bootstrap of world objects until a proper loader is provided.
     */
    private void spawnStaticObjects() {
        // place a handful of trees near the starting area
        int[][] treeTiles = {
            {60, 40},
            {52, 27},
            {54, 30},
            {56, 28}
        };

        for (int[] tile : treeTiles) {
            int worldX = tile[0] * TILE_SIZE;
            int worldY = tile[1] * TILE_SIZE;
            worldManager.add(new OBJ_Tree(worldX, worldY));
        }
    }

}
//-------------------------------------------------------------------------------------------------------------------
