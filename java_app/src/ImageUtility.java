import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;


class ImageUtility
{
    static BufferedImage scale(BufferedImage image, double scale, int interpolationType)
    {
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(transform, interpolationType);
        return scaleOp.filter(image, null);
    }


    private static double sq(double x)
    {
        return x * x;
    }


    private static int sq(int x)
    {
        return x * x;
    }


    static BufferedImage gaussianBlur(BufferedImage image, double radius)
    {
        int kernelSize = (int) Math.ceil(radius);
        kernelSize -= kernelSize % 2 - 1;
        double sigma = radius / 3.0;
        float[] weightMatrix = new float[sq(kernelSize)];
        int center = kernelSize / 2;
        double normConst = 0.0;
        for (int i = 0; i < kernelSize; i++)
        {
            for (int j = 0; j < kernelSize; j++)
            {
                double gauss = Math.exp(-(sq(i - center) + sq(j - center)) / (2 * sq(sigma)));
                weightMatrix[i + j * kernelSize] = (float) gauss;
                normConst += gauss;
            }
        }

        for (int i = 0; i < weightMatrix.length; i++)
        {
            weightMatrix[i] /= normConst;
        }

        // TODO: pad the edge of the input image with alpha=0 pixels
        Kernel kernel = new Kernel(kernelSize, kernelSize, weightMatrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        op.filter(image, output);
        return output;
    }
}
