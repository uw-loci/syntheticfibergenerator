import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;


public class MainWindow extends JFrame
{
    private Distribution length;
    private Distribution straightness;
    private Distribution width;

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
    private JLabel labelSegmentLength;
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
    private JTextField fieldMinStraightness;
    private JTextField fieldMaxStraightness;
    private JTextField fieldAlignment;
    private JTextField fieldMeanAngle;
    private JLabel labelAlignment;
    private JLabel labelMeanAngle;
    private JTextField fieldEdgeBuffer;
    private JLabel labelEdgeBuffer;
    private JTextField fieldMinWidth;
    private JTextField fieldMaxWidth;
    private JTextField fieldMeanWidth;
    private JTextField fieldWidthVariability;
    private JLabel labelWidthVariability;
    private JCheckBox checkBoxSetSeed;
    private JTextField fieldSeed;
    private JCheckBox checkBoxShowScale;
    private JTextField fieldMicronsPerPixel;
    private JPanel panelMicronsPerPixel;
    private JLabel labelMicronsPerPixel;
    private JCheckBox checkBoxGaussianBlur;
    private JTextField fieldGaussianBlurRadius;
    private JCheckBox checkBoxDownsample;
    private JTextField fieldDownsampleFactor;
    private JPanel panelGaussianBlur;
    private JPanel panelDownsample;
    private JLabel labelDownsampleFactor;
    private JLabel labelGaussianBlurRadius;
    private JButton modifyLengthDistrubutionButton;
    private JButton modifyStraightnessDistributionButton;
    private JButton modifyWidthDistributionButton;
    private JLabel lengthDistributionLabel;
    private JLabel straightnessDistributionLabel;
    private JLabel widthDistributionLabel;
    private JLabel lengthDistributionStringLabel;
    private JLabel straightnessDistributionStringLabel;
    private JLabel widthDistributionStringLabel;

    private ArrayList<FiberImage> imageStack;
    private int currentImage;

    private final int IMAGE_PANEL_SIZE = 400;
    private final String IMAGE_FOLDER = "images" + File.separator;
    private final String DATA_FOLDER = "data" + File.separator;


    private MainWindow()
    {
        super("Fiber Generator");
        imageStack = new ArrayList<>();
        setContentPane(panelMain);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        labelImage.setPreferredSize(new Dimension(IMAGE_PANEL_SIZE, IMAGE_PANEL_SIZE));
        setDefaults();

        refreshDistributionDisplays();
        pack();
        setVisible(true);

        buttonGenerate.addActionListener((ActionEvent event) ->
        {
            int nImages;
            FiberImageParams params = new FiberImageParams();
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
                params.segmentLength = IOUtility.tryParseDouble(fieldSegmentLength.getText());
                params.alignment = IOUtility.tryParseDouble(fieldAlignment.getText());
                params.angle = IOUtility.tryParseDouble(fieldMeanAngle.getText());
                params.imageWidth = IOUtility.tryParseInt(fieldImageWidth.getText());
                params.imageHeight = IOUtility.tryParseInt(fieldImageHeight.getText());
                params.edgeBuffer = IOUtility.tryParseInt(fieldEdgeBuffer.getText());
                params.widthVariation = IOUtility.tryParseDouble(fieldWidthVariability.getText());
                params.micronsPerPixel = IOUtility.tryParseDouble(fieldMicronsPerPixel.getText());
                params.downSampleFactor = IOUtility.tryParseDouble(fieldDownsampleFactor.getText());
                params.blurRadius = IOUtility.tryParseDouble(fieldGaussianBlurRadius.getText());

                params.length = length;
                params.straightness = straightness;
                params.fiberWidth = width;

                IOUtility.verifyValue(nImages, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.nFibers, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.segmentLength, 0.0, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(params.alignment, 0.000001, 1.0);
                IOUtility.verifyValue(params.imageWidth, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.imageHeight, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.edgeBuffer, 0, Math.min(params.imageWidth / 2, params.imageHeight / 2));
                IOUtility.verifyValue(params.widthVariation, 0.0, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(params.micronsPerPixel, 0.000001, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(params.downSampleFactor, 0, Math.max(params.imageWidth, params.imageHeight));
                IOUtility.verifyValue(params.blurRadius, 0.0, Double.POSITIVE_INFINITY);
            }
            catch (IllegalArgumentException exception)
            {
                JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            imageStack.clear();
            for (int i = 0; i < nImages; i++)
            {
                FiberImage fiberImage = new FiberImage(params);
                fiberImage.generateFibers();

                // TODO: Allow user to choose smoothing method
                fiberImage.bubbleSmooth();
                fiberImage.swapSmooth();
                fiberImage.splineSmooth();

                fiberImage.drawFibers();
                if (checkBoxGaussianBlur.isSelected())
                {
                    fiberImage.gaussianBlur();
                }
                if (checkBoxShowScale.isSelected())
                {
                    fiberImage.drawScaleBar();
                }
                if (checkBoxDownsample.isSelected())
                {
                    fiberImage.downsample();
                }
                imageStack.add(fiberImage);
                try
                {
                    IOUtility.saveData(fiberImage, DATA_FOLDER + "data" + i + ".json");
                }
                catch (IOException exception)
                {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                try
                {
                    IOUtility.saveImage(fiberImage.getImage(), IMAGE_FOLDER + "image" + i + ".png");
                }
                catch (IOException exception)
                {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            currentImage = 0;
            displayImage(imageStack.get(currentImage).getImage());
        });

        buttonPrevious.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage > 0)
            {
                currentImage--;
                displayImage(imageStack.get(currentImage).getImage());
            }
        });

        buttonNext.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage < imageStack.size() - 1)
            {
                currentImage++;
                displayImage(imageStack.get(currentImage).getImage());
            }
        });
        modifyLengthDistrubutionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                DistributionDialog dialog = new DistributionDialog(length);
                dialog.showDialog();
                length = dialog.distribution;
                refreshDistributionDisplays();
            }
        });
        modifyStraightnessDistributionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                DistributionDialog dialog = new DistributionDialog(straightness);
                dialog.showDialog();
                straightness = dialog.distribution;
                refreshDistributionDisplays();
            }
        });
        modifyWidthDistributionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                DistributionDialog dialog = new DistributionDialog(width);
                dialog.showDialog();
                width = dialog.distribution;
                refreshDistributionDisplays();
            }
        });
    }


    private void refreshDistributionDisplays()
    {
        lengthDistributionStringLabel.setText(length.toString());
        straightnessDistributionStringLabel.setText(straightness.toString());
        widthDistributionStringLabel.setText(width.toString());
        pack();
    }


    private void setDefaults()
    {
        fieldNImage.setText("10");
        fieldNFiber.setText("15");
        fieldSegmentLength.setText("10.0");
        fieldAlignment.setText("0.5");
        fieldMeanAngle.setText("3.14159");
        fieldImageWidth.setText(Integer.toString(IMAGE_PANEL_SIZE));
        fieldImageHeight.setText(Integer.toString(IMAGE_PANEL_SIZE));
        fieldEdgeBuffer.setText(Integer.toString(IMAGE_PANEL_SIZE / 10));
        fieldWidthVariability.setText("0.5");
        checkBoxSetSeed.setSelected(true);
        fieldSeed.setText("1");
        checkBoxShowScale.setSelected(true);
        fieldMicronsPerPixel.setText("5");
        checkBoxDownsample.setSelected(false);
        fieldDownsampleFactor.setText("0.5");
        checkBoxGaussianBlur.setSelected(false);
        fieldGaussianBlurRadius.setText("5.0");
        length = new Gaussian(1.0, 0.1, 1.0, Double.POSITIVE_INFINITY);
        straightness = new Uniform(0.8, 1.0, 0.0, 1.0);
        width = new Gaussian(2.0, 1.0, 1.0, Double.POSITIVE_INFINITY);
    }


    private void displayImage(BufferedImage image)
    {
        double xScale = (double) IMAGE_PANEL_SIZE / image.getWidth();
        double yScale = (double) IMAGE_PANEL_SIZE / image.getHeight();
        double scale = Math.min(xScale, yScale);
        BufferedImage scaled = ImageUtility.scale(image, scale, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

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