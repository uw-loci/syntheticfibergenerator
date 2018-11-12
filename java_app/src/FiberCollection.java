import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;


class FiberCollectionParams
{
    int nFibers;
    int meanLength;
    int minLength;
    int maxLength;
    double segmentLength;
    double meanStraightness;
    double minStraightness;
    double maxStraightness;
    double alignment;
    double meanAngle;
    int imageWidth;
    int imageHeight;
    int edgeBuffer;
    double meanWidth;
    double minWidth;
    double maxWidth;
    double widthVariation;
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
            double angle = Math.atan2(direction.getY(), direction.getX()) / 2.0;
            output.add(new Vector2D(Math.cos(angle), Math.sin(angle)));
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


    void generate()
    {
        ArrayList<Double> lengths = RandomUtility.getRandomList(params.meanLength, params.minLength, params.maxLength, params.nFibers);
        ArrayList<Double> straightnesses = RandomUtility.getRandomList(params.meanStraightness, params.minStraightness, params.maxStraightness, params.nFibers);
        ArrayList<Double> startingWidths = RandomUtility.getRandomList(params.meanWidth, params.minWidth, params.maxWidth, params.nFibers);
        ArrayList<Vector2D> directions = generateDirections();

        for (int i = 0; i < params.nFibers; i++)
        {
            FiberParams fiberParams = new FiberParams();

            // TODO: Come up with a better solution than casting the length
            fiberParams.length = (int) lengths.get(i).doubleValue();
            fiberParams.segmentLength = params.segmentLength;
            fiberParams.straightness = straightnesses.get(i);
            fiberParams.startingWidth = startingWidths.get(i);
            fiberParams.widthVariation = params.widthVariation;

            Vector2D direction = directions.get(i);
            double endDistance = fiberParams.length * fiberParams.segmentLength * fiberParams.straightness;
            fiberParams.start = findFiberStart(endDistance, direction);
            fiberParams.end = fiberParams.start.add(direction.scalarMultiply(endDistance));

            Fiber fiber = new Fiber(fiberParams);
            fiber.generate();
            fibers.add(fiber);
        }
    }


    BufferedImage drawFibers()
    {
        BufferedImage image = new BufferedImage(params.imageWidth, params.imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.DST_OVER, (float) 0.7);
        graphics.setComposite(composite);
        for (Fiber fiber : fibers)
        {
            BufferedImage fiberImage = new BufferedImage(params.imageWidth, params.imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D fiberGraphics = fiberImage.createGraphics();
            for (Segment segment : fiber)
            {
                fiberGraphics.setStroke(new BasicStroke((int) segment.width));
                fiberGraphics.drawLine((int) segment.start.getX(), (int) segment.start.getY(), (int) segment.end.getX(), (int) segment.end.getY());
            }
            graphics.drawImage(fiberImage, 0, 0, image.getWidth(), image.getHeight(), null);
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
}
