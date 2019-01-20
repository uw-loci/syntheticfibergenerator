package syntheticfibergenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


class RngUtility {

    static Random rng = new Random();


    static Vector nextPoint(double xMin, double xMax, double yMin, double yMax) {
        double x = nextDouble(xMin, xMax);
        double y = nextDouble(yMin, yMax);
        return new Vector(x, y);
    }

    /**
     * @return A random integer between min, inclusive, and max, exclusive. Thus an IllegalArgumentException is thrown
     * if min >= max.
     */
    static int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Random bounds are inverted");
        } else if (min == max) {
            throw new IllegalArgumentException("Random range must have nonzero size");
        }
        return min + rng.nextInt(max - min);
    }

    /**
     * Due to the behavior of Random.nextDouble, min is inclusive but max is exclusive. In practice this doesn't matter,
     * as the exact min is only generated ~1/2^54 times.
     */
    static double nextDouble(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Random bounds are inverted");
        }
        return min + rng.nextDouble() * (max - min);
    }

    static ArrayList<Vector> randomChain(Vector start, Vector end, int nSteps, double stepSize)
            throws ArithmeticException {
        if (nSteps <= 0) {
            throw new IllegalArgumentException("Must have at least one step");
        }
        if (stepSize <= 0.0) {
            throw new IllegalArgumentException("Step size must be positive");
        }
        ArrayList<Vector> points = new ArrayList<>(Collections.nCopies(nSteps + 1, null));
        points.set(0, start);
        points.set(nSteps, end);
        randomChainRecursive(points, 0, nSteps, stepSize);
        return points;
    }

    private static void randomChainRecursive(ArrayList<Vector> points, int iStart, int iEnd, double stepSize)
            throws ArithmeticException {
        if (iEnd - iStart <= 1) {
            return;
        }

        int iBridge = (iStart + iEnd) / 2;
        Circle circle1 = new Circle(points.get(iStart), stepSize * (iBridge - iStart));
        Circle circle2 = new Circle(points.get(iEnd), stepSize * (iEnd - iBridge));
        Vector bridge;
        if (iBridge > iStart + 1 && iBridge < iEnd - 1) {
            bridge = Circle.diskDiskIntersect(circle1, circle2);
        } else if (iBridge == iStart + 1 && iBridge == iEnd - 1) {
            Vector[] intersects = Circle.circleCircleIntersect(circle1, circle2);
            bridge = RngUtility.rng.nextBoolean() ? intersects[0] : intersects[1];
        } else if (iBridge == iStart + 1) {
            bridge = Circle.diskCircleIntersect(circle2, circle1);
        } else {
            bridge = Circle.diskCircleIntersect(circle1, circle2);
        }
        points.set(iBridge, bridge);

        randomChainRecursive(points, iStart, iBridge, stepSize);
        randomChainRecursive(points, iBridge, iEnd, stepSize);
    }
}
