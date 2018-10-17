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

        // "Progress" is the projection of the unit step vector onto the vector pointing toward the
        // end of the chain
        ArrayList<Vector2D> chain = new ArrayList<>(nSteps + 1);
        chain.add(start);
        double meanProgress = (start.distance(end) - stepSize) / (stepSize * (nSteps - 1));
        ArrayList<Double> progressValues = RandomUtility.getRandomList(meanProgress, -1.0, 1.0, nSteps - 1);

        Vector2D current = start;
        for (double progress : progressValues)
        {
            double distanceAfter = end.distance(current) - progress * stepSize;
            Vector2D[] possiblePoints = circleIntersection(current, end, stepSize, distanceAfter);
            current = RandomUtility.rng.nextBoolean() ? possiblePoints[0] : possiblePoints[1];
            chain.add(current);
        }
        chain.add(end);
        return chain;
    }

    static private double Sq(double val)
    {
        return val * val;
    }

    // Assumes the circles have two intersection points (this is currently up to the caller to ensure)
    static private Vector2D[] circleIntersection(Vector2D center1, Vector2D center2, double radius1, double radius2)
    {
        // Find the equation of a line passing between the intersection points
        double x1 = center1.getX();
        double y1 = center1.getY();
        double x2 = center2.getX();
        double y2 = center2.getY();
        double c1 = Sq(radius1) - Sq(x1) - Sq(y1);
        double c2 = Sq(radius2) - Sq(x2) - Sq(y2);
        double b = 0.5 * (c2 - c1) / (y1 - y2);
        double a = (x2 - x1) / (y1 - y2);

        // TODO: When the two points are nearly vertical the y=ax+b form does poorly. Switch to x=ay+b in this case (or maybe use a spherical parametrization)
        // Solve the quadratic for the two intersection points
        double d = b - y1;
        double aQ = Sq(a) + 1;
        double bQ = 2 * a * d - 2 * x1;
        double cQ = Sq(x1) + Sq(d) - Sq(radius1);
        double[] solutions = quadraticSolve(aQ, bQ, cQ);
        Vector2D[] output = {
                new Vector2D(solutions[0], a * solutions[0] + b),
                new Vector2D(solutions[1], a * solutions[1] + b)};
        return output;
    }

    // Assumes two real solutions exist
    static private double[] quadraticSolve(double a, double b, double c)
    {
        double b4ac = Math.sqrt(Sq(b) - 4 * a * c);
        if (Sq(b ) - 4 * a * c < 0)
        {
            System.out.println("Bad");
            throw new IllegalArgumentException("Out of range");
        }
        double[] output = {(-b + b4ac) / (2 * a), (-b - b4ac) / (2 * a)};
        return output;
    }
}