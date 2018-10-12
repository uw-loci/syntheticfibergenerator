import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

class ImageParams
{
    int nFibers;
    double minLength, maxLength;
    double meanLength;
    int imageWidth, imageHeight;
}

class AllFibers implements Iterable<Fiber>, Serializable
{
    private ImageParams imageParams;
    private LinkedList<Fiber> allFibers;

    AllFibers(ImageParams imageParams)
    {
        this.imageParams = imageParams;
        this.allFibers = new LinkedList<>();
    }

    public Iterator<Fiber> iterator()
    {
        return allFibers.iterator();
    }

    void generate()
    {
        Double currentMeanLength = 0.0;
        for (int i = 0; i < imageParams.nFibers; i++)
        {
            FiberParams fiberParams = new FiberParams();
            fiberParams.length = Utility.getValidRandom(currentMeanLength, imageParams.meanLength,
                    imageParams.minLength, imageParams.maxLength, i, imageParams.nFibers);
            fiberParams.start = Utility.getRandomPoint(0.0, imageParams.imageWidth, 0.0,
                    imageParams.imageHeight);
            Point2D.Double direction = Utility.getRandomDirection();
            double xEnd = fiberParams.start.x + direction.x * fiberParams.length;
            double yEnd = fiberParams.start.y + direction.y * fiberParams.length;
            fiberParams.end = new Point2D.Double(xEnd, yEnd);

            Fiber fiber = new Fiber(fiberParams);
            fiber.generate();
            allFibers.add(fiber);
        }
    }

    public BufferedImage drawFibers()
    {
        BufferedImage image = new BufferedImage(imageParams.imageWidth, imageParams.imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        for (Fiber fiber : allFibers)
        {
            for (Segment segment : fiber)
            {
                graphics.drawLine((int) segment.start.x, (int) segment.start.y,
                        (int) segment.end.x, (int) segment.end.y);
            }
        }
        return image;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Iterator<Fiber> iter = allFibers.iterator(); iter.hasNext();)
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
