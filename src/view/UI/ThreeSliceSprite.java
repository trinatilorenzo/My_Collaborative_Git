package view.UI;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

//TODO sintassi commenti e revisione codice
/**
 * Horizontal 3-slice sprite (left cap, stretchable middle, right cap) with nearest-neighbor rendering.
 */
public class ThreeSliceSprite {

    private final BufferedImage image;
    private final int sliceLeft;
    private final int sliceRight;
    private final int imgWidth;
    private final int imgHeight;

    public ThreeSliceSprite(String filePath, int sliceLeft, int sliceRight) {
        this(loadImage(filePath), sliceLeft, sliceRight);
    }

    public ThreeSliceSprite(BufferedImage image, int sliceLeft, int sliceRight) {
        this.image = Objects.requireNonNull(image, "image");
        this.sliceLeft = sliceLeft;
        this.sliceRight = sliceRight;
        this.imgWidth = image.getWidth();
        this.imgHeight = image.getHeight();
        validateSlices();
    }

    private void validateSlices() {
        if (sliceLeft < 0 || sliceRight < 0) {
            throw new IllegalArgumentException("Slice margins must be non-negative");
        }
        if (sliceLeft + sliceRight > imgWidth) {
            throw new IllegalArgumentException("Slice margins exceed image width");
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
     * Draws the banner. If {@code height <= 0} it keeps the original image height.
     */
    public void draw(Graphics2D g, int x, int y, int width, int height) {
        int drawHeight = height > 0 ? height : imgHeight;
        int drawWidth = Math.max(width, getMinWidth());

        Object previousHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int dx = x;

        // Left cap (1:1 scale)
        drawRegion(g, dx, y, sliceLeft, drawHeight, 0, 0, sliceLeft, imgHeight);
        dx += sliceLeft;

        int centerWidth = drawWidth - sliceLeft - sliceRight;
        if (centerWidth > 0) {
            drawRegion(g, dx, y, centerWidth, drawHeight,
                    sliceLeft, 0, imgWidth - sliceRight, imgHeight);
            dx += centerWidth;
        }

        // Right cap (1:1 scale)
        drawRegion(g, dx, y, sliceRight, drawHeight,
                imgWidth - sliceRight, 0, imgWidth, imgHeight);

        restoreInterpolation(g, previousHint);
    }

    /** Convenience: draw using only the desired width, preserving native height. */
    public void draw(Graphics2D g, int x, int y, int width) {
        draw(g, x, y, width, imgHeight);
    }

    public int getMinWidth() {
        return sliceLeft + sliceRight;
    }

    public int getHeight() {
        return imgHeight;
    }

    public int getLeftCapWidth() {
        return sliceLeft;
    }

    public int getRightCapWidth() {
        return sliceRight;
    }

    public Dimension getMinSize() {
        return new Dimension(getMinWidth(), imgHeight);
    }

    private void drawRegion(Graphics2D g, int dx1, int dy1, int dWidth, int dHeight,
                            int sx1, int sy1, int sx2, int sy2) {
        if (dWidth <= 0 || dHeight <= 0 || sx2 <= sx1 || sy2 <= sy1) {
            return;
        }
        g.drawImage(image,
                dx1,
                dy1,
                dx1 + dWidth,
                dy1 + dHeight,
                sx1,
                sy1,
                sx2,
                sy2,
                null);
    }

    private void restoreInterpolation(Graphics2D g, Object previousHint) {
        if (previousHint != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previousHint);
        }
    }
}
