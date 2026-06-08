package view.UI;

import java.awt.Rectangle;

public record MainMenuLayout(Rectangle newGameBounds,
                             Rectangle continueBounds,
                             Rectangle settingsBounds,
                             Rectangle toggleYellowBounds,
                             Rectangle toggleRedBounds,
                             Rectangle toggleBlueBounds,
                             Rectangle togglePurpleBounds) {}
