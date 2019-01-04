package syntheticfibergenerator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class CircleTest
{
    @Test
    void testCircleCircleIntersect()
    {
        Circle circle1 = new Circle(new Vector(0.0, 0.0), 10.0);
        Circle circle2 = new Circle(new Vector(0.0, 14.0), 8.0);
        Vector intersect = Circle.circleCircleIntersect(circle1, circle2)[0];
        assertEquals(circle1.getCenter().distance(intersect), circle1.getRadius(), 1e-6);
        assertEquals(circle2.getCenter().distance(intersect), circle2.getRadius(), 1e-6);
    }

    @Test
    void testDiskCircleIntersect()
    {
        Circle disk = new Circle(new Vector(0.0, 0.0), 10.0);
        Circle circle = new Circle(new Vector(0.0, 14.0), 8.0);
        Vector intersect = Circle.diskCircleIntersect(disk, circle);
        assertTrue(disk.getCenter().distance(intersect) < disk.getRadius());
        assertEquals(circle.getCenter().distance(intersect), circle.getRadius(), 1e-6);
    }

    @Test
    void testDiskDiskIntersect()
    {
        fail("Not implemented");
    }
}
