package syntheticfibergenerator;


import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class ImageCollection {

    private ProgramParams params;

    private ArrayList<FiberCollection> imageStack;

    static class ProgramParams {

        interface Parser<Type> {
            Type parse(String s);
        }

        static class Param<Type extends Comparable<Type>> {

            private transient String name;
            private Type value;

            Type getValue() {
                return value;
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
                try {
                    value = p.parse(s);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to parse param \"" + s + "\" for parameter " + name); // TODO: Use better exception
                }
            }
        }

        static class Optional<Type extends Comparable<Type>> extends Param<Type> {

            boolean use;

            void parse(boolean use, String s, Parser<Type> p) {
                this.use = use;
                super.parse(s, p);
            }
        }

        void setNames() {
            nImages.setName("number of images");
            nFibers.setName("number of fibers");
            segmentLength.setName("segment length");
            alignment.setName("alignment");
            meanAngle.setName("mean angle");
            widthVariability.setName("width variability");
            imageWidth.setName("image width");
            imageHeight.setName("image height");
            edgeBuffer.setName("edge buffer");
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

        Param<Integer> nImages;
        Param<Integer> nFibers;
        Param<Double> segmentLength;
        Param<Double> alignment;
        Param<Double> meanAngle;
        Param<Double> widthVariability;
        Param<Integer> imageWidth;
        Param<Integer> imageHeight;
        Param<Integer> edgeBuffer;

        // TODO: set lower and upper bound in constructor (hard-coded)
        Distribution length;
        Distribution straightness;
        Distribution width;

        Optional<Integer> seed;
        Optional<Double> scale;
        Optional<Double> downsample;
        Optional<Double> blur;
        Optional<Double> noise;
        Optional<Double> distance;
        Optional<Integer> bubble;
        Optional<Integer> swap;
        Optional<Integer> spline;
    }

    ImageCollection(ProgramParams params) {
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


