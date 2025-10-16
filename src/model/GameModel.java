package model;

import controller.KeyHandler;
import main.GameSetting;
import model.entity.Player;
import model.world.Tile;
import model.world.WordTileManager;

public class GameModel {
    // ALL THE GAME MODEL STAFF HERE
    // world map , entity, combat, ai, events ...

    private GameSetting gs;
    private WordTileManager world;
    private Player player;

    public GameModel(GameSetting settings) {
        this.gs = settings;
        world = new WordTileManager(gs);
        world.loadMap(gs.MAP_PATH);

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
