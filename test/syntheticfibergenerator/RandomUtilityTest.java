package syntheticfibergenerator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class RandomUtilityTest
{

    @Test
    void testGetValidRandom()
    {
        fail("Not implemented");
    }

    @Test
    void testGetRandomList()
    {
        RandomUtility.RNG.setSeed(1);
        ArrayList<Double> values = RandomUtility.getRandomList(0.8, 0.0, 1.0, 100);
        double mean = 0.0;
        double delta = 0.001;
        for (Double x : values)
        {
            mean += x / values.size();
            if (x < 0.0 - delta || x > 1.0 + delta)
            {
                fail("Value generated out of range");
            }
        }
        assertEquals(mean, 0.8, delta);
    }
}