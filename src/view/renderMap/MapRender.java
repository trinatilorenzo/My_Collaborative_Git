package view.renderMap;

import model.world.Map;
import model.entity.Player;

import java.awt.*;

import static main.GameSetting.*;

// - MAP RENDER CLASS <-- this class will draw the world map
//-------------------------------------------------------------------------------------------------------------------
public class MapRender {

    public MapRender(){
    }

    // Draw all the layers of the map, only the visible part around the player
    // -----------------------------------------------------
    public void DrawMap(Map map, TileSet tileSet, Player player, Graphics2D g2){
        /*-----------------------------------------------
        DEBUG : draw a single layer
        drawLayer(0, map, tileSet, g2);
        drawLayer(1, map, tileSet, g2);
        drawLayer(2, map, tileSet, g2);
        drawLayer(3, map, tileSet, g2);
        drawLayer(4, map, tileSet, g2);
        -----------------------------------------------*/
        for (int i = 0; i < map.getLayerNum(); i++) {
            drawLayer(i, map, tileSet, player, g2);
        }
    }
    //-----------------------------------------------------

    // Draw the single layer of the map
    // -----------------------------------------------------
    void drawLayer(int layer, Map gameMap, TileSet tileSet, Player player, Graphics2D g2){

        int leftCol = Math.max(0, (player.getWorldX() - player.getScreenX())/ TILE_SIZE);
        int rightCol = Math.min(gameMap.getMaxMapCol()-1, player.getWorldX() + (SCREEN_WIDTH - player.getScreenX()) / TILE_SIZE );
        int topRow = Math.max(0, (player.getWorldY() - player.getScreenY()) / TILE_SIZE );
        int bottomRow = Math.min(gameMap.getMaxMapRow()-1, (player.getWorldY() + (SCREEN_HEIGHT - player.getScreenY())) / TILE_SIZE );

        for (int i = topRow; i<= bottomRow; i++){
            for (int j = leftCol; j<= rightCol; j++){
                
                int tileID = gameMap.getMapTile(layer, i, j);// get the tile id to know witch tile to render

                if (tileID != -1){//-1 = trasparente
                    drawTile(tileID, i, j, tileSet, player, g2);
                }
            }
        }

    }
    //-----------------------------------------------------

    // Draw a single tile
    // -----------------------------------------------------
    void drawTile(int tileId, int row, int col, TileSet tileSet, Player player, Graphics2D g2) {

        int worldX = col * TILE_SIZE;
        int worldY = row * TILE_SIZE;

        int screenX = worldX - player.getWorldX() + player.getScreenX();
        int screenY = worldY - player.getWorldY() + player.getScreenY();

        int dx2 = screenX + TILE_SIZE;
        int dy2 = screenY + TILE_SIZE;

        int sx1 = tileSet.getTileX(tileId);
        int sy1 = tileSet.getTileY(tileId);
        int sx2 = sx1 + TILE_SIZE;
        int sy2 = sy1 + TILE_SIZE;


        g2.drawImage(tileSet.getTileSetImg(), 
                     screenX, screenY, dx2, dy2, 
                     sx1, sy1, sx2, sy2, null);
    }
    //-----------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
