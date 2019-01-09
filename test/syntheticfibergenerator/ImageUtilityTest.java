package syntheticfibergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;


class ImageUtilityTest {

    private BufferedImage image;


    /**
     * Creates a non-empty image by drawing some text and shapes.
     */
    @BeforeEach
    void setUp() {
        image = new BufferedImage(512, 640, BufferedImage.TYPE_BYTE_GRAY);
        Graphics graphics = image.getGraphics();
        graphics.drawString("Hello, world!", 100, 100);
        graphics.drawOval(300, 300, 12, 6);
        graphics.drawLine(10, 450, 500, 10);
    }

    @Test
    void testDistanceFunction() {
        assertTrue(sizeTypeMatch(ImageUtility.distanceFunction(image, 64), image));
    }

    @Test
    void testHighFalloff() {
        assertTrue(pixelWiseEqual(ImageUtility.distanceFunction(image, 4096), image));
    }

    @Test
    void testInvalidImage() {
        BufferedImage badImage = new BufferedImage(512, 640, BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalArgumentException.class, () ->
            ImageUtility.distanceFunction(badImage, 64));
    }

    @Test
    void testGaussianBlur() {
        assertTrue(sizeTypeMatch(ImageUtility.gaussianBlur(image, 8), image));
    }

    @Test
    void testScale() {
        double scale = 0.01;
        BufferedImage output = ImageUtility.scale(image, scale, AffineTransformOp.TYPE_BILINEAR);
        assertEquals((double) output.getWidth(), image.getWidth() * scale, 1.0);
        assertEquals((double) output.getHeight(), image.getHeight() * scale, 1.0);
        assertEquals(output.getType(), image.getType());
    }

    private boolean sizeTypeMatch(BufferedImage image1, BufferedImage image2) {
        return
            image1.getWidth() == image2.getWidth() &&
            image1.getHeight() == image2.getHeight() &&
            image1.getType() == image2.getType();
    }

    private boolean pixelWiseEqual(BufferedImage image1, BufferedImage image2) {
        if (!sizeTypeMatch(image1, image2)) {
            return false;
        }
        for (int y = 0; y < image1.getHeight(); y++) {
            for (int x = 0; x < image1.getWidth(); x++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
