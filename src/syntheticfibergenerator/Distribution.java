package syntheticfibergenerator;

import com.google.gson.*;

import java.lang.reflect.Type;


abstract class Distribution {

    static class Serializer implements JsonDeserializer<Distribution> {

        @Override
        public Distribution deserialize(JsonElement element, Type type, JsonDeserializationContext context)
                throws JsonParseException {
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


    transient double lowerBound;
    transient double upperBound;


    @SuppressWarnings("SameParameterValue")
    void setBounds(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    abstract String getType();

    abstract String getString();

    abstract double sample();

    abstract void setNames();

    abstract void setHints();
}


class Gaussian extends Distribution {

    Param<Double> mean = new Param<>();
    Param<Double> sigma = new Param<>();

    transient static final String typename = "Gaussian";


    Gaussian(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setNames();
        setHints();
    }

    public String getType() {
        return typename;
    }

    public String getString() {
        return String.format(getType() + ": \u03BC=%s, \u03C3=%s", mean.string(), sigma.string());
    }

    /**
     * Note that lowerBound and upperBound are inclusive.
     */
    public double sample() {
        double val;
        do {
            val = RngUtility.rng.nextGaussian() * sigma.value() + mean.value();
        }
        while (val < lowerBound || val > upperBound);
        return val;
    }

    void setNames() {
        mean.setName("mean");
        sigma.setName("sigma");
    }

    void setHints() {
        mean.setHint("Mean of the Gaussian");
        sigma.setHint("Standard deviation of the Gaussian");
    }
}


class Uniform extends Distribution {

    Param<Double> min = new Param<>();
    Param<Double> max = new Param<>();

    transient static final String typename = "Uniform";


    Uniform(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setNames();
        setHints();
    }

    public String getType() {
        return typename;
    }

    public String getString() {
        return String.format(getType() + ": %s-%s", min.string(), max.string());
    }

    public double sample() {
        double trimMin = Math.max(lowerBound, min.value());
        double trimMax = Math.min(upperBound, max.value());
        return RngUtility.randomDouble(trimMin, trimMax);
    }

    void setNames() {
        min.setName("minimum");
        max.setName("maximum");
    }

    void setHints() {
        min.setHint("Minimum of the uniform distribution (inclusive)");
        max.setHint("Maximum of the uniform distribution (inclusive)");
    }
}
