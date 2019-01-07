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
import java.text.ParseException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MainWindow extends JFrame {

    // Can be modified via the "Output location" button, therefore not final
    private String outFolder = "output" + File.separator;

    // Save serializer and deserializer so we don't have to re-construct them
    private Gson serializer;
    private Gson deserializer;

    // Current parameters, generated data, and index of displayed image
    private ImageCollection.Params params;
    private ImageCollection collection;
    private int displayIndex;

    // Elements of the display panel
    private JLabel imageDisplay;
    private JButton prevButton;
    private JButton nextButton;

    private JButton generateButton;

    // Elements of the "Session" panel
    private JButton loadButton;
    private JButton saveButton;
    private JTextField pathDisplay;
    private JTextField nImagesField;
    private JCheckBox seedCheck;
    private JTextField seedField;

    // Elements of the "Distributions" panel
    private JButton lengthButton;
    private JTextField lengthDisplay;
    private JButton widthButton;
    private JTextField widthDisplay;
    private JButton straightButton;
    private JTextField straightDisplay;

    // Elements of the "Values" panel
    private JTextField nFibersField;
    private JTextField segmentField;
    private JTextField widthChangeField;
    private JTextField alignmentField;
    private JTextField meanAngleField;

    // Elements of the "Required" panel
    private JTextField imageWidthField;
    private JTextField imageHeightField;
    private JTextField imageBufferField;

    // Elements of the "Optional" panel
    private JCheckBox scaleCheck;
    private JTextField scaleField;
    private JCheckBox sampleCheck;
    private JTextField sampleField;
    private JCheckBox blurCheck;
    private JTextField blurField;
    private JCheckBox noiseCheck;
    private JTextField noiseField;
    private JCheckBox distanceCheck;
    private JTextField distanceField;

    // Elements of the "Smoothing" panel
    private JCheckBox bubbleCheck;
    private JTextField bubbleField;
    private JCheckBox swapCheck;
    private JTextField swapField;
    private JCheckBox splineCheck;
    private JTextField splineField;

    // Constant definitions
    private static final int IMAGE_DISPLAY_SIZE = 512;
    private static final String DEFAULTS_FILE = "defaults.json";
    private static final String DATA_PREFIX = "data";
    private static final String IMAGE_PREFIX = "image";
    private static final String IMAGE_EXT = "tiff";


    public static void main(String[] args) {
        new MainWindow();
    }

    private MainWindow() {
        super("Fiber Generator");
        initParams();
        initGUI();
        displayParams();
        setVisible(true);
    }

    private void initParams() {
        serializer = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();
        deserializer = new GsonBuilder()
                .registerTypeAdapter(Distribution.class, new Distribution.Serializer())
                .create();

        readParamsFile(DEFAULTS_FILE);

        params.length.setBounds(0, Double.POSITIVE_INFINITY);
        params.straightness.setBounds(0, 1);
        params.width.setBounds(0, Double.POSITIVE_INFINITY);
    }

    private void initGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = MiscUtility.newGBC();

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

        gbc = MiscUtility.newGBC();

        JPanel generationPanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Generation", null, generationPanel);
        JPanel structurePanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Structure", null, structurePanel);
        JPanel appearancePanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Appearance", null, appearancePanel);

        imageDisplay = createImageDisplay();
        gbc.gridwidth = 2;
        displayPanel.add(imageDisplay, gbc);

        gbc = MiscUtility.newGBC();

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

        gbc = MiscUtility.newGBC();

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

        loadButton = session.addButtonLine(
                "Parameters:", "Choose parameters file to restore a previous session", "Open...");
        saveButton = session.addButtonLine("Output location:", "Choose directory for output", "Open...");
        pathDisplay = session.addDisplayField();
        nImagesField = session.addFieldLine(params.nImages);
        seedCheck = session.addCheckBox(params.seed);
        seedField = session.addField();

        lengthButton = distribution.addButtonLine(
                "Length distribution:", "Distribution of fiber lengths in pixels", "Modify...");
        lengthDisplay = distribution.addDisplayField();
        widthButton = distribution.addButtonLine(
                "Width distribution:", "Distribution of starting widths in pixels", "Modify...");
        widthDisplay = distribution.addDisplayField();
        straightButton = distribution.addButtonLine(
                "Straightness distribution:", "Distribution of fiber straightnesses", "Modify...");
        straightDisplay = distribution.addDisplayField();

        nFibersField = values.addFieldLine(params.nFibers);
        segmentField = values.addFieldLine(params.segmentLength);
        widthChangeField = values.addFieldLine(params.widthChange);
        alignmentField = values.addFieldLine(params.alignment);
        meanAngleField = values.addFieldLine(params.meanAngle);

        imageWidthField = required.addFieldLine(params.imageWidth);
        imageHeightField = required.addFieldLine(params.imageHeight);
        imageBufferField = required.addFieldLine(params.imageBuffer);

        scaleCheck = optional.addCheckBox(params.scale);
        scaleField = optional.addField();
        sampleCheck = optional.addCheckBox(params.downSample);
        sampleField = optional.addField();
        blurCheck = optional.addCheckBox(params.blur);
        blurField = optional.addField();
        noiseCheck = optional.addCheckBox(params.noise);
        noiseField = optional.addField();
        distanceCheck = optional.addCheckBox(params.distance);
        distanceField = optional.addField();

        bubbleCheck = smooth.addCheckBox(params.bubble);
        bubbleField = smooth.addField();
        swapCheck = smooth.addCheckBox(params.swap);
        swapField = smooth.addField();
        splineCheck = smooth.addCheckBox(params.spline);
        splineField = smooth.addField();

        setupListeners();
        setResizable(false);
        pack();
    }

    private void displayParams() {
        pathDisplay.setPreferredSize(pathDisplay.getSize());
        pathDisplay.setText(outFolder);
        pathDisplay.setToolTipText(outFolder);

        nImagesField.setText(params.nImages.getString());
        seedCheck.setSelected(params.seed.use);
        seedField.setText(params.seed.getString());

        lengthDisplay.setPreferredSize(lengthDisplay.getSize());
        lengthDisplay.setText(params.length.getString());
        widthDisplay.setPreferredSize(widthDisplay.getSize());
        widthDisplay.setText(params.width.getString());
        straightDisplay.setPreferredSize(straightDisplay.getSize());
        straightDisplay.setText(params.straightness.getString());

        nFibersField.setText(params.nFibers.getString());
        segmentField.setText(params.segmentLength.getString());
        widthChangeField.setText(params.widthChange.getString());
        alignmentField.setText(params.alignment.getString());
        meanAngleField.setText(params.meanAngle.getString());

        imageWidthField.setText(params.imageWidth.getString());
        imageHeightField.setText(params.imageHeight.getString());
        imageBufferField.setText(params.imageBuffer.getString());

        scaleCheck.setSelected(params.scale.use);
        scaleField.setText(params.scale.getString());
        sampleCheck.setSelected(params.downSample.use);
        sampleField.setText(params.downSample.getString());
        blurCheck.setSelected(params.blur.use);
        blurField.setText(params.blur.getString());
        noiseCheck.setSelected(params.noise.use);
        noiseField.setText(params.noise.getString());
        distanceCheck.setSelected(params.distance.use);
        distanceField.setText(params.distance.getString());

        bubbleCheck.setSelected(params.bubble.use);
        bubbleField.setText(params.bubble.getString());
        swapCheck.setSelected(params.swap.use);
        swapField.setText(params.swap.getString());
        splineCheck.setSelected(params.spline.use);
        splineField.setText(params.spline.getString());
    }

    private void parseParams() throws ParseException {
        params.nImages.parse(nImagesField.getText(), Integer::parseInt);
        params.seed.parse(seedCheck.isSelected(), seedField.getText(), Long::parseLong);

        params.nFibers.parse(nFibersField.getText(), Integer::parseInt);
        params.segmentLength.parse(segmentField.getText(), Double::parseDouble);
        params.widthChange.parse(widthChangeField.getText(), Double::parseDouble);
        params.alignment.parse(alignmentField.getText(), Double::parseDouble);
        params.meanAngle.parse(meanAngleField.getText(), Double::parseDouble);

        params.imageWidth.parse(imageWidthField.getText(), Integer::parseInt);
        params.imageHeight.parse(imageHeightField.getText(), Integer::parseInt);
        params.imageBuffer.parse(imageBufferField.getText(), Integer::parseInt);

        params.scale.parse(scaleCheck.isSelected(), scaleField.getText(), Double::parseDouble);
        params.downSample.parse(sampleCheck.isSelected(), sampleField.getText(), Double::parseDouble);
        params.blur.parse(blurCheck.isSelected(), blurField.getText(), Double::parseDouble);
        params.noise.parse(noiseCheck.isSelected(), noiseField.getText(), Double::parseDouble);
        params.distance.parse(distanceCheck.isSelected(), distanceField.getText(), Double::parseDouble);

        params.bubble.parse(bubbleCheck.isSelected(), bubbleField.getText(), Integer::parseInt);
        params.swap.parse(swapCheck.isSelected(), swapField.getText(), Integer::parseInt);
        params.spline.parse(splineCheck.isSelected(), splineField.getText(), Integer::parseInt);
    }

    private void verifyParams() throws IllegalArgumentException {
        params.nImages.verify(0, Param::greater);

        params.nFibers.verify(0, Param::greater);
        params.segmentLength.verify(0.0, Param::greater);
        params.widthChange.verify(0.0, Param::greaterEq);
        params.alignment.verify(0.0, Param::greaterEq);
        params.alignment.verify(1.0, Param::lessEq);
        params.meanAngle.verify(0.0, Param::greaterEq);
        params.meanAngle.verify(180.0, Param::lessEq);

        params.imageWidth.verify(0, Param::greater);
        params.imageHeight.verify(0, Param::greater);
        params.imageBuffer.verify(0, Param::greater);

        params.scale.verify(0.0, Param::greater);
        params.downSample.verify(0.0, Param::greater);
        params.blur.verify(0.0, Param::greater);
        params.noise.verify(0.0, Param::greater);
        params.distance.verify(0.0, Param::greater);

        params.bubble.verify(0, Param::greater);
        params.swap.verify(0, Param::greater);
        params.spline.verify(0, Param::greater);
    }

    private void readParamsFile(String filename) {
        try {
            FileReader reader = new FileReader(filename);
            params = deserializer.fromJson(reader, ImageCollection.Params.class);
            reader.close();
        } catch (FileNotFoundException e) {
            MiscUtility.showError("File \"" + filename + "\" not found");
        } catch (IOException e) {
            MiscUtility.showError("Error when reading \"" + filename + '\"');
        } catch (JsonParseException e) {
            MiscUtility.showError("Malformed parameters file \"" + filename + '\"');
        }
        params.setNames();
        params.setHints();
    }

    private void writeResults() {
        writeStringFile(outFolder + "params.json", serializer.toJson(params, ImageCollection.Params.class));
        for (int i = 0; i < collection.size(); i++) {
            String imagePrefix = outFolder + IMAGE_PREFIX + i;
            writeImageFile(imagePrefix, collection.getImage(i));
            String dataFilename = outFolder + DATA_PREFIX + i + ".json";
            writeStringFile(dataFilename, serializer.toJson(collection.get(i), FiberImage.class));
        }
    }

    private void writeStringFile(String filename, String contents) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            MiscUtility.showError("Error while writing \"" + filename + '\"');
        }
    }

    private void writeImageFile(String prefix, BufferedImage image) {
        String filename = prefix + '.' + IMAGE_EXT;
        try {
            ImageIO.write(image, IMAGE_EXT, new File(filename));
        } catch (IOException e) {
            MiscUtility.showError("Error while writing \"" + filename + '\"');
        }
    }

    private void displayImage(BufferedImage image) {
        double xScale = (double) IMAGE_DISPLAY_SIZE / image.getWidth();
        double yScale = (double) IMAGE_DISPLAY_SIZE / image.getHeight();
        double scale = Math.min(xScale, yScale);
        BufferedImage scaled = ImageUtility.scale(image, scale, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        Icon icon = new ImageIcon(scaled);
        imageDisplay.setText(null);
        imageDisplay.setIcon(icon);
    }

    private void setupListeners() {
        generateButton.addActionListener((ActionEvent e) -> generatePressed());
        prevButton.addActionListener((ActionEvent e) -> prevPressed());
        nextButton.addActionListener((ActionEvent e) -> nextPressed());
        lengthButton.addActionListener((ActionEvent e) -> lengthPressed());
        straightButton.addActionListener((ActionEvent e) -> straightPressed());
        widthButton.addActionListener((ActionEvent e) -> widthPressed());
        loadButton.addActionListener((ActionEvent e) -> loadPressed());
        saveButton.addActionListener((ActionEvent e) -> savePressed());
    }

    private void generatePressed() {
        try {
            parseParams();
            verifyParams();
        } catch (Exception e) {
            MiscUtility.showError(e.getMessage());
            return;
        }
        collection = new ImageCollection(params);
        try {
            collection.generateImages();
        } catch (ArithmeticException e) {
            MiscUtility.showError("Unable to construct fibers - change parameters and try again");
            return;
        }
        writeResults();
        displayIndex = 0;
        displayImage(collection.getImage(displayIndex));
    }

    private void prevPressed() {
        if (!collection.isEmpty() && displayIndex > 0) {
            displayIndex--;
            displayImage(collection.getImage(displayIndex));
        }
    }

    private void nextPressed() {
        if (!collection.isEmpty() && displayIndex < collection.size() - 1) {
            displayIndex++;
            displayImage(collection.getImage(displayIndex));
        }
    }

    private void loadPressed() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            readParamsFile(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void savePressed() {
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
    }

    private void lengthPressed() {
        DistributionDialog dialog = new DistributionDialog(params.length);
        params.length = dialog.distribution;
        displayParams();
    }

    private void widthPressed() {
        DistributionDialog dialog = new DistributionDialog(params.width);
        params.width = dialog.distribution;
        displayParams();
    }

    private void straightPressed() {
        DistributionDialog dialog = new DistributionDialog(params.straightness);
        params.straightness = dialog.distribution;
        displayParams();
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
}