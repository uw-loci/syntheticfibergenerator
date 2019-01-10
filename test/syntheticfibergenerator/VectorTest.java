package syntheticfibergenerator;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class VectorTest {

    /**
     * Fix the random seed so we get consistent tests.
     */
    @BeforeEach
    void setUp() {
        RngUtility.rng.setSeed(1);
    }

    @Test
    void testNormalize() {
        for (int i = 0; i < 100; i++) {
            double theta = RngUtility.randomDouble(0.0, 2.0 * Math.PI);
            Vector vector = new Vector(Math.cos(theta), Math.sin(theta));
            vector = vector.scalarMultiply(RngUtility.randomDouble(1e-6, 1e6));
            assertEquals(vector.normalize().getNorm(), 1.0, 1e-6);
        }
    }

    @Test
    void testNormalizeZero() {
        assertThrows(MathArithmeticException.class, () ->
                new Vector(0.0, 0.0).normalize());
    }

    @Test
    void testScalarMultiply() {
        for (int i = 0; i < 100; i++) {
            double theta = RngUtility.randomDouble(0.0, 2.0 * Math.PI);
            Vector vector = new Vector(Math.cos(theta), Math.sin(theta));
            double norm = RngUtility.randomDouble(1e-6, 1e6);
            vector = vector.scalarMultiply(norm);
            assertEquals(vector.getNorm(), norm, 1e-6);
        }
    }

    @Test
    void testAdd() {
        double xMin = -1e6;
        double xMax = 1e6;
        double yMin = -1e6;
        double yMax = 1e6;
        for (int i = 0; i < 100; i++) {
            Vector vec1 = RngUtility.randomPoint(xMin, xMax, yMin, yMax);
            Vector vec2 = RngUtility.randomPoint(xMin, xMax, yMin, yMax);
            Vector sum = vec1.add(vec2);
            assertEquals(sum.getX(), vec1.getX() + vec2.getX(), 1e-6);
            assertEquals(sum.getY(), vec1.getY() + vec2.getY(), 1e-6);
        }
    }

    @Test
    void testSubtract() {
        double xMin = -1e6;
        double xMax = 1e6;
        double yMin = -1e6;
        double yMax = 1e6;
        for (int i = 0; i < 100; i++) {
            Vector vec1 = RngUtility.randomPoint(xMin, xMax, yMin, yMax);
            Vector vec2 = RngUtility.randomPoint(xMin, xMax, yMin, yMax);
            Vector sum = vec1.subtract(vec2);
            assertEquals(sum.getX(), vec1.getX() - vec2.getX(), 1e-6);
            assertEquals(sum.getY(), vec1.getY() - vec2.getY(), 1e-6);
        }
    }

    @Test
    void testTheta() {
        for (int i = 0; i < 100; i++) {
            double theta = RngUtility.randomDouble(-Math.PI, Math.PI);
            Vector vec = new Vector(Math.cos(theta), Math.sin(theta));
            double norm = RngUtility.randomDouble(1e-6, 1e6);
            vec = vec.scalarMultiply(norm);
            assertEquals(theta, vec.theta(), 1e-6);
        }
    }

    @Test
    void testAngleWith() {
        for (int i = 0; i < 100; i++) {
            double theta1 = RngUtility.randomDouble(0, Math.PI);
            double theta2 = RngUtility.randomDouble(0, Math.PI);
            Vector vec1 = new Vector(Math.cos(theta1), Math.sin(theta1));
            Vector vec2 = new Vector(Math.cos(theta2), Math.sin(theta2));
            assertEquals(Math.abs(theta2 - theta1), vec1.angleWith(vec2), 1e-6);
        }
    }

    @Test
    void testAngleWithZero() {
        Vector vec = new Vector(1.0, 0.0);
        assertThrows(ArithmeticException.class, () ->
                vec.angleWith(new Vector()));
    }

    @Test
    void testRotate() { // TODO: Maybe add more to this
        Vector vec = new Vector(1, 0);
        vec = vec.rotate(new Vector(0, 1));
        assertEquals(vec, new Vector(0, 1));
    }

    @Test
    void testRotateZeroAxis() {
        Vector vec = new Vector(1.0, 0.0);
        assertThrows(ArithmeticException.class, () ->
                vec.rotate(new Vector()));
    }
}
