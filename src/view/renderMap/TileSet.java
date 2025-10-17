package view.renderMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

// - Tile Region Class
//   represent the upper left coordinate of a single tile
//      a tile is just a crop of the whole tileset image
//-------------------------------------------------------------------------------------------------------------------
class TileRegion {
    public int id, x,y;

    public TileRegion(int id, int x, int y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

}
//-------------------------------------------------------------------------------------------------------------------

// - TILESET CLASS <-- all tile of the game
//-------------------------------------------------------------------------------------------------------------------

public class TileSet {
    private BufferedImage tileSetImg; // <-- the image with all the texture
    private ArrayList<TileRegion> tiles = new ArrayList<>(); // <-- all the tile order by Id

    public TileSet(String tileImagePath, int tileSize, int maxTilesetRaw, int maxTilesetCol){
        // read the tileset image
        try {
            tileSetImg = ImageIO.read(getClass().getResourceAsStream(tileImagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // create all the frame
        loadTileSet(tileSize, maxTilesetRaw, maxTilesetCol);
    }

    public void loadTileSet(int tileSize, int maxTilesetRaw, int maxTilesetCol){
        int x = 0, y = 0, id = 0;

        for (int i = 0; i < maxTilesetRaw; i++) {
            for (int j = 0; j < maxTilesetCol; j++) {
                tiles.add(new TileRegion(id,x,y));
                x += tileSize;
                id ++;
            }
            x=0;
            y += tileSize;
        }

    }

    // GETTER ----------------------
    public BufferedImage getTileSetImg() {
        return tileSetImg;
    }

    public int getTileX(int id) {
        return tiles.get(id).x;
    }
    public int getTileY(int id) {
        return tiles.get(id).y;
    }

    //---------------------------------


    // SETTER ----------------------
    public void setTileSetImg(BufferedImage tileSetImg){
        this.tileSetImg = tileSetImg;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
