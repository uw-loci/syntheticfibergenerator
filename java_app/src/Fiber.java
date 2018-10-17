import java.util.ArrayList;
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
        points = getRandomChain(params.start, params.end, params.segmentLength, params.length);
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

    private static ArrayList<Vector2D> getRandomChain(Vector2D start, Vector2D end, double stepSize, int nSteps)
    {
        if (stepSize * nSteps < start.distance(end))
        {
            throw new IllegalArgumentException("Path distance must be at least endpoint distance");
        }

        ArrayList<Vector2D> chain = new ArrayList<>(nSteps + 1);
        chain.add(start);

        Vector2D current = start;
        for (int i = 0; i < nSteps - 1; i++)
        {
            // "Progress" is the projection of the unit step vector onto the vector pointing toward
            // the end of the chain
            double maxDistance = (nSteps - i) * stepSize;
            double endDistance = current.distance(end);
            double minProgress = (endDistance - maxDistance) / stepSize;

            double progress;
            if (i == nSteps - 2)
            {
                progress = endDistance / stepSize - 1.0;
                // TODO: this is a temporary fix
                progress = progress > 1 ? 1 : progress;
            }
            else
            {
                // TODO: Modify this to allow for random generation of progress values
//                double min = Math.max(-1.0, minProgress);
//                min = min > 1.0 ? 1.0 : min;
//                double meanProgress = start.distance(end) / (stepSize * nSteps);
//                double range = Math.min(1.0 - meanProgress, meanProgress - min);
//                progress = min + rng.nextDouble() * (2.0 * range);
                progress = start.distance(end) / (stepSize * nSteps);
            }

            Vector2D forward = end.subtract(current).normalize();
            Vector2D perpendicular = new Vector2D(-forward.getY(), forward.getX()).normalize();

            Vector2D stepForward = forward.scalarMultiply(progress * stepSize);
            double notProgress = Math.sqrt(1.0 - progress * progress);
            Vector2D stepPerpendicular = perpendicular.scalarMultiply(notProgress * stepSize);

            Vector2D step;
            if (RandomUtility.rng.nextBoolean())
            {
                step = stepForward.add(stepPerpendicular);
            }
            else
            {
                step = stepForward.add(stepPerpendicular.negate());
            }
            current = current.add(step);
            chain.add(current);
        }

        chain.add(end);
        return chain;
    }
}