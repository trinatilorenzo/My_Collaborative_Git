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

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameModel() {
        worldGameMap = new GameMap(MAP_PATH, MAX_WORLD_ROW, MAX_WORLD_COL, GRAPHIC_LAYER_NUM, GAME_LAYER_NUM);
        player = new Player();
        collisionChecker = new CollisionChecker(this);
    }
    //-------------------------------------------------------------

    /**
     * Update the model status
     */
    //-------------------------------------------------------------
    public void update(KeyHandler keyH, double deltaMs) {
        player.update(keyH); // update player status (speed currently frame-based)
        collisionChecker.checkTile(player); // check collision with tiles

        //move player
        if (!player.isCollisionOn() && player.getState() == PlayerState.WALKING) {
            player.move();
        }

    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    //---------------------------------

    // SETTER ----------------------

    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
