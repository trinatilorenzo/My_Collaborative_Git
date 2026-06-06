package main.CONFIG;

/**
 * UI SETTINGS
 */
//----------------------------------------------------------------------------------------------------------------------
public record UIConfig() {

    //CUSTOM CURSOR
    //-------------------------------------------------------------
    public static final String CURSOR_PATH = "/res/UI/Pointers/01.png";
    //-------------------------------------------------------------

    //MENU
    //-------------------------------------------------------------
    public static final int MAIN_MENU_ITEM_COUNT = 3;
    public static final int MENU_DEFAULT_SELECTION = 0;
    public static final int MENU_NO_SELECTION = -1;

    public static final int MENU_LOGO_WIDTH = 500;

    public static final int MENU_PADDING = 25;

    public static final int MENU_BUTTON_WIDTH = 420;
    public static final int MENU_BUTTON_HEIGHT = 110;

    public static final int MENU_BUTTON_SETTINGS_SIZE = 64;

    public static final int MENU_RIBBON_X = 20;
    public static final int MENU_RIBBON_Y = 16;
    public static final int MENU_RIBBON_SIZE = 64;
    //-------------------------------------------------------------

    //HUD PLAYER LIFE
    public static final int HUD_LIFE_X = 20;
    public static final int HUD_LIFE_Y = 20;

    public static final long DAMAGE_FLASH_DURATION_NS = 1500_000_000;

    //PAUSE
    //-------------------------------------------------------------
    public static final int PAUSE_MENU_ITEM_COUNT = 3;

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
    //-------------------------------------------------------------



}
//----------------------------------------------------------------------------------------------------------------------
