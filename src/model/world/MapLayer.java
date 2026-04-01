package model.world;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * GameMap Layer Class
 * single layer of the map
 */
//---------------------------------------------------------------------------------------------------------------
class MapLayer{

    private int layer[][]; // the layer is a 2-dimensional int array each int is a tile id
    private int level;
    private String pathFile;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public MapLayer(int level, int maxMapRow, int maxMapCol, Element dataElement){
        this.layer = new int[maxMapRow][maxMapCol];
        this.level = level;
        this.pathFile = pathFile;

        loadMapLayer(dataElement); // load the map when a layer is created
    }
    //-------------------------------------------------------------

    // load the tile id into the array by reading the csv map file
    //-------------------------------------------------------------
    public void loadMapLayer(Element dataElement){

        String[] num = dataElement.getTextContent().trim().strip().replaceAll("[^0-9,]", "").split(",");
        int index = 0;
        for (int row = 0; row < layer.length; row++) {

            for (int col = 0; col < layer[row].length; col++) {
                layer[row][col] = Integer.parseInt(num[index++])-1;
                //System.out.print(layer[row][col]);
                /*int gid = Integer.parseInt(num[index++]);

                if (gid == 0) {            // 0 = cella vuota
                    layer[row][col] = -1;
                    continue;
                }

// gestisci solo il tileset principale (firstgid=1)
                if (gid >= 801) {          // secondo tileset: per ora non disegnarlo
                    layer[row][col] = -1;
                    continue;
                }

                int tileId = gid - 1;      // porta il gid 1‑based a indice 0‑based
                layer[row][col] = tileId;*/
            }
            //System.out.println();
        }
        /*System.out.println("Loading layer " + level);
        String[] line = dataElement.getTextContent().split("\n");

            for (int row = 0; row < line.length; row++) {
                String numbers[] = line[row].replaceAll("[^0-9,]", "").split(","); // split the line by comma to get the tile id as string array
                for (int col = 0; col < numbers.length; col++) {
                    layer[row][col] = Integer.parseInt(numbers[col]);
                }
            }
*/


    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getLayerTileId(int row, int col){
        return layer[row][col];
    }
    public int getLevel() { return level;}
    public String getPathFile() { return pathFile;}
    //---------------------------------

    // SETTER ----------------------
    public void setLayerTile(int tileId, int row, int col){
        this.layer[row][col] = tileId;
    }
    public void setLevel(int level) { this.level = level;}
    public void setPathFile(String pathFile) { this.pathFile = pathFile;}
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
