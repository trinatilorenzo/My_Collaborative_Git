package model;

import controller.KeyHandler;
import main.GameSetting;
import model.entity.Player;
import model.world.Tile;
import model.world.WordTileManager;

import static main.GameSetting.*;

public class GameModel {
    // ALL THE GAME MODEL STAFF HERE
    // world map , entity, combat, ai, events ...


    private WordTileManager world;
    private Player player;

    public GameModel() {
        world = new WordTileManager();


        player = new Player();

       // player = new Player(world);
    }

    public void update(KeyHandler keyH) {

        player.update(keyH);
        // altri update: nemici, oggetti, eventi
    }

    public WordTileManager getWorld() { return world; }
    public Player getPlayer() { return player; }
}
