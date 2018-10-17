import java.util.ArrayList;
import java.util.Random;

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
        // TODO: This should usually only take two passes, but may take more
        // TODO: Possibly allow for this to be switched off
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
}
