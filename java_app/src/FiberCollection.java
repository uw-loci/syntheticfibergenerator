import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;


class FiberCollectionParams
{
    int nFibers;
    int meanLength;
    double segmentLength;
    double meanStraightness;
    int imageWidth;
    int imageHeight;
}


class FiberCollection implements Iterable<Fiber>, Serializable
{
    private FiberCollectionParams params;
    // TODO: Do we need to use a LinkedList here?
    private LinkedList<Fiber> fibers;


    FiberCollection(FiberCollectionParams params)
    {
        this.params = params;
        this.fibers = new LinkedList<>();
    }

    void generate()
    {
        // TODO: We may want to generate integer lengths instead of double lengths (currently we're just casting them)
        ArrayList<Double> evenWeights = new ArrayList<>(Collections.nCopies(params.nFibers, 1.0));
        ArrayList<Double> lengths = RandomUtility.getRandomList(params.meanLength, 1.0, Double.POSITIVE_INFINITY, evenWeights);
        ArrayList<Double> straightnesses = RandomUtility.getRandomList(params.meanStraightness, 0.0, 1.0, lengths);

        for (int i = 0; i < params.nFibers; i++)
        {
            FiberParams fiberParams = new FiberParams();
            fiberParams.length = (int) lengths.get(i).doubleValue();
            fiberParams.segmentLength = params.segmentLength;
            fiberParams.straightness = straightnesses.get(i);
            double endDistance = lengths.get(i) * straightnesses.get(i);

            fiberParams.start = RandomUtility.getRandomPoint(0.0, params.imageWidth, 0.0, params.imageHeight);
            Vector2D direction = RandomUtility.getRandomDirection();
            double xEnd = fiberParams.start.getX() + direction.getX() * endDistance;
            double yEnd = fiberParams.start.getY() + direction.getY() * endDistance;
            fiberParams.end = new Vector2D(xEnd, yEnd);

            Fiber fiber = new Fiber(fiberParams);
            fiber.generate();
            fibers.add(fiber);
        }
    }

    BufferedImage drawFibers()
    {
        BufferedImage image = new BufferedImage(params.imageWidth, params.imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        for (Fiber fiber : fibers)
        {
            for (Segment segment : fiber)
            {
                graphics.drawLine((int) segment.start.getX(), (int) segment.start.getY(), (int) segment.end.getX(), (int) segment.end.getY());
            }
        }
        return image;
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
        for (Iterator<Fiber> iter = fibers.iterator(); iter.hasNext();)
        {
            builder.append(iter.next().toString());
            if (iter.hasNext())
            {
                builder.append(",");
            }
        }
        builder.append(String.format("%n]"));
        return builder.toString();
    }
}
