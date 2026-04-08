package model.world;

import org.w3c.dom.Element;

/**
 * GameMap Layer Class
 * single layer of the map
 */
//---------------------------------------------------------------------------------------------------------------
class MapLayer{

    //TODO remove and use only map class

    private int layer[][]; // the layer is a 2-dimensional int array each int is a tile id
    private int level;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public MapLayer(int level, int maxMapRow, int maxMapCol, Element dataElement){
        this.layer = new int[maxMapRow][maxMapCol];
        this.level = level;

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
                layer[row][col] = Integer.parseInt(num[index++]) - 1;
            }
        }
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public int getLayerTileId(int row, int col){
        return layer[row][col];
    }
    public int getLevel() { return level;}
    //---------------------------------

    // SETTER ----------------------
    public void setLayerTile(int tileId, int row, int col){
        this.layer[row][col] = tileId;
    }
    public void setLevel(int level) { this.level = level;}
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
