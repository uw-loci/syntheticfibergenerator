import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


class FiberParams
{
    int nSegments;
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
    // TODO: Allow user to set smoothing parameters
    private static final int SPLINE_RATIO = 4;
    private static final int BUBBLE_SMOOTH_PASSES = 5;
    private static final int SWAP_SMOOTH_RATIO = 10;
    private static final int SWAP_SMOOTH_RESTARTS = 1;


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
        points = RandomUtility.getRandomChain(params.start, params.end, params.nSegments, params.segmentLength);

        double width = params.startingWidth;
        for (int i = 0; i < params.nSegments; i++)
        {
            widths.add(width);
            double variability = Math.min(Math.abs(width), params.widthVariation);
            width += RandomUtility.getRandomDouble(-variability, variability);
        }
    }


    void splineSmooth()
    {
        if (params.nSegments <= 1)
        {
            return;
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        double[] tPoints = new double[points.size()];
        double[] xPoints = new double[points.size()];
        double[] yPoints = new double[points.size()];
        for (int i = 0; i < points.size(); i++)
        {
            tPoints[i] = i;
            xPoints[i] = points.get(i).getX();
            yPoints[i] = points.get(i).getY();
        }
        @SuppressWarnings("SuspiciousNameCombination")
        PolynomialSplineFunction xFunc = interpolator.interpolate(tPoints, xPoints);
        PolynomialSplineFunction yFunc = interpolator.interpolate(tPoints, yPoints);
        int nPoints = points.size();
        points.clear();
        ArrayList<Double> newWidths = new ArrayList<>();
        double t = 0;
        for (; t <= (double) nPoints - 1; t += 1 / (double) SPLINE_RATIO)
        {
            points.add(new Vector2D(xFunc.value(t), yFunc.value(t)));
            if (t + 1 / (double) SPLINE_RATIO <= (double) nPoints - 1)
            {
                newWidths.add(widths.get((int) t));
            }
        }
        widths = newWidths;
    }


    /**
     * Tends to smooth out local "wiggles" in the fibers. 5-10 passes is likely sufficient.
     */
    void bubbleSmooth()
    {
        ArrayList<Vector2D> diffs = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++)
        {
            diffs.add(points.get(i + 1).subtract(points.get(i)));
        }

        // TODO: Break out of the iteration early if a pass gave no changes
        for (int i = 0; i < BUBBLE_SMOOTH_PASSES; i++)
        {
            for (int j = 0; j < diffs.size() - 1; j++)
            {
                double oldChange = testSwap(diffs, j, j + 1);
                Collections.swap(diffs, j, j + 1);
                double newChange = testSwap(diffs, j, j + 1);
                if (newChange >= oldChange)
                {
                    Collections.swap(diffs, j, j + 1);
                }
            }
        }

        for (int i = 0; i < points.size() - 1; i++)
        {
            points.set(i + 1, points.get(i).add(diffs.get(i)));
        }
    }


    /**
     * Results in larger-scale smoothing than bubbleSmooth. Generally more computationally intensive
     * (this depends on SWAP_SMOOTH_RESTARTS and SWAP_SMOOTH_RATIO).
     */
    void swapSmooth()
    {
        ArrayList<Vector2D> diffs = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++)
        {
            diffs.add(points.get(i + 1).subtract(points.get(i)));
        }

        ArrayList<Vector2D> bestDiffs = new ArrayList<>(diffs);
        double bestChange = totalAngle(bestDiffs);

        for (int i = 0; i < SWAP_SMOOTH_RESTARTS; i++)
        {
            Collections.shuffle(diffs, RandomUtility.RNG);

            for (int j = 0; j < SWAP_SMOOTH_RATIO * diffs.size(); j++)
            {
                int d1 = RandomUtility.RNG.nextInt(diffs.size());
                int d2 = RandomUtility.RNG.nextInt(diffs.size());

                double oldChange = testSwap(diffs, d1, d2);
                Collections.swap(diffs, d1, d2);
                double newChange = testSwap(diffs, d1, d2);
                if (newChange >= oldChange)
                {
                    Collections.swap(diffs, d1, d2);
                }
            }

            if (totalAngle(diffs) < bestChange)
            {
                bestDiffs = diffs;
                bestChange = totalAngle(diffs);
            }
        }

        for (int i = 0; i < points.size() - 1; i++)
        {
            points.set(i + 1, points.get(i).add(bestDiffs.get(i)));
        }
    }


    private static double totalAngle(ArrayList<Vector2D> diffs)
    {
        double sum = 0;
        for (int i = 0; i < diffs.size() - 1; i++)
        {
            sum += angle(diffs.get(i), diffs.get(i + 1));
        }
        return sum;
    }


    /**
     * u, v are assumed to be distinct and lie within the bounds of the array.
     *
     * @param diffs
     * @param u
     * @param v
     * @return
     */
    private static double testSwap(ArrayList<Vector2D> diffs, int u, int v)
    {
        int i1 = Math.min(u, v);
        int i2 = Math.max(u, v);
        if (i1 < 0 || i2 > diffs.size() - 1)
        {
            throw new ArrayIndexOutOfBoundsException("u and v must be within the array");
        }

        double sum = 0.0;

        // Don't do this if i1 is right against the beginning of the array
        if (i1 > 0)
        {
            sum += angle(diffs.get(i1 - 1), diffs.get(i1));
        }

        // If i1 < i2 then i1 + 1 <= diffs.size() - 1
        if (i1 < i2)
        {
            sum += angle(diffs.get(i1), diffs.get(i1 + 1));
        }

        // Prevent double-counting of the space between i1 and i2 if they're adjacent
        if (i1 < i2 - 1)
        {
            sum += angle(diffs.get(i2 - 1), diffs.get(i2));
        }

        // Don't do this if i2 is right against the end of the array
        if (i2 < diffs.size() - 1)
        {
            sum += angle(diffs.get(i2), diffs.get(i2 + 1));
        }

        return sum;
    }


    // TODO: Move to general utility, possibly extend Vector2D class
    private static double angle(Vector2D v1, Vector2D v2)
    {
        double cos = v1.normalize().dotProduct(v2.normalize());
        cos = Math.min(1, cos);
        cos = Math.max(-1, cos);
        return Math.acos(cos);
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