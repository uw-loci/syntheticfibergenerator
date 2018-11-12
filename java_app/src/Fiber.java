import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


class FiberParams
{
    int length;
    double straightness;
    double startingWidth;
    double widthVariation;
    double segmentLength;
    Vector2D start;
    Vector2D end;
}


class Segment
{
    Vector2D start;
    Vector2D end;
    double width;


    Segment(Vector2D start, Vector2D end, double width)
    {
        this.start = start;
        this.end = end;
        this.width = width;
    }
}


class Fiber implements Iterable<Segment>
{
    class SegmentIterator implements Iterator<Segment>
    {
        private Iterator<Vector2D> start;
        private Iterator<Vector2D> end;
        private Iterator<Double> width;


        private SegmentIterator(ArrayList<Vector2D> points)
        {
            start = points.iterator();
            end = points.iterator();
            if (end.hasNext())
            {
                end.next();
            }
            width = widths.iterator();
        }


        public Segment next()
        {
            return hasNext() ? new Segment(start.next(), end.next(), width.next()) : null;
        }


        public boolean hasNext()
        {
            return end.hasNext();
        }
    }


    FiberParams params;
    private ArrayList<Vector2D> points;
    private ArrayList<Double> widths;


    Fiber(FiberParams params)
    {
        this.params = params;
        this.points = new ArrayList<>();
        this.widths = new ArrayList<>();
    }


    public Iterator<Segment> iterator()
    {
        return new SegmentIterator(points);
    }


    void generate()
    {
        points = RandomUtility.getRandomChain(params.start, params.end, params.length, params.segmentLength);

        double width = params.startingWidth;
        for (int i = 0; i < params.length; i++)
        {
            widths.add(width);
            double diff;
            do
            {
                diff = RandomUtility.RNG.nextGaussian() * params.widthVariation;
            }
            while (width + diff < 0.0);
            width += diff;
        }
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("\n{ \"points\" : [\n");
        for (Iterator<Vector2D> iter = points.iterator(); iter.hasNext(); )
        {
            Vector2D point = iter.next();
            builder.append(String.format("{ \"x\" : %.4f, ", point.getX()));
            builder.append(String.format("\"y\" : %.4f }", point.getY()));
            if (iter.hasNext())
            {
                builder.append(",\n");
            }
        }
        builder.append(" ],\n\"widths\" : [\n");
        for (Iterator<Double> iter = widths.iterator(); iter.hasNext(); )
        {
            builder.append(String.format("%.4f", iter.next()));
            if (iter.hasNext())
            {
                builder.append(",\n");
            }
        }
        builder.append(" ] }");
        return builder.toString();
    }
}