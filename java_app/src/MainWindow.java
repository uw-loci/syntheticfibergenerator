import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;
import java.util.Random;


class ProgramParams
{
    int nImages;
    int nFibers;
    double segmentLength;
    double alignment;
    double meanAngle;
    int imageWidth;
    int imageHeight;
    int edgeBuffer;
    double widthVariability;
    Distribution length = new Gaussian(1, 1, 2, 3);
    Distribution straightness = new Gaussian(1, 1, 2, 3);
    Distribution width = new Gaussian(1, 1, 2, 3);
    boolean setSeed;
    int seed;
    boolean showScale;
    double pixelsPerMicron;
    boolean downsample;
    double scaleRatio;
    boolean blur;
    double blurRadius;
    boolean addNoise;
    double meanNoise;
}


public class MainWindow extends JFrame
{
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
    private JTextField meanNoiseField;

    private JCheckBox seedCheckBox;
    private JCheckBox showScaleCheckBox;
    private JCheckBox downsampleCheckBox;
    private JCheckBox blurCheckBox;
    private JCheckBox noiseCheckBox;

    private Gson serializer;
    private Gson deserializer;

    private ProgramParams params;

    private ArrayList<FiberImage> imageStack;
    private int currentImage;

    private final int IMAGE_PANEL_SIZE = 500;
    private static String outFolder = "output" + File.separator;
    private static final String DEFAULTS_FILE = "defaults.json";


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


        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel loadSavePanel = new JPanel();
        loadSavePanel.setLayout(new GridBagLayout());
        settingsPanel.add(loadSavePanel, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 100;
        JButton loadButton = new JButton("Load");
        loadSavePanel.add(loadButton, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        JButton saveButton = new JButton("Save");
        loadSavePanel.add(saveButton, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 1;
        gbc.gridy = 1;
        settingsPanel.add(new JLabel("Output location"), gbc);
        gbc.gridy++;
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
        settingsPanel.add(new JLabel("Scale (px/\u00b5)"), gbc);
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
        gbc.gridx--;
        gbc.gridy++;
        noiseCheckBox = new JCheckBox();
        settingsPanel.add(noiseCheckBox, gbc);
        gbc.gridx++;
        settingsPanel.add(new JLabel("Poisson noise"), gbc);

        gbc.gridx++;
        gbc.gridy = 1;
        JButton outputLocationButton = new JButton("Open");
        settingsPanel.add(outputLocationButton, gbc);
        gbc.gridy++;
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
        JButton widthButton = new JButton("View/Modify");
        settingsPanel.add(widthButton, gbc);
        gbc.gridy++;
        JButton straightnessButton = new JButton("View/Modify");
        settingsPanel.add(straightnessButton, gbc);
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
        gbc.gridy++;
        meanNoiseField = new JTextField(10);
        settingsPanel.add(meanNoiseField, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;

        gbc.gridx = 0;
        gbc.gridy++;
        JButton generateButton = new JButton("Generate");
        settingsPanel.add(generateButton, gbc);

        serializer = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();
        deserializer = new GsonBuilder()
                .registerTypeAdapter(Distribution.class, new DistributionDeserializer())
                .create();
        try
        {
            // TODO: Write a method for this
            params = deserializer.fromJson(new BufferedReader(new FileReader(DEFAULTS_FILE)), ProgramParams.class);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Defaults file not found");
            System.exit(1);
        }

        displayParams();
        setResizable(false);
        pack();
        setVisible(true);

        imageStack = new ArrayList<>();

        generateButton.addActionListener((ActionEvent event) ->
        {
            try
            {
                if (params.setSeed)
                {
                    int seed = IOUtility.tryParseInt(seedField.getText());
                    RandomUtility.RNG = new Random((long) seed);
                }
                else
                {
                    RandomUtility.RNG = new Random();
                }
                readParams();
            }
            catch (IllegalArgumentException exception)
            {
                JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            imageStack.clear();
            for (int i = 0; i < params.nImages; i++)
            {
                FiberImage fiberImage = new FiberImage(params);
                fiberImage.generateFibers();

                // TODO: Allow user to choose smoothing method
                fiberImage.bubbleSmooth();
                fiberImage.swapSmooth();
                fiberImage.splineSmooth();

                fiberImage.drawFibers();
                if (params.addNoise)
                {
                    fiberImage.addNoise();
                }
                if (params.blur)
                {
                    fiberImage.gaussianBlur();
                }
                if (params.showScale)
                {
                    fiberImage.drawScaleBar();
                }
                if (params.downsample)
                {
                    fiberImage.downsample();
                }
                imageStack.add(fiberImage);
                try
                {
                    // TODO: Extract to method?
                    FileWriter writer = new FileWriter(outFolder + "params.json");
                    writer.write(serializer.toJson(params, ProgramParams.class));
                    writer.flush();
                    writer.close();
                }
                catch (IOException exception)
                {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                try
                {
                    IOUtility.saveData(fiberImage, outFolder + "data" + i + ".json");
                }
                catch (IOException exception)
                {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                try
                {
                    IOUtility.saveImage(fiberImage.getImage(), outFolder + "image" + i + ".tiff");
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
            DistributionDialog dialog = new DistributionDialog(params.length);
            dialog.showDialog();
            params.length = dialog.distribution;
        });
        straightnessButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.straightness);
            dialog.showDialog();
            params.straightness = dialog.distribution;
        });
        widthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.width);
            dialog.showDialog();
            params.width = dialog.distribution;
        });
        loadButton.addActionListener((ActionEvent event) ->
        {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
            chooser.setFileFilter(filter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    FileReader reader = new FileReader(chooser.getSelectedFile().getAbsolutePath());
                    params = deserializer.fromJson(reader, ProgramParams.class);
                    reader.close();
                    displayParams();
                }
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(null, "Unable to open file");
                }
            }
        });
        saveButton.addActionListener((ActionEvent event) ->
        {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    readParams();
                    FileWriter writer = new FileWriter(chooser.getSelectedFile().getAbsolutePath());
                    writer.write(serializer.toJson(params));
                    writer.close();
                }
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(null, "Unable to save file");
                }
            }
        });
        outputLocationButton.addActionListener((ActionEvent event) ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                outFolder = chooser.getSelectedFile().getAbsolutePath() + File.separator;
            }
        });
    }


    private void readParams() throws IllegalArgumentException
    {
        params.nImages = IOUtility.tryParseInt(nImagesField.getText());
        params.nFibers = IOUtility.tryParseInt(nFibersField.getText());
        params.segmentLength = IOUtility.tryParseDouble(segmentLengthField.getText());
        params.alignment = IOUtility.tryParseDouble(alignmentField.getText());
        params.meanAngle = IOUtility.tryParseDouble(meanAngleField.getText());
        params.imageWidth = IOUtility.tryParseInt(imageWidthField.getText());
        params.imageHeight = IOUtility.tryParseInt(imageHeightField.getText());
        params.edgeBuffer = IOUtility.tryParseInt(edgeBufferField.getText());
        params.widthVariability = IOUtility.tryParseDouble(widthVariabilityField.getText());
        params.setSeed = seedCheckBox.isSelected();
        params.seed = IOUtility.tryParseInt(seedField.getText());
        params.showScale = showScaleCheckBox.isSelected();
        params.pixelsPerMicron = IOUtility.tryParseDouble(scaleField.getText());
        params.downsample = downsampleCheckBox.isSelected();
        params.scaleRatio = IOUtility.tryParseDouble(downsampleField.getText());
        params.blur = blurCheckBox.isSelected();
        params.blurRadius = IOUtility.tryParseDouble(blurRadiusField.getText());
        params.addNoise = noiseCheckBox.isSelected();
        params.meanNoise = IOUtility.tryParseDouble(meanNoiseField.getText());

        IOUtility.verifyValue(params.nImages, 1, Integer.MAX_VALUE);
        IOUtility.verifyValue(params.nFibers, 1, Integer.MAX_VALUE);
        IOUtility.verifyValue(params.segmentLength, 0.0, Double.POSITIVE_INFINITY);
        IOUtility.verifyValue(params.alignment, 0.000001, 1.0);
        IOUtility.verifyValue(params.meanAngle, 0.0, 180.0);
        IOUtility.verifyValue(params.imageWidth, 1, Integer.MAX_VALUE);
        IOUtility.verifyValue(params.imageHeight, 1, Integer.MAX_VALUE);
        IOUtility.verifyValue(params.edgeBuffer, 0, Math.min(params.imageWidth / 2, params.imageHeight / 2));
        IOUtility.verifyValue(params.widthVariability, 0.0, Double.POSITIVE_INFINITY);
        IOUtility.verifyValue(params.pixelsPerMicron, 0.000001, Double.POSITIVE_INFINITY);
        IOUtility.verifyValue(params.scaleRatio, 0, Math.max(params.imageWidth, params.imageHeight));
        IOUtility.verifyValue(params.blurRadius, 0.0, Double.POSITIVE_INFINITY);
        IOUtility.verifyValue(params.meanNoise, 0.000001, Double.MAX_VALUE);
    }


    private void displayParams()
    {
        nImagesField.setText(Integer.toString(params.nImages));
        nFibersField.setText(Integer.toString(params.nFibers));
        segmentLengthField.setText(Double.toString(params.segmentLength));
        alignmentField.setText(Double.toString(params.alignment));
        meanAngleField.setText(Double.toString(params.meanAngle));
        imageWidthField.setText(Integer.toString(params.imageWidth));
        imageHeightField.setText(Integer.toString(params.imageHeight));
        edgeBufferField.setText(Integer.toString(params.edgeBuffer));
        widthVariabilityField.setText(Double.toString(params.widthVariability));
        seedCheckBox.setSelected(params.setSeed);
        seedField.setText(Integer.toString(params.seed));
        showScaleCheckBox.setSelected(params.showScale);
        scaleField.setText(Double.toString(params.pixelsPerMicron));
        downsampleCheckBox.setSelected(params.downsample);
        downsampleField.setText(Double.toString(params.scaleRatio));
        blurCheckBox.setSelected(params.blur);
        blurRadiusField.setText(Double.toString(params.blurRadius));
        noiseCheckBox.setSelected(params.addNoise);
        meanNoiseField.setText(Double.toString(params.meanNoise));
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