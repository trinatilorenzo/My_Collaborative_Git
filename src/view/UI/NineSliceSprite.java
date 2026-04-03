package view.UI;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

//TODO sintassi commenti e revisione codice
/**
 * Utility for drawing 9-slice scalable sprites using nearest-neighbor filtering.
 */
public class NineSliceSprite {

    private final BufferedImage image;
    private final int sliceLeft;
    private final int sliceRight;
    private final int sliceTop;
    private final int sliceBottom;

    private final int imgWidth;
    private final int imgHeight;

    public NineSliceSprite(String filePath, int sliceLeft, int sliceRight, int sliceTop, int sliceBottom) {
        this(loadImage(filePath), sliceLeft, sliceRight, sliceTop, sliceBottom);
    }

    public NineSliceSprite(BufferedImage image, int sliceLeft, int sliceRight, int sliceTop, int sliceBottom) {
        this.image = Objects.requireNonNull(image, "image");
        this.sliceLeft = sliceLeft;
        this.sliceRight = sliceRight;
        this.sliceTop = sliceTop;
        this.sliceBottom = sliceBottom;
        this.imgWidth = image.getWidth();
        this.imgHeight = image.getHeight();
        validateSlices();
    }

    private void validateSlices() {
        if (sliceLeft < 0 || sliceRight < 0 || sliceTop < 0 || sliceBottom < 0) {
            throw new IllegalArgumentException("Slice margins must be non-negative");
        }
        if (sliceLeft + sliceRight > imgWidth || sliceTop + sliceBottom > imgHeight) {
            throw new IllegalArgumentException("Slice margins exceed image dimensions");
        }
    }

    private static BufferedImage loadImage(String filePath) {
        Objects.requireNonNull(filePath, "filePath");
        try {
            return ImageIO.read(new File(filePath));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load image: " + filePath, e);
        }
    }

    /**
     * Draws the 9-slice. Width/height are clamped to the minimum viable size.
     * If {@code width <= 0} or {@code height <= 0} the original image size is used.
     */
    public void draw(Graphics2D g, int x, int y, int width, int height) {
        int drawWidth = Math.max(width > 0 ? width : imgWidth, getMinWidth());
        int drawHeight = Math.max(height > 0 ? height : imgHeight, getMinHeight());

        Object previousHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Corners
        drawRegion(g, x, y, sliceLeft, sliceTop, 0, 0, sliceLeft, sliceTop);
        drawRegion(g, x + drawWidth - sliceRight, y, sliceRight, sliceTop,
                imgWidth - sliceRight, 0, imgWidth, sliceTop);
        drawRegion(g, x, y + drawHeight - sliceBottom, sliceLeft, sliceBottom,
                0, imgHeight - sliceBottom, sliceLeft, imgHeight);
        drawRegion(g, x + drawWidth - sliceRight, y + drawHeight - sliceBottom, sliceRight, sliceBottom,
                imgWidth - sliceRight, imgHeight - sliceBottom, imgWidth, imgHeight);

        // Edges
        int edgeWidth = drawWidth - sliceLeft - sliceRight;
        int edgeHeight = drawHeight - sliceTop - sliceBottom;

        if (edgeWidth > 0) {
            // Top
            drawRegion(g, x + sliceLeft, y, edgeWidth, sliceTop,
                    sliceLeft, 0, imgWidth - sliceRight, sliceTop);
            // Bottom
            drawRegion(g, x + sliceLeft, y + drawHeight - sliceBottom, edgeWidth, sliceBottom,
                    sliceLeft, imgHeight - sliceBottom, imgWidth - sliceRight, imgHeight);
        }

        if (edgeHeight > 0) {
            // Left
            drawRegion(g, x, y + sliceTop, sliceLeft, edgeHeight,
                    0, sliceTop, sliceLeft, imgHeight - sliceBottom);
            // Right
            drawRegion(g, x + drawWidth - sliceRight, y + sliceTop, sliceRight, edgeHeight,
                    imgWidth - sliceRight, sliceTop, imgWidth, imgHeight - sliceBottom);
        }

        // Center
        if (edgeWidth > 0 && edgeHeight > 0) {
            drawRegion(g, x + sliceLeft, y + sliceTop, edgeWidth, edgeHeight,
                    sliceLeft, sliceTop, imgWidth - sliceRight, imgHeight - sliceBottom);
        }

        restoreInterpolation(g, previousHint);
    }

    /** Convenience overload using only width/height from a {@link java.awt.Dimension}. */
    public void draw(Graphics2D g, int x, int y, java.awt.Dimension size) {
        draw(g, x, y, size.width, size.height);
    }

    public int getMinWidth() {
        return sliceLeft + sliceRight;
    }

    public int getMinHeight() {
        return sliceTop + sliceBottom;
    }

    public int getImageWidth() {
        return imgWidth;
    }

    public int getImageHeight() {
        return imgHeight;
    }

    public int getLeftInset() {
        return sliceLeft;
    }

    public int getRightInset() {
        return sliceRight;
    }

    public int getTopInset() {
        return sliceTop;
    }

    public int getBottomInset() {
        return sliceBottom;
    }

    public java.awt.Dimension getMinSize() {
        return new java.awt.Dimension(getMinWidth(), getMinHeight());
    }

    private void drawRegion(Graphics2D g, int dx1, int dy1, int dWidth, int dHeight,
                            int sx1, int sy1, int sx2, int sy2) {
        if (dWidth <= 0 || dHeight <= 0 || sx2 <= sx1 || sy2 <= sy1) {
            return; // Nothing to draw
        }
        g.drawImage(
                image,
                dx1,
                dy1,
                dx1 + dWidth,
                dy1 + dHeight,
                sx1,
                sy1,
                sx2,
                sy2,
                null
        );
    }

    private void restoreInterpolation(Graphics2D g, Object previousHint) {
        if (previousHint != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previousHint);
        }
    }
}
