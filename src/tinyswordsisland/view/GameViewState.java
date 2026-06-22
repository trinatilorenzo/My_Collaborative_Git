package tinyswordsisland.view;

import tinyswordsisland.config.enu.GameState;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.model.GameMap;
import tinyswordsisland.model.IRenderable;

import java.awt.Rectangle;
import java.util.List;

public record GameViewState(
        GameState gameState,
        boolean debugMode,
        int playerWorldX,
        int playerWorldY,
        int playerCurrentLayer,
        int playerLife,
        int playerMaxLife,
        boolean playerHasShield,
        double playerShieldTimerMs,
        Rectangle playerSolidArea,
        GameMap worldMap,
        List<IRenderable> renderables,
        String currentDialogue,
        String currentMessage,
        boolean soundEnabled,
        boolean musicEnabled,
        int resolutionValue,
        PlayerColor playerColor
) {}
