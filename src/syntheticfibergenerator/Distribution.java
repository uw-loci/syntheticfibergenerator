package syntheticfibergenerator;

import com.google.gson.*;

import java.lang.reflect.Type;


class DistributionSerializer implements JsonDeserializer<Distribution> {
    @Override
    public Distribution deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();
        String className = object.get("type").getAsString();
        if (className.equals(Gaussian.typename)) {
            return context.deserialize(element, Gaussian.class);
        } else if (className.equals(Uniform.typename)) {
            return context.deserialize(element, Uniform.class);
        } else {
            throw new JsonParseException("Unknown distribution typename: " + className);
        }
    }
}


abstract class Distribution {
    transient double lowerBound;
    transient double upperBound;

    void setBounds(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    abstract void setNames();

    abstract double sample();

    abstract String getType();
}


class Gaussian extends Distribution {
    // TODO: Move Param to its own class
    Param<Double> mean = new Param<>();
    Param<Double> sdev = new Param<>();

    transient static String typename = "Gaussian";

    Gaussian(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setNames();
    }

    void setNames() {
        mean.setName("mean");
        sdev.setName("sigma");
    }

    public String getType() {
        return typename;
    }

    // TODO: What is the point of this constructor?
    Gaussian(double mean, double sdev, double lowerBound, double upperBound) {
        this(lowerBound, upperBound);
        this.mean.setValue(mean);
        this.sdev.setValue(sdev);
    }

    Gaussian(double mean, double sdev, Distribution other) {
        this(mean, sdev, other.lowerBound, other.upperBound);
    }

    @Override
    public double sample() {
        double val;
        do {
            val = RandomUtility.RNG.nextGaussian() * sdev.getValue() + mean.getValue();
        }
        while (val < lowerBound || val > upperBound);
        return val;
    }

    @Override
    public String toString() {
        return String.format(getType() + ": \u03BC=%s, \u03C3=%s", mean.getString(), sdev.getString());
    }
}


class Uniform extends Distribution {
    Param<Double> min = new Param<>();
    Param<Double> max = new Param<>();

    transient static String typename = "Uniform";

    Uniform(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setNames();
    }


    Uniform(double min, double max, double lowerBound, double upperBound) {
        this(lowerBound, upperBound);
        this.min.setValue(Math.max(min, lowerBound));
        this.max.setValue(Math.min(max, upperBound));
    }

    void setNames() {
        min.setName("minimum");
        max.setName("maximum");
    }


    Uniform(double min, double max, Distribution other) {
        this(min, max, other.lowerBound, other.upperBound);
    }


    public String getType() {
        return typename;
    }


    @Override
    public double sample() {
        return RandomUtility.getRandomDouble(min.getValue(), max.getValue());
    }


    @Override
    public String toString() {
        return String.format(getType() + ": %s-%s", min.getString(), max.getString());
    }
}
