package view.renderer.map;

import javax.imageio.ImageIO;
import view.Animation.FrameTimeline;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TILE REGION CLASS
 * Represent the upper left coordinate of a single tile
 * a tile is just a crop of the whole tileset image
 */

//-------------------------------------------------------------------------------------------------------------------
class TileRegion {
    public int id, x,y;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public TileRegion(int id, int x, int y){
        this.id = id;
        this.x = x;
        this.y = y;
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------

/**
 * Represents an animated tile
 * An animated tile cycles through a series of frames over time.
 */
//-------------------------------------------------------------------------------------------------------------------
class AnimatedTile {
    private int[] frameIds;
    private FrameTimeline timeline;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public AnimatedTile(int[] frameIds, double frameDurationMs) {
        this.frameIds = frameIds;
        this.timeline = new FrameTimeline(frameIds.length, frameDurationMs, true);
    }
    //-------------------------------------------------------------

    /**
     * Aggiorna l'animazione: incrementa il contatore e avanza al frame successivo se necessario
     */
    //-------------------------------------------------------------
    public void update(double deltaMs) {
        timeline.update(deltaMs);
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getCurrentFrameId() {
        return frameIds[timeline.getCurrentFrame()];
    }
    public int getBaseId() {
        return frameIds[0];
    }
    //---------------------------------

    // SETTER ----------------------

    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------

/**
 * TILESET CLASS
 * all tile of the game
 */
//-------------------------------------------------------------------------------------------------------------------

public class TileSet {
    private BufferedImage tileSetImg; // <-- the image with all the texture
    private ArrayList<TileRegion> tiles = new ArrayList<>(); // <-- all the tile order by Id
    private Map<Integer, AnimatedTile> animatedTiles = new HashMap<>();

    // COSTRUCTOR
    //-------------------------------------------------------------
    public TileSet(String tileImagePath, int tileSize, int maxTilesetRaw, int maxTilesetCol){
        // read the tileset image
        try {
            tileSetImg = ImageIO.read(getClass().getResourceAsStream(tileImagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // split the tileset image in tiles
        loadTileSet(tileSize, maxTilesetRaw, maxTilesetCol);
        // load the animated tiles
        loadAnimatedTiles();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
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
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadAnimatedTiles() {
        // ESEMPIO 1: tile acqua (ID base 10, frame 10-13, velocità media)
        //TODO: load from file

        animatedTiles.put(5, new AnimatedTile(new int[]{5, 6, 7, 8, 9,10,11,12}, 100));
        animatedTiles.put(13, new AnimatedTile(new int[]{13, 33, 53, 73, 14,34,54,74}, 100));
        animatedTiles.put(16, new AnimatedTile(new int[]{16, 36, 56, 76, 17,37,57,77}, 100));
        animatedTiles.put(18, new AnimatedTile(new int[]{18, 19, 38, 39, 58, 59,79,79}, 100));

        // AGGIUNGI QUI LE TUE TILE ANIMATE
        // animatedTiles.put(ID_BASE, new AnimatedTile(new int[]{frame1, frame2, frame3, ...}, delay));
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void updateAnimTile(double deltaMs) {
        // Update alle the animated tile to the current frame
        for (AnimatedTile anim : animatedTiles.values()) {
            anim.update(deltaMs);
        }
    }
    //-------------------------------------------------------------



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

    public int getTileIdToDraw(int originalId) {
        AnimatedTile anim = animatedTiles.get(originalId);
        if (anim != null) {
            return anim.getCurrentFrameId();
        }
        return originalId; // tile statica
    }
    //---------------------------------


    // SETTER ----------------------
    public void setTileSetImg(BufferedImage tileSetImg){
        this.tileSetImg = tileSetImg;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
