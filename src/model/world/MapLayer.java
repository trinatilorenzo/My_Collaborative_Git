package model.world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


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
            InputStream is = getClass().getResourceAsStream(pathFile); // read the file as stream
            BufferedReader br = new BufferedReader(new InputStreamReader(is)); // read the stream with a buffer reader

            if (is == null) {
                throw new RuntimeException("File not found: " + pathFile);
            }
            for (int row = 0; row < layer.length; row++) {
                String line = br.readLine(); // read a line of the csv file
                if (line == null) break; 
                String numbers[] = line.split(","); // split the line by comma to get the tile id as string array
                for (int col = 0; col < layer[row].length; col++) {
                    layer[row][col] = Integer.parseInt(numbers[col]);
                }
            }
            br.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getLayerTileId(int row, int col){
        return layer[row][col];
    }
    //---------------------------------

    // SETTER ----------------------
    public void setLayerTile(int tileId, int row, int col){
        this.layer[row][col] = tileId;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
