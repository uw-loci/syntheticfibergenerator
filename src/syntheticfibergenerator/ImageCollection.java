package syntheticfibergenerator;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;


class ImageCollection {

    static class Params extends FiberImage.Params {

        Param<Integer> nImages = new Param<>();
        Optional<Long> seed = new Optional<>();

        void setNames() {
            super.setNames();
            nImages.setName("number of images");
            seed.setName("seed");
        }

        void setHints() {
            super.setHints();
            nImages.setHint("The number of images to generate");
            seed.setHint("Check to fix the random seed; value is the seed");
        }
    }

    private Params params;
    private ArrayList<FiberImage> imageStack;


    ImageCollection(Params params) {
        imageStack = new ArrayList<>();
        this.params = params;
    }

    void generateImages() throws ArithmeticException {
        if (params.seed.use) {
            RngUtility.rng = new Random(params.seed.getValue());
        }

        imageStack.clear();
        for (int i = 0; i < params.nImages.getValue(); i++) {
            FiberImage image = new FiberImage(params);
            image.generateFibers();
            image.smooth();
            image.drawFibers();
            image.applyEffects();
            imageStack.add(image);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isEmpty() {
        return imageStack.isEmpty();
    }

    FiberImage get(int i) {
        return imageStack.get(i);
    }

    BufferedImage getImage(int i) {
        return get(i).getImage();
    }

    int size() {
        return imageStack.size();
    }
}