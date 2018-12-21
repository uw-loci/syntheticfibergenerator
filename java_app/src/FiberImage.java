import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.Color;
import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;


class FiberImageParams {
    Distribution length;
    Distribution straightness;
    Distribution fiberWidth;
    double alignment;
    double angle;
    int nFibers;
    double segmentLength;
    int imageWidth;
    int imageHeight;
    int edgeBuffer;
    double widthVariation;
    double micronsPerPixel;
    double downSampleFactor;
    double blurRadius;
}


class FiberImage implements Iterable<Fiber> {
    private static final float COMPOSITE_ALPHA = 0.6F;
    private static final int FADE_STEPS = 3;

    // The approximate fraction of the image's width that the scale bar should occupy
    private static final double IDEAL_SCALE_FRAC = 0.2;

    // TODO: Express these in terms of the total image size
    // Visual properties of the scale bar
    private static final int CAP_SIZE = 5;
    private static final int LABEL_BUFF = 5;
    private static final int SCALE_BUFF = 20;

    private transient ProgramParams params;
    private transient BufferedImage image;
    private ArrayList<Fiber> fibers;


    FiberImage(ProgramParams params) {
        this.params = params;
        this.fibers = new ArrayList<>(this.params.nFibers);
        this.image = new BufferedImage(params.imageWidth, params.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
    }


    private ArrayList<Vector2D> convertToDifferences(ArrayList<Vector2D> arrayList) {
        ArrayList<Vector2D> output = new ArrayList<>();
        for (int i = 0; i < arrayList.size() - 1; i++) {
            output.add(arrayList.get(i + 1).subtract(arrayList.get(i)));
        }
        return output;
    }


    private ArrayList<Vector2D> generateDirections() {
        Vector2D sum = new Vector2D(Math.cos(Math.toRadians(-params.meanAngle) * 2.0), Math.sin(Math.toRadians(-params.meanAngle) * 2.0));
        sum = sum.scalarMultiply(params.alignment * params.nFibers);
        ArrayList<Vector2D> directions = RandomUtility.getRandomChain(new Vector2D(0.0, 0.0), sum, params.nFibers, 1.0);
        directions = convertToDifferences(directions);

        ArrayList<Vector2D> output = new ArrayList<>();
        for (Vector2D direction : directions) {
            double fiberAngle = Math.atan2(direction.getY(), direction.getX()) / 2.0;
            output.add(new Vector2D(Math.cos(fiberAngle), Math.sin(fiberAngle)));
        }
        return output;
    }


    private Vector2D findFiberStart(double length, Vector2D direction) {
        double xDisp = direction.normalize().getX() * length;
        double yDisp = direction.normalize().getY() * length;

        // TODO: If not all of the fiber can be shown, at least show as much as possible (right now we just give up)
        if (Math.abs(xDisp) > params.imageWidth - 2 * params.edgeBuffer || Math.abs(yDisp) > params.imageHeight - 2 * params.edgeBuffer) {
            System.out.println("Warning: fiber will not fit in image frame");
            return RandomUtility.getRandomPoint(0.0, params.imageWidth, 0.0, params.imageHeight);
        }

        double xMin = Math.max(params.edgeBuffer, params.edgeBuffer - xDisp);
        double yMin = Math.max(params.edgeBuffer, params.edgeBuffer - yDisp);
        double xMax = Math.min(params.imageWidth - params.edgeBuffer, params.imageWidth - params.edgeBuffer - xDisp);
        double yMax = Math.min(params.imageHeight - params.edgeBuffer, params.imageHeight - params.edgeBuffer - yDisp);

        return RandomUtility.getRandomPoint(xMin, xMax, yMin, yMax);
    }


    void generateFibers() {
        ArrayList<Vector2D> directions = generateDirections();

        for (int i = 0; i < params.nFibers; i++) {
            FiberParams fiberParams = new FiberParams();

            // TODO: Come up with a better solution than casting the nSegments
            fiberParams.nSegments = (int) Math.round(params.length.sample() / params.segmentLength);
            fiberParams.straightness = params.straightness.sample();
            fiberParams.startingWidth = params.width.sample();
            fiberParams.segmentLength = params.segmentLength;
            fiberParams.widthVariation = params.widthVariability;

            Vector2D direction = directions.get(i);
            double endDistance = fiberParams.nSegments * fiberParams.segmentLength * fiberParams.straightness;
            fiberParams.start = findFiberStart(endDistance, direction);
            fiberParams.end = fiberParams.start.add(direction.scalarMultiply(endDistance));

            Fiber fiber = new Fiber(fiberParams);
            fiber.generate();
            fibers.add(fiber);
        }
    }


    void splineSmooth() {
        for (Fiber fiber : fibers) {
            fiber.splineSmooth();
        }
    }


    void bubbleSmooth() {
        for (Fiber fiber : fibers) {
            fiber.bubbleSmooth();
        }
    }


    void swapSmooth() {
        for (Fiber fiber : fibers) {
            fiber.swapSmooth();
        }
    }


    BufferedImage getImage() {
        return this.image;
    }


    void drawFibers() {
        Graphics2D graphics = image.createGraphics();
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
        double micronsPerPixel = 1.0 / params.pixelsPerMicron;
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
        image = ImageUtility.scale(image, params.scaleRatio, AffineTransformOp.TYPE_BILINEAR);
    }


    void gaussianBlur() {
        image = ImageUtility.gaussianBlur(image, params.blurRadius);
    }


    public Iterator<Fiber> iterator() {
        return fibers.iterator();
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Iterator<Fiber> iter = fibers.iterator(); iter.hasNext(); ) {
            builder.append(iter.next().toString());
            if (iter.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("\n]");
        return builder.toString();
    }


    void addNoise() {
        // Sequence of poisson seeds depends on the initial RNG seed
        PoissonDistribution noise = new PoissonDistribution(params.meanNoise);
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
}
