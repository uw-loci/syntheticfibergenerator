import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class FiberImageTest
{
    @Test
    void testGenerateDirections()
    {
//        fiberphantom.FiberImageParams params = new fiberphantom.FiberImageParams();
//        params.nFibers = 100;
//        params.meanLength = 50;
//        params.minLength = 40;
//        params.maxLength = 60;
//        params.segmentLength = 5.0;
//        params.meanStraightness = 0.8;
//        params.minStraightness = 0.7;
//        params.maxStraightness = 1.0;
//        params.alignment = 0.8;
//        params.meanAngle = 0.113;
//        params.imageWidth = 1000;
//        params.imageHeight = 1000;
//        fiberphantom.FiberImage collection = new fiberphantom.FiberImage(params);
//        collection.generateFibers();
//
//        Vector2D complexMean = new Vector2D(0.0, 0.0);
//        for (Fiber fiber : collection)
//        {
//            Vector2D direction = fiber.params.end.subtract(fiber.params.start);
//            double theta = Math.atan2(direction.getY(), direction.getX()) * 2.0;
//            Vector2D complexVal = new Vector2D(Math.cos(theta), Math.sin(theta));
//            complexMean = complexMean.add(complexVal);
//        }
//        complexMean = complexMean.scalarMultiply(1.0 / params.nFibers);
//        double angle = Math.atan2(complexMean.getY(), complexMean.getX()) / 2.0;
//
//        assertEquals(params.alignment, complexMean.getNorm(), 1e-6);
//        assertEquals(params.meanAngle, angle, 1e-6);
    }
}
