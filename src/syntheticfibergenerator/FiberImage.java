package syntheticfibergenerator; // TODO: Cleaned up

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.awt.Color;
import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;


class FiberImage implements Iterable<Fiber> {

    static class Params {

        Param<Integer> nFibers = new Param<>();
        Param<Double> segmentLength = new Param<>();
        Param<Double> alignment = new Param<>();
        Param<Double> meanAngle = new Param<>();
        Param<Double> widthChange = new Param<>();
        Param<Integer> imageWidth = new Param<>();
        Param<Integer> imageHeight = new Param<>();
        Param<Integer> imageBuffer = new Param<>();

        Distribution length = new Uniform(0, Double.POSITIVE_INFINITY);
        Distribution width = new Uniform(0, Double.POSITIVE_INFINITY);
        Distribution straightness = new Uniform(0, 1);

        Optional<Double> scale = new Optional<>();
        Optional<Double> downSample = new Optional<>();
        Optional<Double> blur = new Optional<>();
        Optional<Double> noise = new Optional<>();
        Optional<Double> distance = new Optional<>();
        Optional<Integer> bubble = new Optional<>();
        Optional<Integer> swap = new Optional<>();
        Optional<Integer> spline = new Optional<>();

        void setNames() {
            nFibers.setName("number of fibers");
            segmentLength.setName("segment length");
            alignment.setName("alignment");
            meanAngle.setName("mean angle");
            widthChange.setName("width change");
            imageWidth.setName("image width");
            imageHeight.setName("image height");
            imageBuffer.setName("edge buffer");

            length.setNames();
            straightness.setNames();
            width.setNames();

            scale.setName("scale");
            downSample.setName("down sample");
            blur.setName("blur");
            noise.setName("noise");
            distance.setName("distance");
            bubble.setName("bubble");
            swap.setName("swap");
            spline.setName("spline");
        }
    }


    private ArrayList<Fiber> fibers;
    private transient Params params;
    private transient BufferedImage image;

    // Visual properties of the scale bar
    private static final double TARGET_SCALE_SIZE = 0.2;
    private static final double CAP_RATIO = 0.01;
    private static final double BUFF_RATIO = 0.015;


    FiberImage(ImageCollection.Params params) {
        this.params = params;
        this.fibers = new ArrayList<>(this.params.nFibers.getValue());
        this.image = new BufferedImage(params.imageWidth.getValue(), params.imageHeight.getValue(), BufferedImage.TYPE_BYTE_GRAY);
    }

    @Override
    public Iterator<Fiber> iterator() {
        return fibers.iterator();
    }

    void generateFibers() throws ArithmeticException {
        ArrayList<Vector> directions = generateDirections();

        for (Vector direction : directions) {
            Fiber.Params fiberParams = new Fiber.Params();

            fiberParams.segmentLength = params.segmentLength.getValue();
            fiberParams.widthChange = params.widthChange.getValue();

            fiberParams.nSegments = (int) Math.round(params.length.sample() / params.segmentLength.getValue());
            fiberParams.straightness = params.straightness.sample();
            fiberParams.startWidth = params.width.sample();

            double endDistance = fiberParams.nSegments * fiberParams.segmentLength * fiberParams.straightness;
            fiberParams.start = findFiberStart(endDistance, direction);
            fiberParams.end = fiberParams.start.add(direction.scalarMultiply(endDistance));

            Fiber fiber = new Fiber(fiberParams);
            fiber.generate();
            fibers.add(fiber);
        }
    }

    void smooth() {
        for (Fiber fiber : fibers) {
            if (params.bubble.use) {
                fiber.bubbleSmooth(params.bubble.getValue());
            }
            if (params.swap.use) {
                fiber.swapSmooth(params.swap.getValue());
            }
            if (params.spline.use) {
                fiber.splineSmooth(params.spline.getValue());
            }
        }
    }

    void drawFibers() {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setColor(Color.WHITE);
        for (Fiber fiber : fibers) {
            for (Fiber.Segment segment : fiber) {
                graphics.setStroke(
                        new BasicStroke((float) segment.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.drawLine(
                        (int) segment.start.getX(), (int) segment.start.getY(),
                        (int) segment.end.getX(), (int) segment.end.getY());
            }
        }
    }

    void applyEffects() {
        if (params.distance.use) {
            image = ImageUtility.distanceFunction(image, params.distance.getValue());
        }
        if (params.noise.use) {
            addNoise();
        }
        if (params.blur.use) {
            image = ImageUtility.gaussianBlur(image, params.blur.getValue());
        }
        if (params.scale.use) {
            drawScaleBar();
        }
        if (params.downSample.use) {
            image = ImageUtility.scale(image, params.scale.getValue(), AffineTransformOp.TYPE_BILINEAR);
        }
    }

    BufferedImage getImage() {
        return this.image;
    }

    private ArrayList<Vector> generateDirections() {
        double sumAngle = Math.toRadians(-params.meanAngle.getValue());
        Vector sumDirection = new Vector(Math.cos(sumAngle * 2.0), Math.sin(sumAngle * 2.0));
        Vector sum = sumDirection.scalarMultiply(params.alignment.getValue() * params.nFibers.getValue());

        ArrayList<Vector> chain = RandomUtility.getRandomChain(new Vector(), sum, params.nFibers.getValue(), 1.0);
        ArrayList<Vector> directions = Utility.toDeltas(chain);

        ArrayList<Vector> output = new ArrayList<>();
        for (Vector direction : directions) {
            double angle = direction.theta() / 2.0;
            output.add(new Vector(Math.cos(angle), Math.sin(angle)));
        }
        return output;
    }

    private Vector findFiberStart(double length, Vector direction) {
        double xLength = direction.normalize().getX() * length;
        double yLength = direction.normalize().getY() * length;
        double x = findStart(xLength, params.imageWidth.getValue(), params.imageBuffer.getValue());
        double y = findStart(yLength, params.imageHeight.getValue(), params.imageBuffer.getValue());
        return new Vector(x, y);
    }

    private void drawScaleBar() {

        // Determine the size in microns of the scale bar
        double targetSize = TARGET_SCALE_SIZE * image.getWidth() / params.scale.getValue();
        double floorPow = Math.floor(Math.log10(targetSize));
        double[] options = {Math.pow(10, floorPow), 5 * Math.pow(10, floorPow), Math.pow(10, floorPow + 1)};
        double bestSize = options[0];
        for (double size : options) {
            if (Math.abs(targetSize - size) < Math.abs(targetSize - bestSize)) {
                bestSize = size;
            }
        }

        // Format the scale label
        String label;
        if (Math.abs(Math.floor(Math.log10(bestSize))) <= 2) {
            label = new DecimalFormat("0.## \u00B5").format(bestSize);
        } else {
            label = String.format("%.1e \u00B5", bestSize);
        }

        // Determine pixel dimensions of the scale bar
        int capSize = (int) (CAP_RATIO * image.getHeight());
        int xBuff = (int) (BUFF_RATIO * image.getWidth());
        int yBuff = (int) (BUFF_RATIO * image.getHeight());
        int scaleHeight = image.getHeight() - yBuff - capSize;
        int scaleRight = xBuff + (int) (bestSize * params.scale.getValue());

        // Draw the scale bar and label
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.drawLine(xBuff, scaleHeight, scaleRight, scaleHeight);
        graphics.drawLine(xBuff, scaleHeight + capSize, xBuff, scaleHeight - capSize);
        graphics.drawLine(scaleRight, scaleHeight + capSize, scaleRight, scaleHeight - capSize);
        graphics.drawString(label, xBuff, scaleHeight - capSize - yBuff);
    }

    private void addNoise() {

        // Sequence of poisson seeds depends on the initial RNG seed
        PoissonDistribution noise = new PoissonDistribution(params.noise.getValue());
        noise.reseedRandomGenerator(RandomUtility.RNG.nextInt());

        WritableRaster raster = image.getRaster();
        int[] pixel = new int[1];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                raster.getPixel(x, y, pixel);
                pixel[0] = Math.min(0xFF, pixel[0] + noise.sample());
                raster.setPixel(x, y, pixel);
            }
        }
    }

    private static double findStart(double length, int dimension, int buffer) {
        if (length > dimension) {
            return RandomUtility.getRandomDouble(dimension - length, 0);
        }
        if (length > dimension - buffer) {
            buffer = 0;
        }
        double min = Math.max(buffer, buffer - length);
        double max = Math.min(dimension - buffer, dimension - buffer - length);
        return RandomUtility.getRandomDouble(min, max);
    }
}
