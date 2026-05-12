package view.UI;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;


/**
 * Scalable sprite supporting both 9-slice (all four margins) and horizontal 3-slice
 * (left/right caps only, sliceTop=0 sliceBottom=0). Uses nearest-neighbor rendering.
 */
//-------------------------------------------------------------------------------------------------------------------
public class SliceSprite {

    private final BufferedImage image; //Sprite image
    private final int imgWidth;
    private final int imgHeight;

    private final int sliceLeft; // left cap width
    private final int sliceRight; // right cap width
    private final int sliceTop; // top cap height
    private final int sliceBottom; // bottom cap height


    // 3-slice constructors (sliceTop=0, sliceBottom=0)
    //-------------------------------------------------------------
    /**
     * 3-slice CONSTRUCTOR using file path (horizontal: left cap, stretchable center, right cap).
     */
    //-------------------------------------------------------------
    public SliceSprite(String filePath, int sliceLeft, int sliceRight) {
        this(loadImage(filePath), sliceLeft, sliceRight, 0, 0);
    }
    //-------------------------------------------------------------
    /**
     * 3-slice CONSTRUCTOR using buffered image.
     */
    //-------------------------------------------------------------
    public SliceSprite(BufferedImage image, int sliceLeft, int sliceRight) {
        this(image, sliceLeft, sliceRight, 0, 0);
    }
    //-------------------------------------------------------------


    // 9-slice constructors
    //-------------------------------------------------------------
    /**
     * 9-slice CONSTRUCTOR using file path and slice dimensions.
     */
    //-------------------------------------------------------------
    public SliceSprite(String filePath, int sliceLeft, int sliceRight, int sliceTop, int sliceBottom) {
        this(loadImage(filePath), sliceLeft, sliceRight, sliceTop, sliceBottom);
    }
    //-------------------------------------------------------------
    /**
     * 9-slice CONSTRUCTOR using buffered image and slice dimensions.
     */
    //-------------------------------------------------------------
    public SliceSprite(BufferedImage image, int sliceLeft, int sliceRight, int sliceTop, int sliceBottom) {
        this.image = Objects.requireNonNull(image, "image");
        this.sliceLeft = sliceLeft;
        this.sliceRight = sliceRight;
        this.sliceTop = sliceTop;
        this.sliceBottom = sliceBottom;
        this.imgWidth = image.getWidth();
        this.imgHeight = image.getHeight();
        validateSlices();
    }
    //-------------------------------------------------------------


    /**
     * Convenience draw using only width; preserves native image height.
     * Intended for 3-slice use.
     */
    //-------------------------------------------------------------
    public void draw(Graphics2D g, int x, int y, int width) {
        draw(g, x, y, width, imgHeight);
    }
    //-------------------------------------------------------------

    /**
     * Draws the sprite. Width/height are clamped to the minimum viable size.
     * If {@code width <= 0} or {@code height <= 0} the original image size is used.
     */
    //-------------------------------------------------------------
    public void draw(Graphics2D g, int x, int y, int width, int height) {
        int drawWidth = Math.max(width > 0 ? width : imgWidth, getMinWidth());
        int drawHeight = Math.max(height > 0 ? height : imgHeight, getMinHeight());

        // Nearest-neighbor interpolation is used to preserve the original image size when scaling.
        Object previousHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Corners
        //-------------------------------------------------------------
        // top left
        drawRegion(g, x, y, sliceLeft, sliceTop, 0, 0, sliceLeft, sliceTop);
        // top right
        drawRegion(g, x + drawWidth - sliceRight, y, sliceRight, sliceTop,
                imgWidth - sliceRight, 0, imgWidth, sliceTop);
        // bottom left
        drawRegion(g, x, y + drawHeight - sliceBottom, sliceLeft, sliceBottom,
                0, imgHeight - sliceBottom, sliceLeft, imgHeight);
        // bottom rigth
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

        // the interpolations method for scaling is restored
        restoreInterpolation(g, previousHint);
    }
    //-------------------------------------------------------------


    /**
     * UTILITY METHOD to draw a region of the image by cropping the source image
     * using default g.drawImage() method.
     */
    //-------------------------------------------------------------
    private void drawRegion(Graphics2D g, int dx1, int dy1, int dWidth, int dHeight,
                            int sx1, int sy1, int sx2, int sy2) {
        if (dWidth <= 0 || dHeight <= 0 || sx2 <= sx1 || sy2 <= sy1) {
            return;
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
    //-------------------------------------------------------------

    /**
     * UTILITY METHOD to validate if the slice margins are valid
     */
    //-------------------------------------------------------------
    private void validateSlices() {
        if (sliceLeft < 0 || sliceRight < 0 || sliceTop < 0 || sliceBottom < 0) {
            throw new IllegalArgumentException("Slice margins must be non-negative");
        }
        if (sliceLeft + sliceRight > imgWidth || sliceTop + sliceBottom > imgHeight) {
            throw new IllegalArgumentException("Slice margins exceed image dimensions");
        }
    }
    //-------------------------------------------------------------

    /**
     * UTILITY METHOD to load an image from a file path
     */
    //-------------------------------------------------------------
    private static BufferedImage loadImage(String filePath) {
        Objects.requireNonNull(filePath, "filePath");
        try {
            return ImageIO.read(new File(filePath));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load image: " + filePath, e);
        }
    }
    //-------------------------------------------------------------

    /**
     * UTILITY METHOD to restore the scaling method
     */
    //-------------------------------------------------------------
    private void restoreInterpolation(Graphics2D g, Object previousHint) {
        if (previousHint != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previousHint);
        }
    }
    //-------------------------------------------------------------


    //GETTERS
    //-------------------------------------------------------------
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
    public Dimension getMinSize() {
        return new Dimension(getMinWidth(), getMinHeight());
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
//end class
