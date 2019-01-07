package syntheticfibergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class CircleTest {

    @BeforeEach
    void setUp() {
        RngUtility.rng = new Random(1);
    }

    @Test
    void testContains() {
        Circle circle = new Circle(new Vector(1, 2), 0.5);
        assertTrue(circle.contains(new Vector(1, 1.75)));
        assertFalse(circle.contains(new Vector(0.5, 1.5)));
    }

    @Test
    void testEquals() {
        Circle circle1 = new Circle(new Vector(0, 1.23), 4.56);
        Circle circle2 = new Circle(new Vector(0, 1.23), 4.56);
        Circle circle3 = new Circle(new Vector(0, 1.22), 4.56);
        assertEquals(circle1, circle2);
        assertNotEquals(circle1, circle3);
    }

    @Test
    void testCircleCircleIntersect() {
        Circle circle1 = new Circle(new Vector(0.0, 0.0), 10.0);
        Circle circle2 = new Circle(new Vector(0.0, 14.0), 8.0);
        Vector[] intersects = Circle.circleCircleIntersect(circle1, circle2);
        assertEquals(circle1.getCenter().distance(intersects[0]), circle1.getRadius(), 1e-6);
        assertEquals(circle1.getCenter().distance(intersects[1]), circle1.getRadius(), 1e-6);
        assertEquals(circle2.getCenter().distance(intersects[0]), circle2.getRadius(), 1e-6);
        assertEquals(circle2.getCenter().distance(intersects[1]), circle2.getRadius(), 1e-6);
    }

    @Test
    void testDiskCircleIntersect() {
        Circle disk = new Circle(new Vector(0.0, 0.0), 10.0);
        Circle circle = new Circle(new Vector(0.0, 14.0), 8.0);
        for (int i = 0; i < 100; i++) {
            Vector intersect = Circle.diskCircleIntersect(disk, circle);
            assertTrue(disk.contains(intersect));
            assertEquals(circle.getCenter().distance(intersect), circle.getRadius(), 1e-6);
        }
    }

    @Test
    void testDiskDiskIntersect() {
        Circle disk1 = new Circle(new Vector(0.0, 0.0), 10.0);
        Circle disk2 = new Circle(new Vector(0.0, 14.0), 8.0);
        for (int i = 0; i < 100; i++) {
            Vector intersect = Circle.diskDiskIntersect(disk1, disk2);
            assertTrue(disk1.contains(intersect));
            assertTrue(disk2.contains(intersect));
        }
    }
}