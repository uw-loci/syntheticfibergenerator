import com.google.gson.*;

import java.lang.reflect.Type;


class DistributionDeserializer implements JsonDeserializer<Object>
{
    @Override
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
    {
        JsonObject object = jsonElement.getAsJsonObject();
        String className = object.get("type").getAsString();
        if (className.equals("Gaussian"))
        {
            return jsonDeserializationContext.deserialize(jsonElement, Gaussian.class);
        }
        else
        {
            return jsonDeserializationContext.deserialize(jsonElement, Uniform.class);
        }
    }
}


abstract class Distribution
{
    double lowerBound;
    double upperBound;

    abstract double sample();
}


class Gaussian extends Distribution
{
    final String type = "Gaussian";

    double mean;
    double sigma;


    public Gaussian(double mean, double sigma, double lowerBound, double upperBound)
    {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.mean = mean;
        this.sigma = sigma;
    }


    public Gaussian(double mean, double sigma, Distribution other)
    {
        this(mean, sigma, other.lowerBound, other.upperBound);
    }


    @Override
    public double sample()
    {
        double val;
        do
        {
            val = RandomUtility.RNG.nextGaussian() * sigma + mean;
        }
        while (val < lowerBound || val > upperBound);
        return val;
    }


    @Override
    public String toString()
    {
        return String.format("Gaussian(mean=%f, sigma=%f)", mean, sigma);
    }
}


class Uniform extends Distribution
{
    final String type = "Uniform";

    double min;
    double max;


    Uniform(double min, double max, double lowerBound, double upperBound)
    {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.min = Math.max(min, lowerBound);
        this.max = Math.min(max, upperBound);
    }


    Uniform(double min, double max, Distribution other)
    {
        this(min, max, other.lowerBound, other.upperBound);
    }


    @Override
    public double sample()
    {
        return RandomUtility.getRandomDouble(min, max);
    }


    @Override
    public String toString()
    {
        return String.format("Uniform(min=%f, max=%f)", min, max);
    }
}
