import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


class RandomUtility
{
    final static Random rng = new Random();

    static double getRandomDouble(double min, double max)
    {
        return min + rng.nextDouble() * (max - min);
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

    static ArrayList<Double> getRandomList(double desiredMean, double minPossible, double maxPossible, ArrayList<Double> weights)
    {
        ArrayList<Double> output = new ArrayList<>(weights.size());

        double range;
        if (maxPossible == Double.POSITIVE_INFINITY)
        {
            range = desiredMean - minPossible;
        }
        else if (minPossible == Double.NEGATIVE_INFINITY)
        {
            range = maxPossible - desiredMean;
        }
        else
        {
            range = Math.min(desiredMean - minPossible, maxPossible - desiredMean);
        }
        double min = desiredMean - range;
        double max = desiredMean + range;
        double totalWeight = 0.0;
        for (Double weight : weights)
        {
            double value = min + rng.nextDouble() * (max - min);
            output.add(value);
            totalWeight += weight;
        }

        // Correct values so the have exactly the desired mean
        // TODO: This should usually only take two passes, but may take more - possibly allow it to be switched off
        boolean redo = true;
        while (redo)
        {
            double mean = 0;
            for (int i = 0; i < output.size(); i++)
            {
                mean += output.get(i) * weights.get(i);
            }
            mean = mean / totalWeight;
            double diff = desiredMean - mean;
            redo = false;
            for (int i = 0; i < output.size(); i++)
            {
                double value = output.get(i) + diff;
                if (value < minPossible || value > maxPossible)
                {
                    value = min + rng.nextDouble() * (max - min);
                    redo = true;
                }
                output.set(i, value);
            }
        }

        return output;
    }

    // TODO: This function should take a mutable double
    // TODO: This may be a more effective way to generate random values with a fixed mean, but values will need to be shuffled
    static double getConformingRandom(Double currentMean, double totalMean, double minPossible, double maxPossible, int currentWeight, int totalWeight)
    {
        if (currentWeight >= totalWeight)
        {
            throw new IllegalArgumentException("Total weight must be greater than current weight");
        }
        else if (currentWeight == totalWeight - 1)
        {
            currentMean = totalMean;
            return totalWeight * totalMean - currentWeight * currentMean;
        }
        else
        {
            double max = totalWeight * totalMean - currentWeight * currentMean - (totalWeight - currentWeight) * minPossible;
            double min = totalWeight * totalMean - currentWeight * currentMean - (totalWeight - currentWeight) * maxPossible;
            max = (max > maxPossible) ? maxPossible : max;
            min = (min < minPossible) ? minPossible : min;
            double output = min + rng.nextDouble() * (max - min);
            currentMean = (currentMean * currentWeight + output) / (currentWeight + 1);
            return output;
        }
    }

    static ArrayList<Vector2D> extractSteps(ArrayList<Vector2D> points)
    {
        ArrayList<Vector2D> steps = new ArrayList<>(points.size() - 1);
        for (int i = 0; i < points.size() - 1; i++)
        {
            steps.set(i, points.get(i + 1).subtract(points.get(i)));
        }
        return steps;
    }

    static void capSublistSum(ArrayList<Double> list, double max)
    {
        for (int i = 0, sum = 0; i < list.size(); i++)
        {
            sum += list.get(i);
            if (sum > max)
            {
                List<Double> listEnd = list.subList(i, list.size());
                list.removeAll(listEnd);
                list.addAll(list.size() - listEnd.size(), listEnd);
                sum = 0;
                i = 0;
            }
        }
    }

    static ArrayList<Vector2D> getRandomChain(Vector2D start, Vector2D end, ArrayList<Double> weights)
    {
        double totalWeight = 0.0;
        double lastWeight = weights.get(weights.size() - 1);
        weights.remove(weights.size() - 1);
        for (double weight : weights)
        {
            totalWeight += weight;
        }
        double meanProgress = (start.distance(end) - lastWeight) / totalWeight;
        ArrayList<Double> progressValues = RandomUtility.getRandomList(meanProgress, -1.0, 1.0, weights);

        // TODO: calculate the correct max here
        capSublistSum(progressValues, meanProgress * weights.size() + 1);

        // "Progress" is the projection of the unit step vector onto the vector pointing toward the
        // end of the chain
        ArrayList<Vector2D> chain = new ArrayList<>(weights.size() + 1);
        chain.add(start);

        Vector2D current = start;
        for (int i = 0; i < progressValues.size(); i++)
        {
            double distanceAfter = end.distance(current) - progressValues.get(i) * weights.get(i);
            Vector2D[] possiblePoints = circleIntersection(current, end, weights.get(i), distanceAfter);
            current = RandomUtility.rng.nextBoolean() ? possiblePoints[0] : possiblePoints[1];
            chain.add(current);
        }
        chain.add(end);
        return chain;
    }

    static ArrayList<Vector2D> getRandomChain(Vector2D start, Vector2D end, int nSteps, double stepSize)
    {
        ArrayList<Double> evenWeights = new ArrayList<>(Collections.nCopies(nSteps, stepSize));
        return getRandomChain(start, end, evenWeights);
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
            throw new IllegalArgumentException("Out of range");
        }
        double[] output = {(-b + b4ac) / (2 * a), (-b - b4ac) / (2 * a)};
        return output;
    }
}
