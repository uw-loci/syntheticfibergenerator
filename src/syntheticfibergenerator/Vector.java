package syntheticfibergenerator;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


public class Vector extends Vector2D {

    Vector() {
        super(0.0, 0.0);
    }

    Vector(double x, double y) {
        super(x, y);
    }

    private Vector(Vector2D vec) {
        this(vec.getX(), vec.getY());
    }

    /**
     * Throws an exception if the input is a zero vector.
     */
    @Override
    public Vector normalize() {
        return new Vector(super.normalize());
    }

    @Override
    public Vector scalarMultiply(double scalar) {
        return new Vector(super.scalarMultiply(scalar));
    }

    Vector add(Vector2D other) {
        return new Vector(super.add(other));
    }

    Vector subtract(Vector2D other) {
        return new Vector(super.subtract(other));
    }

    /**
     * The behavior for a zero or NaN vector is given in the Math.atan2 specification.
     *
     * @return an angle between -pi and pi
     */
    double theta() {
        return Math.atan2(getY(), getX());
    }

    double angleWith(Vector other) {
        if (this.isZero() || other.isZero()) {
            throw new ArithmeticException("Cannot compute angle between vectors if one is zero");
        }
        double cos = this.normalize().dotProduct(other.normalize());
        cos = Math.min(+1, cos);
        cos = Math.max(-1, cos);
        return Math.acos(cos);
    }

    /**
     * Rotates points counter-clockwise by angle newXAxis.theta()
     */
    Vector rotate(Vector newXAxis) {
        if (newXAxis.isZero()) {
            throw new ArithmeticException("New x-axis must be nonzero");
        }
        newXAxis = newXAxis.normalize();
        Vector newYAxis = new Vector(-newXAxis.getY(), newXAxis.getX());
        Vector xRotated = newXAxis.scalarMultiply(getX());
        Vector yRotated = newYAxis.scalarMultiply(getY());
        return xRotated.add(yRotated);
    }

    private boolean isZero() {
        return getX() == 0 && getY() == 0;
    }
}
