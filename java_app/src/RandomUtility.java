import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


class RandomUtility
{
    final static Random rng = new Random();


    static double getRandomDouble(double min, double max)
    {
        return min + rng.nextDouble() * (max - min);
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
            return min + rng.nextInt(max - min);
        }
    }


    static Vector2D getRandomDirection()
    {
        double theta = rng.nextDouble() * 2.0 * Math.PI;
        return new Vector2D(Math.cos(theta), Math.sin(theta));
    }


    static Vector2D getRandomPoint(double minX, double maxX, double minY, double maxY)
    {
        double x = getRandomDouble(minX, maxX);
        double y = getRandomDouble(minY, maxY);
        return new Vector2D(x, y);
    }


    static ArrayList<Double> getRandomList(double desiredMean, double minPossible, double maxPossible, int nValues)
    {
        ArrayList<Double> evenWeights = new ArrayList<>(Collections.nCopies(nValues, 1.0));
        return getRandomList(desiredMean, minPossible, maxPossible, evenWeights);
    }


    static ArrayList<Double> getRandomList(double mean, double min, double max, ArrayList<Double> weights)
    {
        double totalWeight = 0.0;
        for (Double weight : weights)
        {
            totalWeight += weight;
        }

        ArrayList<Double> output = new ArrayList<>();
        double currentMean = 0.0;
        double currentWeight = 0.0;
        for (int i = 0; i < weights.size() - 1; i++)
        {
            // TODO: Need to check this math
            double currentMax = Math.min(totalWeight * mean - currentWeight * currentMean - (totalWeight - currentWeight) * min, max);
            double currentMin = Math.max(totalWeight * mean - currentWeight * currentMean - (totalWeight - currentWeight) * max, min);
            double value = getRandomDouble(currentMin, currentMax);
            output.add(value);

            double prevWeight = currentWeight;
            currentWeight += weights.get(i);
            currentMean = (currentMean * prevWeight + value * weights.get(i)) / currentWeight;
        }
        output.add(totalWeight * mean - currentWeight * currentMean);

        Collections.shuffle(output);
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
    static ArrayList<Vector2D> getRandomChain(Vector2D start, Vector2D end, int nSteps, double stepSize)
    {
        ArrayList<Vector2D> points = new ArrayList<>(Collections.nCopies(nSteps, null));
        points.set(0, start);
        points.set(nSteps - 1, end);
        randomChainRecursive(points, 0, nSteps - 1, stepSize);
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
    private static void randomChainRecursive(ArrayList<Vector2D> points, int iStart, int iEnd, double stepSize)
    {
        // Base case
        if (iEnd - iStart < 1)
        {
            return;
        }

        int iBridge = getRandomInt(iStart + 1, iEnd);
        Circle circle1 = new Circle(points.get(iStart), stepSize * (iBridge - iStart));
        Circle circle2 = new Circle(points.get(iEnd), stepSize * (iEnd - iBridge));
        Vector2D bridge;
        if (iBridge > iStart + 1 && iBridge < iEnd - 1)
        {
            bridge = Circle.diskDiskIntersect(circle1, circle2);
        }
        else if (iBridge == iStart + 1 && iBridge == iEnd - 1)
        {
            bridge = Circle.circleCircleIntersect(circle1, circle2);
        }
        else if (iBridge == iStart + 1)
        {
            bridge = Circle.diskCircleIntersect(circle2, circle2);
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
