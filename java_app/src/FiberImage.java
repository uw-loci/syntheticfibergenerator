import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.Color;
import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


class FiberImageParams
{
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


class FiberImage implements Iterable<Fiber>
{
    private static final float COMPOSITE_ALPHA = 0.6F;
    private static final int FADE_STEPS = 3;

    // The approximate fraction of the image's width that the scale bar should occupy
    private static final double IDEAL_SCALE_FRAC = 0.2;

    // TODO: Express these in terms of the total image size
    // Visual properties of the scale bar
    private static final int CAP_SIZE = 5;
    private static final int LABEL_BUFF = 5;
    private static final int SCALE_BUFF = 20;

    private ProgramParams params;
    private ArrayList<Fiber> fibers;
    private BufferedImage image;


    FiberImage(ProgramParams params)
    {
        this.params = params;
        this.fibers = new ArrayList<>(this.params.nFibers);
        this.image = new BufferedImage(params.imageWidth, params.imageHeight, BufferedImage.TYPE_INT_ARGB);
    }


    private ArrayList<Vector2D> convertToDifferences(ArrayList<Vector2D> arrayList)
    {
        ArrayList<Vector2D> output = new ArrayList<>();
        for (int i = 0; i < arrayList.size() - 1; i++)
        {
            output.add(arrayList.get(i + 1).subtract(arrayList.get(i)));
        }
        return output;
    }


    private ArrayList<Vector2D> generateDirections()
    {
        Vector2D sum = new Vector2D(Math.cos(params.meanAngle * 2.0), Math.sin(params.meanAngle * 2.0));
        sum = sum.scalarMultiply(params.alignment * params.nFibers);
        ArrayList<Vector2D> directions = RandomUtility.getRandomChain(new Vector2D(0.0, 0.0), sum, params.nFibers, 1.0);
        directions = convertToDifferences(directions);

        ArrayList<Vector2D> output = new ArrayList<>();
        for (Vector2D direction : directions)
        {
            double fiberAngle = Math.atan2(direction.getY(), direction.getX()) / 2.0;
            output.add(new Vector2D(Math.cos(fiberAngle), Math.sin(fiberAngle)));
        }
        return output;
    }


    private Vector2D findFiberStart(double length, Vector2D direction)
    {
        double xDisp = direction.normalize().getX() * length;
        double yDisp = direction.normalize().getY() * length;

        // TODO: If not all of the fiber can be shown, at least show as much as possible (right now we just give up)
        if (Math.abs(xDisp) > params.imageWidth - 2 * params.edgeBuffer || Math.abs(yDisp) > params.imageHeight - 2 * params.edgeBuffer)
        {
            System.out.println("Warning: fiber will not fit in image frame");
            return RandomUtility.getRandomPoint(0.0, params.imageWidth, 0.0, params.imageHeight);
        }

        double xMin = Math.max(params.edgeBuffer, params.edgeBuffer - xDisp);
        double yMin = Math.max(params.edgeBuffer, params.edgeBuffer - yDisp);
        double xMax = Math.min(params.imageWidth - params.edgeBuffer, params.imageWidth - params.edgeBuffer - xDisp);
        double yMax = Math.min(params.imageHeight - params.edgeBuffer, params.imageHeight - params.edgeBuffer - yDisp);

        return RandomUtility.getRandomPoint(xMin, xMax, yMin, yMax);
    }


    void generateFibers()
    {
        ArrayList<Vector2D> directions = generateDirections();

        for (int i = 0; i < params.nFibers; i++)
        {
            FiberParams fiberParams = new FiberParams();

            // TODO: Come up with a better solution than casting the length
            fiberParams.length = (int) Math.round(params.length.sample());
            fiberParams.straightness = params.straightness.sample();
            fiberParams.startingWidth = params.width.sample();
            fiberParams.segmentLength = params.segmentLength;
            fiberParams.widthVariation = params.widthVariability;

            Vector2D direction = directions.get(i);
            double endDistance = fiberParams.length * fiberParams.segmentLength * fiberParams.straightness;
            fiberParams.start = findFiberStart(endDistance, direction);
            fiberParams.end = fiberParams.start.add(direction.scalarMultiply(endDistance));

            Fiber fiber = new Fiber(fiberParams);
            fiber.generate();
            fibers.add(fiber);
        }
    }


    private void drawLine(Graphics2D graphics, Segment segment, float alpha, int width)
    {
        graphics.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.setColor(new Color(1.0F, 1.0F, 1.0F, alpha));
        graphics.drawLine((int) segment.start.getX(), (int) segment.start.getY(), (int) segment.end.getX(), (int) segment.end.getY());
    }


    void splineSmooth()
    {
        for (Fiber fiber : fibers)
        {
            fiber.splineSmooth();
        }
    }


    void bubbleSmooth()
    {
        for (Fiber fiber : fibers)
        {
            fiber.bubbleSmooth();
        }
    }


    void swapSmooth()
    {
        for (Fiber fiber : fibers)
        {
            fiber.swapSmooth();
        }
    }


    BufferedImage getImage()
    {
        return this.image;
    }


    // TODO: Allow fibers to overlap with themselves (but adjacent segments can't overlap)
    void drawFibers()
    {

        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, COMPOSITE_ALPHA));

        for (Fiber fiber : fibers)
        {
            BufferedImage layerImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            Graphics2D layerGraphics = layerImage.createGraphics();
            layerGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));

            double slope = 1.0 / (FADE_STEPS + 1.0);
            for (int i = FADE_STEPS; i >= 0; i--)
            {
                float alpha = (float) (1.0 - i * slope);
                for (Segment segment : fiber)
                {
                    drawLine(layerGraphics, segment, alpha, (int) segment.width + i);
                }
            }

            graphics.drawImage(layerImage, 0, 0, image.getWidth(), image.getHeight(), null);
        }
    }


    void drawScaleBar()
    {
        // Determine the size in microns of the scale bar
        double targetSize = IDEAL_SCALE_FRAC * params.micronsPerPixel * image.getWidth();
        double floorPow = Math.floor(Math.log10(targetSize));
        double possibleSizes[] = {Math.pow(10, floorPow), 5 * Math.pow(10, floorPow), Math.pow(10, floorPow + 1)};
        double bestSize = possibleSizes[0];
        for (double size : possibleSizes)
        {
            if (Math.abs(targetSize - size) < Math.abs(targetSize - bestSize))
            {
                bestSize = size;
            }
        }

        // Format the scale label
        String label;
        if (Math.abs(Math.floor(Math.log10(bestSize))) <= 2)
        {
            label = new DecimalFormat("0.## \u00B5").format(bestSize);
        }
        else
        {
            label = String.format("%.1e \u00B5", bestSize);
        }

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int scaleHeight = image.getHeight() - SCALE_BUFF - CAP_SIZE;
        int scaleRight = SCALE_BUFF + (int) (bestSize / params.micronsPerPixel);

        // Draw the scale
        graphics.drawLine(SCALE_BUFF, scaleHeight, scaleRight, scaleHeight);
        graphics.drawLine(SCALE_BUFF, scaleHeight + CAP_SIZE, SCALE_BUFF, scaleHeight - CAP_SIZE);
        graphics.drawLine(scaleRight, scaleHeight + CAP_SIZE, scaleRight, scaleHeight - CAP_SIZE);

        // Draw the scale label
        graphics.drawString(label, SCALE_BUFF, scaleHeight - CAP_SIZE - LABEL_BUFF);
    }


    void downsample()
    {
        image = ImageUtility.scale(image, params.scaleRatio, AffineTransformOp.TYPE_BILINEAR);
    }


    void gaussianBlur()
    {
        image = ImageUtility.gaussianBlur(image, params.blurRadius);
    }


    public Iterator<Fiber> iterator()
    {
        return fibers.iterator();
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Iterator<Fiber> iter = fibers.iterator(); iter.hasNext(); )
        {
            builder.append(iter.next().toString());
            if (iter.hasNext())
            {
                builder.append(",");
            }
        }
        builder.append("\n]");
        return builder.toString();
    }


    void addNoise()
    {
        PoissonDistribution dist = new PoissonDistribution(params.meanNoise);

        // Sequence of poisson seeds depends on the initial RNG seed
        dist.reseedRandomGenerator(RandomUtility.RNG.nextInt());
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int noise = dist.sample();
                Color color = new Color(image.getRGB(x, y));
                float[] hsb = new float[3];
                Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), hsb);
                // TODO: Not finished...
            }
        }
    }
}
