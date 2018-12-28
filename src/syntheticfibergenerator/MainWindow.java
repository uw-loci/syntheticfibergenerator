package syntheticfibergenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MainWindow extends JFrame {

    private JButton generateButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton lengthButton;
    private JButton straightnessButton;
    private JButton widthButton;
    private JButton loadButton;
    private JButton saveButton;

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
    private JTextField noiseField;
    private JTextField distanceField;

    private JTextField outputPathLabel;
    private JTextField lengthDistributionLabel;
    private JTextField widthDistributionLabel;
    private JTextField straightnessDistributionLabel;

    private JCheckBox seedCheck;
    private JCheckBox scaleCheck;
    private JCheckBox downsampleCheck;
    private JCheckBox blurCheckBox;
    private JCheckBox noiseCheck;
    private JCheckBox distanceCheck;

    private JTextField bubbleField;
    private JTextField swapField;
    private JTextField splineField;

    private JCheckBox bubbleCheck;
    private JCheckBox swapCheck;
    private JCheckBox splineCheck;

    private Gson serializer;
    private Gson deserializer;

    ImageCollection.ProgramParams params;
    private ImageCollection collection;

    private int displayIdx;

    private String outFolder = "output" + File.separator;

    private static final int FIELD_W = 5;
    private static final int IMAGE_PANEL_SIZE = 512;
    private static final String DEFAULTS_FILE = "defaults.json";
    private static final String IMAGE_PREFIX = "image";
    private static final String IMAGE_EXTENSION = "tiff";
    private static final String DATA_PREFIX = "data";


    private GridBagConstraints resetGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }


    private String guiName(ImageCollection.ProgramParams.Param param) {
        String name = param.getName();
        String uppercase = name.substring(0, 1).toUpperCase() + name.substring(1);
        return uppercase + ":";
    }


    private void initGUI() {
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

        // TODO: Start here
        gbc = resetGBC();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.ipadx = 10;
        gbc.ipady = 10;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 100;
        gbc.weighty = 100;
        OptionPanel session = new OptionPanel("Session");
        generationPanel.add(session, gbc);

        gbc.weighty = 0;
        OptionPanel distribution = new OptionPanel("Distributions");
        structurePanel.add(distribution, gbc);
        gbc.weighty = 100;
        gbc.gridy++;
        OptionPanel values = new OptionPanel("Values");
        structurePanel.add(values, gbc);

        gbc.weighty = 0;
        gbc.gridy = 0;
        OptionPanel required = new OptionPanel("Required");
        appearancePanel.add(required, gbc);
        gbc.gridy++;
        OptionPanel optional = new OptionPanel("Optional");
        appearancePanel.add(optional, gbc);
        gbc.weighty = 100;
        gbc.gridy++;
        OptionPanel smooth = new OptionPanel("Smoothing");
        appearancePanel.add(smooth, gbc);

        session.addLabel("Parameters:");
        loadButton = session.addButton("Open...");
        session.addLabel("Output location:");
        saveButton = session.addButton("Open...");
        outputPathLabel = session.addFieldRow(outFolder);
        nImagesField = session.addFieldLine(guiName(params.nImages));
        seedField = session.addFieldLine(guiName(params.seed));

        distribution.addLabel("Length distribution:");
        lengthButton = distribution.addButton("Modify...");
        lengthDistributionLabel = distribution.addFieldRow(params.length.toString()); // TODO: Do we need to resize after this?
        distribution.addLabel("Width distribution:");
        widthButton = distribution.addButton("Modify...");
        widthDistributionLabel = distribution.addFieldRow(params.width.toString()); // TODO: Do we need to resize after this?
        distribution.addLabel("Straightness distribution:");
        straightnessButton = distribution.addButton("Modify...");
        straightnessDistributionLabel = distribution.addFieldRow(params.straightness.toString()); // TODO: Do we need to resize after this?

        widthVariabilityField = values.addFieldLine(guiName(params.widthVariability));
        nFibersField = values.addFieldLine(guiName(params.nFibers));
        alignmentField = values.addFieldLine(guiName(params.alignment));
        meanAngleField = values.addFieldLine(guiName(params.meanAngle));
        segmentLengthField = values.addFieldLine(guiName(params.segmentLength));

        imageHeightField = required.addFieldLine(guiName(params.imageHeight));
        imageWidthField = required.addFieldLine(guiName(params.imageWidth));
        edgeBufferField = required.addFieldLine(guiName(params.edgeBuffer));

        scaleCheck = optional.addCheckbox(guiName(params.scale));
        scaleField = optional.addField();
        downsampleCheck = optional.addCheckbox(guiName(params.downsample));
        downsampleField = optional.addField();
        blurCheckBox = optional.addCheckbox(guiName(params.blur));
        blurRadiusField = optional.addField();
        noiseCheck = optional.addCheckbox(guiName(params.noise));
        noiseField = optional.addField();
        distanceCheck = optional.addCheckbox(guiName(params.distance));
        distanceField = optional.addField();

        bubbleCheck = smooth.addCheckbox(guiName(params.bubble));
        bubbleField = smooth.addField();
        swapCheck = smooth.addCheckbox(guiName(params.swap));
        swapField = smooth.addField();
        splineCheck = smooth.addCheckbox(guiName(params.spline));
        splineField = smooth.addField();

        setupListeners();
        setResizable(false);
        pack();
        setVisible(true);
    }

    private void writeStringFile(String filename, String contents) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            showError("Error while writing \"" + filename + '\"');
        }
    }

    private void writeImageFile(String prefix, BufferedImage image) {
        String filename = prefix + '.' + IMAGE_EXTENSION;
        try {
            ImageIO.write(image, IMAGE_EXTENSION, new File(filename));
        } catch (IOException exception) {
            showError("Error while writing \"" + filename + '\"');
        }
    }

    private void writeResults() {
        writeStringFile(outFolder + "params.json", serializer.toJson(params, ImageCollection.ProgramParams.class));
        for (int i = 0; i < collection.size(); i++) {
            String imagePrefix = outFolder + IMAGE_PREFIX + i;
            writeImageFile(imagePrefix, collection.get(i).getImage());
            String dataFilename = outFolder + DATA_PREFIX + i + ".json";
            writeStringFile(dataFilename, serializer.toJson(collection.get(i), FiberCollection.class));
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void readParamsFile(String filename) {
        try {
            FileReader reader = new FileReader(filename);
            params = deserializer.fromJson(reader, ImageCollection.ProgramParams.class);
            reader.close();
            displayParams();
        } catch (FileNotFoundException e) {
            showError("File \"" + filename + "\" not found");
        } catch (IOException e) {
            showError("Error when reading \"" + filename + '\"');
        } catch (JsonParseException e) {
            showError("Malformed parameters file \"" + filename + '\"');
        }
    }

    private void setupListeners() {
        generateButton.addActionListener((ActionEvent event) ->
        {
            readParams();
            collection = new ImageCollection(params);
            collection.generateFibers();
            writeResults();
            displayIdx = 0;
            displayImage(collection.get(displayIdx).getImage());
        });

        prevButton.addActionListener((ActionEvent event) ->
        {
            if (!collection.isEmpty() && displayIdx > 0) {
                displayIdx--;
                displayImage(collection.get(displayIdx).getImage());
            }
        });
        nextButton.addActionListener((ActionEvent event) ->
        {
            if (!collection.isEmpty() && displayIdx < collection.size() - 1) {
                displayIdx++;
                displayImage(collection.get(displayIdx).getImage());
            }
        });
        lengthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.length);
            params.length = dialog.distribution;
            lengthDistributionLabel.setText(params.length.toString());
        });
        straightnessButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.straightness);
            params.straightness = dialog.distribution;
            straightnessDistributionLabel.setText(params.straightness.toString());
        });
        widthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.width);
            params.width = dialog.distribution;
            widthDistributionLabel.setText(params.width.toString());
        });
        loadButton.addActionListener((ActionEvent event) ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                readParamsFile(chooser.getSelectedFile().getAbsolutePath());
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
            }
        });
    }

    private void initParams() {
        serializer = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();
        deserializer = new GsonBuilder()
                .registerTypeAdapter(Distribution.class, new DistributionSerializer())
                .create();

        readParamsFile(DEFAULTS_FILE);

        params.setNames();
        params.length.setBounds(0, Double.POSITIVE_INFINITY);
        params.straightness.setBounds(0, 1);
        params.width.setBounds(0, Double.POSITIVE_INFINITY);
    }

    private MainWindow() {
        super("Fiber Generator");

        initParams();
        initGUI();
    }


    private void readParams() throws IllegalArgumentException {
        params.nImages.parse(nImagesField.getText(), Integer::parseInt);
        params.nFibers.parse(nFibersField.getText(), Integer::parseInt);
        params.segmentLength.parse(segmentLengthField.getText(), Double::parseDouble);
        params.alignment.parse(alignmentField.getText(), Double::parseDouble);
        params.meanAngle.parse(meanAngleField.getText(), Double::parseDouble);
        params.imageWidth.parse(imageWidthField.getText(), Integer::parseInt);
        params.imageHeight.parse(imageHeightField.getText(), Integer::parseInt);
        params.edgeBuffer.parse(edgeBufferField.getText(), Integer::parseInt);
        params.widthVariability.parse(widthVariabilityField.getText(), Double::parseDouble);

        params.seed.parse(seedCheck.isSelected(), seedField.getText(), Integer::parseInt);
        params.scale.parse(scaleCheck.isSelected(), scaleField.getText(), Double::parseDouble);
        params.downsample.parse(downsampleCheck.isSelected(), downsampleField.getText(), Double::parseDouble);
        params.noise.parse(noiseCheck.isSelected(), noiseField.getText(), Double::parseDouble);
        params.distance.parse(distanceCheck.isSelected(), distanceField.getText(), Double::parseDouble);
        params.bubble.parse(bubbleCheck.isSelected(), bubbleField.getText(), Integer::parseInt);
        params.swap.parse(swapCheck.isSelected(), swapField.getText(), Integer::parseInt);
        params.spline.parse(splineCheck.isSelected(), splineField.getText(), Integer::parseInt);

        params.nImages.verifyGreater(0);
        params.nFibers.verifyGreater(0);
        params.segmentLength.verifyGreater(0.0);
        params.alignment.verifyGreaterEq(0.0);
        params.alignment.verifyLessEq(1.0);
        params.meanAngle.verifyGreaterEq(0.0);
        params.meanAngle.verifyLessEq(180.0);
        params.imageWidth.verifyGreater(0);
        params.imageHeight.verifyGreater(0);
        params.edgeBuffer.verifyGreater(0);
        params.edgeBuffer.verifyLessEq(Math.min(params.imageWidth.getValue() / 2, params.imageHeight.getValue() / 2));
        params.imageWidth.verifyGreaterEq(0);

        params.scale.verifyGreater(0.0);
        params.downsample.verifyGreater(0.0);
        params.noise.verifyGreater(0.0);
        params.distance.verifyGreater(0.0);
        params.bubble.verifyGreater(0);
        params.swap.verifyGreater(0);
        params.spline.verifyGreater(0);
    }


    private void displayParams() {
        nImagesField.setText(Integer.toString(params.nImages.getValue()));
        nFibersField.setText(Integer.toString(params.nFibers.getValue()));
        segmentLengthField.setText(Double.toString(params.segmentLength.getValue()));
        alignmentField.setText(Double.toString(params.alignment.getValue()));
        meanAngleField.setText(Double.toString(params.meanAngle.getValue()));
        imageWidthField.setText(Integer.toString(params.imageWidth.getValue()));
        imageHeightField.setText(Integer.toString(params.imageHeight.getValue()));
        edgeBufferField.setText(Integer.toString(params.edgeBuffer.getValue()));
        widthVariabilityField.setText(Double.toString(params.widthVariability.getValue()));
        seedCheck.setSelected(params.seed.use);
        seedField.setText(Integer.toString(params.seed.getValue()));
        scaleCheck.setSelected(params.scale.use);
        scaleField.setText(Double.toString(params.scale.getValue()));
        downsampleCheck.setSelected(params.downsample.use);
        downsampleField.setText(Double.toString(params.downsample.getValue()));
        blurCheckBox.setSelected(params.blur.use);
        blurRadiusField.setText(Double.toString(params.blur.getValue()));
        noiseCheck.setSelected(params.noise.use);
        noiseField.setText(Double.toString(params.noise.getValue()));
        distanceCheck.setSelected(params.distance.use);
        distanceField.setText(Double.toString(params.distance.getValue()));
        bubbleCheck.setSelected(params.bubble.use);
        bubbleField.setText(Integer.toString(params.bubble.getValue()));
        swapCheck.setSelected(params.swap.use);
        swapField.setText(Integer.toString(params.swap.getValue()));
        splineCheck.setSelected(params.spline.use);
        splineField.setText(Integer.toString(params.spline.getValue()));

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