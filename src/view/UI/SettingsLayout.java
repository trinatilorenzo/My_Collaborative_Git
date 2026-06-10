package view.UI;

import java.awt.Rectangle;

public record SettingsLayout( Rectangle settingsBounds,
                              Rectangle settingsIconBounds,
                              Rectangle audioRibbonBounds,
                             Rectangle musicBounds,
                              Rectangle soundBounds,
                              Rectangle fpsRibbonBounds,
                              Rectangle fpsBounds1,
                              Rectangle fpsBounds2,
                              Rectangle fpsBounds3,
                              Rectangle resRibbonBounds,
                              Rectangle resFullBounds,
                              Rectangle resHalfBounds,
                              Rectangle resMinBounds) {}
