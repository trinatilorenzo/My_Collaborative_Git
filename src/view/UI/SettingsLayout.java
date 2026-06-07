package view.UI;

import java.awt.Rectangle;

public record SettingsLayout( Rectangle settingsBounds,
                              Rectangle settingsIconBounds,
                              Rectangle audioTextBounds,
                              Rectangle audioRibbonBounds,
                             Rectangle musicBounds,
                              Rectangle soundBounds,
                              Rectangle fpsRibbonBounds,
                              Rectangle fpsTextBounds,
                              Rectangle fpsBounds1,
                              Rectangle fpsBounds2,
                              Rectangle fpsBounds3,
                              Rectangle resTextBounds,
                              Rectangle resRibbonBounds,
                              Rectangle resFullBounds,
                              Rectangle resHalfBounds,
                              Rectangle resMinBounds) {}
