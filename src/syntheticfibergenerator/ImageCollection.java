package syntheticfibergenerator;


import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class ImageCollection {

    private Params params;

    private ArrayList<FiberCollection> imageStack;

    static class Params {

        Param<Integer> nImages = new Param<>();
        Param<Integer> nFibers = new Param<>();
        Param<Double> segmentLength = new Param<>();
        Param<Double> alignment = new Param<>();
        Param<Double> meanAngle = new Param<>();
        Param<Double> widthChange = new Param<>();
        Param<Integer> imageWidth = new Param<>();
        Param<Integer> imageHeight = new Param<>();
        Param<Integer> imageBuffer = new Param<>();

        Distribution length = new Uniform(0, Double.POSITIVE_INFINITY);
        Distribution straightness = new Uniform(0, 1);
        Distribution width = new Uniform(0, Double.POSITIVE_INFINITY);

        Optional<Integer> seed = new Optional<>();
        Optional<Double> scale = new Optional<>();
        Optional<Double> downsample = new Optional<>();
        Optional<Double> blur = new Optional<>();
        Optional<Double> noise = new Optional<>();
        Optional<Double> distance = new Optional<>();
        Optional<Integer> bubble = new Optional<>();
        Optional<Integer> swap = new Optional<>();
        Optional<Integer> spline = new Optional<>();

        void setNames() {
            nImages.setName("number of images");
            nFibers.setName("number of fibers");
            segmentLength.setName("segment length");
            alignment.setName("alignment");
            meanAngle.setName("mean angle");
            widthChange.setName("width change");
            imageWidth.setName("image width");
            imageHeight.setName("image height");
            imageBuffer.setName("edge buffer");

            length.setNames();
            straightness.setNames();
            width.setNames();

            seed.setName("seed");
            scale.setName("scale");
            downsample.setName("downsample");
            blur.setName("blur");
            noise.setName("noise");
            distance.setName("distance");
            bubble.setName("bubble");
            swap.setName("swap");
            spline.setName("spline");
        }
    }

    ImageCollection(Params params) {
        imageStack = new ArrayList<>();
        this.params = params;
    }

    void generateFibers() throws ArithmeticException {
        try {
            if (params.seed.use) {
                RandomUtility.RNG = new Random((long) params.seed.getValue());
            } else {
                RandomUtility.RNG = new Random();
            }
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        imageStack.clear();
        for (int i = 0; i < params.nImages.getValue(); i++) {
            FiberCollection fiberImage = new FiberCollection(params);
            fiberImage.generateFibers();

            // TODO: Move this to fiberImage class
            if (params.bubble.use) {
                fiberImage.bubbleSmooth();
            }
            if (params.swap.use) {
                fiberImage.swapSmooth();
            }
            if (params.spline.use) {
                fiberImage.splineSmooth();
            }

            fiberImage.drawFibers();
            if (params.distance.use) {
                fiberImage.distanceFunction();
            }
            if (params.noise.use) {
                fiberImage.addNoise();
            }
            if (params.blur.use) {
                fiberImage.gaussianBlur();
            }
            if (params.scale.use) {
                fiberImage.drawScaleBar();
            }
            if (params.downsample.use) {
                fiberImage.downsample();
            }
            imageStack.add(fiberImage);
        }
    }

    boolean isEmpty() {
        return imageStack.isEmpty();
    }

    FiberCollection get(int i) {
        if (i < 0 || i >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return imageStack.get(i);
    }

    int size() {
        return imageStack.size();
    }
}


