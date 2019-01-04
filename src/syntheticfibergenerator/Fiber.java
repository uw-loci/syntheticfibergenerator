package syntheticfibergenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;


class FiberParams {
    int nSegments;
    double straightness;
    double startingWidth;
    double widthVariation;
    double segmentLength;
    Vector start;
    Vector end;
}


class Segment {
    Vector start;
    Vector end;
    double width;


    Segment(Vector start, Vector end, double width) {
        this.start = start;
        this.end = end;
        this.width = width;
    }
}


class Fiber implements Iterable<Segment> {

    class SegmentIterator implements Iterator<Segment> {
        private Iterator<Vector> start;
        private Iterator<Vector> end;
        private Iterator<Double> width;


        private SegmentIterator(ArrayList<Vector> points) {
            start = points.iterator();
            end = points.iterator();
            if (end.hasNext()) {
                end.next();
            }
            width = widths.iterator();
        }


        public Segment next() {
            return hasNext() ? new Segment(start.next(), end.next(), width.next()) : null;
        }


        public boolean hasNext() {
            return end.hasNext();
        }
    }


    FiberParams params;
    private ArrayList<Vector> points;
    private ArrayList<Double> widths;


    Fiber(FiberParams params) {
        this.params = params;
        this.points = new ArrayList<>();
        this.widths = new ArrayList<>();
    }


    public Iterator<Segment> iterator() {
        return new SegmentIterator(points);
    }


    void generate() throws ArithmeticException {
        points = RandomUtility.getRandomChain(params.start, params.end, params.nSegments, params.segmentLength);

        double width = params.startingWidth;
        for (int i = 0; i < params.nSegments; i++) {
            widths.add(width);
            double variability = Math.min(Math.abs(width), params.widthVariation);
            width += RandomUtility.getRandomDouble(-variability, variability);
        }
    }


    void splineSmooth(int splineRatio) {
        if (params.nSegments <= 1) {
            return;
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        double[] tPoints = new double[points.size()];
        double[] xPoints = new double[points.size()];
        double[] yPoints = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
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
        for (; t <= (double) nPoints - 1; t += 1 / (double) splineRatio) {
            points.add(new Vector(xFunc.value(t), yFunc.value(t)));
            if (t + 1 / (double) splineRatio <= (double) nPoints - 1) {
                newWidths.add(widths.get((int) t));
            }
        }
        widths = newWidths;
    }


    /**
     * Tends to smooth out local "wiggles" in the fibers. 5-10 passes is likely sufficient.
     */
    void bubbleSmooth(int bubblePasses) {
        // TODO: create a class which represents a list of points with toDiffs() and fromDiffs() methods
        ArrayList<Vector> diffs = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            diffs.add(points.get(i + 1).subtract(points.get(i)));
        }

        for (int i = 0; i < bubblePasses; i++) {
            boolean modified = false;
            for (int j = 0; j < diffs.size() - 1; j++) {
                double oldDiff = testSwap(diffs, j, j + 1);
                Collections.swap(diffs, j, j + 1);
                double newDiff = testSwap(diffs, j, j + 1);
                if (newDiff >= oldDiff) {
                    Collections.swap(diffs, j, j + 1);
                } else {
                    modified = true;
                }
            }
            if (!modified) {
                break;
            }
        }

        for (int i = 0; i < points.size() - 1; i++) {
            points.set(i + 1, points.get(i).add(diffs.get(i)));
        }
    }


    /**
     * Results in larger-scale smoothing than bubbleSmooth. Generally more computationally intensive
     * (this depends on SWAP_SMOOTH_RESTARTS and SWAP_SMOOTH_RATIO).
     */
    void swapSmooth(int swapRatio) {
        ArrayList<Vector> diffs = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            diffs.add(points.get(i + 1).subtract(points.get(i)));
        }

        for (int j = 0; j < swapRatio * diffs.size(); j++) {
            int d1 = RandomUtility.RNG.nextInt(diffs.size());
            int d2 = RandomUtility.RNG.nextInt(diffs.size());

            double oldChange = testSwap(diffs, d1, d2);
            Collections.swap(diffs, d1, d2);
            double newChange = testSwap(diffs, d1, d2);
            if (newChange >= oldChange) {
                Collections.swap(diffs, d1, d2);
            }
        }

        for (int i = 0; i < points.size() - 1; i++) {
            points.set(i + 1, points.get(i).add(diffs.get(i)));
        }
    }


    private static double totalAngle(ArrayList<Vector> diffs) {
        double sum = 0;
        for (int i = 0; i < diffs.size() - 1; i++) {
            sum += diffs.get(i).angleWith(diffs.get(i + 1));
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
    private static double testSwap(ArrayList<Vector> diffs, int u, int v) {
        int i1 = Math.min(u, v);
        int i2 = Math.max(u, v);
        if (i1 < 0 || i2 > diffs.size() - 1) {
            throw new ArrayIndexOutOfBoundsException("u and v must be within the array");
        }

        double sum = 0.0;

        // Don't do this if i1 is right against the beginning of the array
        if (i1 > 0) {
            sum += diffs.get(i1 - 1).angleWith(diffs.get(i1));
        }

        // If i1 < i2 then i1 + 1 <= diffs.size() - 1
        if (i1 < i2) {
            sum += diffs.get(i1).angleWith(diffs.get(i1 + 1));
        }

        // Prevent double-counting of the space between i1 and i2 if they're adjacent
        if (i1 < i2 - 1) {
            sum += diffs.get(i2 - 1).angleWith(diffs.get(i2));
        }

        // Don't do this if i2 is right against the end of the array
        if (i2 < diffs.size() - 1) {
            sum += diffs.get(i2).angleWith(diffs.get(i2 + 1));
        }

        return sum;
    }
}