package syntheticfibergenerator;

import com.google.gson.*;

import java.lang.reflect.Type;


class DistributionSerializer implements JsonDeserializer<Distribution> {
    @Override
    public Distribution deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String className = object.get("type").getAsString();
        if (className.equals(Gaussian.typename)) {
            return jsonDeserializationContext.deserialize(jsonElement, Gaussian.class);
        } else if (className.equals(Uniform.typename)) {
            return jsonDeserializationContext.deserialize(jsonElement, Uniform.class);
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

    abstract double sample();

    abstract String getType();
}


class Gaussian extends Distribution {
    double mean;
    double sdev;

    transient static String typename = "Gaussian";
    String type = "Gaussian";

    public Gaussian(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.mean = Double.NaN;
        this.sdev = Double.NaN;
    }

    public String getType() {
        return typename;
    }


    public Gaussian(double mean, double sdev, double lowerBound, double upperBound) {
        this(lowerBound, upperBound);
        this.mean = mean;
        this.sdev = sdev;
    }


    public Gaussian(double mean, double sdev, Distribution other) {
        this(mean, sdev, other.lowerBound, other.upperBound);
    }


    @Override
    public double sample() {
        double val;
        do {
            val = RandomUtility.RNG.nextGaussian() * sdev + mean;
        }
        while (val < lowerBound || val > upperBound);
        return val;
    }


    @Override
    public String toString() {
        return String.format(getType() + ": \u03BC=%s, \u03C3=%s", Double.toString(mean), Double.toString(sdev));
    }
}


class Uniform extends Distribution {
    double min;
    double max;

    transient static String typename = "Uniform";
    String type = "Uniform";

    Uniform(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.min = Double.NaN;
        this.max = Double.NaN;
    }


    Uniform(double min, double max, double lowerBound, double upperBound) {
        this(lowerBound, upperBound);
        this.min = Math.max(min, lowerBound);
        this.max = Math.min(max, upperBound);
    }


    Uniform(double min, double max, Distribution other) {
        this(min, max, other.lowerBound, other.upperBound);
    }


    public String getType() {
        return typename;
    }


    @Override
    public double sample() {
        return RandomUtility.getRandomDouble(min, max);
    }


    @Override
    public String toString() {
        return String.format(getType() + ": %s-%s", Double.toString(min), Double.toString(max));
    }
}
