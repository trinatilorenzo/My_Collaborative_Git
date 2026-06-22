package tinyswordsisland.view.ui;

import tinyswordsisland.config.enu.ButtonValue;
import tinyswordsisland.config.enu.GameState;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.Supplier;

public final class MenuHitTester {

    private MenuHitTester() {}

    public static Enum<?> hitTest(GameState screen, Point point, MenuLayouts layouts) {
        if (point == null || layouts == null) return null;
        return switch (screen) {
            case MENU -> hitMainMenu(point, layouts.mainMenu().get());
            case PAUSED -> hitPauseMenu(point, layouts.pauseMenu().get());
            case SETTINGS -> hitSettingsMenu(point, layouts.settings().get());
            case GAME_OVER -> hitGameOverMenu(point, layouts.gameOver().get());
            case WIN -> hitWinMenu(point, layouts.win().get());
            default -> null;
        };
    }

    public record MenuLayouts(
            Supplier<MainMenuLayout> mainMenu,
            Supplier<PauseMenuLayout> pauseMenu,
            Supplier<SettingsLayout> settings,
            Supplier<GameOverLayout> gameOver,
            Supplier<WinLayout> win
    ) {}

    private static Enum<?> hitMainMenu(Point point, MainMenuLayout l) {
        if (l == null) return null;
        if (contains(l.newGameBounds(), point)) return ButtonValue.MainMenu.NEW_GAME;
        if (contains(l.continueBounds(), point)) return ButtonValue.MainMenu.LOAD_GAME;
        if (contains(l.settingsBounds(), point)) return ButtonValue.MainMenu.SETTINGS;
        if (contains(l.toggleBlueBounds(), point)) return ButtonValue.MainMenu.TOGGLE_BLUE;
        if (contains(l.toggleYellowBounds(), point)) return ButtonValue.MainMenu.TOGGLE_YELLOW;
        if (contains(l.toggleRedBounds(), point)) return ButtonValue.MainMenu.TOGGLE_RED;
        if (contains(l.togglePurpleBounds(), point)) return ButtonValue.MainMenu.TOGGLE_PURPLE;
        return null;
    }

    private static Enum<?> hitPauseMenu(Point point, PauseMenuLayout l) {
        if (l == null) return null;
        if (contains(l.resumeBounds(), point)) return ButtonValue.PauseMenu.RESUME;
        if (contains(l.settingsBounds(), point)) return ButtonValue.PauseMenu.PAUSE_SETTINGS;
        if (contains(l.saveBounds(), point)) return ButtonValue.PauseMenu.SAVE;
        return null;
    }

    private static Enum<?> hitSettingsMenu(Point point, SettingsLayout l) {
        if (l == null) return null;
        if (contains(l.settingsIconBounds(), point)) return ButtonValue.SettingsMenu.SETTINGS_ICON;
        if (contains(l.musicBounds(), point)) return ButtonValue.SettingsMenu.MUSIC;
        if (contains(l.soundBounds(), point)) return ButtonValue.SettingsMenu.SOUND;
        if (contains(l.resFullBounds(), point)) return ButtonValue.SettingsMenu.RES_FULL;
        if (contains(l.resHalfBounds(), point)) return ButtonValue.SettingsMenu.RES_MID;
        if (contains(l.resMinBounds(), point)) return ButtonValue.SettingsMenu.RES_MIN;
        if (contains(l.quitBounds(), point)) return ButtonValue.SettingsMenu.QUIT;
        return null;
    }

    private static Enum<?> hitGameOverMenu(Point point, GameOverLayout l) {
        if (l == null) return null;
        if (contains(l.homeButtonBounds(), point)) return ButtonValue.GameOverMenu.HOME_OVER;
        if (contains(l.quitButtonBounds(), point)) return ButtonValue.GameOverMenu.QUIT_OVER;
        return null;
    }

    private static Enum<?> hitWinMenu(Point point, WinLayout l) {
        if (l == null) return null;
        if (contains(l.homeButtonBounds(), point)) return ButtonValue.WinMenu.HOME_WIN;
        if (contains(l.quitButtonBounds(), point)) return ButtonValue.WinMenu.QUIT_WIN;
        return null;
    }

    private static boolean contains(Rectangle bounds, Point p) {
        return bounds != null && p != null && bounds.contains(p);
    }
}
