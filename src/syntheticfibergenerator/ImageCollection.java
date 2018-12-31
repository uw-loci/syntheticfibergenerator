package syntheticfibergenerator;


import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class ImageCollection {

    private Params params;

    private ArrayList<FiberCollection> imageStack;

    static class Params {

        interface Parser<Type> {
            Type parse(String s);
        }

        static class Param<Type extends Comparable<Type>> {

            private transient String name;
            private Type value;

            Type getValue() {
                return value;
            }

            String getString() {
                return value == null ? "" : value.toString();
            }

            void setName(String name) {
                this.name = name;
            }

            String getName() {
                return name;
            }

            void verifyGreater(Type min) {
                if (getValue().compareTo(min) <= 0) {
                    throw new IllegalArgumentException("Error: " + name + " must be greater than than " + min); // TODO: Use better exception
                }
            }

            void verifyLess(Type max) {
                if (getValue().compareTo(max) >= 0) {
                    throw new IllegalArgumentException("Error: " + name + " must be less than " + max);
                }
            }

            void verifyGreaterEq(Type min) {
                if (getValue().compareTo(min) < 0) {
                    throw new IllegalArgumentException("Error: " + name + " must be greater than or equal to " + min);
                }
            }

            void verifyLessEq(Type max) {
                if (getValue().compareTo(max) > 0) {
                    throw new IllegalArgumentException("Error: " + name + " must be less than or equal to " + max);
                }
            }

            void parse(String s, Parser<Type> p) {
                if (s.replaceAll("\\s+","").isEmpty()) {
                    throw new IllegalArgumentException("Value of \"" + name + "\" must be non-empty");
                }
                try {
                    value = p.parse(s);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to parse value \"" + s + "\" for parameter \"" + name + '\"'); // TODO: Use better exception
                }
            }
        }

        static class Optional<Type extends Comparable<Type>> extends Param<Type> {

            boolean use;

            void parse(boolean use, String s, Parser<Type> p) {
                this.use = use;
                if (use) {
                    super.parse(s, p);
                }
            }
        }

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

    void generateFibers() {
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


