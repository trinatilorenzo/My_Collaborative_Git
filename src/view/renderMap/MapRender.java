package view.renderMap;

import model.world.Map;

import java.awt.*;
import java.awt.image.BufferedImage;

import static main.GameSetting.*;

// - MAP RENDER CLASS <-- this class wil draw the world map
//-------------------------------------------------------------------------------------------------------------------
public class MapRender {

    public MapRender(){
    }

    // Draw all the layer of map
    // -----------------------------------------------------
    public void DrawMap(Map map, TileSet tileSet, Graphics2D g2){
        /*-----------------------------------------------
        DEBUG : draw a single layer
        drawLayer(0, map, tileSet, g2);
        drawLayer(1, map, tileSet, g2);
        drawLayer(2, map, tileSet, g2);
        drawLayer(3, map, tileSet, g2);
        drawLayer(4, map, tileSet, g2);
        -----------------------------------------------*/
        for (int i = 0; i < map.getLayerNum(); i++) {
            drawLayer(i, map, tileSet, g2);
        }
    }
    //-----------------------------------------------------

    // Draw the single layer of the map
    // -----------------------------------------------------
    void drawLayer(int layer, Map gameMap, TileSet tileSet, Graphics2D g2){
        int tileID = 0;

        for (int i = 0; i < gameMap.getMaxMapRow(); i++) {
            for (int j = 0; j < gameMap.getMaxMapCol(); j++) {

                tileID = gameMap.getMapTile(layer, i,j); // get the tile id to know witch tile to render

                if( tileID != -1){
                    // -1 == TRANSPARENT
                    // draw only if is not transparent
                    drawTile(tileID, i,j, tileSet, g2);
                }
            }
        }
    }
    //-----------------------------------------------------

    // Draw a single tile
    // -----------------------------------------------------
    void drawTile(int tileId, int row, int col, TileSet tileSet, Graphics2D g2) {

        int screenY = row * TILE_SIZE;
        int screenX = col * TILE_SIZE;

        int dx2 = screenX + TILE_SIZE;
        int dy2 = screenY + TILE_SIZE;

        int sx1 = tileSet.getTileX(tileId);
        int sy1 = tileSet.getTileY(tileId);
        int sx2 = sx1 + TILE_SIZE;
        int sy2 = sy1 + TILE_SIZE;


        g2.drawImage(tileSet.getTileSetImg(), screenX, screenY, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }
    //-----------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
