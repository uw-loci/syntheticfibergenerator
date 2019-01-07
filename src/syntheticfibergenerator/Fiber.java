package syntheticfibergenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;


class Fiber implements Iterable<Fiber.Segment> {

    static class Params {

        double segmentLength;
        double widthChange;

        int nSegments;
        double startWidth;
        double straightness;

        Vector start;
        Vector end;
    }


    static class Segment {

        Vector start;
        Vector end;
        double width;


        Segment(Vector start, Vector end, double width) {
            this.start = start;
            this.end = end;
            this.width = width;
        }
    }


    class SegmentIterator implements Iterator<Segment> {

        int curr = 0;


        public Segment next() {
            if (hasNext()) {
                Segment output = new Segment(points.get(curr), points.get(curr + 1), widths.get(curr));
                curr++;
                return output;
            } else {
                return null;
            }
        }

        public boolean hasNext() {
            return curr < points.size() - 1;
        }
    }


    private Params params;
    private ArrayList<Vector> points;
    private ArrayList<Double> widths;


    Fiber(Params params) {
        this.params = params;
        this.points = new ArrayList<>();
        this.widths = new ArrayList<>();
    }

    @Override
    public Iterator<Segment> iterator() {
        return new SegmentIterator();
    }

    void generate() throws ArithmeticException {
        points = RngUtility.randomChain(params.start, params.end, params.nSegments, params.segmentLength);
        double width = params.startWidth;
        for (int i = 0; i < params.nSegments; i++) {
            widths.add(width);
            double variability = Math.min(Math.abs(width), params.widthChange);
            width += RngUtility.randomDouble(-variability, variability);
        }
    }

    void bubbleSmooth(int passes) {
        ArrayList<Vector> deltas = MiscUtility.toDeltas(points);
        for (int i = 0; i < passes; i++) {
            for (int j = 0; j < deltas.size() - 1; j++) {
                trySwap(deltas, j, j + 1);
            }
        }
        points = MiscUtility.fromDeltas(deltas, points.get(0));
    }

    void swapSmooth(int ratio) {
        ArrayList<Vector> deltas = MiscUtility.toDeltas(points);
        for (int j = 0; j < ratio * deltas.size(); j++) {
            int u = RngUtility.rng.nextInt(deltas.size());
            int v = RngUtility.rng.nextInt(deltas.size());
            trySwap(deltas, u, v);
        }
        points = MiscUtility.fromDeltas(deltas, points.get(0));
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
        for (double t = 0; t <= (double) nPoints - 1; t += 1 / (double) splineRatio) {
            points.add(new Vector(xFunc.value(t), yFunc.value(t)));
            if (t + 1 / (double) splineRatio <= (double) nPoints - 1) {
                newWidths.add(widths.get((int) t));
            }
        }
        widths = newWidths;
    }

    private static void trySwap(ArrayList<Vector> deltas, int u, int v) {
        double oldDiff = localDiffSum(deltas, u, v);
        Collections.swap(deltas, u, v);
        double newDiff = localDiffSum(deltas, u, v);
        if (newDiff > oldDiff) {
            Collections.swap(deltas, u, v);
        }
    }

    private static double localDiffSum(ArrayList<Vector> deltas, int u, int v) {
        int i1 = Math.min(u, v);
        int i2 = Math.max(u, v);
        if (i1 < 0 || i2 > deltas.size() - 1) {
            throw new ArrayIndexOutOfBoundsException("u and v must be within the array");
        }

        double sum = 0.0;
        if (i1 > 0) { // Don't do this if i1 is right against the beginning of the array
            sum += deltas.get(i1 - 1).angleWith(deltas.get(i1));
        }
        if (i1 < i2) { // If i1 < i2 then i1 + 1 <= deltas.size() - 1
            sum += deltas.get(i1).angleWith(deltas.get(i1 + 1));
        }
        if (i1 < i2 - 1) { // Prevent double-counting of the space between i1 and i2 if they're adjacent
            sum += deltas.get(i2 - 1).angleWith(deltas.get(i2));
        }
        if (i2 < deltas.size() - 1) { // Don't do this if i2 is right against the end of the array
            sum += deltas.get(i2).angleWith(deltas.get(i2 + 1));
        }
        return sum;
    }
}