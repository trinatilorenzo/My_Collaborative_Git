package controller;

import main.CONFIG.enu.GameState;
import main.CONFIG.enu.PlayerColor;
import model.GameMap;
import model.IRenderable;
import model.event.AudioEventType;

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
    List<AudioEventType> consumeAudioEvents();
}