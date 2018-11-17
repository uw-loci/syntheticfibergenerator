import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;
import java.text.DecimalFormat;


public class MainWindow extends JFrame
{
    private JPanel panelMain;
    private JPanel panelLeft;
    private JPanel panelRight;
    private JPanel panelNextPrev;
    private JPanel panelSettings;
    private JButton buttonPrevious;
    private JButton buttonNext;
    private JButton buttonGenerate;
    private JLabel labelNImage;
    private JLabel labelNFiber;
    private JLabel labelMeanLength;
    private JLabel labelSegmentLength;
    private JLabel labelMeanStraightness;
    private JLabel labelImageWidth;
    private JLabel labelImageHeight;
    private JLabel labelImage;
    private JTextField fieldNImage;
    private JTextField fieldNFiber;
    private JTextField fieldMeanLength;
    private JTextField fieldSegmentLength;
    private JTextField fieldMeanStraightness;
    private JTextField fieldImageWidth;
    private JTextField fieldImageHeight;
    private JTextField fieldMinLength;
    private JTextField fieldMaxLength;
    private JLabel labelMinLength;
    private JLabel labelMaxLength;
    private JTextField fieldMinStraightness;
    private JTextField fieldMaxStraightness;
    private JLabel labelMinStraightness;
    private JLabel labelMaxStraightness;
    private JTextField fieldAlignment;
    private JTextField fieldMeanAngle;
    private JLabel labelAlignment;
    private JLabel labelMeanAngle;
    private JTextField fieldEdgeBuffer;
    private JLabel labelEdgeBuffer;
    private JTextField fieldMinWidth;
    private JTextField fieldMaxWidth;
    private JTextField fieldMeanWidth;
    private JLabel labelMinWidth;
    private JLabel labelMaxWidth;
    private JLabel labelMeanWidth;
    private JTextField fieldWidthVariability;
    private JLabel labelWidthVariability;
    private JCheckBox checkBoxSetSeed;
    private JTextField fieldSeed;
    private JCheckBox checkBoxShowScale;
    private JTextField fieldMicronsPerPixel;
    private JPanel panelMicronsPerPixel;
    private JLabel labelMicronsPerPixel;

    private ArrayList<BufferedImage> imageStack;
    private int currentImage;

    private double micronsPerPixel;

    private final int IMAGE_PANEL_SIZE = 400;
    private final String IMAGE_FOLDER = "images" + File.separator;
    private final String DATA_FOLDER = "data" + File.separator;

    // The approximate fraction of the image's width that the scale bar should occupy
    private static final double IDEAL_SCALE_FRAC = 0.2;

    // Visual properties of the scale bar
    private static final int CAP_SIZE = 5;
    private static final int LABEL_BUFF = 5;
    private static final int SCALE_BUFF = 20;


    private MainWindow()
    {
        super("Fiber Generator");
        imageStack = new ArrayList<>();
        setContentPane(panelMain);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        labelImage.setPreferredSize(new Dimension(IMAGE_PANEL_SIZE, IMAGE_PANEL_SIZE));
        setDefaults();
        pack();
        setVisible(true);

        buttonGenerate.addActionListener((ActionEvent event) ->
        {
            int nImages;
            FiberCollectionParams params = new FiberCollectionParams();
            try
            {
                if (checkBoxSetSeed.isSelected())
                {
                    int seed = IOUtility.tryParseInt(fieldSeed.getText());
                    RandomUtility.RNG = new Random((long) seed);
                }
                else
                {
                    RandomUtility.RNG = new Random();
                }

                nImages = IOUtility.tryParseInt(fieldNImage.getText());
                params.nFibers = IOUtility.tryParseInt(fieldNFiber.getText());
                params.meanLength = IOUtility.tryParseInt(fieldMeanLength.getText());
                params.minLength = IOUtility.tryParseInt(fieldMinLength.getText());
                params.maxLength = IOUtility.tryParseInt(fieldMaxLength.getText());
                params.segmentLength = IOUtility.tryParseDouble(fieldSegmentLength.getText());
                params.meanStraightness = IOUtility.tryParseDouble(fieldMeanStraightness.getText());
                params.minStraightness = IOUtility.tryParseDouble(fieldMinStraightness.getText());
                params.maxStraightness = IOUtility.tryParseDouble(fieldMaxStraightness.getText());
                params.alignment = IOUtility.tryParseDouble(fieldAlignment.getText());
                params.meanAngle = IOUtility.tryParseDouble(fieldMeanAngle.getText());
                params.imageWidth = IOUtility.tryParseInt(fieldImageWidth.getText());
                params.imageHeight = IOUtility.tryParseInt(fieldImageHeight.getText());
                params.edgeBuffer = IOUtility.tryParseInt(fieldEdgeBuffer.getText());
                params.meanWidth = IOUtility.tryParseDouble(fieldMeanWidth.getText());
                params.minWidth = IOUtility.tryParseDouble(fieldMinWidth.getText());
                params.maxWidth = IOUtility.tryParseDouble(fieldMaxWidth.getText());
                params.widthVariation = IOUtility.tryParseDouble(fieldWidthVariability.getText());
                micronsPerPixel = IOUtility.tryParseDouble(fieldMicronsPerPixel.getText());

                IOUtility.verifyValue(nImages, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.nFibers, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.minLength, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.maxLength, params.minLength, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.meanLength, params.minLength, params.maxLength);
                IOUtility.verifyValue(params.segmentLength, 0.0, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(params.minStraightness, 0.0, 1.0);
                IOUtility.verifyValue(params.maxStraightness, params.minStraightness, 1.0);
                IOUtility.verifyValue(params.meanStraightness, params.minStraightness, params.maxStraightness);
                if (params.meanStraightness == 0.0)
                {
                    throw new IllegalArgumentException("Mean straightness must be nonzero");
                }
                IOUtility.verifyValue(params.alignment, 0.000001, 1.0);
                IOUtility.verifyValue(params.imageWidth, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.imageHeight, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.edgeBuffer, 0, Math.min(params.imageWidth / 2, params.imageHeight / 2));
                IOUtility.verifyValue(params.minWidth, 1.0, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(params.maxWidth, params.minWidth, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(params.meanWidth, params.minWidth, params.maxWidth);
                IOUtility.verifyValue(params.widthVariation, 0.0, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(micronsPerPixel, 0.000001, Double.POSITIVE_INFINITY);
            }
            catch (IllegalArgumentException exception)
            {
                JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            imageStack.clear();
            for (int i = 0; i < nImages; i++)
            {
                FiberCollection fiberCollection = new FiberCollection(params);
                fiberCollection.generate();

                // TODO: Allow user to choose smoothing method
                fiberCollection.bubbleSmooth();
                fiberCollection.swapSmooth();

                fiberCollection.splineSmooth();
                BufferedImage image = fiberCollection.drawFibers();
                if (checkBoxShowScale.isSelected())
                {
                    drawScaleBar(image);
                }
                imageStack.add(image);
                try
                {
                    IOUtility.saveData(fiberCollection, DATA_FOLDER + "data" + i + ".json");
                }
                catch (IOException exception)
                {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                try
                {
                    IOUtility.saveImage(image, IMAGE_FOLDER + "image" + i + ".png");
                }
                catch (IOException exception)
                {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            currentImage = 0;
            displayImage(imageStack.get(currentImage));
        });

        buttonPrevious.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage > 0)
            {
                currentImage--;
                displayImage(imageStack.get(currentImage));
            }
        });

        buttonNext.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage < imageStack.size() - 1)
            {
                currentImage++;
                displayImage(imageStack.get(currentImage));
            }
        });
    }


    private void setDefaults()
    {
        fieldNImage.setText("10");
        fieldNFiber.setText("15");
        fieldMeanLength.setText(Integer.toString(IMAGE_PANEL_SIZE / 30));
        fieldMinLength.setText(Integer.toString(IMAGE_PANEL_SIZE / 60));
        fieldMaxLength.setText(Integer.toString(IMAGE_PANEL_SIZE / 20));
        fieldSegmentLength.setText("10.0");
        fieldMeanStraightness.setText("0.8");
        fieldMinStraightness.setText("0.7");
        fieldMaxStraightness.setText("1.0");
        fieldAlignment.setText("0.5");
        fieldMeanAngle.setText("3.14159");
        fieldImageWidth.setText(Integer.toString(IMAGE_PANEL_SIZE));
        fieldImageHeight.setText(Integer.toString(IMAGE_PANEL_SIZE));
        fieldEdgeBuffer.setText(Integer.toString(IMAGE_PANEL_SIZE / 10));
        fieldMeanWidth.setText("3.0");
        fieldMinWidth.setText("2.0");
        fieldMaxWidth.setText("4.0");
        fieldWidthVariability.setText("0.5");
        checkBoxSetSeed.setSelected(true);
        fieldSeed.setText("1");
        checkBoxShowScale.setSelected(true);
        fieldMicronsPerPixel.setText("5");
    }


    private void drawScaleBar(BufferedImage image)
    {
        // Determine the size in microns of the scale bar
        double targetSize = IDEAL_SCALE_FRAC * micronsPerPixel * image.getWidth();
        double floorPow = Math.floor(Math.log10(targetSize));
        double possibleSizes[] = {Math.pow(10, floorPow), 5 * Math.pow(10, floorPow), Math.pow(10, floorPow + 1)};
        double bestSize = possibleSizes[0];
        for (double size : possibleSizes)
        {
            if (Math.abs(targetSize - size) < Math.abs(targetSize - bestSize))
            {
                bestSize = size;
            }
        }

        // Format the scale label
        String label;
        if (Math.abs(Math.floor(Math.log10(bestSize))) <= 2)
        {
            label = new DecimalFormat("0.## \u00B5").format(bestSize);
        }
        else
        {
            label = String.format("%.1e \u00B5", bestSize);
        }

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int scaleHeight = image.getHeight() - SCALE_BUFF - CAP_SIZE;
        int scaleRight = SCALE_BUFF + (int) (bestSize / micronsPerPixel);

        // Draw the scale
        graphics.drawLine(SCALE_BUFF, scaleHeight, scaleRight, scaleHeight);
        graphics.drawLine(SCALE_BUFF, scaleHeight + CAP_SIZE, SCALE_BUFF, scaleHeight - CAP_SIZE);
        graphics.drawLine(scaleRight, scaleHeight + CAP_SIZE, scaleRight, scaleHeight - CAP_SIZE);

        // Draw the scale label
        graphics.drawString(label, SCALE_BUFF, scaleHeight - CAP_SIZE - LABEL_BUFF);
    }


    private void displayImage(BufferedImage image)
    {
        AffineTransform transform = new AffineTransform();
        double xScale = (double) IMAGE_PANEL_SIZE / image.getWidth();
        double yScale = (double) IMAGE_PANEL_SIZE / image.getHeight();
        double scale = Math.min(xScale, yScale);
        transform.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage scaled = scaleOp.filter(image, null);

        Icon icon = new ImageIcon(scaled);
        labelImage.setText(null);
        labelImage.setIcon(icon);
        pack();
    }


    public static void main(String[] args)
    {
        new MainWindow();
    }
}