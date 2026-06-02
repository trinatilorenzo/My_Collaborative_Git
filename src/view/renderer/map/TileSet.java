package view.renderer.map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final int[] frameIds;
    private final int[] frameDurationsMs;
    private int currentFrameIndex = 0;
    private double accumulatorMs = 0.0;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public AnimatedTile(int[] frameIds, double frameDurationMs) {
        this(frameIds, fillDurations(frameIds.length, (int) Math.round(frameDurationMs)));
    }

    public AnimatedTile(int[] frameIds, int[] frameDurationsMs) {
        if (frameIds.length == 0 || frameIds.length != frameDurationsMs.length) {
            throw new IllegalArgumentException("Animated tiles must have one duration for each frame.");
        }
        for (int duration : frameDurationsMs) {
            if (duration <= 0) {
                throw new IllegalArgumentException("Animated tile frame durations must be greater than zero.");
            }
        }
        this.frameIds = frameIds;
        this.frameDurationsMs = frameDurationsMs;
    }
    //-------------------------------------------------------------

    /**
     * Update the animation: increments counter and advances to next frame if necessary
     */
    //-------------------------------------------------------------
    public void update(double deltaMs) {
        if (frameIds.length <= 1) {
            return;
        }

        accumulatorMs += deltaMs;
        while (accumulatorMs >= frameDurationsMs[currentFrameIndex]) {
            accumulatorMs -= frameDurationsMs[currentFrameIndex];
            currentFrameIndex++;
            if (currentFrameIndex >= frameIds.length) {
                currentFrameIndex = 0;
            }
        }
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getCurrentFrameId() {
        return frameIds[currentFrameIndex];
    }
    public int getBaseId() {
        return frameIds[0];
    }
    //---------------------------------

    private static int[] fillDurations(int frameCount, int frameDurationMs) {
        int[] durations = new int[frameCount];
        Arrays.fill(durations, frameDurationMs);
        return durations;
    }
}
//-------------------------------------------------------------------------------------------------------------------

/**
 * TILESET CLASS
 * all tile of the game
 */
//-------------------------------------------------------------------------------------------------------------------

public class TileSet {
    private BufferedImage tileSetImg; // <-- the image with all the texture
    private final ArrayList<TileRegion> tiles = new ArrayList<>(); // <-- all the tile order by Id
    private final Map<Integer, AnimatedTile> animatedTiles = new HashMap<>();

    // COSTRUCTOR
    //-------------------------------------------------------------
    public TileSet(String tileImagePath, int tileSize, int maxTilesetRaw, int maxTilesetCol){
        this(tileImagePath, tileSize, maxTilesetRaw, maxTilesetCol, null);
    }

    public TileSet(String tileImagePath, int tileSize, int maxTilesetRaw, int maxTilesetCol, Document tilesetDoc){
        // read the tileset image
        this.tileSetImg = loadTileSetImage(tileImagePath);
        // split the tileset image in tiles
        loadTileSet(tileSize, maxTilesetRaw, maxTilesetCol);
        // load the animated tiles
        loadAnimatedTiles(tilesetDoc);
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
    private void loadAnimatedTiles(Document tilesetDoc) {
        if (tilesetDoc == null) {
            return;
        }

        NodeList tileNodes = tilesetDoc.getElementsByTagName("tile");
        for (int i = 0; i < tileNodes.getLength(); i++) {
            Element tileElement = (Element) tileNodes.item(i);
            NodeList animationNodes = tileElement.getElementsByTagName("animation");
            if (animationNodes.getLength() == 0) {
                continue;
            }

            int tileId = Integer.parseInt(tileElement.getAttribute("id"));
            Element animationElement = (Element) animationNodes.item(0);
            NodeList frameNodes = animationElement.getElementsByTagName("frame");
            int frameCount = frameNodes.getLength();
            if (frameCount == 0) {
                continue;
            }

            int[] frameIds = new int[frameCount];
            int[] frameDurationsMs = new int[frameCount];
            for (int j = 0; j < frameCount; j++) {
                Element frameElement = (Element) frameNodes.item(j);
                frameIds[j] = Integer.parseInt(frameElement.getAttribute("tileid"));
                frameDurationsMs[j] = Integer.parseInt(frameElement.getAttribute("duration"));
            }

            animatedTiles.put(tileId, new AnimatedTile(frameIds, frameDurationsMs));
        }

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

    private BufferedImage loadTileSetImage(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        try (InputStream is = TileSet.class.getResourceAsStream(normalized)) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while loading tileset image: " + path, e);
        }
        throw new IllegalArgumentException("Tileset not found in the classpath: " + normalized);
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
