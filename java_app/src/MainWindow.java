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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


class ProgramParams {
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


public class MainWindow extends JFrame {
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

    private JTextField outputPathLabel;
    private JTextField lengthDistributionLabel;
    private JTextField widthDistributionLabel;
    private JTextField straightnessDistributionLabel;

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

    private final int FIELD_W = 5;
    private final int IMAGE_PANEL_SIZE = 512;
    private static String outFolder = "output" + File.separator;
    private static final String DEFAULTS_FILE = "defaults.json";


    private GridBagConstraints resetGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    private MainWindow() {
        super("Fiber Generator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = resetGBC();
        gbc.gridheight = 2;
        JPanel displayPanel = new JPanel(new GridBagLayout());
        add(displayPanel, gbc);
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridx++;
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        add(tabbedPane, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton generateButton = new JButton("Generate...");
        add(generateButton, gbc);

        JPanel generationPanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Generation", null, generationPanel);
        JPanel structurePanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Structure", null, structurePanel);
        JPanel appearancePanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Appearance", null, appearancePanel);

        gbc = resetGBC();
        gbc.gridwidth = 2;
        imageDisplay = new JLabel("Press \"Generate\" to view images");
        imageDisplay.setHorizontalAlignment(JLabel.CENTER);
        imageDisplay.setForeground(Color.WHITE);
        imageDisplay.setBackground(Color.BLACK);
        imageDisplay.setOpaque(true);
        imageDisplay.setPreferredSize(new Dimension(IMAGE_PANEL_SIZE, IMAGE_PANEL_SIZE));
        displayPanel.add(imageDisplay, gbc);

        gbc = resetGBC();
        gbc.weightx = 100;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        JButton prevButton = new JButton("Previous");
        displayPanel.add(prevButton, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        JButton nextButton = new JButton("Next");
        nextButton.setPreferredSize(prevButton.getPreferredSize());
        displayPanel.add(nextButton, gbc);

        gbc = resetGBC();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.ipadx = 10;
        gbc.ipady = 10;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 100;
        gbc.weighty = 100;
        JPanel sessionPanel = new JPanel(new GridBagLayout());
        sessionPanel.setBorder(BorderFactory.createTitledBorder("Session"));
        generationPanel.add(sessionPanel, gbc);

        gbc.weighty = 0;
        JPanel distributionPanel = new JPanel(new GridBagLayout());
        distributionPanel.setBorder(BorderFactory.createTitledBorder("Distributions"));
        structurePanel.add(distributionPanel, gbc);
        gbc.weighty = 100;
        gbc.gridy++;
        JPanel valuePanel = new JPanel(new GridBagLayout());
        valuePanel.setBorder(BorderFactory.createTitledBorder("Values"));
        structurePanel.add(valuePanel, gbc);

        gbc.weighty = 0;
        gbc.gridy = 0;
        JPanel requiredPanel = new JPanel(new GridBagLayout());
        requiredPanel.setBorder(BorderFactory.createTitledBorder("Required"));
        appearancePanel.add(requiredPanel, gbc);
        gbc.weighty = 100;
        gbc.gridy++;
        JPanel optionalPanel = new JPanel(new GridBagLayout());
        optionalPanel.setBorder(BorderFactory.createTitledBorder("Optional"));
        appearancePanel.add(optionalPanel, gbc);

        gbc = resetGBC();
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.anchor = GridBagConstraints.WEST;
        sessionPanel.add(new JLabel("Parameters:"), gbc);
        gbc.gridy++;
        sessionPanel.add(new JLabel("Output location:"), gbc);
        gbc.gridwidth = 2;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        outputPathLabel = new JTextField(outFolder);
        outputPathLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        outputPathLabel.setOpaque(false);
        outputPathLabel.setEditable(false);
        sessionPanel.add(outputPathLabel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridy++;
        sessionPanel.add(new JLabel("Number of images:"), gbc);
        gbc.gridy++;
        seedCheckBox = new JCheckBox("Random seed:");
        sessionPanel.add(seedCheckBox, gbc);
        gbc.gridy++;

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 0, 0);
        JButton loadButton = new JButton("Open...");
        sessionPanel.add(loadButton, gbc);
        gbc.gridy++;
        JButton saveButton = new JButton("Open...");
        sessionPanel.add(saveButton, gbc);
        gbc.gridy += 2;
        nImagesField = new JTextField(FIELD_W);
        sessionPanel.add(nImagesField, gbc);
        gbc.gridy++;
        seedField = new JTextField(FIELD_W);
        sessionPanel.add(seedField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 5);
        distributionPanel.add(new JLabel("Length distribution:"), gbc);
        gbc.gridwidth = 2;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lengthDistributionLabel = new JTextField();
        lengthDistributionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        lengthDistributionLabel.setOpaque(false);
        lengthDistributionLabel.setEditable(false);
        distributionPanel.add(lengthDistributionLabel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridy++;
        distributionPanel.add(new JLabel("Width distribution:"), gbc);
        gbc.gridwidth = 2;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        widthDistributionLabel = new JTextField();
        widthDistributionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        widthDistributionLabel.setOpaque(false);
        widthDistributionLabel.setEditable(false);
        distributionPanel.add(widthDistributionLabel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridy++;
        distributionPanel.add(new JLabel("Straightness distribution:"), gbc);
        gbc.gridwidth = 2;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        straightnessDistributionLabel = new JTextField();
        straightnessDistributionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        straightnessDistributionLabel.setOpaque(false);
        straightnessDistributionLabel.setEditable(false);
        distributionPanel.add(straightnessDistributionLabel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 0, 0);
        JButton lengthButton = new JButton("Modify...");
        distributionPanel.add(lengthButton, gbc);
        gbc.gridy += 2;
        JButton widthButton = new JButton("Modify...");
        distributionPanel.add(widthButton, gbc);
        gbc.gridy += 2;
        JButton straightnessButton = new JButton("Modify...");
        distributionPanel.add(straightnessButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 5);
        valuePanel.add(new JLabel("Width change:"), gbc);
        gbc.gridy++;
        valuePanel.add(new JLabel("Fibers per image:"), gbc);
        gbc.gridy++;
        valuePanel.add(new JLabel("Alignment:"), gbc);
        gbc.gridy++;
        valuePanel.add(new JLabel("Angle:"), gbc);
        gbc.gridy++;
        valuePanel.add(new JLabel("Segment length:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 0, 0);
        widthVariabilityField = new JTextField(FIELD_W);
        valuePanel.add(widthVariabilityField, gbc);
        gbc.gridy++;
        nFibersField = new JTextField(FIELD_W);
        valuePanel.add(nFibersField, gbc);
        gbc.gridy++;
        alignmentField = new JTextField(FIELD_W);
        valuePanel.add(alignmentField, gbc);
        gbc.gridy++;
        meanAngleField = new JTextField(FIELD_W);
        valuePanel.add(meanAngleField, gbc);
        gbc.gridy++;
        segmentLengthField = new JTextField(FIELD_W);
        valuePanel.add(segmentLengthField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 5);
        requiredPanel.add(new JLabel("Image height:"), gbc);
        gbc.gridy++;
        requiredPanel.add(new JLabel("Image width:"), gbc);
        gbc.gridy++;
        requiredPanel.add(new JLabel("Edge buffer:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 0, 0);
        imageHeightField = new JTextField(FIELD_W);
        requiredPanel.add(imageHeightField, gbc);
        gbc.gridy++;
        imageWidthField = new JTextField(FIELD_W);
        requiredPanel.add(imageWidthField, gbc);
        gbc.gridy++;
        edgeBufferField = new JTextField(FIELD_W);
        requiredPanel.add(edgeBufferField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 5);
        showScaleCheckBox = new JCheckBox("Scale (px/\u03BC):");
        optionalPanel.add(showScaleCheckBox, gbc);
        gbc.gridy++;
        downsampleCheckBox = new JCheckBox("Downsampling:");
        optionalPanel.add(downsampleCheckBox, gbc);
        gbc.gridy++;
        blurCheckBox = new JCheckBox("Blur radius:");
        optionalPanel.add(blurCheckBox, gbc);
        gbc.gridy++;
        noiseCheckBox = new JCheckBox("Poisson noise:");
        optionalPanel.add(noiseCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 0, 0);
        scaleField = new JTextField(FIELD_W);
        optionalPanel.add(scaleField, gbc);
        gbc.gridy++;
        downsampleField = new JTextField(FIELD_W);
        optionalPanel.add(downsampleField, gbc);
        gbc.gridy++;
        blurRadiusField = new JTextField(FIELD_W);
        optionalPanel.add(blurRadiusField, gbc);
        gbc.gridy++;
        meanNoiseField = new JTextField(FIELD_W);
        optionalPanel.add(meanNoiseField, gbc);

        serializer = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();
        deserializer = new GsonBuilder()
                .registerTypeAdapter(Distribution.class, new DistributionDeserializer())
                .create();
        try {
            // TODO: Write a method for this
            params = deserializer.fromJson(new BufferedReader(new FileReader(DEFAULTS_FILE)), ProgramParams.class);
        } catch (FileNotFoundException e) {
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
            try {
                if (params.setSeed) {
                    int seed = IOUtility.tryParseInt(seedField.getText());
                    RandomUtility.RNG = new Random((long) seed);
                } else {
                    RandomUtility.RNG = new Random();
                }
                readParams();
            } catch (IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            imageStack.clear();
            for (int i = 0; i < params.nImages; i++) {
                FiberImage fiberImage = new FiberImage(params);
                fiberImage.generateFibers();

                // TODO: Allow user to choose smoothing method
                fiberImage.bubbleSmooth();
                fiberImage.swapSmooth();
                fiberImage.splineSmooth();

                fiberImage.drawFibers();
                if (params.addNoise) {
                    fiberImage.addNoise();
                }
                if (params.blur) {
                    fiberImage.gaussianBlur();
                }
                if (params.showScale) {
                    fiberImage.drawScaleBar();
                }
                if (params.downsample) {
                    fiberImage.downsample();
                }
                imageStack.add(fiberImage);
                try {
                    // TODO: Extract to method?
                    FileWriter writer = new FileWriter(outFolder + "params.json");
                    writer.write(serializer.toJson(params, ProgramParams.class));
                    writer.flush();
                    writer.close();
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                try {
                    IOUtility.saveData(fiberImage, outFolder + "data" + i + ".json");
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                try {
                    IOUtility.saveImage(fiberImage.getImage(), outFolder + "image" + i + ".tiff");
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            currentImage = 0;
            displayImage(imageStack.get(currentImage).getImage());
        });
        prevButton.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage > 0) {
                currentImage--;
                displayImage(imageStack.get(currentImage).getImage());
            }
        });
        nextButton.addActionListener((ActionEvent event) ->
        {
            if (!imageStack.isEmpty() && currentImage < imageStack.size() - 1) {
                currentImage++;
                displayImage(imageStack.get(currentImage).getImage());
            }
        });
        lengthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.length);
            dialog.showDialog();
            params.length = dialog.distribution;
            displayParams();
        });
        straightnessButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.straightness);
            dialog.showDialog();
            params.straightness = dialog.distribution;
            displayParams();
        });
        widthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.width);
            dialog.showDialog();
            params.width = dialog.distribution;
            displayParams();
        });
        loadButton.addActionListener((ActionEvent event) ->
        {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
            chooser.setFileFilter(filter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    FileReader reader = new FileReader(chooser.getSelectedFile().getAbsolutePath());
                    params = deserializer.fromJson(reader, ProgramParams.class);
                    reader.close();
                    displayParams();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Unable to open file");
                }
            }
        });
        saveButton.addActionListener((ActionEvent event) ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                if (chooser.getSelectedFile().isDirectory()) {
                    outFolder = chooser.getSelectedFile().getAbsolutePath();
                } else {
                    outFolder = chooser.getCurrentDirectory().getAbsolutePath();
                }
                displayParams();
            }
        });
    }


    private void readParams() throws IllegalArgumentException {
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


    private void displayParams() {
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

        pack();
        outputPathLabel.setPreferredSize(outputPathLabel.getSize());
        widthDistributionLabel.setPreferredSize(widthDistributionLabel.getSize());
        lengthDistributionLabel.setPreferredSize(lengthDistributionLabel.getSize());
        straightnessDistributionLabel.setPreferredSize(straightnessDistributionLabel.getSize());

        outputPathLabel.setToolTipText(outFolder);
        outputPathLabel.setText(outFolder);
        widthDistributionLabel.setText(params.width.toString());
        lengthDistributionLabel.setText(params.length.toString());
        straightnessDistributionLabel.setText(params.straightness.toString());
    }


    private void displayImage(BufferedImage image) {
        double xScale = (double) IMAGE_PANEL_SIZE / image.getWidth();
        double yScale = (double) IMAGE_PANEL_SIZE / image.getHeight();
        double scale = Math.min(xScale, yScale);
        BufferedImage scaled = ImageUtility.scale(image, scale, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        Icon icon = new ImageIcon(scaled);
        imageDisplay.setText(null);
        imageDisplay.setIcon(icon);
        pack();
    }


    public static void main(String[] args) {
        new MainWindow();
    }
}