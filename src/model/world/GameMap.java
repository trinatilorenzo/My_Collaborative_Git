package model.world;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static main.GameSetting.*;
// - MAP CLASS <-- all the map layer
//-------------------------------------------------------------------------------------------------------------------
public class GameMap {

    private int maxMapRow, maxMapCol;
    private int graphicLayerNum, gameLayerNum;

    private ArrayList <MapLayer> map = new ArrayList<>(); // use an ArrayLyst to manage all the layer
    private boolean[][][] collisionMap; // [collisionLayer][row][col]

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
    public GameMap(String mapPath, int maxMapRow , int maxMapCol){
        this(mapPath, maxMapRow, maxMapCol, 1, 1);
    }
    //-------------------------------------------------------------

    // load the layer into the arrayLyst by calling the MapLayer Constructor
    public void loadMap(String mapPath){

        // load graphic layers
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
            loadCollisionLayers(mapPath + "COLLISION" + i + ".csv", i);
        }
    }
    //-------------------------------------------------------------
    private void loadCollisionLayers(String pathFile, int layer) {
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

    public boolean hasCollision(int layer,int row, int col) {
        if (layer < 0 || layer >= gameLayerNum) { 
            return true; // invalid layer = collision
        }
        if (row < 0 || col < 0 || row >= maxMapRow || col >= maxMapCol) {
            return true; // out of bounds = collision
        }
        return collisionMap[layer][row][col];
    }

    // GETTER ----------------------
    public int getMapTile(int layer, int mapX, int mapY){
        return map.get(layer).getLayerTileId(mapX,mapY);
    }
    public int getMaxMapCol() { return maxMapCol;}    
    public int getMaxMapRow() { return maxMapRow;}
    public int getLayerNum() { return graphicLayerNum;}
    //---------------------------------

    // SETETR ----------------------
    public void setMapTile(int layer, int mapX, int mapY, int newId){
        map.get(layer).setLayerTile(newId, mapX, mapY);
    }
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------