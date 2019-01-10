package syntheticfibergenerator;

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

        void setHints() {
            nFibers.setHint("The number of fibers per image to generate");
            segmentLength.setHint("The length in pixels of fiber segments");
            alignment.setHint("A value between 0 and 1 indicating how close fibers are to the mean angle on average");
            meanAngle.setHint("The average fiber angle in degrees");
            widthChange.setHint("The maximum segment-to-segment width change of a fiber (in pixels)");
            imageWidth.setHint("The width of the saved image in pixels");
            imageHeight.setHint("The height of the saved image in pixels");
            imageBuffer.setHint("The size of the empty border around the edge of the image");

            length.setHints();
            straightness.setHints();
            width.setHints();

            scale.setHint("Check to draw a scale bar on the image; value is the number of pixels per micron");
            downSample.setHint("Check to enable down sampling; value is the ratio of final size to original size");
            blur.setHint("Check to enable Gaussian blurring; value is the radius of the blur");
            noise.setHint("Check to add Poisson noise; value is the Poisson mean on a scale of 0-255");
            distance.setHint("Check to apply a distance filter; value controls the sharpness of the intensity falloff");
            bubble.setHint("Check to apply \"bubble smoothing\"; value is the number of passes");
            swap.setHint("Check to apply \"swap smoothing\"; number of swaps is this value times number of segments");
            spline.setHint("Check to enable spline smoothing; value is the number of interpolated points per segment");
        }
    }


    private ArrayList<Fiber> fibers;
    private transient Params params;
    private transient BufferedImage image;

    // Visual properties of the scale bar
    private static final double TARGET_SCALE_SIZE = 0.2;
    private static final double CAP_RATIO = 0.01;
    private static final double BUFF_RATIO = 0.015;


    FiberImage(Params params) {
        this.params = params;
        this.fibers = new ArrayList<>(this.params.nFibers.value());
        this.image = new BufferedImage(
                params.imageWidth.value(), params.imageHeight.value(), BufferedImage.TYPE_BYTE_GRAY);
    }

    @Override
    public Iterator<Fiber> iterator() {
        return fibers.iterator();
    }

    void generateFibers() throws ArithmeticException {
        ArrayList<Vector> directions = generateDirections();

        for (Vector direction : directions) {
            Fiber.Params fiberParams = new Fiber.Params();

            fiberParams.segmentLength = params.segmentLength.value();
            fiberParams.widthChange = params.widthChange.value();

            fiberParams.nSegments = (int) Math.round(params.length.sample() / params.segmentLength.value());
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
                fiber.bubbleSmooth(params.bubble.value());
            }
            if (params.swap.use) {
                fiber.swapSmooth(params.swap.value());
            }
            if (params.spline.use) {
                fiber.splineSmooth(params.spline.value());
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
            image = ImageUtility.distanceFunction(image, params.distance.value());
        }
        if (params.noise.use) {
            addNoise();
        }
        if (params.blur.use) {
            image = ImageUtility.gaussianBlur(image, params.blur.value());
        }
        if (params.scale.use) {
            drawScaleBar();
        }
        if (params.downSample.use) {
            image = ImageUtility.scale(image, params.downSample.value(), AffineTransformOp.TYPE_BILINEAR);
        }
    }

    BufferedImage getImage() {
        return this.image;
    }

    private ArrayList<Vector> generateDirections() {
        double sumAngle = Math.toRadians(-params.meanAngle.value());
        Vector sumDirection = new Vector(Math.cos(sumAngle * 2.0), Math.sin(sumAngle * 2.0));
        Vector sum = sumDirection.scalarMultiply(params.alignment.value() * params.nFibers.value());

        ArrayList<Vector> chain = RngUtility.randomChain(new Vector(), sum, params.nFibers.value(), 1.0);
        ArrayList<Vector> directions = MiscUtility.toDeltas(chain);

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
        double x = findStart(xLength, params.imageWidth.value(), params.imageBuffer.value());
        double y = findStart(yLength, params.imageHeight.value(), params.imageBuffer.value());
        return new Vector(x, y);
    }

    private void drawScaleBar() {

        // Determine the size in microns of the scale bar
        double targetSize = TARGET_SCALE_SIZE * image.getWidth() / params.scale.value();
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
        int scaleRight = xBuff + (int) (bestSize * params.scale.value());

        // Draw the scale bar and label
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.drawLine(xBuff, scaleHeight, scaleRight, scaleHeight);
        graphics.drawLine(xBuff, scaleHeight + capSize, xBuff, scaleHeight - capSize);
        graphics.drawLine(scaleRight, scaleHeight + capSize, scaleRight, scaleHeight - capSize);
        graphics.drawString(label, xBuff, scaleHeight - capSize - yBuff);
    }

    private void addNoise() {

        // Sequence of poisson seeds depends on the initial rng seed
        PoissonDistribution noise = new PoissonDistribution(params.noise.value());
        noise.reseedRandomGenerator(RngUtility.rng.nextInt());

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
        double min, max;
        buffer = (int) Math.max(length / 2, buffer);
        if (Math.abs(length) > dimension) {
            min = Math.min(dimension - length, dimension);
            max = Math.max(0, -length);
            return RngUtility.randomDouble(min, max);
        }
        if (Math.abs(length) > dimension - 2 * buffer) {
            buffer = 0;
        }
        min = Math.max(buffer, buffer - length);
        max = Math.min(dimension - buffer - length, dimension - buffer);
        return RngUtility.randomDouble(min, max);
    }
}