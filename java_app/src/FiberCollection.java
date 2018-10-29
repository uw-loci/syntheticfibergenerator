import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;


class FiberCollectionParams
{
    int nFibers;
    int meanLength;
    double segmentLength;
    double meanStraightness;
    double alignment;
    double meanAngle;
    int imageWidth;
    int imageHeight;
}


class FiberCollection implements Iterable<Fiber>
{
    private FiberCollectionParams params;
    private ArrayList<Fiber> fibers;


    FiberCollection(FiberCollectionParams params)
    {
        this.params = params;
        this.fibers = new ArrayList<>(this.params.nFibers);
    }

    void generate()
    {
        // TODO: May want to generate integer lengths instead of double lengths (currently just casting them)
        ArrayList<Double> lengths = RandomUtility.getRandomList(params.meanLength, 1.0, Double.POSITIVE_INFINITY, params.nFibers);
        ArrayList<Double> straightnesses = RandomUtility.getRandomList(params.meanStraightness, 0.0, 1.0, lengths);

        for (int i = 0; i < params.nFibers; i++)
        {
            FiberParams fiberParams = new FiberParams();
            fiberParams.length = (int) lengths.get(i).doubleValue();
            fiberParams.segmentLength = params.segmentLength;
            fiberParams.straightness = straightnesses.get(i);

            // TODO: Problem with this calculation here
            fiberParams.start = RandomUtility.getRandomPoint(0.0, params.imageWidth, 0.0, params.imageHeight);
            Vector2D direction = RandomUtility.getRandomDirection();
            double endDistance = fiberParams.length * fiberParams.segmentLength * fiberParams.straightness;
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
