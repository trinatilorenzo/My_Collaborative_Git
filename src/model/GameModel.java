package model;

import controller.KeyHandler;
import model.entity.Player;
import model.world.Map;

import static main.GameSetting.*;

// ALL THE GAME MODEL STAFF HERE
// world map , entity, combat, ai, events ...
//-------------------------------------------------------------------------------------------------------------------
public class GameModel {

    private Map worldMap;
    private Player player;

    public GameModel() {
        worldMap = new Map(MAP_PATH, MAX_WORLD_ROW, MAX_WORLD_COL, MAP_LAYER_NUM);
        player = new Player();
    }

    // update the model status
    public void update(KeyHandler keyH) {
        player.update(keyH);
        // altri update: nemici, oggetti, eventi
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public Map getWorldMap() { return worldMap; }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------