package model;

import controller.KeyHandler;
import model.collision.CollisionChecker;
import model.entity.Player;
import model.world.GameMap;

import static main.GameSetting.*;

/**
 * ALL THE GAME MODEL STAFF HERE
 * world map, entity, combat, AI, events ...
*/
//-------------------------------------------------------------------------------------------------------------------
public class GameModel {

    private GameMap worldGameMap;
    private Player player;
    private CollisionChecker collisionChecker;

    private GameState gameState;
    private boolean debugMode = false;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameModel() {
        worldGameMap = new GameMap(MAP_PATH, MAX_WORLD_ROW, MAX_WORLD_COL, GRAPHIC_LAYER_NUM, GAME_LAYER_NUM);
        player = new Player();
        collisionChecker = new CollisionChecker(this);
        gameState = GameState.PLAYING;
    }
    //-------------------------------------------------------------

    /**
     * Update the model status
     */
    //-------------------------------------------------------------
    public void update(KeyHandler keyH, double deltaMs) {

        if (gameState == GameState.PLAYING) {
            player.update(keyH, deltaMs); // update player status time-based
            collisionChecker.checkTile(player); // check collision with tiles

            //move player
            if (!player.isCollisionOn() && player.getState() == PlayerState.WALKING) {
                player.move();
            }
        }

    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    public GameState getGameState() { return gameState; }
    public boolean isDebugMode() { return debugMode; }
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
