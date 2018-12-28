package syntheticfibergenerator;

public class Circle
{
    /* Sometimes we'll have two circles that should be touching but are actually some very small
     * distance apart because of floating-point limitations. In this case we can try widening both
     * by some small amount. */
    private static final double BUFF = 1e-10;

    Vector center;
    double radius;


    public Circle(Vector center, double radius)
    {
        this.center = center;
        this.radius = radius;
    }

    public Vector getCenter() {
        // Vector2D instances are guaranteed to be immutable
        return center;
    }

    public double getRadius() {
        return radius;
    }


    private boolean contains(Vector point)
    {
        return sq(point.getX() - center.getX()) + sq(point.getY() - center.getY()) <= sq(radius + BUFF);
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
    public static Vector circleCircleIntersect(Circle circle1, Circle circle2)
    {
        double d = circle2.center.subtract(circle1.center).getNorm();

        // Check that there are exactly two intersection points
        boolean nested = d < Math.abs(circle1.radius - circle2.radius);

        // Correct small floating-point errors
        double space = d - circle1.radius - circle2.radius;
        if (space > 0)
        {
            circle1.radius += BUFF;
            circle2.radius += BUFF;
            space = d - circle1.radius - circle2.radius;
        }

        if (circle1.equals(circle2) || nested || space > 0)
        {
            throw new IllegalArgumentException("Circles do not intersect");
        }

        // a: distance to the center of the lens, h: distance from axis to intersection point
        double a = (sq(circle1.radius) - sq(circle2.radius) + sq(d)) / (2 * d);
        double h = Math.sqrt(sq(circle1.radius) - sq(a));

        Vector axis = circle2.center.subtract(circle1.center).normalize();
        Vector intersect1 = circle1.center.add(new Vector(a, h).rotate(axis));
        Vector intersect2 = circle1.center.add(new Vector(a, -h).rotate(axis));

        return RandomUtility.RNG.nextBoolean() ? intersect1 : intersect2;
    }


    /**
     * Random choice
     *
     * @param disk
     * @param circle
     * @return
     */
    public static Vector diskCircleIntersect(Circle disk, Circle circle)
    {
        // Check whether the circle is within the disk (the other way around is invalid)
        double d = disk.center.distance(circle.center);
        if (d < disk.radius - circle.radius)
        {
            double randAngle = RandomUtility.getRandomDouble(-Math.PI, Math.PI);
            return circle.center.add(new Vector(Math.cos(randAngle), Math.sin(randAngle)));
        }
        else
        {
            Vector intersect = circleCircleIntersect(disk, circle);

            // Axis points from the center of "circle" to the center of "disk"
            Vector axisDir = disk.center.subtract(circle.center).normalize();
            double axisAngle = Math.atan2(axisDir.getY(), axisDir.getX());

            // Determine the range of angles to use
            Vector intersectDir = intersect.subtract(circle.center).normalize();

            // Choose a random point between minAngle and maxAngle on the circle
            double width = axisDir.angleWith(intersectDir);
            double minAngle = axisAngle - width;
            double maxAngle = axisAngle + width;
            double randAngle = RandomUtility.getRandomDouble(minAngle, maxAngle);
            Vector randDir = new Vector(Math.cos(randAngle), Math.sin(randAngle));
            return circle.center.add(randDir.scalarMultiply(circle.radius));
        }
    }


    /**
     * Random choice. Repeatedly generates points in a box bounding the "lens" (intersection of
     * the two circles) until one is found which is within both circles.
     *
     * @param disk1
     * @param disk2
     * @return
     */
    static Vector diskDiskIntersect(Circle disk1, Circle disk2)
    {
        // Check whether the disks are nested
        double d = disk1.center.distance(disk2.center);
        if (d < Math.abs(disk1.radius - disk2.radius))
        {
            Circle inner = disk1.radius < disk2.radius ? disk1 : disk2;
            double xMin = inner.center.getX() + inner.radius;
            double xMax = inner.center.getX() - inner.radius;
            double yMin = inner.center.getY() + inner.radius;
            double yMax = inner.center.getY() - inner.radius;

            Vector point;
            do
            {
                double xRand = RandomUtility.getRandomDouble(xMin, xMax);
                double yRand = RandomUtility.getRandomDouble(yMin, yMax);
                point = new Vector(xRand, yRand);
            }
            while (!inner.contains(point));
            return point;
        }
        else
        {
            // Axis points from the center of "filled1" to the center of "filled2"
            Vector axisDir = disk2.center.subtract(disk1.center).normalize();

            // Determine the distance from the axis to the intersect points
            Vector intersect = circleCircleIntersect(disk1, disk2);
            Vector intersectShift = (intersect.subtract(disk1.center));
            Vector parallelComp = axisDir.scalarMultiply(intersectShift.dotProduct(axisDir));

            // The dimensions of our "lens" bounding box
            double boxHeight = intersectShift.subtract(parallelComp).getNorm();
            double boxLeft = d - disk2.radius;
            double boxRight = disk1.radius;

            // Generate outputs until we find a good one
            Vector point;
            do
            {
                double xRand = RandomUtility.getRandomDouble(boxLeft, boxRight);
                double yRand = RandomUtility.getRandomDouble(-boxHeight, boxHeight);

                Vector displacement = new Vector(xRand, yRand).rotate(axisDir);
                point = disk1.center.add(displacement);
            }
            while (!disk1.contains(point) || !disk2.contains(point));
            return point;
        }
    }


    private static double sq(double val)
    {
        return val * val;
    }
}
