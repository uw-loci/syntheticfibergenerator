import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class CircleTest
{
    @Test
    void circleCircleIntersect()
    {
        Circle circle1 = new Circle(new Vector2D(0.0, 0.0), 10.0);
        Circle circle2 = new Circle(new Vector2D(0.0, 14.0), 8.0);
        Vector2D intersect = Circle.circleCircleIntersect(circle1, circle2);
        assertEquals(circle1.center.distance(intersect), circle1.radius, 1e-6);
        assertEquals(circle2.center.distance(intersect), circle2.radius, 1e-6);
    }

    @Test
    void diskCircleIntersect()
    {
        Circle disk = new Circle(new Vector2D(0.0, 0.0), 10.0);
        Circle circle = new Circle(new Vector2D(0.0, 14.0), 8.0);
        Vector2D intersect = Circle.diskCircleIntersect(disk, circle);
        assertTrue(disk.center.distance(intersect) < disk.radius);
        assertEquals(circle.center.distance(intersect), circle.radius, 1e-6);
    }

    @Test
    void diskDiskIntersect()
    {
        fail("Not implemented");
    }
}
