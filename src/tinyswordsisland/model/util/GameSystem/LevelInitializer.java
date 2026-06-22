package tinyswordsisland.model.util.GameSystem;


import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.ObjConfig;
import tinyswordsisland.config.SpawnPoint;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.config.enu.PowerUpType;
import tinyswordsisland.model.GameMap;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.model.object.OBJ_Tree;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * util class that initializes the game world with all the necessary entities and objects.
 */
//-------------------------------------------------------------------------------------------------------------------
public final class LevelInitializer {

    // this record is used to return the initialized world object
    public record InitializedWorld(
            GameMap worldGameMap,
            Player player,
            Monk monk,
            List<EnemyTNT> tntEnemies,
            List<EnemyDynamite> dynamiteEnemies,
            List<DynamiteProjectile> projectiles,
            List<EnemyTorch> torchEnemies,
            List<GameObject> objects
    ) {}

    // --- Constructor ---
    private LevelInitializer() {}
    //-------------------------------------------------------------

    /**
     * Creates a new world with all the necessary entities and objects.
     */
    //-------------------------------------------------------------
    public static InitializedWorld createNewWorld(GameConfig gameConfig, PlayerColor playerColor) {
        GameMap worldGameMap = new GameMap(gameConfig.mapConfig(), gameConfig.mapDoc());
        Player player = new Player(gameConfig.entityConfig(), playerColor);

        EntityConfig entityConfig = gameConfig.entityConfig();
        Monk monk = new Monk(entityConfig.MONK_START_X(), entityConfig.MONK_START_Y(), entityConfig);
        List<EnemyTNT> tntEnemies = spawnTntEnemies(entityConfig);
        List<DynamiteProjectile> projectiles = new ArrayList<>();
        List<EnemyDynamite> dynamiteEnemies = spawnDynamiteEnemies(entityConfig, projectiles);
        List<EnemyTorch> torchEnemies = spawnTorchEnemies(entityConfig);

        ObjConfig objC = gameConfig.ObjConfig();
        List<GameObject> objects = new ArrayList<>();

        spawnTrees(objects, objC.TREES_03_SPAWNPOINT(), objC.TREE_TAG_03(), objC.TREE_03_WIDTH, objC.TREE_03_HEIGHT,
                new Dimension(objC.TREE_03_HITBOX_WIDTH, objC.TREE_03_HITBOX_HEIGHT), objC.TREE_03_HITBOX_OFFSET_Y, objC);
        spawnTrees(objects, objC.TREES_02_SPAWNPOINT(), objC.TREE_TAG_02(), objC.TREE_02_WIDTH, objC.TREE_02_HEIGHT,
                new Dimension(objC.TREE_02_HITBOX_WIDTH, objC.TREE_02_HITBOX_HEIGHT), objC.TREE_02_HITBOX_OFFSET_Y, objC);
        spawnTrees(objects, objC.TREES_01_SPAWNPOINT(), objC.TREE_TAG_01(), objC.TREE_01_WIDTH, objC.TREE_01_HEIGHT,
                new Dimension(objC.TREE_01_HITBOX_WIDTH, objC.TREE_01_HITBOX_HEIGHT), objC.TREE_01_HITBOX_OFFSET_Y, objC);

        spawnBuildings(objects, objC.CASTLE_SPAWNPOINT(), objC.CASTLE_TAG(), ObjConfig.CASTLE_WIDTH, ObjConfig.CASTLE_HEIGHT,
                ObjConfig.CASTLE_HITBOX_WIDTH, ObjConfig.CASTLE_HITBOX_HEIGHT, ObjConfig.CASTLE_HITBOX_OFFSET_Y, objC);
        spawnBuildings(objects, objC.TOWER_SPAWNPOINT(), objC.TOWER_TAG(), ObjConfig.TOWER_WIDTH, ObjConfig.TOWER_HEIGHT,
                ObjConfig.TOWER_HITBOX_WIDTH, ObjConfig.TOWER_HITBOX_HEIGHT, ObjConfig.TOWER_HITBOX_OFFSET_Y, objC);
        spawnBuildings(objects, objC.GOLDMINE_SPAWNPOINT(), objC.GOLDMINE_TAG(), ObjConfig.GOLDMINE_WIDTH, ObjConfig.GOLDMINE_HEIGHT,
                ObjConfig.GOLDMINE_HITBOX_WIDTH, ObjConfig.GOLDMINE_HITBOX_HEIGHT, ObjConfig.GOLDMINE_HITBOX_OFFSET_Y, objC);
        spawnBuildings(objects, objC.GOBLIN_HOME_SPAWNPOINT(), objC.GOBLIN_HOME_TAG(), ObjConfig.GOBLIN_HOME_WIDTH, ObjConfig.GOBLIN_HOME_HEIGHT,
                ObjConfig.GOBLIN_HOME_HITBOX_WIDTH, ObjConfig.GOBLIN_HOME_HITBOX_HEIGHT, ObjConfig.GOBLIN_HOME_HITBOX_OFFSET_Y, objC);

        assignRandomPowerUps(objects, objC);

        return new InitializedWorld(
                worldGameMap, player, monk, tntEnemies, dynamiteEnemies, projectiles, torchEnemies, objects
        );
    }
    //-------------------------------------------------------------



    /**
     * HELPERS METHOD
     */
    private static List<EnemyTNT> spawnTntEnemies(EntityConfig entityConfig) {
        List<EnemyTNT> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.TNT_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.TNT_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyTNT(spawnPoint, entityConfig));
            }
        }
        return enemies;
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn dynamite enemies based on the spawn points defined.
     * It creates an EnemyDynamite instance for each spawn point and adds it to the list of enemies.
     */
    private static List<EnemyDynamite> spawnDynamiteEnemies(EntityConfig entityConfig, List<DynamiteProjectile> projectileStore) {
        List<EnemyDynamite> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.DYNAMITE_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.DYNAMITE_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyDynamite(spawnPoint, entityConfig, projectileStore));
            }
        }
        return enemies;
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn torch enemies based on the spawn points.
     * It creates an EnemyTorch instance for each spawn point and adds it to the list of enemies.
     */
    private static List<EnemyTorch> spawnTorchEnemies(EntityConfig entityConfig) {
        List<EnemyTorch> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.TORCH_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.TORCH_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyTorch(spawnPoint, entityConfig));
            }
        }
        return enemies;
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn trees based on the spawn points defined in the ObjConfig.
     * It creates an OBJ_Tree instance for each spawn point and adds it to the list of game objects.
     */
    private static void spawnTrees(List<GameObject> objects, List<SpawnPoint> spawnPoints, String treeTag,
                                   int treeWidth, int treeHeight, Dimension hitboxDim, int hitboxOffsetY, ObjConfig objConfig) {
        for (SpawnPoint spawnPoint : spawnPoints) {
            objects.add(new OBJ_Tree(objConfig, treeTag, spawnPoint, treeWidth, treeHeight,
                    createTreeSolidArea(treeWidth, hitboxDim, hitboxOffsetY)));
        }
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn buildings based on the spawn points defined in the ObjConfig.
     */
    private static void spawnBuildings(List<GameObject> objects, List<SpawnPoint> spawnPoints, String buildingTag,
                                       int buildingWidth, int buildingHeight, int hitboxWidth, int hitboxHeight,
                                       int hitboxOffsetY, ObjConfig objConfig) {
        for (SpawnPoint spawnPoint : spawnPoints) {
            GameObject building = new GameObject(
                    objConfig,
                    buildingTag,
                    spawnPoint,
                    buildingWidth,
                    buildingHeight,
                    createBuildingSolidArea(buildingWidth, hitboxWidth, hitboxHeight, hitboxOffsetY),
                    ObjConfig.BUILDING_SOLID
            );
            if (buildingTag.equals(objConfig.GOLDMINE_TAG())) {
                building.setSolid(false);
            }
            objects.add(building);
        }
    }
    //-------------------------------------------------------------
    /**
     * Helper method to create a solid area for trees.
     * It calculates the position and size of the solid area relative to the tree's visual representation.
     */
    private static Rectangle createTreeSolidArea(int treeWidth, Dimension treeHitbox, int hitboxOffsetY) {
        return new Rectangle(
                treeWidth / 2 - (treeHitbox.width / 2),
                hitboxOffsetY,
                treeHitbox.width,
                treeHitbox.height);
    }
    //-------------------------------------------------------------
    /**
     * Helper method to create a solid area for buildings.
     */
    private static Rectangle createBuildingSolidArea(int buildingWidth, int hitboxWidth, int hitboxHeight, int hitboxOffsetY) {
        return new Rectangle(
                buildingWidth / 2 - (hitboxWidth / 2),
                hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
        );
    }
    /**
     * Assigns random power-ups to a subset of trees in the game world.
     */
    private static void assignRandomPowerUps(List<GameObject> objects, ObjConfig objC) {
        assignPowerUpToRandomTree(objects, objC.TREE_TAG_03(), PowerUpType.SHIELD);
        assignPowerUpToRandomTree(objects, objC.TREE_TAG_02(), PowerUpType.HEALTH_RESTORE);
        assignPowerUpToRandomTree(objects, objC.TREE_TAG_01(), PowerUpType.SPEED_BOOST);
    }
    //---------------------------------------------
    private static void assignPowerUpToRandomTree(List<GameObject> objects, String treeTag, PowerUpType type) {
        List<OBJ_Tree> validTrees = new ArrayList<>();
        for (GameObject obj : objects) {
            if (obj instanceof OBJ_Tree tree && tree.getName().equals(treeTag)) {
                validTrees.add(tree);
            }
        }
        if (!validTrees.isEmpty()) {
            int randomIndex = (int) (Math.random() * validTrees.size());
            validTrees.get(randomIndex).setHiddenPowerUp(type);
        }
    }
    //end helpers -------------------------------------------------


}
//-------------------------------------------------------------------------------------------------------------------