package syntheticfibergenerator; // TODO: Cleaned up

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;


class ImageUtility {

    private static final int DIST_SEARCH_STEP = 4;


    /**
     * Expects a 4-byte grey scale image as input. Outputs a 4-byte grey scale image. Each output pixel's param is equal
     * to "falloff" times the 2-norm distance to the closest background pixel.
     */
    static BufferedImage distanceFunction(BufferedImage image, double falloff) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Raster inRaster = image.getRaster();
        WritableRaster outRaster = output.getRaster();
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                if (getPixel(inRaster, x, y) == 0) {
                    setPixel(outRaster, x, y, 0);
                    continue;
                }
                double minDist = backgroundDist(inRaster, x, y);
                int outValue = 255;
                if (minDist > 0) {
                    outValue = Math.min(255, (int) (minDist * falloff));
                }
                setPixel(outRaster, x, y, outValue);
            }
        }
        return output;
    }

    static BufferedImage gaussianBlur(BufferedImage image, double radius) {
        Kernel kernel = gaussianKernel(radius);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        int pad = kernel.getWidth() / 2;
        BufferedImage padded = zeroPad(image, pad);
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        op.filter(padded, output);
        return output;
    }

    static BufferedImage scale(BufferedImage image, double ratio, int interpolationType) {
        AffineTransform transform = new AffineTransform();
        transform.scale(ratio, ratio);
        AffineTransformOp scaleOp = new AffineTransformOp(transform, interpolationType);
        return scaleOp.filter(image, null);
    }

    private static double backgroundDist(Raster raster, int x, int y) {
        int rMax = (int) Math.sqrt(Utility.sq(raster.getWidth()) + Utility.sq(raster.getHeight())) + 1;
        boolean found = false;
        double minDist = Double.POSITIVE_INFINITY;
        for (int r = DIST_SEARCH_STEP; r < rMax && !found; r += DIST_SEARCH_STEP) {
            int xMin = Math.max(0, x - r);
            int xMax = Math.min(raster.getWidth(), x + r);
            int yMin = Math.max(0, y - r);
            int yMax = Math.min(raster.getHeight(), y + r);
            for (int yIn = yMin; yIn < yMax; yIn++) {
                for (int xIn = xMin; xIn < xMax; xIn++) {
                    if (getPixel(raster, xIn, yIn) >0) {
                        continue;
                    }
                    double dist = Math.sqrt(Utility.sq(xIn - x) + Utility.sq(yIn - y));
                    if (dist <= r && dist < minDist) {
                        found = true;
                        minDist = dist;
                    }
                }
            }
        }
        return minDist;
    }

    private static int getPixel(Raster raster, int x, int y) {
        int[] pixel = new int[1];
        raster.getPixel(x, y, pixel);
        return pixel[0];
    }

    private static void setPixel(WritableRaster raster, int x, int y, int value) {
        int[] pixel = {value};
        raster.setPixel(x, y, pixel);
    }

    private static Kernel gaussianKernel(double radius) {
        double sigma = radius / 3.0;
        int size = (int) Math.ceil(radius);
        size -= size % 2 - 1;
        float[] weightMatrix = new float[Utility.sq(size)];
        int center = size / 2;
        double normConst = 0.0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double gauss = Math.exp(-(Utility.sq(i - center) + Utility.sq(j - center)) / (2 * Utility.sq(sigma)));
                weightMatrix[i + j * size] = (float) gauss;
                normConst += gauss;
            }
        }
        for (int i = 0; i < weightMatrix.length; i++) {
            weightMatrix[i] /= normConst;
        }
        return new Kernel(size, size, weightMatrix);
    }

    private static BufferedImage zeroPad(BufferedImage image, int pad) {
        int width = image.getWidth() + pad * 2;
        int height = image.getHeight() + pad * 2;
        BufferedImage output = new BufferedImage(width, height, image.getType());
        Graphics2D graphics = output.createGraphics();
        graphics.drawImage(image, pad, pad, null);
        return output;
    }
}
