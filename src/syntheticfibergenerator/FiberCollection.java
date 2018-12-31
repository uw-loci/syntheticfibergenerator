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


class FiberCollection implements Iterable<Fiber> {

    // The approximate fraction of the image's width that the scale bar should occupy
    private static final double IDEAL_SCALE_FRAC = 0.2;

    // TODO: Express these in terms of the total image size
    // Visual properties of the scale bar
    private static final int CAP_SIZE = 5;
    private static final int LABEL_BUFF = 5;
    private static final int SCALE_BUFF = 20;

    private transient ImageCollection.Params params;
    private transient BufferedImage image;
    private ArrayList<Fiber> fibers;


    FiberCollection(ImageCollection.Params params) {
        this.params = params;
        this.fibers = new ArrayList<>(this.params.nFibers.getValue());
        this.image = new BufferedImage(params.imageWidth.getValue(), params.imageHeight.getValue(), BufferedImage.TYPE_BYTE_GRAY);
    }


    private ArrayList<Vector> convertToDifferences(ArrayList<Vector> arrayList) {
        ArrayList<Vector> output = new ArrayList<>();
        for (int i = 0; i < arrayList.size() - 1; i++) {
            output.add(arrayList.get(i + 1).subtract(arrayList.get(i)));
        }
        return output;
    }


    private ArrayList<Vector> generateDirections() {
        Vector sum = new Vector(Math.cos(Math.toRadians(-params.meanAngle.getValue()) * 2.0), Math.sin(Math.toRadians(-params.meanAngle.getValue()) * 2.0));
        sum = sum.scalarMultiply(params.alignment.getValue() * params.nFibers.getValue());
        ArrayList<Vector> directions = RandomUtility.getRandomChain(new Vector(0.0, 0.0), sum, params.nFibers.getValue(), 1.0);
        directions = convertToDifferences(directions);

        ArrayList<Vector> output = new ArrayList<>();
        for (Vector direction : directions) {
            double fiberAngle = Math.atan2(direction.getY(), direction.getX()) / 2.0;
            output.add(new Vector(Math.cos(fiberAngle), Math.sin(fiberAngle)));
        }
        return output;
    }


    private Vector findFiberStart(double length, Vector direction) {
        double xDisp = direction.normalize().getX() * length;
        double yDisp = direction.normalize().getY() * length;

        // TODO: If not all of the fiber can be shown, at least show as much as possible (right now we just give up)
        if (Math.abs(xDisp) > params.imageWidth.getValue() - 2 * params.imageBuffer.getValue() || Math.abs(yDisp) > params.imageHeight.getValue() - 2 * params.imageBuffer.getValue()) {
            System.out.println("Warning: fiber will not fit in image frame");
            return RandomUtility.getRandomPoint(0.0, params.imageWidth.getValue(), 0.0, params.imageHeight.getValue());
        }

        double xMin = Math.max(params.imageBuffer.getValue(), params.imageBuffer.getValue() - xDisp);
        double yMin = Math.max(params.imageBuffer.getValue(), params.imageBuffer.getValue() - yDisp);
        double xMax = Math.min(params.imageWidth.getValue() - params.imageBuffer.getValue(), params.imageWidth.getValue() - params.imageBuffer.getValue() - xDisp);
        double yMax = Math.min(params.imageHeight.getValue() - params.imageBuffer.getValue(), params.imageHeight.getValue() - params.imageBuffer.getValue() - yDisp);

        return RandomUtility.getRandomPoint(xMin, xMax, yMin, yMax);
    }


    void generateFibers() {
        ArrayList<Vector> directions = generateDirections();

        for (int i = 0; i < params.nFibers.getValue(); i++) {
            FiberParams fiberParams = new FiberParams();

            // TODO: Come up with a better solution than casting the nSegments
            fiberParams.nSegments = (int) Math.round(params.length.sample() / params.segmentLength.getValue());
            fiberParams.straightness = params.straightness.sample();
            fiberParams.startingWidth = params.width.sample();
            fiberParams.segmentLength = params.segmentLength.getValue();
            fiberParams.widthVariation = params.widthChange.getValue();

            Vector direction = directions.get(i);
            double endDistance = fiberParams.nSegments * fiberParams.segmentLength * fiberParams.straightness;
            fiberParams.start = findFiberStart(endDistance, direction);
            fiberParams.end = fiberParams.start.add(direction.scalarMultiply(endDistance));

            Fiber fiber = new Fiber(fiberParams);
            fiber.generate();
            fibers.add(fiber);
        }
    }


    void bubbleSmooth() {
        for (Fiber fiber : fibers) {
            fiber.bubbleSmooth(params.bubble.getValue());
        }
    }


    void swapSmooth() {
        for (Fiber fiber : fibers) {
            fiber.swapSmooth(params.swap.getValue());
        }
    }


    void splineSmooth() {
        for (Fiber fiber : fibers) {
            fiber.splineSmooth(params.spline.getValue());
        }
    }


    BufferedImage getImage() {
        return this.image;
    }


    void drawFibers() {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setColor(new Color(255, 255, 255));
        for (Fiber fiber : fibers) {
            for (Segment segment : fiber) {
                graphics.setStroke(new BasicStroke((float) segment.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.drawLine((int) segment.start.getX(), (int) segment.start.getY(), (int) segment.end.getX(), (int) segment.end.getY());
            }
        }
    }


    void drawScaleBar() {
        // Determine the size in microns of the scale bar
        double micronsPerPixel = 1.0 / params.scale.getValue();
        double targetSize = IDEAL_SCALE_FRAC * micronsPerPixel * image.getWidth();
        double floorPow = Math.floor(Math.log10(targetSize));
        double possibleSizes[] = {Math.pow(10, floorPow), 5 * Math.pow(10, floorPow), Math.pow(10, floorPow + 1)};
        double bestSize = possibleSizes[0];
        for (double size : possibleSizes) {
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

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int scaleHeight = image.getHeight() - SCALE_BUFF - CAP_SIZE;
        int scaleRight = SCALE_BUFF + (int) (bestSize / micronsPerPixel);

        // Draw the scale
        graphics.drawLine(SCALE_BUFF, scaleHeight, scaleRight, scaleHeight);
        graphics.drawLine(SCALE_BUFF, scaleHeight + CAP_SIZE, SCALE_BUFF, scaleHeight - CAP_SIZE);
        graphics.drawLine(scaleRight, scaleHeight + CAP_SIZE, scaleRight, scaleHeight - CAP_SIZE);

        // Draw the scale label
        graphics.drawString(label, SCALE_BUFF, scaleHeight - CAP_SIZE - LABEL_BUFF);
    }


    void downsample() {
        image = ImageUtility.scale(image, params.scale.getValue(), AffineTransformOp.TYPE_BILINEAR);
    }


    void gaussianBlur() {
        image = ImageUtility.gaussianBlur(image, params.blur.getValue());
    }


    public Iterator<Fiber> iterator() {
        return fibers.iterator();
    }


    void addNoise() {
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

    void distanceFunction() {
        image = ImageUtility.distanceFunction(image, params.distance.getValue());
    }
}
