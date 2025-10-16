package model.world;

import main.GameSetting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WordTileManager {


    private Tile[] tileSet;
    private int[][] mapTileNum;
    private GameSetting gs;

    public WordTileManager(GameSetting setting) {
        this.gs = setting;

        this.tileSet = new Tile[gs.TILESNUM];
        mapTileNum = new int[gs.MAX_WORLD_ROW][gs.MAX_WORLD_COL];
        getTileImage();
    }

    public void getTileImage(){
        try {
            tileSet[0] = new Tile();
            tileSet[0].image = ImageIO.read(getClass().getResourceAsStream("/res/tiles/001.png"));
            tileSet[0].collision = false;

            tileSet[1] = new Tile();
            tileSet[1].image = ImageIO.read(getClass().getResourceAsStream("/res/tiles/003.png"));
            tileSet[1].collision = false;

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void loadMap(String pathFile) {
        try {
            InputStream is = getClass().getResourceAsStream(pathFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int i = 0; i < gs.MAX_WORLD_ROW ;i++) {
                String line = br.readLine();
                String numbers[] = line.split(" ");
                for (int j = 0; j < gs.MAX_WORLD_COL; j++) {
                    int num = Integer.parseInt(numbers[j]);
                    mapTileNum[i][j] = num;
                }
            }
            br.close();

        }catch (Exception e){
            // to do
            e.printStackTrace();
        }
    }

    // TO-DO : put in view part

    public void draw(Graphics2D g2){

        for (int i = 0; i < gs.MAX_WORLD_ROW; i++) {
            for (int j = 0; j < gs.MAX_WORLD_COL; j++) {

                int worldX = j * gs.TILE_SIZE;
                int worldY = i * gs.TILE_SIZE;


                int screenX = worldX;  //- gp.player.worldX + gp.player.screenX;
                int screenY= worldY ; //- gp. player.worldY + gp.player.screenY;

                // render only near tiles (buffered buy only an extra old.VIEW.tile // to do add rendere distance )
               /*if(worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                   worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                   worldY + gp.tileSize > gp. player.worldY - gp.player.screenY &&
                   worldY - gp.tileSize < gp. player.worldY + gp.player.screenY){

                    g2.drawImage(old.VIEW.tile[mapTileNum[i][j]].image, screenX,screenY, null);
                }*/
                //--------- RENDER ALL ---------------

                g2.drawImage(tileSet[mapTileNum[i][j]].image, screenX,screenY,gs.TILE_SIZE,gs.TILE_SIZE, null);
                //------------------------------------

            }

        }
    }
}