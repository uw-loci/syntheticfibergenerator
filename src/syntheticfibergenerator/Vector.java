package syntheticfibergenerator;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Vector extends Vector2D {

    Vector(double x, double y) {
        super(x, y);
    }

    Vector(Vector2D vec) {
        this(vec.getX(), vec.getY());
    }

    public double angleWith(Vector other) {
        double cos = this.normalize().dotProduct(other.normalize());
        cos = Math.min(+1, cos);
        cos = Math.max(-1, cos);
        return Math.acos(cos);
    }

    public Vector add(Vector2D other) {
        return new Vector(super.add(other));
    }

    public Vector subtract(Vector2D other) {
        return new Vector(super.subtract(other));
    }

    public Vector normalize() {
        return new Vector(super.normalize());
    }

    public Vector scalarMultiply(double scalar) {
        return new Vector(super.scalarMultiply(scalar));
    }

    public Vector rotate(Vector newXAxis) {
        Vector newYAxis = new Vector(-newXAxis.getY(), newXAxis.getX());
        Vector xRotated = newXAxis.scalarMultiply(getX());
        Vector yRotated = newYAxis.scalarMultiply(getY());
        return xRotated.add(yRotated);
    }

    public double theta() {
        return Math.atan2(getY(), getX());
    }
}
