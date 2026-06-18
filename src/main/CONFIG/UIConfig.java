package main.CONFIG;

import main.CONFIG.enu.ButtonValue;

/**
 * UI SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record UIConfig() {

    //CUSTOM CURSOR
    //-------------------------------------------------------------
    public static final String CURSOR_PATH = "/res/UI/Pointers/01.png";
    //-------------------------------------------------------------

    //Default button Selection
    public static final ButtonValue.MainMenu MENU_DEFAULT_SELECTION = null ;
    public static final ButtonValue.PauseMenu PAUSE_MENU_DEFAULT_SELECTION = null;
    public static final ButtonValue.SettingsMenu SETTINGS_MENU_DEFAULT_SELECTION = null;
    public static final ButtonValue.GameOverMenu GAME_OVER_DEFAULT_SELECTION = null;
    public static final ButtonValue.WinMenu WIN_DEFAULT_SELECTION = null;

    public static final int MAX_BUTTON_TEXT_SIZE = 90;
    public static final int MIN_BUTTON_TEXT_SIZE = 15;
    public static final int MAX_RIBBON_TEXT_SIZE = 150;

    //MENU
    //-------------------------------------------------------------



    public static final int MENU_PADDING = 25;

    public static final int MENU_BUTTON_WIDTH = 420;
    public static final int MENU_BUTTON_HEIGHT = 110;

    public static final int MENU_BUTTON_SETTINGS_SIZE = 64;

    public static final int MENU_RIBBON_X = 20;
    public static final int MENU_RIBBON_Y = 16;
    public static final int MENU_RIBBON_SIZE = 100;
    //-------------------------------------------------------------

    //HUD PLAYER LIFE
    public static final int HUD_LIFE_X = 20;
    public static final int HUD_LIFE_Y = 20;

    public static final long DAMAGE_FLASH_DURATION_NS = 1500_000_000;

    //HUD PLAYER SHIELD
    public static final int BAR_SHIELD_WIDTH = 160;
    public static final int BAR_SHIELD_HEIGHT = 18;
    public static final int ICON_SHIELD_SIZE = 30;
    public static final int SHIELD_OFFSET_SCREEN = 20;

    //PAUSE
    //-------------------------------------------------------------

    public static final int BANNER_WIDTH = 192*4;
    public static final int PAUSE_RIBBON_OFFSET_Y = 24;
    public static final int PAUSE_TITLE_FONT_SIZE = 90;

    public static final int RESUME_BUTTON_WIDTH = 320;
    public static final int RESUME_BUTTON_HEIGHT = 100;

    public static final int SAVE_BUTTON_WIDTH = 320;
    public static final int SAVE_BUTTON_HEIGHT = 100;

    public static final int PAUSE_PADDING = 15;

    //GAME OVER
    //-------------------------------------------------------------
    public static final double GAME_OVER_DELAY_MS = 500.0;
    public static final int GAME_OVER_RIBBON_HEIGHT = 130;
    public static final int GAME_OVER_PADDING = 80;
    //-------------------------------------------------------------

    //SETTINGS MENU
    //-------------------------------------------------------------

    public static final int SETTINGS_PADDING = 20;
    public static final int SETTINGS_RIBBON_WIDTH = 500;
    public static final int SETTINGS_RIBBON_HEIGHT = 60;
    public static final int SETTINGS_ICON_SIZE = 80;
    public static final int SETTINGS_FIRST_RIBBON_Y = 60;
    public static final int SETTINGS_OPTION_BUTTON_WIDTH = 140;
    public static final int SETTINGS_OPTION_BUTTON_HEIGHT = 50;

    public static final int SETTINGS_BUTTON_WIDTH = 64 * 3;
    public static final int SETTINGS_BUTTON_HEIGHT = 64;
    //-------------------------------------------------------------

    public static final float BUTTON_INSIDE_PADDING_W = 0.84f;
    public static final float BUTTON_INSIDE_PADDING_H = 0.44f;

    // --- Main Menu ---
    public static final float MENU_LOGO_WIDTH = 0.35f; // % screenWidth
    public static final float MENU_BUTTON_WIDTH_PCT          = 0.28f;  // % screenWidth
    public static final float MENU_BUTTON_HEIGHT_PCT         = 0.12f; // % screenHeight
    public static final float MENU_BUTTON_SETTINGS_SIZE_PCT  = 0.1f;  // % min(width,height)
    public static final float MENU_RIBBON_SIZE_PCT           = 0.12f; // % min(width,height)
    public static final float MENU_PADDING_PCT               = 0.02f;// % screenHeight

    // --- Settings ---
    public static final float SETTINGS_RIBBON_HEIGHT_PCT     = 0.09f; // % screenHeight
    public static final float SETTINGS_PADDING_PCT           = 0.02f;// % screenHeight
    public static final float SETTINGS_ICON_SIZE_PCT         = 0.1f; // % min(width,height)
    public static final float SETTINGS_BUTTON_WIDTH_PCT      = 0.16f; // % screenWidth
    public static final float SETTINGS_BUTTON_HEIGHT_PCT     = 0.09f; // % screenHeight

    // --- Game Over ---
    public static final float GAME_OVER_RIBBON_WIDTH_PCT     = 0.55f; // % screenWidth
    public static final float GAME_OVER_RIBBON_HEIGHT_PCT    = 0.18f; // % screenHeight
    public static final float GAME_OVER_PADDING_PCT          = 0.08f; // % screenHeight
    public static final float GAME_OVER_BUTTON_WIDTH_PCT        = 0.24f; // % screenWidth
    public static final float GAME_OVER_BUTTON_HEIGHT_PCT       = 0.12f;// % screenHeight

    // --- Win ---
    public static final float WIN_RIBBON_WIDTH_PCT     = 0.55f; // % screenWidth
    public static final float WIN_RIBBON_HEIGHT_PCT    = 0.18f; // % screenHeight
    public static final float WIN_PADDING_PCT          = 0.08f; // % screenHeight
    public static final float WIN_BUTTON_WIDTH_PCT        = 0.24f; // % screenWidth
    public static final float WIN_BUTTON_HEIGHT_PCT       = 0.12f;// % screenHeight

    // --- Pause Menu ---
    public static final float BANNER_WIDTH_PCT               = 0.45f; // % screenWidth
    public static final int BANNER_ASPECT_RATIO      = 3;
    public static final float PAUSE_RIBBON_OFFSET_Y_PCT      = 0.2f; // % screenHeight
    public static final float PAUSE_BUTTON_WIDTH_PCT          = 0.20f; // % screenWidth
    public static final float PAUSE_BUTTON_HEIGHT_PCT         = 0.10f;// % screenHeight
    public static final float PAUSE_PADDING_PCT              = 0.02f;// % screenHeight




}
//----------------------------------------------------------------------------------------------------------------------
