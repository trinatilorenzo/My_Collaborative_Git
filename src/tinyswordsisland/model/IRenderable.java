package tinyswordsisland.model;
import java.awt.Rectangle;

public interface IRenderable {

    RenderableType getRenderableType();

    int getWorldX();
    int getWorldY();
    Rectangle getSolidArea();
    int getWidth();
    int getHeight();

    default int getRenderState() { return 0; }
    default int getRenderDirection() { return 0; }
    default boolean isFacingRightRender() { return true; }
    default int getLifeRender() { return 0; }
    default int getMaxLifeRender() { return 0; }
    default boolean isShieldedRender() { return false; }
    default boolean isSpeedBoostedRender() { return false; }
    default boolean isHealthRestoredRender() { return false; }
    default boolean isDeadRender() { return false; }
    default boolean isFlashingRender() { return false; }
    default boolean hasPowerUpRender() { return false; }
    default boolean isRemovedRender() { return false; }
    default String getRenderVariant() { return ""; }
    default int getRenderLayer() { return 0; }
    default int getAttackAreaX() { return 0; }
    default int getAttackAreaY() { return 0; }
    default int getAttackAreaWidth() { return 0; }
    default int getAttackAreaHeight() { return 0; }
    default double getRenderAngle() { return 0.0; }

}