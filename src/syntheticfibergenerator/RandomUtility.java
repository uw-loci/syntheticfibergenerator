package syntheticfibergenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


public class RandomUtility
{
    public static Random RNG;

    /* If we generate random values too close to the upper or lower bounds it can cause problems
     * with limits of floating-point arithmetic; narrow the bounds by this amount so the user
     * doesn't have to worry about this. */
    private static final double BUFF = 1e-10;

    static double getRandomDouble(double min, double max)
    {
        return min + RNG.nextDouble() * (max - min);
    }


    /**
     * Upper bound is exclusive, lower bound is inclusive. Must have  max >= min (if they are the same, max = min is returned
     *
     * @param min
     * @param max
     * @return
     */
    static int getRandomInt(int min, int max)
    {
        if (max == min)
        {
            return min;
        }
        else
        {
            return min + RNG.nextInt(max - min);
        }
    }


    static Vector2D getRandomDirection()
    {
        double theta = RNG.nextDouble() * 2.0 * Math.PI;
        return new Vector2D(Math.cos(theta), Math.sin(theta));
    }


    static Vector getRandomPoint(double xMin, double xMax, double yMin, double yMax)
    {
        double x = getRandomDouble(xMin, xMax);
        double y = getRandomDouble(yMin, yMax);
        return new Vector(x, y);
    }


    public static ArrayList<Double> getRandomList(double mT, double xMin, double xMax, int nValues)
    {
        ArrayList<Double> evenWeights = new ArrayList<>(Collections.nCopies(nValues, 1.0));
        return getRandomList(mT, xMin, xMax, evenWeights);
    }


    static ArrayList<Double> getRandomList(double mT, double xMin, double xMax, ArrayList<Double> weights)
    {
        // See comment with the definition of the constant "BUFF"
        xMin += BUFF;
        xMax -= BUFF;
        mT = Math.min(xMax, mT);
        mT = Math.max(xMin, mT);

        // m: mean, w: weight, x: param
        // C: cumulative (not including current), I: current, R: remaining (not including current), T: total
        double wT = 0.0;
        for (Double w : weights)
        {
            wT += w;
        }

        ArrayList<Double> output = new ArrayList<>();
        double mC = 0.0;
        double wC = 0.0;
        for (int i = 0; i < weights.size() - 1; i++)
        {
            double wI = weights.get(i);
            double wR = wT - wC - wI;
            double xIMin = (mT * wT - mC * wC - xMax * wR) / wI;
            double xIMax = (mT * wT - mC * wC - xMin * wR) / wI;
            xIMin = Math.max(xIMin, xMin);
            xIMax = Math.min(xIMax, xMax);

            double xI = getRandomDouble(xIMin, xIMax);
            output.add(xI);
            mC = (mC * wC + xI * wI) / (wC + wI);
            wC += wI;
        }
        double last = wT * mT - wC * mC;
        output.add(last);

        Collections.shuffle(output, RandomUtility.RNG);
        return output;
    }


    /**
     * Public driver method
     *
     * @param start
     * @param end
     * @param nSteps
     * @param stepSize
     * @return
     */
    static ArrayList<Vector> getRandomChain(Vector start, Vector end, int nSteps, double stepSize) throws ArithmeticException
    {
        ArrayList<Vector> points = new ArrayList<>(Collections.nCopies(nSteps + 1, null));
        points.set(0, start);
        points.set(nSteps, end);
        randomChainRecursive(points, 0, nSteps, stepSize);
        return points;
    }


    /**
     * Recursive method
     *
     * @param points
     * @param iStart
     * @param iEnd
     * @param stepSize
     */
    private static void randomChainRecursive(ArrayList<Vector> points, int iStart, int iEnd, double stepSize) throws ArithmeticException
    {
        // Base case: the start and end points are the same or adjacent
        if (iEnd - iStart <= 1)
        {
            return;
        }

        // Note: Using the midpoint as the bridge gives more interesting paths
        // TODO: Pass a flag to switch between midpoint bridge and random bridge
        int iBridge = (iStart + iEnd) / 2;
        Circle circle1 = new Circle(points.get(iStart), stepSize * (iBridge - iStart));
        Circle circle2 = new Circle(points.get(iEnd), stepSize * (iEnd - iBridge));
        Vector bridge;
        if (iBridge > iStart + 1 && iBridge < iEnd - 1)
        {
            bridge = Circle.diskDiskIntersect(circle1, circle2);
        }
        else if (iBridge == iStart + 1 && iBridge == iEnd - 1)
        {
            Vector[] intersects = Circle.circleCircleIntersect(circle1, circle2);
            bridge = RandomUtility.RNG.nextBoolean() ? intersects[0] : intersects[1];
        }
        else if (iBridge == iStart + 1)
        {
            bridge = Circle.diskCircleIntersect(circle2, circle1);
        }
        else
        {
            bridge = Circle.diskCircleIntersect(circle1, circle2);
        }
        points.set(iBridge, bridge);

        // Recursive call
        randomChainRecursive(points, iStart, iBridge, stepSize);
        randomChainRecursive(points, iBridge, iEnd, stepSize);
    }
}
