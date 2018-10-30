import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


public class Circle
{
    private Vector2D center;
    private double radius;


    Circle(Vector2D center, double radius)
    {
        this.center = center;
        this.radius = radius;
    }


    private boolean contains(Vector2D point)
    {
        return sq(point.getX() - center.getX()) + sq(point.getY() - center.getY()) <= sq(radius);
    }


    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Circle))
        {
            return false;
        }
        Circle otherCircle = (Circle) other;
        return this.center.equals(otherCircle.center) && (this.radius == otherCircle.radius);
    }


    /**
     * Random choice
     *
     * @param circle1
     * @param circle2
     * @return
     */
    static Vector2D circleCircleIntersect(Circle circle1, Circle circle2)
    {
        double d = circle2.center.subtract(circle1.center).getNorm();

        // Check that there are exactly two intersection points
        boolean nested = d < Math.abs(circle1.radius - circle2.radius);
        boolean tooDistant = d > circle1.radius + circle2.radius;
        if (circle1.equals(circle2) || nested || tooDistant)
        {
            throw new IllegalArgumentException("Circles do not intersect");
        }

        // a: distance to the center of the lens, h: distance from axis to intersection point
        double a = (sq(circle1.radius) - sq(circle2.radius) + sq(d)) / (2 * d);
        double h = Math.sqrt(sq(circle1.radius) - sq(a));

        Vector2D axis = circle2.center.subtract(circle1.center);
        Vector2D intersect1 = rotateVector(new Vector2D(a, h), axis);
        Vector2D intersect2 = rotateVector(new Vector2D(a, -h), axis);

        return RandomUtility.rng.nextBoolean() ? intersect1 : intersect2;
    }


    /**
     * Random choice
     *
     * @param disk
     * @param circle
     * @return
     */
    static Vector2D diskCircleIntersect(Circle disk, Circle circle)
    {
        Vector2D intersect = circleCircleIntersect(disk, circle);

        // Axis points from the center of "disk" to the center of "circle"
        Vector2D axisDir = circle.center.subtract(disk.center).normalize();
        double axisAngle = Math.atan2(axisDir.getX(), axisDir.getY());

        // Determine the range of angles to use
        Vector2D intersectDir = intersect.subtract(disk.center).normalize();
        double width = Math.acos(axisDir.dotProduct(intersectDir));
        double minAngle = axisAngle - width;
        double maxAngle = axisAngle + width;

        // Choose a random point between minAngle and maxAngle on "disk"
        double randAngle = RandomUtility.getRandomDouble(minAngle, maxAngle);
        Vector2D randDir = new Vector2D(Math.cos(randAngle), Math.sin(randAngle));
        return disk.center.add(randDir.scalarMultiply(disk.radius));
    }


    /**
     * Random choice. Repeatedly generates points in a box bounding the "lens" (intersection of
     * the two circles) until one is found which is within both circles.
     *
     * @param disk1
     * @param disk2
     * @return
     */
    static Vector2D diskDiskIntersect(Circle disk1, Circle disk2)
    {
        // Axis points from the center of "filled1" to the center of "filled2"
        Vector2D axisDir = disk2.center.subtract(disk1.center).normalize();

        // Determine the distance from the axis to the intersect points
        Vector2D intersect = circleCircleIntersect(disk1, disk2);
        Vector2D intersectShift = (intersect.subtract(disk1.center));
        Vector2D parallelComp = axisDir.scalarMultiply(intersectShift.dotProduct(axisDir));

        // The dimensions of our "lens" bounding box
        double boxHeight = intersectShift.subtract(parallelComp).getNorm() * 2.0;
        double boxWidth = disk1.radius + disk2.radius - disk1.center.distance(disk2.center);

        // Generate outputs until we find a good one
        Vector2D point;
        do
        {
            double xRand = RandomUtility.getRandomDouble(0.0, boxWidth);
            double yRand = RandomUtility.getRandomDouble(0.0, boxHeight);

            Vector2D displacement = rotateVector(new Vector2D(xRand, yRand), axisDir);
            point = disk1.center.add(displacement);

            if (disk1.contains(point) && disk2.contains(point))
            {
                break;
            }
        }
        while (!disk1.contains(point) || !disk2.contains(point));
        return point;
    }


    private static Vector2D rotateVector(Vector2D vector, Vector2D newXAxis)
    {
        Vector2D newYAxis = new Vector2D(-newXAxis.getY(), newXAxis.getX());
        Vector2D xRotated = newXAxis.scalarMultiply(vector.getX());
        Vector2D yRotated = newYAxis.scalarMultiply(vector.getY());
        return xRotated.add(yRotated);
    }


    private static double sq(double val)
    {
        return val * val;
    }
}
