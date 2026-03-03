package model.world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static main.GameSetting.*;

// - GameMap Layer Class
//   single layer of the map
//-------------------------------------------------------------------------------------------------------------------
class MapLayer{

    private int layer[][]; // the layer is a 2-dimensional int array each int is a tile id
    private int level;
    private String pathFile;


    public MapLayer(int level, int maxMapRow, int maxMapCol, String pathFile){
        this.layer = new int[maxMapRow][maxMapCol];
        this.level = level;
        this.pathFile = pathFile;

        loadMapLayer(pathFile); // load the map when a layer is created
    }

    // load the tile id into the array by reading the csv map file
    public void loadMapLayer(String pathFile){
        try {
            InputStream is = getClass().getResourceAsStream(pathFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int i = 0; i < MAX_WORLD_ROW ;i++) {
                String line = br.readLine();
                String numbers[] = line.split(","); // csv
                for (int j = 0; j < MAX_WORLD_COL; j++) {
                    int num = Integer.parseInt(numbers[j]);
                    layer[i][j] = num;
                }
            }
            br.close();

        }catch (Exception e){
            //  // TO_DO: bettter error
            e.printStackTrace();
        }

    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getLayerTileId(int mapX, int mapY){
        return layer[mapX][mapY];
    }
    //---------------------------------

    // SETTER ----------------------
    public void setLayerTile(int tileId, int tileX, int tileY){
        this.layer[tileX][tileY] = tileId;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------


// - MAP CLASS <-- all the map layer
//-------------------------------------------------------------------------------------------------------------------
public class GameMap {

    private int maxMapRow, maxMapCol, graphicLayerNum, gameLayerNum;

    private ArrayList <MapLayer> map = new ArrayList<>(); // use an ArrayLyst to manage all the layer
    private boolean collisionMap[][][]; // [collisionLayer][row][col]

    public GameMap(String mapPath, int maxMapRow , int maxMapCol, int graphicLayerNum, int gameLayerNum){
        this.maxMapCol = maxMapCol;
        this.maxMapRow = maxMapRow;
        this.graphicLayerNum = graphicLayerNum;
        this.gameLayerNum = gameLayerNum;

        this.collisionMap = new boolean [gameLayerNum][maxMapRow][maxMapCol];
        loadMap(mapPath);
    }

    // ALTERNATIVE CONSTRUCTOR

    // if layer num not provided --> layer num = 1
    //-------------------------------------------------------------
    public GameMap(String mapPath, int maxMapRow , int maxMapdCol){
        this.maxMapCol = maxMapdCol;
        this.maxMapRow = maxMapRow;
        this.graphicLayerNum = 1;
        this.gameLayerNum = 1;

        loadMap(mapPath);
    }
    //-------------------------------------------------------------

    // load the layer into the arrayLyst by calling the MapLayer Constructor
    public void loadMap(String mapPath){
        for (int i = 0; i < graphicLayerNum; i++) {
            try{
                map.add(new MapLayer(i,this.maxMapRow, this.maxMapCol, mapPath+i+".csv"));
                // System.out.println(mapPath+i+".csv");
            }catch (Exception e){
                // TO_DO: bettter error
                System.out.println(e);
            }
        }
        // load collison map
        for (int i = 0; i < gameLayerNum; i++) {
            loadCollisionLayers(mapPath+"COLLISION"+i+".csv", i);
        }
    }
    //-------------------------------------------------------------
    public void loadCollisionLayers(String pathFile, int layer) {
        try {
            InputStream is = getClass().getResourceAsStream(pathFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int i = 0; i < MAX_WORLD_ROW ;i++) {
                String line = br.readLine();
                String numbers[] = line.split(","); // csv

                for (int j = 0; j < MAX_WORLD_COL; j++) {
                    int num = Integer.parseInt(numbers[j]);
                    if (num == 1){
                        collisionMap[layer][i][j] = true;// num = 1 --> collison
                    }else{
                        // non collison
                        collisionMap[layer][i][j] = false;
                    }
                }
            }
            br.close();

        }catch (Exception e){
            //  // TO_DO: bettter error
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------

    public boolean hasCollision(int row, int col) {
        for (int k = 0; k < gameLayerNum; k++) {
            if (collisionMap[k][row][col]) return true;
        }
        return false;
    }



    // GETTER ----------------------
    public int getMapTile(int layer, int mapX, int mapY){
        return map.get(layer).getLayerTileId(mapX,mapY);
    }

    public int getMaxMapCol() {
        return maxMapCol;
    }
    public int getMaxMapRow(){
        return maxMapRow;
    }
    public int getLayerNum() {
        return graphicLayerNum;
    }
    //---------------------------------

    // SETETR ----------------------
    public void setMapTile(int layer, int mapX, int mapY, int newId){
        map.get(layer).setLayerTile(newId, mapX, mapY);
    }
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------