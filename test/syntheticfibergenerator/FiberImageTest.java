package syntheticfibergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;


class FiberImageTest
{
    private FiberImage.Params params;
    private FiberImage image;


    @BeforeEach
    void setUp() {
        params = new FiberImage.Params();
        try {
            params.nFibers.parse("10", Integer::parseInt);
            params.segmentLength.parse("5.0", Double::parseDouble);
            params.alignment.parse("0.8", Double::parseDouble);
            params.meanAngle.parse("70.0", Double::parseDouble);
            params.widthChange.parse("0.5", Double::parseDouble);
            params.imageWidth.parse("256", Integer::parseInt);
            params.imageHeight.parse("256", Integer::parseInt);
            params.imageBuffer.parse("16", Integer::parseInt);
            Uniform length = new Uniform(params.length.lowerBound, params.length.upperBound);
            length.min.parse("20.0", Double::parseDouble);
            length.max.parse("100.0", Double::parseDouble);
            params.length = length;
            Uniform width = new Uniform(params.width.lowerBound, params.width.upperBound);
            width.min.parse("1.0", Double::parseDouble);
            width.max.parse("5.0", Double::parseDouble);
            params.width = width;
            Uniform straightness = new Uniform(params.straightness.lowerBound, params.straightness.upperBound);
            straightness.min.parse("0.7", Double::parseDouble);
            straightness.max.parse("1.0", Double::parseDouble);
            params.straightness = straightness;
            params.downSample.use = true;
            params.downSample.parse("0.625", Double::parseDouble);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        params.setNames();
        params.setHints();
        image = new FiberImage(params);
    }

    @Test
    void testAlignment() {
        image.generateFibers();
        assertEquals(params.alignment.value(), alignment(image), 1e-6);
    }

    @Test
    void testMeanAngle() {
        image.generateFibers();
        assertEquals(params.meanAngle.value(), meanAngle(image), 1e-6);
    }

    @Test
    void testImageProperties() {
        image.generateFibers();
        image.drawFibers();
        assertEquals((int) params.imageWidth.value(), image.getImage().getWidth());
        assertEquals((int) params.imageHeight.value(), image.getImage().getHeight());
    }

    @Test
    void testDownSample() {
        image.generateFibers();
        image.drawFibers();
        image.applyEffects();
        BufferedImage buff = image.getImage();
        assertEquals(params.imageWidth.value() * params.downSample.value(), buff.getWidth());
        assertEquals(params.imageHeight.value() * params.downSample.value(), image.getImage().getHeight());
    }

    private double alignment(FiberImage image) {
        return complexMean(image).getNorm();
    }

    private double meanAngle(FiberImage image) {
        return -complexMean(image).theta() * 90 / Math.PI;
    }

    private Vector complexMean(FiberImage image) {
        Vector sum = new Vector();
        for (Fiber fiber : image) {
            double theta = fiber.getDirection().theta();
            Vector direction = new Vector(Math.cos(2.0 * theta), Math.sin(2.0 * theta));
            sum = sum.add(direction);
        }
        return sum.scalarMultiply(1.0 / params.nFibers.value());
    }
}