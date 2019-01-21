/*
 * Written for the Laboratory for Optical and Computational Instrumentation, UW-Madison
 *
 * Author: Matthew Dutson
 * Email: dutson@wisc.edu, mattdutson@icloud.com
 * GitHub: https://github.com/uw-loci/syntheticfibergenerator
 *
 * Copyright (c) 2019, Board of Regents of the University of Wisconsin-Madison
 */

package syntheticfibergenerator;

import com.google.gson.*;

import java.lang.reflect.Type;


/**
 * Abstract class representing a distribution over the real numbers with fixed bounds {@code lowerBound} and {@code
 * upperBound}.
 */
abstract class Distribution {

    /**
     * Determines the context for the JSON deserialization of a {@code Distribution} based on the {@code "type"} field.
     */
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

    /* lowerBound and upperBound are tagged as transient (non-serializable), because modifying them outside the source
     * can result in undefined behavior. */

    // The minimum possible for values sampled from this distribution
    transient double lowerBound;

    // The maximum possible fro values sampled from this distribution
    transient double upperBound;


    /**
     * @param lowerBound The lower bound for this distribution
     * @param upperBound The upper bound for this distribution
     */
    @SuppressWarnings("SameParameterValue")
    void setBounds(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * @return The type of the distribution (e.g. "Gaussian" or "Uniform")
     */
    abstract String getType();

    /**
     * @return A string representation of this distribution for displaying to the user
     */
    abstract String getString();

    /**
     * @return A random value sampled from this distribution
     */
    abstract double sample();

    /**
     * This isn't part of the constructor because Distribution objects are often constructed during their
     * deserialization from JSON and the JSON representation doesn't contain names.
     */
    abstract void setNames();

    /**
     * Not part of the constructor for the same reason as setNames().
     */
    abstract void setHints();
}

/**
 * A normal/Gaussian distribution with some mean and standard deviation. The mean is not required to lie within the
 * range {@code (lowerBound, upperBound)} - if it doesn't we just sample from one of the tails.
 */
class Gaussian extends Distribution {

    // The mean/peak of the normal distribution
    Param<Double> mean = new Param<>();

    // The standard deviation of the normal distribution
    Param<Double> sigma = new Param<>();

    // To see whether a distribution is Gaussian use distribution.getType().equals(Gaussian.typename)
    transient static final String typename = "Gaussian";


    /**
     * @param lowerBound The lower bound for this distribution
     * @param upperBound The upper bound for this distribution
     */
    Gaussian(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setNames();
        setHints();
    }

    /**
     * @return "Uniform"
     */
    public String getType() {
        return typename;
    }

    /**
     * @return The string "Gaussian: mean=val, sigma=val"
     */
    public String getString() {
        return String.format(getType() + ": \u03BC=%s, \u03C3=%s", mean.string(), sigma.string());
    }

    /**
     * Note that this may need to sample many times if the range {@code (lowerBound, upperBound)} is in one of the
     * extreme tails of the distribution.
     *
     * @return A value sampled from this distribution between lowerBound (inclusive) and upperBound (exclusive)
     */
    public double sample() {
        double val;
        do {
            val = RngUtility.rng.nextGaussian() * sigma.value() + mean.value();
        }
        while (val < lowerBound || val > upperBound);
        return val;
    }

    /**
     * See notes for {@code Distribution.setNames()}.
     */
    void setNames() {
        mean.setName("mean");
        sigma.setName("sigma");
    }

    /**
     * See notes for {@code Distribution.setHints()}.
     */
    void setHints() {
        mean.setHint("Mean of the Gaussian");
        sigma.setHint("Standard deviation of the Gaussian");
    }
}

/**
 * A flat/uniform distribution with some min and max. If the min is less than {@code lowerBound} or the max is greater
 * than {@code upperBound} they are automatically adjusted when {@code sample} is called.
 */
class Uniform extends Distribution {

    // The minimum of the uniform distribution
    Param<Double> min = new Param<>();

    // The maximum of the uniform distribution
    Param<Double> max = new Param<>();

    // To see whether a distribution is uniform use distribution.getType().equals(Uniform.typename)
    transient static final String typename = "Uniform";


    /**
     * @param lowerBound The lower bound for this distribution
     * @param upperBound The upper bound for this distribution
     */
    Uniform(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setNames();
        setHints();
    }

    /**
     * @return "Uniform"
     */
    public String getType() {
        return typename;
    }

    /**
     * @return The string "Uniform: min-max"
     */
    public String getString() {
        return String.format(getType() + ": %s-%s", min.string(), max.string());
    }

    /**
     * @return A value between Math.max(lowerBound, min) inclusive and Math.min(upperBound, max) exclusive
     */
    public double sample() {
        double trimMin = Math.max(lowerBound, min.value());
        double trimMax = Math.min(upperBound, max.value());
        return RngUtility.nextDouble(trimMin, trimMax);
    }

    /**
     * See notes for {@code Distribution.setNames()}.
     */
    void setNames() {
        min.setName("minimum");
        max.setName("maximum");
    }

    /**
     * See notes for {@code Distribution.setHints()}.
     */
    void setHints() {
        min.setHint("Minimum of the uniform distribution (inclusive)");
        max.setHint("Maximum of the uniform distribution (inclusive)");
    }
}
