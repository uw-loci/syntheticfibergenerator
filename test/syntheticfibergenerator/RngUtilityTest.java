package syntheticfibergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class RngUtilityTest {

    /**
     * Fix the random seed so we get consistent tests.
     */
    @BeforeEach
    void setUp() {
        RngUtility.rng.setSeed(1);
    }

    @Test
    void testRandomPoint() {
        double xMin = -10.0;
        double xMax = 356.2;
        double yMin = 500.3;
        double yMax = 12345;
        for (int i = 0; i < 100; i++) {
            Vector point = RngUtility.randomPoint(xMin, xMax, yMin, yMax);
            assertTrue(point.getX() >= xMin && point.getX() < xMax);
            assertTrue(point.getY() >= yMin && point.getY() < yMax);
        }
    }

    @Test
    void testRandomPointInverted() {
        double xMin = 356.2;
        double xMax = -10.0;
        double yMin = 500.3;
        double yMax = 12345;
        assertThrows(IllegalArgumentException.class, () ->
                RngUtility.randomPoint(xMin, xMax, yMin, yMax));
    }

    @Test
    void testRandomPointZeroHeight() {
        double xMin = 12.7;
        double xMax = 13.3;
        double y = 7.8;
        for (int i = 0; i < 100; i++) { // TODO: Extract loop max to constant
            Vector point = RngUtility.randomPoint(xMin, xMax, y, y);
            assertEquals(point.getY(), y);
            assertTrue(point.getX() >= xMin && point.getX() < xMax);
        }
    }

    @Test
    void testRandomPointZeroVolume() {
        double x = 10.33;
        double y = -23.6;
        for (int i = 0; i < 100; i++) {
            Vector point = RngUtility.randomPoint(x, x, y, y);
            assertEquals(point.getX(), x);
            assertEquals(point.getY(), y);
        }
    }

    @Test
    void testRandomDouble() {
        double min = -1.0;
        double max = 3.14;
        for (int i = 0; i < 100; i++) {
            double value = RngUtility.randomDouble(min, max);
            assertTrue(value >= min && value < max);
        }
    }

    @Test
    void testRandomDoubleInverted() {
        double min = 3.14;
        double max = -1.0;
        assertThrows(IllegalArgumentException.class, () ->
                RngUtility.randomDouble(min, max));
    }

    @Test
    void testRandomDoubleZeroWidth() {
        double val = 107.9;
        for (int i = 0; i < 100; i++) {
            assertEquals(RngUtility.randomDouble(val, val), val);
        }
    }

    @Test
    void testRandomChain() { // TODO: is this redundant?
        int nSteps = 23;
        double stepSize = 7.0;
        Vector start = new Vector(0.0, 0.0);
        Vector end = new Vector(-1, 1).normalize().scalarMultiply(0.7 * nSteps * stepSize);
        ArrayList<Vector> chain = RngUtility.randomChain(start, end, nSteps, stepSize);
        Vector prev = chain.get(0);
        for (int i = 1; i < chain.size(); i++) {
            Vector current = chain.get(i);
            assertEquals(current.subtract(prev).getNorm(), stepSize, 1e-6); // TODO: extract tolerance to constant
            prev = current;
        }
        assertEquals(chain.get(0), start);
        assertEquals(chain.get(chain.size() - 1), end);
    }

    @Test
    void testRandomChainExceptions() {
        int nSteps = 23;
        double stepSize = 7.0;
        Vector start = new Vector(0.0, 0.0);
        Vector end = new Vector(-1, 1).normalize().scalarMultiply(0.7 * nSteps * stepSize);
        assertThrows(IllegalArgumentException.class, () ->
                RngUtility.randomChain(start, end, 0, stepSize));
        assertThrows(IllegalArgumentException.class, () ->
                RngUtility.randomChain(start, end, -10, stepSize));
        assertThrows(IllegalArgumentException.class, () ->
                RngUtility.randomChain(start, end, nSteps, 0.0));
        assertThrows(IllegalArgumentException.class, () ->
                RngUtility.randomChain(start, end, nSteps, -7.0));
    }

    @Test
    void testRandomChainNotExists() {
        int nSteps = 23;
        double stepSize = 7.0;
        Vector start = new Vector(0.0, 0.0);
        Vector end = new Vector(-1, 1).normalize().scalarMultiply(1.1 * nSteps * stepSize);
        assertThrows(ArithmeticException.class, () ->
                RngUtility.randomChain(start, end, nSteps, stepSize));
    }
}