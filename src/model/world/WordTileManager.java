package model.world;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ListIterator;

import static main.GameSetting.*;



public class WordTileManager {

    // load map
    private Map gameMap = new Map();

    // load tileset
    private TileSet tileSet = new TileSet(TILESET_PATH);

    public void DrawMap(Graphics2D g2){
        /*-----------------------------------------------
        draw single later for debug
        System.out.println("layer"+ 0);
        drawLayer(gameMap.getLayer(0).getLayerMap(), g2);
        System.out.println("layer"+ 1);
        drawLayer(gameMap.getLayer(1).getLayerMap(), g2);
        -----------------------------------------------*/
        drawLayer(gameMap.getLayer(0).getLayerMap(), g2);
        drawLayer(gameMap.getLayer(1).getLayerMap(), g2);
        drawLayer(gameMap.getLayer(2).getLayerMap(), g2);
        drawLayer(gameMap.getLayer(3).getLayerMap(), g2);
        drawLayer(gameMap.getLayer(4).getLayerMap(), g2);

        /*ListIterator<MapLayer> it = gameMap.getMap().listIterator();
        while (it.hasNext()){
            drawLayer(it.next().getLayerMap(), g2);
        }*/



    }
    void drawLayer(int[][] layer, Graphics2D g2){
        for (int i = 0; i < MAX_WORLD_ROW; i++) {
            for (int j = 0; j < MAX_WORLD_COL; j++) {

                // draw tile [i][j]

                if(layer[i][j] != -1){
                    Tile tile = tileSet.getTileById(layer[i][j]);

                    drawTile(g2,tileSet.getTileSetImg(), tile, i, j );
                }


            }
        }
    }

    void drawTile(Graphics2D g2, BufferedImage tileset, Tile tile, int i, int j) {

        int screenX = j * TILE_SIZE;
        int screenY = i * TILE_SIZE;

        int sx1 = tile.x ;
        int sy1 = tile.y ;
        int sx2 = sx1 + TILE_SIZE;
        int sy2 = sy1 + TILE_SIZE;

        int dx1 = screenX;
        int dy1 = screenY;
        int dx2 = screenX + TILE_SIZE;
        int dy2 = screenY + TILE_SIZE;

        g2.drawImage(tileset, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }


    /*
    public void testLoadMap(){
        ListIterator<MapLayer> it = gameMap.getMap().listIterator();

        while (it.hasNext()){
            int[][] mLayer = it.next().getLayerMap();
            System.out.println("new layer"+ it.previousIndex());
            for (int i = 0; i < MAX_WORLD_ROW; i++) {
                for (int j = 0; j < MAX_WORLD_COL; j++) {
                    System.out.print(mLayer[i][j] + " ");
                }
                System.out.println("");
            }
        }

    }*/

    // draw

}