package main.CONFIG.enu;

public final class ButtonValue {

    public enum MainMenu {
        NEW_GAME, LOAD_GAME, SETTINGS,
        TOGGLE_BLUE, TOGGLE_YELLOW, TOGGLE_RED, TOGGLE_PURPLE
    }

    public enum PauseMenu {
        RESUME, SAVE, PAUSE_SETTINGS,
    }

    public enum SettingsMenu {
        MUSIC, SOUND,
        RES_FULL, RES_MID, RES_MIN,
        QUIT, SETTINGS_ICON
    }

    public enum GameOverMenu {
        HOME_OVER, QUIT_OVER
    }

    public enum WinMenu{
        HOME_WIN, QUIT_WIN
    }
}

