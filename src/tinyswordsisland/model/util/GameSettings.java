package tinyswordsisland.model.util;

import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.enu.PlayerColor;

import java.io.Serial;
import java.io.Serializable;

public class GameSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean isDebugMode;
    private boolean isSettingsMenuOpen;
    private boolean musicEnabled;
    private boolean soundEnabled;
    private int resolutionValue;
    private PlayerColor playerColor;

    // --- Constructor ---
    public GameSettings() {
        isDebugMode = false;
        musicEnabled = true;
        soundEnabled = true;
        resolutionValue = 0;
        playerColor = EntityConfig.DEFAULT_COLOR;
    }
    //-------------------------------------------------------------//

    //getter
    public boolean isDebugMode() {
        return isDebugMode;
    }
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    public int getResolutionValue() {
        return resolutionValue;
    }
    public PlayerColor getPlayerColor() {
        return playerColor;
    }
    //-------------------------------------------------------------


    //setter
    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }
    public void toggleMusic() {
        musicEnabled = !musicEnabled;
    }
    public void toggleSound() {
        soundEnabled = !soundEnabled;
    }
    public void setMinResolution() {
        resolutionValue = 0;
    }
    public void setMidResolution() {
        resolutionValue = 1;
    }
    public void setMaxResolution() {
        resolutionValue = 2;
    }
    public void setPlayerColor(PlayerColor color) {
        if (color != null) {
            playerColor = color;
        }
    }
    //-------------------------------------------------------------
}
