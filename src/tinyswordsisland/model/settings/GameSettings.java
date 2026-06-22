package tinyswordsisland.model.settings;

import tinyswordsisland.config.enu.PlayerColor;

import java.io.Serial;
import java.io.Serializable;

public class GameSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private int resolutionValue;
    private PlayerColor playerColor;

    public GameSettings(PlayerColor defaultColor) {
        this.playerColor = defaultColor;
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
}
