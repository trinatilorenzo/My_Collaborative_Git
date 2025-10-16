package model.world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static main.GameSetting.*;

class MapLayer{

    private int layer[][];
    private int level;
    private String pathFile;


    public MapLayer(int level, String pathFile){
        this.layer = new int[MAX_WORLD_ROW][MAX_WORLD_COL];
        this.level = level;
        this.pathFile = pathFile;

        loadMapLayer(pathFile);
    }

    public void loadMapLayer(String pathFile){
        try {
            InputStream is = getClass().getResourceAsStream(pathFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int i = 0; i < MAX_WORLD_ROW ;i++) {
                String line = br.readLine();
                String numbers[] = line.split(",");
                for (int j = 0; j < MAX_WORLD_COL; j++) {
                    int num = Integer.parseInt(numbers[j]);
                    layer[i][j] = num;
                }
            }
            br.close();

        }catch (Exception e){
            // to do
            e.printStackTrace();
        }

    }

    public int[][] getLayerMap(){
        return layer;
    }

}
public class Map {

    private ArrayList <MapLayer> map = new ArrayList<>();

    public Map(){
        loadMap();
    }

    public void loadMap(){
        for (int i = 0; i < MAP_LAYER_NUM; i++) {

            try{
                map.add(new MapLayer(i,MAP_PATH+i+".csv"));
                System.out.println(MAP_PATH+i+".csv");
            }catch (Exception e){
                System.out.println(e);
            }

        }
    }

    public MapLayer getLayer(int layer){
        return map.get(layer);

    }

    public ArrayList <MapLayer> getMap(){
        return map;
    }
}
