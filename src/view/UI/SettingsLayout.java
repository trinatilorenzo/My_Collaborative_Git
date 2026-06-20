package view.UI;

import java.awt.Rectangle;

public record SettingsLayout( Rectangle settingsBounds,
                              Rectangle settingsIconBounds,
                              Rectangle audioRibbonBounds,
                             Rectangle musicBounds,
                              Rectangle soundBounds,
                              Rectangle quitBounds,
                              Rectangle resRibbonBounds,
                              Rectangle resFullBounds,
                              Rectangle resHalfBounds,
                              Rectangle resMinBounds) {}
