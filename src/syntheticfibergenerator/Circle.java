package syntheticfibergenerator;


public class Circle {

    /* Sometimes we'll have two circles that should be touching but are actually some very small distance apart because
     * of floating-point limitations. In this case we can try widening both by some small amount BUFF. */
    private static final double BUFF = 1e-10;

    private Vector center;
    private double radius;


    Circle(Vector center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    /**
     * Note that Vector instances are guaranteed to be immutable.
     */
    Vector center() {
        return center;
    }

    double radius() {
        return radius;
    }

    boolean contains(Vector point) {
        return center.subtract(point).getNorm() <= radius + BUFF;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Circle)) {
            return false;
        }
        Circle circle = (Circle) other;
        return this.center.equals(circle.center) && (this.radius == circle.radius);
    }

    private Vector choosePoint(double minTheta, double maxTheta) {
        double theta = RngUtility.nextDouble(minTheta, maxTheta);
        Vector dir = new Vector(Math.cos(theta), Math.sin(theta));
        return center.add(dir.scalarMultiply(radius));
    }

    /**
     * Returns two points representing the intersection of the two circles. In the degenerate case, these two points are
     * the same.
     */
    static Vector[] circleCircleIntersect(Circle circle1, Circle circle2) throws ArithmeticException {

        // Enlarge the circles slightly if they're too far away to intersect
        double d = circle1.center.distance(circle2.center);
        double space = d - circle1.radius - circle2.radius;
        if (space > 0) {
            circle1 = new Circle(circle1.center, circle1.radius + BUFF);
            circle2 = new Circle(circle2.center, circle2.radius + BUFF);
            space -= 2 * BUFF;
        }

        // Check that the circles (possibly enlarged) intersect
        boolean nested = d < Math.abs(circle1.radius - circle2.radius);
        if (circle1.equals(circle2) || nested || space > 0) {
            throw new ArithmeticException("Circles do not intersect");
        }

        // a: distance to the center of the lens, h: distance from axis to intersection point
        double a = (MiscUtility.sq(circle1.radius) - MiscUtility.sq(circle2.radius) + MiscUtility.sq(d)) / (2 * d);
        double h = Math.sqrt(MiscUtility.sq(circle1.radius) - MiscUtility.sq(a));

        // Return both intersection points
        Vector axis = circle2.center.subtract(circle1.center).normalize();
        @SuppressWarnings("UnnecessaryLocalVariable")
        Vector[] points = {
                circle1.center.add(new Vector(a, h).rotate(axis)),
                circle1.center.add(new Vector(a, -h).rotate(axis))};
        return points;
    }

    static Vector diskCircleIntersect(Circle disk, Circle circle) throws ArithmeticException {

        // Check the special case where the circle is entirely within the disk
        double d = disk.center.distance(circle.center);
        if (d < disk.radius - circle.radius) {
            return circle.choosePoint(-Math.PI, Math.PI);
        }

        // Determine the range of valid angles on the circle's border
        Vector axis = disk.center.subtract(circle.center).normalize();
        Vector[] points = circleCircleIntersect(disk, circle);
        double delta = axis.angleWith(points[0].subtract(circle.center));

        return circle.choosePoint(axis.theta() - delta, axis.theta() + delta);
    }

    /**
     * Random choice. Repeatedly generates points in a box bounding the "lens" (intersection of the two circles) until
     * one is found which is within both circles.
     */
    static Vector diskDiskIntersect(Circle disk1, Circle disk2) throws ArithmeticException {

        // Check the special case where the disks are nested
        double d = disk1.center.distance(disk2.center);
        if (d < Math.abs(disk1.radius - disk2.radius)) {
            Circle inner = disk1.radius < disk2.radius ? disk1 : disk2;
            double xMin = inner.center.getX() - inner.radius;
            double xMax = inner.center.getX() + inner.radius;
            double yMin = inner.center.getY() - inner.radius;
            double yMax = inner.center.getY() + inner.radius;

            Vector result;
            do {
                result = RngUtility.nextPoint(xMin, xMax, yMin, yMax);
            } while (!inner.contains(result));
            return result;
        }

        // Determine the dimensions of the "lens" bounding box
        Vector[] points = circleCircleIntersect(disk1, disk2);
        double boxHeight = points[0].subtract(points[1]).getNorm();
        double boxLeft = Math.min(d - disk2.radius, disk1.radius);
        double boxRight = Math.max(d - disk2.radius, disk1.radius);

        Vector axis = disk2.center.subtract(disk1.center).normalize();
        Vector result;
        do {
            Vector delta = RngUtility.nextPoint(boxLeft, boxRight, -boxHeight, boxHeight);
            result = disk1.center.add(delta.rotate(axis));
        } while (!disk1.contains(result) || !disk2.contains(result));
        return result;
    }
}
