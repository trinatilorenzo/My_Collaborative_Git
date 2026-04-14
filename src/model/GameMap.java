package model;

import main.CONFIG.MapConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;


/**
 * MAP CLASS <-- all the map layer
 * It supports graphical layers for visual
 * representation and game layers for collision and gameplay logic.
 */

//-------------------------------------------------------------------------------------------------------------------
public class GameMap {

    private final MapConfig mConf;
    private final int GRAPHIC_LAYER_NUM, GAME_LAYER_NUM;


   // private final MapLayer[] graphicLayers;
    private final int[][][] mapLayer; // [level][row][col]
    private final boolean[][][] collisionMap; // [level][row][col]

    // constructor
    //-------------------------------------------------------------
    public GameMap(MapConfig mapConfig, Document mapDoc) {
        this.mConf = mapConfig;

        LoadedMapData data = loadMap(mapDoc);

        this.mapLayer = data.mapLayer();
        //this.graphicLayers = data.graphicLayers();
        this.collisionMap = data.collisionMap();
        this.GRAPHIC_LAYER_NUM = mapLayer.length;
        this.GAME_LAYER_NUM = collisionMap.length;
    }

    /**
     * a record use to convert an array list to a fixed size array
     */
    //-------------------------------------------------------------
    private record LoadedMapData(
            //MapLayer[] graphicLayers,
            int[][][] mapLayer,
            boolean[][][] collisionMap
    ) {}
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

        int colLayer = 0;
        for (int i = 0; i < layers.getLength(); i++) {
            Element layer = (Element) layers.item(i);
            Element dataElement = (Element) layer.getElementsByTagName("data").item(0);

            if (layer.getAttribute("class").equals(MapConfig.COLL_TAG)) {
                collisionList.add(buildCollisionLayer(dataElement));
            } else {
                // add a single graphic layer
                graphicList.add(buildMapLayer(dataElement));
            }
        }


        return new LoadedMapData(graphicList.toArray(new int[graphicList.size()][][]),
                collisionList.toArray(new boolean[collisionList.size()][][]));

    }
    //-------------------------------------------------------------


    /**
     *Loads a single collision layer from the provided XML data.
     */
    //-------------------------------------------------------------
    public boolean[][] buildCollisionLayer(Element dataElement){
        boolean[][] collisionMap = new boolean[mConf.MAX_WORLD_ROW()][mConf.MAX_WORLD_COL()];

        String[] num = dataElement.getTextContent().trim().strip().replaceAll("[^0-9,]", "").split(",");
        int index = 0;
        for (int row = 0; row < mConf.MAX_WORLD_ROW(); row++) {

            for (int col = 0; col < mConf.MAX_WORLD_COL(); col++) {
                collisionMap[row][col] = Integer.parseInt(num[index++]) == mConf.COLL_ID;
            }
        }
        return collisionMap;
    }
    //-------------------------------------------------------------

    // load the tile id into the array by reading the csv map file
    //-------------------------------------------------------------
    public int[][] buildMapLayer(Element dataElement){
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
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
