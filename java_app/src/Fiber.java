import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


class FiberParams
{
    int length;
    double straightness;
    double segmentLength;
    Vector2D start;
    Vector2D end;
}


class Segment
{
    Vector2D start;
    Vector2D end;

    Segment(Vector2D start, Vector2D end)
    {
        this.start = start;
        this.end = end;
    }
}


class Fiber implements Iterable<Segment>
{
    class SegmentIterator implements Iterator<Segment>
    {
        private Iterator<Vector2D> start;
        private Iterator<Vector2D> end;


        private SegmentIterator(ArrayList<Vector2D> points)
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
            return hasNext() ? new Segment(start.next(), end.next()) : null;
        }

        public boolean hasNext()
        {
            return end.hasNext();
        }
    }


    private FiberParams params;
    private ArrayList<Vector2D> points;


    Fiber(FiberParams params)
    {
        this.params = params;
        this.points = new ArrayList<>();
    }

    public Iterator<Segment> iterator()
    {
        return new SegmentIterator(points);
    }

    void generate()
    {
        points = RandomUtility.getRandomChain(params.start, params.end, params.length, params.segmentLength);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%n{ \"points\" : [%n"));
        for (Iterator<Vector2D> iter = points.iterator(); iter.hasNext();)
        {
            Vector2D point = iter.next();
            builder.append(String.format("{ \"x\" : %.4f, ", point.getX()));
            builder.append(String.format("\"y\" : %.4f }", point.getY()));
            if (iter.hasNext())
            {
                builder.append(String.format(",%n"));
            }
        }
        builder.append(String.format("%n] }"));
        return builder.toString();
    }


}