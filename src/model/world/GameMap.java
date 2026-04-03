package model.world;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static main.GameSetting.GRAPHIC_LAYER_NUM;

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
        /*//TODO: import della mappa tramite joson
        for (int i = 0; i < graphicLayerNum; i++) {
            try {
                graphicLayers.add(new MapLayer(i, maxMapRow, maxMapCol, mapPath + i + ".csv"));
            } catch (Exception e) {
                System.err.println("Failed to load graphic layer " + i + ": " + e.getMessage());
            }
        }
        for (int i = 0; i < gameLayerNum; i++) {
            loadCollisionLayer(mapPath + "COLLISION" + i + ".csv", i);
        }*/
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File("src/res/maps/MappaGiocoV4.tmx"));
            doc.getDocumentElement().normalize();

            NodeList layers = doc.getElementsByTagName("layer");

            for (int i = 0; i < layers.getLength(); i++) {
                Element layer = (Element) layers.item(i);
                int width = Integer.parseInt(layer.getAttribute("width"));
                int height = Integer.parseInt(layer.getAttribute("height"));

                Element dataElement = (Element) layer.getElementsByTagName("data").item(0);

                if (layer.getAttribute("class").equals("collision")) {
                    System.out.println("colllayer");
                    //loadCollisionLayer(mapPath + "COLLISION" + (i-9) + ".csv", i-9);
                    loadCollisionLayer(dataElement, i-GRAPHIC_LAYER_NUM);
                } else {
                    System.out.println("layer" + i);
                    graphicLayers.add(new MapLayer(i, height, width, dataElement));

                }
            }

        } catch (Exception e) {
            System.err.println("Errore nel parsing del TMX: " + e.getMessage());
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

    public void loadCollisionLayer(Element dataElement, int layer){

        String[] num = dataElement.getTextContent().trim().strip().replaceAll("[^0-9,]", "").split(",");
        int index = 0;
        for (int row = 0; row < maxMapRow; row++) {

            for (int col = 0; col < maxMapCol; col++) {
                collisionMap[layer][row][col] = Integer.parseInt(num[index++]) == 2;
            }
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