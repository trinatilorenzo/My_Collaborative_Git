package model.world;

import main.CONFIG.MapConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * MAP CLASS <-- all the map layer
 * It supports graphical layers for visual
 * representation and game layers for collision and gameplay logic.
 */

//-------------------------------------------------------------------------------------------------------------------
public class GameMap {

    private final MapConfig mapConfig;
    private final int GRAPHIC_LAYER_NUM, GAME_LAYER_NUM;


    private final MapLayer[] graphicLayers;
    private final boolean[][][] collisionMap;

    // constructor
    //-------------------------------------------------------------
    public GameMap(MapConfig mapConfig, Document mapDoc) {
        this.mapConfig = mapConfig;

        LoadedMapData data = loadMap(mapDoc);

        this.graphicLayers = data.graphicLayers();
        this.collisionMap = data.collisionMap();
        this.GRAPHIC_LAYER_NUM = graphicLayers.length;
        this.GAME_LAYER_NUM = collisionMap.length;
    }

    /**
     * a record use to convert an array list to a fixed size array
     */
    //-------------------------------------------------------------
    private record LoadedMapData(
            MapLayer[] graphicLayers,
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

        List<MapLayer> graphicList = new ArrayList<>();
        List<boolean[][]> collisionList = new ArrayList<>();

        int colLayer = 0;
        for (int i = 0; i < layers.getLength(); i++) {
            Element layer = (Element) layers.item(i);
            Element dataElement = (Element) layer.getElementsByTagName("data").item(0);

            if (layer.getAttribute("class").equals(MapConfig.COLL_TAG)) {
                collisionList.add(buildCollisionLayer(mapConfig.MAX_WORLD_ROW(), mapConfig.MAX_WORLD_COL(),dataElement));
            } else {
                // add a single graphic layer
                graphicList.add(new MapLayer(i, mapConfig.MAX_WORLD_ROW(), mapConfig.MAX_WORLD_COL(), dataElement));
            }
        }


        return new LoadedMapData(graphicList.toArray(new MapLayer[graphicList.size()]),
                collisionList.toArray(new boolean[collisionList.size()][][]));

    }
    //-------------------------------------------------------------


    /**
     *Loads a single collision layer from the provided XML data.
     */
    //-------------------------------------------------------------
    public boolean[][] buildCollisionLayer(int maxMapRow, int maxMapCol, Element dataElement){
        boolean[][] collisionMap = new boolean[maxMapRow][maxMapCol];

        String[] num = dataElement.getTextContent().trim().strip().replaceAll("[^0-9,]", "").split(",");
        int index = 0;
        for (int row = 0; row < maxMapRow; row++) {

            for (int col = 0; col < maxMapCol; col++) {
                collisionMap[row][col] = Integer.parseInt(num[index++]) == mapConfig.COLL_ID;
            }
        }
        return collisionMap;
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
        if (row < 0 || col < 0 || row >= mapConfig.MAX_WORLD_ROW() || col >= mapConfig.MAX_WORLD_COL()) {
            return true;
        }
        return collisionMap[layer][row][col];
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getMapTile(int layer, int row, int col) {
        return graphicLayers[layer].getLayerTileId(row, col);
    }
    public int getMaxMapCol() {
        return mapConfig.MAX_WORLD_COL();
    }
    public int getMaxMapRow() {
        return mapConfig.MAX_WORLD_ROW();
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
        graphicLayers[layer].setLayerTile(newId, row, col);
    }
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
