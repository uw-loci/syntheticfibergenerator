package syntheticfibergenerator; // TODO: Cleaned up

import com.google.gson.*;

import java.lang.reflect.Type;


abstract class Distribution {

    static class Serializer implements JsonDeserializer<Distribution> {

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


    transient double lowerBound;
    transient double upperBound;


    @SuppressWarnings("SameParameterValue")
    void setBounds(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    abstract String getType();

    abstract String getString();

    abstract void setNames();

    abstract double sample();
}


class Gaussian extends Distribution {

    Param<Double> mean = new Param<>();
    Param<Double> sigma = new Param<>();

    transient static final String typename = "Gaussian";


    Gaussian(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setNames();
    }

    public String getType() {
        return typename;
    }

    public String getString() {
        return String.format(getType() + ": \u03BC=%s, \u03C3=%s", mean.getString(), sigma.getString());
    }

    void setNames() {
        mean.setName("mean");
        sigma.setName("sigma");
    }

    public double sample() {
        double val;
        do {
            val = RandomUtility.RNG.nextGaussian() * sigma.getValue() + mean.getValue();
        }
        while (val < lowerBound || val > upperBound);
        return val;
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
    }

    public String getType() {
        return typename;
    }

    public String getString() {
        return String.format(getType() + ": %s-%s", min.getString(), max.getString());
    }

    void setNames() {
        min.setName("minimum");
        max.setName("maximum");
    }

    public double sample() {
        return RandomUtility.getRandomDouble(min.getValue(), max.getValue());
    }
}
