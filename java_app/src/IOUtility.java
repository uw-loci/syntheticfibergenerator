import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;


class IOUtility
{
    static int tryParseInt(String text) throws NumberFormatException
    {
        try
        {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException exception)
        {
            throw new NumberFormatException("Parse error: " + text + " is not a valid integer");
        }
    }


    static double tryParseDouble(String text) throws NumberFormatException
    {
        try
        {
            return Double.parseDouble(text);
        }
        catch (NumberFormatException exception)
        {
            throw new NumberFormatException("Parse error: " + text + " is not a valid decimal number");
        }
    }


    static void verifyValue(int value, int min, int max) throws IllegalArgumentException
    {
        if (value < min || value > max)
        {
            throw new IllegalArgumentException("Invalid argument: " + value + " is not between " + min + " and " + max);
        }
    }


    static void verifyValue(double value, double min, double max) throws IllegalArgumentException
    {
        if (value < min || value > max)
        {
            throw new IllegalArgumentException("Invalid argument: " + value + " is not between " + min + " and " + max);
        }
    }


    static void saveImage(BufferedImage image, String name) throws IOException
    {
        try
        {
            File outFile = new File(name);
            ImageIO.write(image, "png", outFile);
        }
        catch (IOException exception)
        {
            throw new IOException("Unable to write to image file " + name);
        }
    }


    static void saveData(FiberCollection fibers, String name) throws IOException
    {
        try
        {
            FileWriter writer = new FileWriter(name);
            writer.write(fibers.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException exception)
        {
            throw new IOException("Unable to write to data file" + name);
        }
    }
}
