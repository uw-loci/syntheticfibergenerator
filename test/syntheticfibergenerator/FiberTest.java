package syntheticfibergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class FiberTest {

    private Fiber.Params params;
    private Fiber fiber;

    @BeforeEach
    void setUp() {
        RngUtility.rng = new Random(1);
        Fiber.Params params = new Fiber.Params();
        params.segmentLength = 3.0;
        params.widthChange = 1.0;
        params.nSegments = 18;
        params.startWidth = 5.0;
        params.straightness = 0.9;
        params.start = new Vector();
        params.end = new Vector(2.0, 1.0).normalize().scalarMultiply(48.6);
        this.params = params;
        fiber = new Fiber(params);
    }

    @Test
    void testIterator() {
        fiber.generate();
        Iterator<Fiber.Segment> iterator = fiber.iterator();
        for (int i = 0; i < params.nSegments; i++) {
            assertTrue(iterator.hasNext());
            iterator.next();
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    void testPointGeneration() {
        fiber.generate();
        Vector prevEnd = params.start;
        for (Fiber.Segment segment : fiber) {
            assertEquals(segment.start.distance(segment.end), params.segmentLength, 1e-6);
            assertEquals(prevEnd, segment.start);
            prevEnd = segment.end;
        }
        assertEquals(prevEnd, params.end);
    }

    @Test
    void testWidthGeneration() {
        fiber.generate();
        Iterator<Fiber.Segment> iterator = fiber.iterator();
        Fiber.Segment first = iterator.next();
        assertEquals(first.width, params.startWidth);
        double prevWidth = params.startWidth;
        for (Fiber.Segment segment : fiber) {
            assertTrue(Math.abs(segment.width - prevWidth) <= params.widthChange);
            assertTrue(segment.width > 0.0);
            prevWidth = segment.width;
        }
    }

    @Test
    void testBubbleSmooth() {
        fiber.generate();
        double oldSum = angleChangeSum(fiber);
        for (int i = 0; i < 10; i++) {
            fiber.bubbleSmooth(1);
            double newSum = angleChangeSum(fiber);
            assertTrue(newSum <= oldSum);
            oldSum = newSum;
        }
    }

    @Test
    void testSwapSmooth() {
        fiber.generate();
        double oldSum = angleChangeSum(fiber);
        for (int i = 0; i < 10; i++) {
            fiber.swapSmooth(1);
            double newSum = angleChangeSum(fiber);
            assertTrue(newSum <= oldSum);
            oldSum = newSum;
        }
    }

    @Test
    void testSplineSmooth() {
        int smoothRatio = 4;
        fiber.generate();
        ArrayList<Vector> oldPoints = fiber.getPoints();
        fiber.splineSmooth(smoothRatio);
        ArrayList<Vector> newPoints = fiber.getPoints();
        assertEquals(newPoints.size(), (oldPoints.size() - 1) * smoothRatio + 1);
        for (int i = 0; i < newPoints.size(); i++) {
            if (i % smoothRatio == 0) {
                assertEquals(newPoints.get(i), oldPoints.get(i / smoothRatio));
            }
        }
    }

    private double angleChangeSum(Fiber fiber) {
        ArrayList<Vector> deltas = MiscUtility.toDeltas(fiber.getPoints());
        double sum = 0.0;
        for (int i = 0; i < deltas.size() - 1; i++) {
            sum += deltas.get(i).angleWith(deltas.get(i + 1));
        }
        return sum;
    }
}
