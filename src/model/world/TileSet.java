package model.world;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static main.GameSetting.*;
import static main.GameSetting.TILE_SIZE;


public class TileSet {
    private BufferedImage tileSetImg;
    ArrayList<Tile> tiles = new ArrayList<>();

    public TileSet(String tileImagePath){
        try {
            tileSetImg = ImageIO.read(getClass().getResourceAsStream(tileImagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loadTileSet();

    }

    public void loadTileSet(){

        int x = 0;
        int y = 0;
        int id = 0;

        for (int i = 0; i < MAX_TILESET_RAW; i++) {
            for (int j = 0; j < MAX_TILESET_COL; j++) {
                tiles.add(new Tile(id,x,y));
                x += TILE_SIZE;
                id ++;
            }
            x=0;
            y += TILE_SIZE;
        }

    }

    public BufferedImage getTileSetImg() {
        return tileSetImg;
    }

    public Tile getTileById(int id) {
        return tiles.get(id);
    }
}
