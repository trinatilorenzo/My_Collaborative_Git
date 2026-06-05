package view.UI;

import java.awt.Rectangle;

public record PauseMenuLayout(Rectangle resumeBounds,
                              Rectangle settingsBounds,
                              Rectangle saveBounds,
                              Rectangle pauseTextBounds,
                              Rectangle pauseRibbonBounds) {}
