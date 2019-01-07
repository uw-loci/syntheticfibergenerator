package syntheticfibergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class DistributionTest {

    @BeforeEach
    void setUp() {
        RngUtility.rng = new Random(1);
    }

    @Test
    void testGaussianSample() {
        Gaussian gaussian = new Gaussian(1.0, 5.0);
        try {
            gaussian.mean.parse("2.0", Double::parseDouble);
            gaussian.sigma.parse("3.0", Double::parseDouble);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        for (int i = 0; i < 100; i++) {
            double value = gaussian.sample();
            assertTrue(value >= 1.0 && value <= 5.0);
        }
    }

    @Test
    void testGaussianInvalidMean() {
        Gaussian gaussian = new Gaussian(1.0, 5.0);
        try {
            gaussian.mean.parse("-1.0", Double::parseDouble);
            gaussian.sigma.parse("3.0", Double::parseDouble);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        for (int i = 0; i < 100; i++) {
            double value = gaussian.sample();
            assertTrue(value >= 1.0 && value <= 5.0);
        }
    }

    @Test
    void testUniformSample() {
        Uniform uniform = new Uniform(-10.0, 17.0);
        try {
            uniform.min.parse("-8.0", Double::parseDouble);
            uniform.max.parse("12.0", Double::parseDouble);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        for (int i = 0; i < 100; i++) {
            double value = uniform.sample();
            assertTrue(value >= -8.0 && value < 12.0);
        }
    }

    @Test
    void testUniformTrim() {
        Uniform uniform = new Uniform(-10.0, 17.0);
        try {
            uniform.min.parse("-15.0", Double::parseDouble);
            uniform.max.parse("17.5", Double::parseDouble);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        for (int i = 0; i < 100; i++) {
            double value = uniform.sample();
            assertTrue(value >= -10.0 && value < 17.0);
        }
    }
}
