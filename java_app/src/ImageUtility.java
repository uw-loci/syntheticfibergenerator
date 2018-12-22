import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;


class ImageUtility {

    private static final int DIST_SEARCH_STEP = 4;

    static BufferedImage scale(BufferedImage image, double scale, int interpolationType) {
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(transform, interpolationType);
        return scaleOp.filter(image, null);
    }


    private static double sq(double x) {
        return x * x;
    }


    private static int sq(int x) {
        return x * x;
    }


    static BufferedImage gaussianBlur(BufferedImage image, double radius) {
        int kernelSize = (int) Math.ceil(radius);
        kernelSize -= kernelSize % 2 - 1;
        double sigma = radius / 3.0;
        float[] weightMatrix = new float[sq(kernelSize)];
        int center = kernelSize / 2;
        double normConst = 0.0;
        for (int i = 0; i < kernelSize; i++) {
            for (int j = 0; j < kernelSize; j++) {
                double gauss = Math.exp(-(sq(i - center) + sq(j - center)) / (2 * sq(sigma)));
                weightMatrix[i + j * kernelSize] = (float) gauss;
                normConst += gauss;
            }
        }

        for (int i = 0; i < weightMatrix.length; i++) {
            weightMatrix[i] /= normConst;
        }

        // TODO: pad the edge of the input image with alpha=0 pixels
        Kernel kernel = new Kernel(kernelSize, kernelSize, weightMatrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        op.filter(image, output);
        return output;
    }

    /**
     * Expects a 4-byte grayscale image as input. Outputs a 4-byte greyscale image.
     * Each output pixel's value is equal to falloff times the Euclidian distance to the closest background pixel.
     *
     * @param input
     * @param falloff
     * @return
     */
    static BufferedImage distanceFunction(BufferedImage input, double falloff) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        // TODO: possibly use something other than writable raster
        WritableRaster inRaster = input.getRaster();
        WritableRaster outRaster = output.getRaster();
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                int[] pixel = new int[1];
                inRaster.getPixel(x, y, pixel);
                if (pixel[0] == 0) {
                    outRaster.setPixel(x, y, pixel);
                } else {
                    int maxR = (int) Math.sqrt(sq(input.getWidth()) + sq(input.getHeight())) + 1;
                    for (int r = DIST_SEARCH_STEP; r < maxR; r += DIST_SEARCH_STEP) {
                        // TODO: possibly use 2-element int array to clean this up
                        int xMin = Math.max(0, x - r);
                        int xMax = Math.min(input.getWidth(), x + r);
                        int yMin = Math.max(0, y - r);
                        int yMax = Math.min(input.getHeight(), y + r);
                        int bestX = -1;
                        int bestY = -1;
                        double minDist = Double.POSITIVE_INFINITY;
                        for (int yIn = yMin; yIn < yMax; yIn++) {
                            for (int xIn = xMin; xIn < xMax; xIn++) {
                                int[] inPixel = new int[1];
                                inRaster.getPixel(xIn, yIn, inPixel);
                                if (inPixel[0] > 0) {
                                    continue;
                                }
                                double dist = Math.sqrt(sq(xIn - x) + sq(yIn - y));
                                if (dist <= r && dist < minDist) {
                                    minDist = dist;
                                    bestX = xIn;
                                    bestY = yIn;
                                }
                            }
                        }
                        int[] outPixel = new int[1];
                        if (bestX != -1 && bestY != -1) {
                            outPixel[0] = Math.min(255, (int) (minDist * falloff));
                        } else {
                            outPixel[0] = 255;
                        }
                        outRaster.setPixel(x, y, outPixel);
                    }
                }
            }
        }
        return output;
    }
}
