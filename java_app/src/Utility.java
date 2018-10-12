import java.util.Random;
import java.awt.geom.Point2D;

class Utility
{
    private final static Random rng = new Random();

    /**
     *
     * @param currentMean
     * @param totalMean
     * @param minPossible
     * @param maxPossible
     * @param currentWeight
     * @param totalWeight
     * @return
     */
    static double getValidRandom(Double currentMean, double totalMean, double minPossible,
                                        double maxPossible, int currentWeight, int totalWeight)
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
            double max = totalWeight * totalMean - currentWeight * currentMean
                    - (totalWeight - currentWeight) * minPossible;
            double min = totalWeight * totalMean - currentWeight * currentMean
                    - (totalWeight - currentWeight) * maxPossible;
            max = (max > maxPossible) ? maxPossible : max;
            min = (min < minPossible) ? minPossible : min;
            double output = min + rng.nextDouble() * (max - min);
            currentMean = (currentMean * currentWeight + output) / (currentWeight + 1);
            return output;
        }
    }

    static Point2D.Double getRandomDirection()
    {
        double theta = rng.nextDouble() * 2.0 * Math.PI;
        return new Point2D.Double(Math.cos(theta), Math.sin(theta));
    }

    static Point2D.Double getRandomPoint(double minX, double maxX, double minY, double maxY)
    {
        double x = minX + rng.nextDouble() * (maxX - minX);
        double y = minY + rng.nextDouble() * (maxY - minY);
        return new Point2D.Double(x, y);
    }
}
