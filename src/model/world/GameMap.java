package model.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * MAP CLASS <-- all the map layer
 * It supports graphical layers for visual
 * representation and game layers for collision and gameplay logic.
 */

//-------------------------------------------------------------------------------------------------------------------
public class GameMap {

    private final int maxMapRow;
    private final int maxMapCol;
    private final int graphicLayerNum;
    private final int gameLayerNum;

    private final List<MapLayer> graphicLayers = new ArrayList<>();
    private final boolean[][][] collisionMap; // [layer][row][col]

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameMap(String mapPath, int maxMapRow, int maxMapCol, int graphicLayerNum, int gameLayerNum) {
        this.maxMapCol = maxMapCol;
        this.maxMapRow = maxMapRow;
        this.graphicLayerNum = graphicLayerNum;
        this.gameLayerNum = gameLayerNum;

        this.collisionMap = new boolean[gameLayerNum][maxMapRow][maxMapCol];
        loadMap(mapPath);
    }
    //-------------------------------------------------------------

    // ALTERNATIVE CONSTRUCTOR
    // if layer num not provided --> layer num = 1
    //-------------------------------------------------------------
    public GameMap(String mapPath, int maxMapRow, int maxMapCol) {
        this(mapPath, maxMapRow, maxMapCol, 1, 1);
    }
    //-------------------------------------------------------------

    /**
     * Loads the game map by initializing graphic and collision layers
     * from the provided map files.
     */
    //-------------------------------------------------------------
    private void loadMap(String mapPath) {
        //TODO: import della mappa tramite joson
        for (int i = 0; i < graphicLayerNum; i++) {
            try {
                graphicLayers.add(new MapLayer(i, maxMapRow, maxMapCol, mapPath + i + ".csv"));
            } catch (Exception e) {
                System.err.println("Failed to load graphic layer " + i + ": " + e.getMessage());
            }
        }
        for (int i = 0; i < gameLayerNum; i++) {
            loadCollisionLayer(mapPath + "COLLISION" + i + ".csv", i);
        }
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void loadCollisionLayer(String pathFile, int layer) {
        try (InputStream is = getClass().getResourceAsStream(pathFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            for (int row = 0; row < maxMapRow; row++) {
                String line = br.readLine();
                String[] numbers = line.split(",");
                for (int col = 0; col < maxMapCol; col++) {
                    collisionMap[layer][row][col] = Integer.parseInt(numbers[col]) == 1;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load collision layer " + layer + " from " + pathFile);
            e.printStackTrace();
        }
    }
    //-------------------------------------------------------------

    /**
     * Determines whether a collision occurs at the specified layer, row, and column
     * in the game map.
     */
    //-------------------------------------------------------------
    public boolean hasCollision(int layer, int row, int col) {
        if (layer < 0 || layer >= gameLayerNum) {
            // out of layer bounds
            return true;
        }
        if (row < 0 || col < 0 || row >= maxMapRow || col >= maxMapCol) {
            // out of map bounds
            return true;
        }
        return collisionMap[layer][row][col];
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getMapTile(int layer, int row, int col) {
        return graphicLayers.get(layer).getLayerTileId(row, col);
    }
    public int getMaxMapCol() {
        return maxMapCol;
    }
    public int getMaxMapRow() {
        return maxMapRow;
    }
    public int getGraphicLayerNum() {
        return graphicLayerNum;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setMapTile(int layer, int row, int col, int newId) {
        graphicLayers.get(layer).setLayerTile(newId, row, col);
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------