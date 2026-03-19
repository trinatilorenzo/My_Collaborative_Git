package model.object;

/**
 * Represents an item in the game world.
 */
public class Item extends GameObject {

    private int amount;

    public Item(String name, int worldX, int worldY, int amount) {
        this.name = name;
        this.worldX = worldX;
        this.worldY = worldY;
        this.amount = amount;
        this.solid = false; // Items are not solid by default
    }

    // GETTER
    public int getAmount() {
        return amount;
    }

    @Override
    public void interact() {
        // This method can be called by the player when picking up the item (e.g., pressing an action key while near it)
        // For example, you could add logic here to add the item to the player's inventory and then mark it as removed from the world
        removed = true; // Mark the item as removed from the world after interaction
    }
    
}
