package model;

import main.CONFIG.MapConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.Serializable;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.List;

/**
 * MAP CLASS <-- all the map layer
 * It supports graphical layers for visual representation
 *  and game layers for collision and gameplay logic.
 */

//-------------------------------------------------------------------------------------------------------------------
public class GameMap implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient MapConfig mConf;
    private final int GRAPHIC_LAYER_NUM, GAME_LAYER_NUM;


   // private final MapLayer[] graphicLayers;
    private final int[][][] mapLayer; // [level][row][col]
    private final boolean[][][] collisionMap; // [level][row][col]

    private final Point[][] stairs;

    // a record use to convert an array list to a fixed size array

    //-------------------------------------------------------------
    private record LoadedMapData(

            int[][][] mapLayer,
            boolean[][][] collisionMap,
            Point[][] stairs
    ) {}
    //-------------------------------------------------------------


    /**
     * COSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameMap(MapConfig mapConfig, Document mapDoc) {
        this.mConf = mapConfig;

        LoadedMapData data = loadMap(mapDoc);

        this.mapLayer = data.mapLayer();
        this.stairs = data.stairs();
        this.collisionMap = data.collisionMap();
        this.GRAPHIC_LAYER_NUM = mapLayer.length;
        this.GAME_LAYER_NUM = collisionMap.length;

        /*unlockStairsLevel(3);
        unlockStairsLevel(2);
        unlockStairsLevel(1);*/
    }
    //-------------------------------------------------------------

    /**
     * Loads the game map by initializing graphic and collision layers
     * from the provided map files.
     */
    //-------------------------------------------------------------
    private LoadedMapData loadMap(Document mapDoc) {
        NodeList layers = mapDoc.getElementsByTagName("layer");

        List<int[][]> graphicList = new ArrayList<>();
        List<boolean[][]> collisionList = new ArrayList<>();
        List<Point[]> stairsList = new ArrayList<>();

        int colLayer = 0;
        for (int i = 0; i < layers.getLength(); i++) {
            Element layer = (Element) layers.item(i);
            Element dataElement = (Element) layer.getElementsByTagName("data").item(0);

            if (layer.getAttribute("class").equals(MapConfig.COLL_TAG)) {
                List<Point> currentLayerStairs = new ArrayList<>();
                boolean[][] collisionLayer = buildCollisionLayer(dataElement, currentLayerStairs);

                collisionList.add(collisionLayer);
                stairsList.add(currentLayerStairs.toArray(new Point[0]));
            } else {
                // add a single graphic layer
                graphicList.add(buildMapLayer(dataElement));
            }
        }


        return new LoadedMapData(graphicList.toArray(new int[graphicList.size()][][]),
                collisionList.toArray(new boolean[collisionList.size()][][]), stairsList.toArray(new Point[0][]));

    }
    /**
     * HELPERS METHOD
     */
    //Loads a single collision layer from the provided XML data.
    //-------------------------------------------------------------
    private boolean[][] buildCollisionLayer(Element dataElement, List<Point> stairsList){
        boolean[][] collisionMap = new boolean[mConf.MAX_WORLD_ROW()][mConf.MAX_WORLD_COL()];

        String[] num = dataElement.getTextContent().trim().strip().replaceAll("[^0-9,]", "").split(",");
        int index = 0;
        for (int row = 0; row < mConf.MAX_WORLD_ROW(); row++) {

            for (int col = 0; col < mConf.MAX_WORLD_COL(); col++) {
                boolean isStairs = Integer.parseInt(num[index]) == mConf.STAIRS_ID;
                collisionMap[row][col] = Integer.parseInt(num[index]) == mConf.COLL_ID || isStairs;
                if (isStairs){
                    stairsList.add(new Point(col, row));
                }

                index++;
            }

        }
        return collisionMap;
    }
    //-------------------------------------------------------------
    // load the tile id into the array by reading the csv map file
    //-------------------------------------------------------------
    private int[][] buildMapLayer(Element dataElement){
        int[][] layerMap = new int[mConf.MAX_WORLD_ROW()][mConf.MAX_WORLD_COL()];

        String[] num = dataElement.getTextContent().trim().strip().replaceAll("[^0-9,]", "").split(",");
        int index = 0;
        for (int row = 0; row < mConf.MAX_WORLD_ROW(); row++) {

            for (int col = 0; col < mConf.MAX_WORLD_COL(); col++) {
                layerMap[row][col] = Integer.parseInt(num[index++]) -1; // -1 because the map offest id
            }
        }
        return layerMap;
    }
    // end helpers ------------------------------------------------
    //-------------------------------------------------------------

    /**
     * Determines whether a collision occurs at the specified layer, row, and column
     * in the game map.
     */
    //-------------------------------------------------------------
    public boolean hasCollision(int layer, int row, int col) {
        if (layer < 0 || layer >= GAME_LAYER_NUM){
            return true;
        }
        if (row < 0 || col < 0 || row >= mConf.MAX_WORLD_ROW() || col >= mConf.MAX_WORLD_COL()) {
            return true;
        }
        return collisionMap[layer][row][col];
    }
    //-------------------------------------------------------------

    // GETTER ----------------------

    public int getMapTile(int layer, int row, int col) {
        return mapLayer[layer][row][col];
    }
    public int getMaxMapCol() {
        return mConf.MAX_WORLD_COL();
    }
    public int getMaxMapRow() {
        return mConf.MAX_WORLD_ROW();
    }
    public Point[] getStairs(int layer) {
        return stairs[layer];
    }
    public int getGraphicLayerNum() {
        return GRAPHIC_LAYER_NUM;
    }
    public int getGameLayerNum() {
        return GAME_LAYER_NUM;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setMapTile(int layer, int row, int col, int newId) {
        mapLayer[layer][row][col] = newId ;
    }
    public void setCollisionTile(int layer, int row, int col, boolean collision) {
        collisionMap[layer][row][col] = collision;
    }

    public void mConf(MapConfig mConf){
        this.mConf = mConf;
    }

    public void unlockStairsLevel(int level) {
        Point[] stairs = getStairs(level);

        for (Point p : stairs) {
            setCollisionTile(level, p.y, p.x, false);
            System.out.println("unlock stairs"+ level + " " + p.x + " " + p.y);
        }
    }
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
