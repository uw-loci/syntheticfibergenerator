import java.awt.*;
import java.awt.event.ActionEvent;
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

    private JLabel imageDisplay;

    private JTextField nImagesField;
    private JTextField nFibersField;
    private JTextField segmentLengthField;
    private JTextField alignmentField;
    private JTextField meanAngleField;
    private JTextField imageWidthField;
    private JTextField imageHeightField;
    private JTextField edgeBufferField;
    private JTextField widthVariabilityField;
    private JTextField seedField;
    private JTextField scaleField;
    private JTextField downsampleField;
    private JTextField blurRadiusField;

    private JCheckBox seedCheckBox;
    private JCheckBox showScaleCheckBox;
    private JCheckBox downsampleCheckBox;
    private JCheckBox blurCheckBox;

    private ArrayList<FiberImage> imageStack;
    private int currentImage;

    private final int IMAGE_PANEL_SIZE = 500;
    private final String IMAGE_FOLDER = "images" + File.separator;
    private final String DATA_FOLDER = "data" + File.separator;


    private MainWindow()
    {
        super("Fiber Generator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridBagLayout());
        add(displayPanel, gbc);

        gbc.gridx++;
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        add(settingsPanel, gbc);

        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 0;
        imageDisplay = new JLabel("Press \"Generate\" to view images");
        imageDisplay.setHorizontalAlignment(JLabel.CENTER);
        imageDisplay.setForeground(Color.WHITE);
        imageDisplay.setBackground(Color.BLACK);
        imageDisplay.setOpaque(true);
        imageDisplay.setPreferredSize(new Dimension(IMAGE_PANEL_SIZE, IMAGE_PANEL_SIZE));
        displayPanel.add(imageDisplay, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 100;

        gbc.gridy++;
        JButton prevButton = new JButton("Previous");
        displayPanel.add(prevButton, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx++;
        JButton nextButton = new JButton("Next");
        nextButton.setPreferredSize(prevButton.getPreferredSize());
        displayPanel.add(nextButton, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 1;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("Number of images"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Fibers per image"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Segment length"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Alignment"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Mean angle"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Image width (px)"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Image height (px)"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Image buffer (px)"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Width variability"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Length distribution"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Width distribution"), gbc);
        gbc.gridy++;
        settingsPanel.add(new JLabel("Straightness distribution"), gbc);

        gbc.gridx--;
        gbc.gridy++;
        seedCheckBox = new JCheckBox();
        settingsPanel.add(seedCheckBox, gbc);
        gbc.gridx++;
        settingsPanel.add(new JLabel("Set random seed"), gbc);
        gbc.gridx--;
        gbc.gridy++;
        showScaleCheckBox = new JCheckBox();
        settingsPanel.add(showScaleCheckBox, gbc);
        gbc.gridx++;
        settingsPanel.add(new JLabel("Scale (\u00b5/px)"), gbc);
        gbc.gridx--;
        gbc.gridy++;
        downsampleCheckBox = new JCheckBox();
        settingsPanel.add(downsampleCheckBox, gbc);
        gbc.gridx++;
        settingsPanel.add(new JLabel("Downsample"), gbc);
        gbc.gridx--;
        gbc.gridy++;
        blurCheckBox = new JCheckBox();
        settingsPanel.add(blurCheckBox, gbc);
        gbc.gridx++;
        settingsPanel.add(new JLabel("Gaussian blur"), gbc);


        gbc.gridx++;
        gbc.gridy = 0;
        nImagesField = new JTextField(10);
        settingsPanel.add(nImagesField, gbc);
        gbc.gridy++;
        nFibersField = new JTextField(10);
        settingsPanel.add(nFibersField, gbc);
        gbc.gridy++;
        segmentLengthField = new JTextField(10);
        settingsPanel.add(segmentLengthField, gbc);
        gbc.gridy++;
        alignmentField = new JTextField(10);
        settingsPanel.add(alignmentField, gbc);
        gbc.gridy++;
        meanAngleField = new JTextField(10);
        settingsPanel.add(meanAngleField, gbc);
        gbc.gridy++;
        imageWidthField = new JTextField(10);
        settingsPanel.add(imageWidthField, gbc);
        gbc.gridy++;
        imageHeightField = new JTextField(10);
        settingsPanel.add(imageHeightField, gbc);
        gbc.gridy++;
        edgeBufferField = new JTextField(10);
        settingsPanel.add(edgeBufferField, gbc);
        gbc.gridy++;
        widthVariabilityField = new JTextField(10);
        settingsPanel.add(widthVariabilityField, gbc);
        gbc.gridy++;
        JButton lengthButton = new JButton("View/Modify");
        settingsPanel.add(lengthButton, gbc);
        gbc.gridy++;
        JButton straightnessButton = new JButton("View/Modify");
        settingsPanel.add(straightnessButton, gbc);
        gbc.gridy++;
        JButton widthButton = new JButton("View/Modify");
        settingsPanel.add(widthButton, gbc);
        gbc.gridy++;
        seedField = new JTextField(10);
        settingsPanel.add(seedField, gbc);
        gbc.gridy++;
        scaleField = new JTextField(10);
        settingsPanel.add(scaleField, gbc);
        gbc.gridy++;
        downsampleField = new JTextField(10);
        settingsPanel.add(downsampleField, gbc);
        gbc.gridy++;
        blurRadiusField = new JTextField(10);
        settingsPanel.add(blurRadiusField, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;

        gbc.gridx = 0;
        gbc.gridy++;
        JButton generateButton = new JButton("Generate");
        settingsPanel.add(generateButton, gbc);

        setDefaults();
        setResizable(false);
        pack();
        setVisible(true);

        imageStack = new ArrayList<>();

        generateButton.addActionListener((ActionEvent event) ->
        {
            int nImages;
            FiberImageParams params = new FiberImageParams();
            try
            {
                if (seedCheckBox.isSelected())
                {
                    int seed = IOUtility.tryParseInt(seedField.getText());
                    RandomUtility.RNG = new Random((long) seed);
                }
                else
                {
                    RandomUtility.RNG = new Random();
                }

                nImages = IOUtility.tryParseInt(nImagesField.getText());
                params.nFibers = IOUtility.tryParseInt(nFibersField.getText());
                params.segmentLength = IOUtility.tryParseDouble(segmentLengthField.getText());
                params.alignment = IOUtility.tryParseDouble(alignmentField.getText());
                params.angle = IOUtility.tryParseDouble(meanAngleField.getText());
                params.imageWidth = IOUtility.tryParseInt(imageWidthField.getText());
                params.imageHeight = IOUtility.tryParseInt(imageHeightField.getText());
                params.edgeBuffer = IOUtility.tryParseInt(edgeBufferField.getText());
                params.widthVariation = IOUtility.tryParseDouble(widthVariabilityField.getText());
                params.micronsPerPixel = IOUtility.tryParseDouble(scaleField.getText());
                params.downSampleFactor = IOUtility.tryParseDouble(downsampleField.getText());
                params.blurRadius = IOUtility.tryParseDouble(blurRadiusField.getText());

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
                if (blurCheckBox.isSelected())
                {
                    fiberImage.gaussianBlur();
                }
                if (showScaleCheckBox.isSelected())
                {
                    fiberImage.drawScaleBar();
                }
                if (downsampleCheckBox.isSelected())
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

        prevButton.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage > 0)
            {
                currentImage--;
                displayImage(imageStack.get(currentImage).getImage());
            }
        });

        nextButton.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage < imageStack.size() - 1)
            {
                currentImage++;
                displayImage(imageStack.get(currentImage).getImage());
            }
        });
        lengthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(length);
            dialog.showDialog();
            length = dialog.distribution;
        });
        straightnessButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(straightness);
            dialog.showDialog();
            straightness = dialog.distribution;
        });
        widthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(width);
            dialog.showDialog();
            width = dialog.distribution;
        });
    }


    private void setDefaults()
    {
        nImagesField.setText("10");
        nFibersField.setText("15");
        segmentLengthField.setText("10.0");
        alignmentField.setText("0.5");
        meanAngleField.setText("3.14159");
        imageWidthField.setText(Integer.toString(IMAGE_PANEL_SIZE));
        imageHeightField.setText(Integer.toString(IMAGE_PANEL_SIZE));
        edgeBufferField.setText(Integer.toString(IMAGE_PANEL_SIZE / 10));
        widthVariabilityField.setText("0.5");
        seedCheckBox.setSelected(true);
        seedField.setText("1");
        showScaleCheckBox.setSelected(true);
        scaleField.setText("5");
        downsampleCheckBox.setSelected(false);
        downsampleField.setText("0.5");
        blurCheckBox.setSelected(false);
        blurRadiusField.setText("5.0");
        length = new Gaussian(10.0, 2.0, 1.0, Double.POSITIVE_INFINITY);
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
        imageDisplay.setText(null);
        imageDisplay.setIcon(icon);
        pack();
    }


    public static void main(String[] args)
    {
        new MainWindow();
    }
}