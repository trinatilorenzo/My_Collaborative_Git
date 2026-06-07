package main.CONFIG.enu;

public final class ButtonValue {

    public enum MainMenu {
        NEW_GAME, LOAD_GAME, SETTINGS,
        TOGGLE_BLUE, TOGGLE_YELLOW, TOGGLE_RED, TOGGLE_PURPLE
    }

    public enum Pause {
        RESUME, SAVE, PAUSE_SETTINGS,
    }

    public enum Settings {
        MUSIC, SOUND,
        RES_FULL, RES_MID, RES_MIN,
        FPS_60, FPS_120, FPS_240,
        SETTINGS_ICON
    }

    public enum GameOver {
        RESTART
    }
}

