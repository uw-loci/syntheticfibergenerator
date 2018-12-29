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

    // Constant definitions
    private static final int IMAGE_DISPLAY_SIZE = 512;
    private static final String DEFAULTS_FILE = "defaults.json";
    private static final String DATA_PREFIX = "data";
    private static final String IMAGE_PREFIX = "image";
    private static final String IMAGE_EXT = "tiff";

    // Can be modified via the "Output location" button, therefore not final
    private String outFolder = "output" + File.separator;

    // Save serializer and deserializer so we don't have to re-construct them
    private Gson serializer;
    private Gson deserializer;

    // Current parameters, generated data, and index of displayed image
    private ImageCollection.Params params;
    private ImageCollection collection;
    private int displayIdx;

    // Elements of the display panel
    private JLabel imageDisplay;
    private JButton prevButton;
    private JButton nextButton;

    private JButton generateButton;

    // Elements of the "Session" panel
    private JButton loadButton;
    private JButton saveButton;
    private JTextField outputPathDisplay;
    private JTextField nImagesField;
    private JCheckBox seedCheck;
    private JTextField seedField;

    // Elements of the "Distribution" panel
    private JButton lengthButton;
    private JTextField lengthDisplay;
    private JButton widthButton;
    private JTextField widthDisplay;
    private JButton straightButton;
    private JTextField straightDisplay;

    private JTextField nFibersField;
    private JTextField segmentLengthField;
    private JTextField alignmentField;
    private JTextField meanAngleField;
    private JTextField imageWidthField;
    private JTextField imageHeightField;
    private JTextField edgeBufferField;
    private JTextField widthVariabilityField;
    private JTextField scaleField;
    private JTextField downsampleField;
    private JTextField blurRadiusField;
    private JTextField noiseField;
    private JTextField distanceField;




    private JCheckBox scaleCheck;
    private JCheckBox downsampleCheck;
    private JCheckBox blurCheck;
    private JCheckBox noiseCheck;
    private JCheckBox distanceCheck;

    private JTextField bubbleField;
    private JTextField swapField;
    private JTextField splineField;

    private JCheckBox bubbleCheck;
    private JCheckBox swapCheck;
    private JCheckBox splineCheck;


    private MainWindow() {
        super("Fiber Generator");
        initParams();
        initGUI();
        displayParams();
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

    private void initGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = newGBC();

        JPanel displayPanel = new JPanel(new GridBagLayout());
        gbc.gridheight = 2;
        add(displayPanel, gbc);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridheight = 1;
        gbc.gridx++;
        add(tabbedPane, gbc);

        generateButton = new JButton("Generate...");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridy++;
        add(generateButton, gbc);

        gbc = newGBC();

        JPanel generationPanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Generation", null, generationPanel);
        JPanel structurePanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Structure", null, structurePanel);
        JPanel appearancePanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Appearance", null, appearancePanel);

        imageDisplay = createImageDisplay();
        gbc.gridwidth = 2;
        displayPanel.add(imageDisplay, gbc);

        gbc = newGBC();

        prevButton = new JButton("Previous");
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 100;
        gbc.gridy = 1;
        displayPanel.add(prevButton, gbc);

        nextButton = new JButton("Next");
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        nextButton.setPreferredSize(prevButton.getPreferredSize());
        displayPanel.add(nextButton, gbc);

        gbc = newGBC();

        OptionPanel session = new OptionPanel("Session");
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.ipadx = 10;
        gbc.ipady = 10;
        gbc.weightx = 100;
        gbc.weighty = 100;
        generationPanel.add(session, gbc);

        OptionPanel distribution = new OptionPanel("Distributions");
        gbc.weighty = 0;
        gbc.gridy = 0;
        structurePanel.add(distribution, gbc);

        OptionPanel values = new OptionPanel("Values");
        gbc.weighty = 100;
        gbc.gridy++;
        structurePanel.add(values, gbc);

        OptionPanel required = new OptionPanel("Required");
        gbc.weighty = 0;
        gbc.gridy = 0;
        appearancePanel.add(required, gbc);

        OptionPanel optional = new OptionPanel("Optional");
        gbc.gridy++;
        appearancePanel.add(optional, gbc);

        OptionPanel smooth = new OptionPanel("Smoothing");
        gbc.weighty = 100;
        gbc.gridy++;
        appearancePanel.add(smooth, gbc);

        loadButton = session.addButtonLine("Parameters:", "Open...");
        saveButton = session.addButtonLine("Output location:", "Open...");
        outputPathDisplay = session.addDisplayField(outFolder);
        nImagesField = session.addFieldLine(guiName(params.nImages));
        seedCheck = session.addCheckBox(guiName(params.seed));
        seedField = session.addField();

        lengthButton = distribution.addButtonLine("Length distribution:", "Modify...");
        lengthDisplay = distribution.addDisplayField(params.length.toString());
        widthButton = distribution.addButtonLine("Width distribution:", "Modify...");
        widthDisplay = distribution.addDisplayField(params.width.toString());
        straightButton = distribution.addButtonLine("Straightness distribution:", "Modify...");
        straightDisplay = distribution.addDisplayField(params.straightness.toString());

        widthVariabilityField = values.addFieldLine(guiName(params.widthVariability));
        nFibersField = values.addFieldLine(guiName(params.nFibers));
        alignmentField = values.addFieldLine(guiName(params.alignment));
        meanAngleField = values.addFieldLine(guiName(params.meanAngle));
        segmentLengthField = values.addFieldLine(guiName(params.segmentLength));

        imageHeightField = required.addFieldLine(guiName(params.imageHeight));
        imageWidthField = required.addFieldLine(guiName(params.imageWidth));
        edgeBufferField = required.addFieldLine(guiName(params.edgeBuffer));

        scaleCheck = optional.addCheckBox(guiName(params.scale));
        scaleField = optional.addField();
        downsampleCheck = optional.addCheckBox(guiName(params.downsample));
        downsampleField = optional.addField();
        blurCheck = optional.addCheckBox(guiName(params.blur));
        blurRadiusField = optional.addField();
        noiseCheck = optional.addCheckBox(guiName(params.noise));
        noiseField = optional.addField();
        distanceCheck = optional.addCheckBox(guiName(params.distance));
        distanceField = optional.addField();

        bubbleCheck = smooth.addCheckBox(guiName(params.bubble));
        bubbleField = smooth.addField();
        swapCheck = smooth.addCheckBox(guiName(params.swap));
        swapField = smooth.addField();
        splineCheck = smooth.addCheckBox(guiName(params.spline));
        splineField = smooth.addField();

        setupListeners();
        setResizable(false);
        pack();
        setVisible(true);
    }

    private static JLabel createImageDisplay() {
        JLabel output = new JLabel("Press \"Generate\" to view images");
        output.setHorizontalAlignment(JLabel.CENTER);
        output.setForeground(Color.WHITE);
        output.setBackground(Color.BLACK);
        output.setOpaque(true);
        output.setPreferredSize(new Dimension(IMAGE_DISPLAY_SIZE, IMAGE_DISPLAY_SIZE));
        return output;
    }

    private static GridBagConstraints newGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    private String guiName(ImageCollection.Params.Param param) {
        String name = param.getName();
        String uppercase = name.substring(0, 1).toUpperCase() + name.substring(1);
        return uppercase + ":";
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
        String filename = prefix + '.' + IMAGE_EXT;
        try {
            ImageIO.write(image, IMAGE_EXT, new File(filename));
        } catch (IOException exception) {
            showError("Error while writing \"" + filename + '\"');
        }
    }

    private void writeResults() {
        writeStringFile(outFolder + "params.json", serializer.toJson(params, ImageCollection.Params.class));
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
            params = deserializer.fromJson(reader, ImageCollection.Params.class);
            reader.close();
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
            lengthDisplay.setText(params.length.toString());
        });
        straightButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.straightness);
            params.straightness = dialog.distribution;
            straightDisplay.setText(params.straightness.toString());
        });
        widthButton.addActionListener((ActionEvent event) ->
        {
            DistributionDialog dialog = new DistributionDialog(params.width);
            params.width = dialog.distribution;
            widthDisplay.setText(params.width.toString());
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
        blurCheck.setSelected(params.blur.use);
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
        outputPathDisplay.setPreferredSize(outputPathDisplay.getSize());
        widthDisplay.setPreferredSize(widthDisplay.getSize());
        lengthDisplay.setPreferredSize(lengthDisplay.getSize());
        straightDisplay.setPreferredSize(straightDisplay.getSize());

        outputPathDisplay.setToolTipText(outFolder);
        outputPathDisplay.setText(outFolder);
        widthDisplay.setText(params.width.toString());
        lengthDisplay.setText(params.length.toString());
        straightDisplay.setText(params.straightness.toString());
    }


    private void displayImage(BufferedImage image) {
        double xScale = (double) IMAGE_DISPLAY_SIZE / image.getWidth();
        double yScale = (double) IMAGE_DISPLAY_SIZE / image.getHeight();
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