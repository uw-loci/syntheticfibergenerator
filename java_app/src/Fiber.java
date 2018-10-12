import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Iterator;

class FiberParams
{
    double length;
    Point2D.Double start, end;
}

class Segment
{
    Point2D.Double start;
    Point2D.Double end;
    int width = 5;

    Segment(Point2D.Double start, Point2D.Double end)
    {
        this.start = start;
        this.end = end;
    }
}

class Fiber implements Iterable<Segment>
{
    class SegmentIterator implements Iterator<Segment>
    {
        private Iterator<Point2D.Double> start;
        private Iterator<Point2D.Double> end;

        private SegmentIterator(LinkedList<Point2D.Double> points)
        {
            start = points.iterator();
            end = points.iterator();
            if (end.hasNext())
            {
                end.next();
            }
        }

        public Segment next()
        {
            if (hasNext())
            {
                return new Segment(start.next(), end.next());
            }
            else
            {
                return null;
            }
        }

        public boolean hasNext()
        {
            return end.hasNext();
        }
    }

    private FiberParams fiberParams;
    private LinkedList<Point2D.Double> points;

    Fiber(FiberParams fiberParams)
    {
        this.fiberParams = fiberParams;
        this.points = new LinkedList<>();
    }

    public Iterator<Segment> iterator()
    {
        return new SegmentIterator(points);
    }

    void generate()
    {
        points.add(fiberParams.start);
        points.add(fiberParams.end);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%n{ \"points\" : [%n"));
        for (Iterator<Point2D.Double> iter = points.iterator(); iter.hasNext();)
        {
            Point2D.Double point = iter.next();
            builder.append(String.format("{ \"x\" : %.4f, ", point.x));
            builder.append(String.format("\"y\" : %.4f }", point.y));
            if (iter.hasNext())
            {
                builder.append(String.format(",%n"));
            }
        }
        builder.append(String.format("%n] }"));
        return builder.toString();
    }
}