package tinyswordsisland.model;

import tinyswordsisland.input.InputState;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.UIConfig;
import tinyswordsisland.config.enu.*;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.event.AudioEventQueue;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.model.object.OBJ_PowerUp;
import tinyswordsisland.model.object.OBJ_Tree;
import tinyswordsisland.model.settings.GameSettings;
import tinyswordsisland.model.system.GameplayContext;
import tinyswordsisland.model.system.InteractionSystem;
import tinyswordsisland.model.system.LevelInitializer;
import tinyswordsisland.model.system.LevelInitializer.InitializedWorld;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameModel implements Serializable, IGameModel, CollisionWorld, GameplayContext {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient GameConfig gameConfig;
    private transient CollisionChecker collisionChecker;
    private transient InteractionSystem interactionSystem;

    private GameState gameState;
    private boolean debugMode;

    private GameMap worldGameMap;
    private List<GameObject> objects;

    private Player player;
    private Monk monk;
    private List<EnemyTNT> tntEnemies;
    private List<EnemyDynamite> dynamiteEnemies;
    private List<DynamiteProjectile> projectiles;
    private List<EnemyTorch> torchEnemies;

    private boolean settingsMenuOpen;
    private boolean settingsPauseOpen;
    private GameSettings settings;

    private String currentDialogue;
    private String currentMessage;
    private double messageTimer;
    private double deadStateElapsedMs;

    private AudioEventQueue audioEvents = new AudioEventQueue();

    private int currentLevel;
    private boolean levelCompleted;
    private boolean currentLevelPowerUpCollected;

    public GameModel(GameConfig GS) {
        gameConfig = GS;
        collisionChecker = new CollisionChecker(this);
        interactionSystem = new InteractionSystem();

        gameState = GameState.MENU;
        debugMode = false;
        settings = new GameSettings(GS.entityConfig().DEFAULT_COLOR);

        currentDialogue = "";
        currentMessage = "";
        messageTimer = 0.0;
        deadStateElapsedMs = 0.0;
        settingsMenuOpen = false;
        settingsPauseOpen = false;
    }

    @Override
    public void initializeNewGame() {
        InitializedWorld world = LevelInitializer.createNewWorld(gameConfig, settings.getPlayerColor());
        worldGameMap = world.worldGameMap();
        player = world.player();
        monk = world.monk();
        tntEnemies = world.tntEnemies();
        dynamiteEnemies = world.dynamiteEnemies();
        projectiles = world.projectiles();
        torchEnemies = world.torchEnemies();
        objects = world.objects();

        currentLevelPowerUpCollected = false;
        currentLevel = 0;
        levelCompleted = false;
        gameState = GameState.PLAYING;
    }

    @Override
    public void update(InputState input, double deltaMs) {
        switch (gameState) {
            case MENU, SETTINGS, GAME_OVER, WIN -> { }
            case PLAYING -> updatePlayingState(input, deltaMs);
            case PAUSED -> updateState(input);
        }
    }

    private void updatePlayingState(InputState input, double deltaMs) {
        int lifeBeforeUpdate = player.getLife();
        updatePlayer(input, deltaMs);
        updateEnemies(deltaMs);
        updateMonk(input);

        if (player.getState() == PlayerState.WALKING) {
            player.move();
        }

        List<GameObject> toSpawn = new ArrayList<>();
        for (GameObject obj : objects) {
            if (!obj.isRemoved()) {
                obj.update(deltaMs);
                if (obj instanceof OBJ_Tree tree && tree.shouldDropPowerUp()) {
                    Rectangle treeHitbox = tree.getSolidArea();
                    int powerUpX = tree.getWorldX() + treeHitbox.x + (treeHitbox.width - gameConfig.ObjConfig().POWER_UP_SIZE) / 2;
                    int powerUpY = tree.getWorldY() + treeHitbox.y + (treeHitbox.height - gameConfig.ObjConfig().POWER_UP_SIZE) / 2;
                    toSpawn.add(new OBJ_PowerUp(gameConfig.ObjConfig(), tree.getHiddenPowerUp(), powerUpX, powerUpY, tree.getLayer()));
                }
            }
        }
        if (!toSpawn.isEmpty()) {
            objects.addAll(toSpawn);
        }
        objects.removeIf(GameObject::isRemoved);

        monk.update(player, deltaMs);
        interactionSystem.update(this);
        updateMessage(deltaMs);
        updateEvents(lifeBeforeUpdate);
        updateState(input);
    }

    private void updatePlayer(InputState input, double deltaMs) {
        PlayerState playerStateBeforeUpdate = player.getState();

        if (player.isDying() || player.isDead()) {
            updateGameOverCountdown(deltaMs);
            updateDeathSequence(deltaMs);
            return;
        }

        player.update(input, deltaMs);
        collisionChecker.checkTile(player);
        collisionChecker.checkObjects(player);

        if (playerStateBeforeUpdate != PlayerState.ATTACKING && player.getState() == PlayerState.ATTACKING) {
            addAudioEvent(AudioEventType.PLAYER_ATTACK);
        }
        if (playerStateBeforeUpdate == PlayerState.ATTACKING && player.getState() != PlayerState.ATTACKING) {
            addAudioEvent(AudioEventType.PLAYER_ATTACK_STOP);
        }
        if (playerStateBeforeUpdate != PlayerState.WALKING && player.getState() == PlayerState.WALKING) {
            addAudioEvent(AudioEventType.PLAYER_WALK_START);
        }
        if (playerStateBeforeUpdate == PlayerState.WALKING && player.getState() != PlayerState.WALKING) {
            addAudioEvent(AudioEventType.PLAYER_WALK_STOP);
        }
    }

    private void updateEnemies(double deltaMs) {
        for (EnemyTNT tnt : tntEnemies) {
            TNTState previousState = tnt.getState();
            if (tnt.getState() != TNTState.EXPLODED) {
                tnt.update(player, deltaMs);
                collisionChecker.checkEntity(player, tnt);
                collisionChecker.checkTile(tnt);
                collisionChecker.checkObjects(tnt);
                tnt.move();
            }
            if (previousState != tnt.getState() && tnt.getState() == TNTState.EXPLODING) {
                addAudioEvent(AudioEventType.TNT_EXPLOSION);
            }
            if (previousState != tnt.getState() && tnt.getState() == TNTState.TRIGGERED) {
                addAudioEvent(AudioEventType.TNT_TRIGGERED);
            }
        }
        tntEnemies.removeIf(EnemyTNT::isExploded);

        for (EnemyDynamite dynamite : dynamiteEnemies) {
            DynamiteState previousState = dynamite.getState();
            if (dynamite.getState() != DynamiteState.DEAD) {
                dynamite.update(player, deltaMs);
                collisionChecker.checkEntity(player, dynamite);
                collisionChecker.checkTile(dynamite);
                collisionChecker.checkObjects(dynamite);
                dynamite.move();
                if (previousState != DynamiteState.ATTACKING && dynamite.getState() == DynamiteState.ATTACKING) {
                    addAudioEvent(AudioEventType.PROJECTILE_LAUNCHED);
                }
            }
        }
        dynamiteEnemies.removeIf(EnemyDynamite::isDead);

        for (DynamiteProjectile proj : projectiles) {
            proj.update(deltaMs);
            collisionChecker.checkTile(proj);
            if (collisionChecker.intersects(player, proj)) {
                player.takeDamage();
                proj.explode();
            }
            if (proj.isExploded()) {
                addAudioEvent(AudioEventType.PROJECTILE_EXPLODED);
            }
        }
        projectiles.removeIf(DynamiteProjectile::isExploded);

        for (EnemyTorch torch : torchEnemies) {
            if (torch.getState() != TorchState.DEAD) {
                torch.update(player, deltaMs);
                collisionChecker.checkEntity(torch, player);
                collisionChecker.checkEntity(player, torch);
                collisionChecker.checkTile(torch);
                collisionChecker.checkObjects(torch);
                torch.move();
            }
        }
        torchEnemies.removeIf(EnemyTorch::isDead);
    }

    private void updateMonk(InputState input) {
        if (monk.getState() == MonkState.IDLE) {
            currentDialogue = "";
        }
        if (monk.getState() == MonkState.TALKING && currentDialogue.isEmpty()) {
            currentDialogue = monk.getCurrentDialogue();
            addAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
        }
        if (monk.getState() == MonkState.TALKING && input.interact()) {
            monk.nextDialogue();
            if (!monk.hasFinishedDialogue()) {
                currentDialogue = monk.getCurrentDialogue();
                addAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
            } else {
                currentDialogue = "";
                monk.setState(MonkState.DISAPPEARING);
                addAudioEvent(AudioEventType.DIALOGUE_CLOSE);
            }
        }
    }

    private void updateMessage(double deltaMs) {
        if (currentMessage.isEmpty()) return;
        messageTimer += deltaMs;
        if (messageTimer >= UIConfig.MESSAGE_TIMER_MS) {
            currentMessage = "";
            messageTimer = 0;
        }
    }

    private void updateEvents(int lifeBeforeUpdate) {
        if (player.getLife() < lifeBeforeUpdate) {
            addAudioEvent(AudioEventType.PLAYER_DAMAGED);
        }
    }

    private void updateDeathSequence(double deltaMs) {
        monk.update(player, deltaMs);
        for (EnemyTNT tnt : tntEnemies) {
            if (tnt.getState() == TNTState.TRIGGERED || tnt.getState() == TNTState.EXPLODING) {
                tnt.update(player, deltaMs);
            }
        }
        tntEnemies.removeIf(EnemyTNT::isExploded);

        for (DynamiteProjectile proj : projectiles) {
            proj.update(deltaMs);
            collisionChecker.checkTile(proj);
        }
        projectiles.removeIf(DynamiteProjectile::isExploded);
    }

    private void updateGameOverCountdown(double deltaMs) {
        boolean readyForGameOver = player.isDeathAnimationCompleted() && !hasPendingTransientAnimations();
        if (readyForGameOver) {
            deadStateElapsedMs += deltaMs;
        } else {
            deadStateElapsedMs = 0.0;
        }
        if (deadStateElapsedMs >= UIConfig.GAME_OVER_DELAY_MS) {
            gameState = GameState.GAME_OVER;
            deadStateElapsedMs = 0.0;
        }
    }

    private void updateState(InputState input) {
        if (gameState == GameState.GAME_OVER || gameState == GameState.WIN) return;
        if (player.isDying() || player.isDead()) {
            gameState = GameState.PLAYING;
            return;
        }
        gameState = input.pause() ? GameState.PAUSED : GameState.PLAYING;
    }

    private boolean hasPendingTransientAnimations() {
        for (EnemyTNT tnt : tntEnemies) {
            if (tnt.getState() == TNTState.TRIGGERED || tnt.getState() == TNTState.EXPLODING) {
                return true;
            }
        }
        return !projectiles.isEmpty();
    }

    @Override
    public List<AudioEventType> consumeAudioEvents() {
        return audioEvents.consume();
    }

    @Override
    public void addAudioEvent(AudioEventType event) {
        audioEvents.add(event);
    }

    public void beforeSave() {
        audioEvents.clear();
    }

    @Override
    public void restoreTransientState(GameConfig config) {
        this.gameConfig = config;
        this.collisionChecker = new CollisionChecker(this);
        this.interactionSystem = new InteractionSystem();
        audioEvents.ensureInitialized();
        audioEvents.clear();

        if (settings == null) {
            settings = new GameSettings(config.entityConfig().DEFAULT_COLOR);
        }

        var objC = config.ObjConfig();
        var entC = config.entityConfig();

        if (player != null) player.setEntityConfig(entC);
        if (monk != null) monk.setEntityConfig(entC);
        if (tntEnemies != null) tntEnemies.forEach(e -> e.setEntityConfig(entC));
        if (dynamiteEnemies != null) dynamiteEnemies.forEach(e -> e.setEntityConfig(entC));
        if (torchEnemies != null) torchEnemies.forEach(e -> e.setEntityConfig(entC));
        if (objects != null) objects.forEach(obj -> obj.setObjConfig(objC));
        if (worldGameMap != null) worldGameMap.mConf(config.mapConfig());
    }

    public void afterLoad() {
        this.collisionChecker = new CollisionChecker(this);
        this.interactionSystem = new InteractionSystem();
        audioEvents.ensureInitialized();
        audioEvents.clear();

        if (objects == null) objects = new ArrayList<>();
        if (tntEnemies == null) tntEnemies = new ArrayList<>();
        if (dynamiteEnemies == null) dynamiteEnemies = new ArrayList<>();
        if (projectiles == null) projectiles = new ArrayList<>();
        if (torchEnemies == null) torchEnemies = new ArrayList<>();
        if (settings == null) settings = new GameSettings(PlayerColor.BLUE);
        if (currentDialogue == null) currentDialogue = "";
        if (currentMessage == null) currentMessage = "";
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        afterLoad();
    }

    @Override
    public void forcePlayingState() {
        gameState = GameState.PLAYING;
        settingsPauseOpen = false;
        settingsMenuOpen = false;
    }

    @Override
    public List<IRenderable> getAllRenderables() {
        List<IRenderable> allRenderables = new ArrayList<>();
        if (player != null) allRenderables.add(player);
        if (monk != null) allRenderables.add(monk);
        if (tntEnemies != null) allRenderables.addAll(tntEnemies);
        if (torchEnemies != null) allRenderables.addAll(torchEnemies);
        if (dynamiteEnemies != null) allRenderables.addAll(dynamiteEnemies);
        if (projectiles != null) allRenderables.addAll(projectiles);
        if (objects != null) allRenderables.addAll(objects);
        return allRenderables;
    }

    @Override
    public int getTileSize() {
        return gameConfig.screenConfig().TILE_SIZE();
    }

    @Override
    public GameState getGameState() { return gameState; }

    @Override
    public boolean isDebugMode() { return debugMode; }

    @Override
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    @Override
    public Player getPlayer() { return player; }

    @Override
    public GameMap getWorldMap() { return worldGameMap; }

    public CollisionChecker getCollisionChecker() { return collisionChecker; }

    @Override
    public List<GameObject> getObjects() { return objects; }

    @Override
    public Monk getMonk() { return monk; }

    @Override
    public List<EnemyTNT> getTntEnemies() { return tntEnemies; }

    @Override
    public List<EnemyDynamite> getDynamiteEnemies() { return dynamiteEnemies; }

    @Override
    public List<EnemyTorch> getTorchEnemies() { return torchEnemies; }

    @Override
    public String getCurrentDialogue() { return currentDialogue; }

    @Override
    public String getCurrentMessage() { return currentMessage; }

    @Override
    public GameConfig getGameConfig() { return gameConfig; }

    @Override
    public void resumeFromPause() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }

    @Override
    public void toggleSettingsFromPause() {
        if (settingsPauseOpen) {
            settingsPauseOpen = false;
        } else {
            settingsPauseOpen = true;
            settingsMenuOpen = false;
            gameState = GameState.SETTINGS;
        }
    }

    @Override
    public void toggleSettingsFromMenu() {
        if (settingsMenuOpen) {
            settingsMenuOpen = false;
        } else {
            settingsMenuOpen = true;
            settingsPauseOpen = false;
            gameState = GameState.SETTINGS;
        }
    }

    @Override
    public void closeSettings() {
        if (settingsMenuOpen) {
            gameState = GameState.MENU;
            settingsMenuOpen = false;
        }
        if (settingsPauseOpen) {
            gameState = GameState.PAUSED;
            settingsPauseOpen = false;
        }
    }

    @Override
    public void returnToMenu() {
        gameState = GameState.MENU;
    }

    @Override
    public void toggleSound() { settings.toggleSound(); }

    @Override
    public void toggleMusic() { settings.toggleMusic(); }

    @Override
    public void setMinResolution() { settings.setMinResolution(); }

    @Override
    public void setMidResolution() { settings.setMidResolution(); }

    @Override
    public void setMaxResolution() { settings.setMaxResolution(); }

    @Override
    public void setPlayerColor(PlayerColor playerColor) {
        settings.setPlayerColor(playerColor);
        if (player != null) {
            player.setColor(playerColor);
        }
    }

    @Override
    public boolean isSoundEnabled() { return settings.isSoundEnabled(); }

    @Override
    public boolean isMusicEnabled() { return settings.isMusicEnabled(); }

    @Override
    public int getResolutionValue() { return settings.getResolutionValue(); }

    @Override
    public PlayerColor getPlayerColor() {
        if (player != null) {
            settings.setPlayerColor(player.getColor());
        }
        return settings.getPlayerColor();
    }

    @Override
    public int getCurrentLevel() { return currentLevel; }

    @Override
    public void setCurrentLevel(int level) { this.currentLevel = level; }

    @Override
    public boolean isLevelCompleted() { return levelCompleted; }

    @Override
    public void setLevelCompleted(boolean completed) { this.levelCompleted = completed; }

    @Override
    public boolean isCurrentLevelPowerUpCollected() { return currentLevelPowerUpCollected; }

    @Override
    public void setCurrentLevelPowerUpCollected(boolean collected) {
        this.currentLevelPowerUpCollected = collected;
    }

    @Override
    public void setCurrentMessage(String message) { this.currentMessage = message; }

    @Override
    public void setGameState(GameState state) { this.gameState = state; }
}
