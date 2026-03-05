package view.renderMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// - Tile Region Class
// This class represents a single tile in the tileset, with its ID and position in the tileset image
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

// - TILESET CLASS <-- this class will manage the tileset image, the tile regions and the animated tile
//-------------------------------------------------------------------------------------------------------------------

public class TileSet {
    private BufferedImage tileSetImg; // <-- the image with all the texture
    private ArrayList<TileRegion> tiles = new ArrayList<>(); // <-- all the tile order by Id
    private Map<Integer, AnimatedTile> animatedTiles = new HashMap<>();
    
    public TileSet(String tileImagePath, int tileSize, int maxTilesetRaw, int maxTilesetCol){
        // read the tileset image
        try {
            tileSetImg = ImageIO.read(getClass().getResourceAsStream(tileImagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // create all the frame
        loadTileSet(tileSize, maxTilesetRaw, maxTilesetCol);

        loadAnimatedTiles();
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

    private void loadAnimatedTiles() {
        // ESEMPIO 1: tile acqua (ID base 10, frame 10-13, velocità media)
        animatedTiles.put(5, new AnimatedTile(new int[]{5, 6, 7, 8, 9,10,11}, 100));

        // AGGIUNGI QUI LE TUE TILE ANIMATE
        // animatedTiles.put(ID_BASE, new AnimatedTile(new int[]{frame1, frame2, frame3, ...}, delay));
    }

    public void updateAnimTile() {
        // Update alle the animated tile to the current frame
        for (AnimatedTile anim : animatedTiles.values()) {
            anim.update();
        }
    }

    public int getTileIdToDraw(int originalId) {
        AnimatedTile anim = animatedTiles.get(originalId);
        if (anim != null) {
            return anim.getCurrentFrameId();
        }
        return originalId; // tile statica
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
