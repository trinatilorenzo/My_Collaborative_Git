package tinyswordsisland.controller;

import tinyswordsisland.config.enu.GameState;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.model.GameMap;
import tinyswordsisland.model.IRenderable;

import java.awt.Rectangle;
import java.util.List;

public interface IController {
    GameState getGameState();
    boolean isDebugMode();
    int getPlayerWorldX();
    int getPlayerWorldY();
    int getPlayerCurrentLayer();
    int getPlayerLife();
    int getPlayerMaxLife();
    boolean playerHasShield();
    double getPlayerShieldTimerMs();
    Rectangle getPlayerSolidArea();
    GameMap getWorldMap();
    List<IRenderable> getAllRenderables();
    String getCurrentDialogue();
    String getCurrentMessage();
    boolean isSoundEnabled();
    boolean isMusicEnabled();
    int getResolutionValue();
    PlayerColor getPlayerColor();
}