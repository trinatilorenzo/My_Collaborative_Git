package model;

//import controller.KeyHandler;
import model.collision.CollisionChecker;
import model.entity.Player;
import model.world.GameMap;
import model.object.GameObject;

import input.InputState;
import main.GameSetting.GameState;
import main.GameSetting.PlayerState;

import static main.GameSetting.*;

import java.util.ArrayList;
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
    private List<GameObject> objects; 
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
    public void update(InputState input, double deltaMs) {

        if (gameState == GameState.PLAYING) {
            player.update(input, deltaMs);

            collisionChecker.checkTile(player);
            if (player.getState() == PlayerState.WALKING) {
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
    public List<GameObject> getObjects() { return objects; }
    public boolean isDebugMode() { return debugMode; }
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
