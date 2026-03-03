package model;

import controller.KeyHandler;
import model.collision.CollisionChecker;
import model.entity.Player;
import model.world.GameMap;

import static main.GameSetting.*;

// ALL THE GAME MODEL STAFF HERE
// world map , entity, combat, ai, events ...
//-------------------------------------------------------------------------------------------------------------------
public class GameModel {

    private GameMap worldGameMap;
    private Player player;

    private CollisionChecker collisionChecker;

    public GameModel() {
        worldGameMap = new GameMap(MAP_PATH, MAX_WORLD_ROW, MAX_WORLD_COL, MAP_LAYER_NUM);
        player = new Player();
        collisionChecker = new CollisionChecker(this);
    }

    // update the model status
    public void update(KeyHandler keyH) {
        player.update(keyH); // update player status
        collisionChecker.checkTile(player); // check collision with tiles

        if (!player.isCollisionOn()){
            player.move();
        }

        // altri update: nemici, oggetti, eventi
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------