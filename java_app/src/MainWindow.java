import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import javax.swing.*;
import java.util.ArrayList;


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

    private ArrayList<BufferedImage> imageStack;
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
        pack();
        setVisible(true);

        buttonGenerate.addActionListener((ActionEvent event) ->
        {
            int nImages;
            FiberCollectionParams params = new FiberCollectionParams();
            try
            {
                nImages = IOUtility.tryParseInt(fieldNImage.getText());
                params.nFibers = IOUtility.tryParseInt(fieldNFiber.getText());
                params.meanLength = IOUtility.tryParseInt(fieldMeanLength.getText());
                params.segmentLength = IOUtility.tryParseDouble(fieldSegmentLength.getText());
                params.meanStraightness = IOUtility.tryParseDouble(fieldMeanStraightness.getText());
                params.imageWidth = IOUtility.tryParseInt(fieldImageWidth.getText());
                params.imageHeight = IOUtility.tryParseInt(fieldImageHeight.getText());

                IOUtility.verifyValue(nImages, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.nFibers, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.meanLength, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.segmentLength, 0.0, Double.POSITIVE_INFINITY);
                IOUtility.verifyValue(params.meanStraightness, 0.0, 1.0);
                if (params.meanStraightness == 0.0)
                {
                    throw new IllegalArgumentException("Mean straightness must be nonzero");
                }
                IOUtility.verifyValue(params.imageWidth, 1, Integer.MAX_VALUE);
                IOUtility.verifyValue(params.imageHeight, 1, Integer.MAX_VALUE);
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
                BufferedImage image = fiberCollection.drawFibers();
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
        fieldNFiber.setText("50");
        fieldMeanLength.setText(Integer.toString(IMAGE_PANEL_SIZE / 10));
        fieldSegmentLength.setText("5.0");
        fieldMeanStraightness.setText("0.8");
        fieldImageWidth.setText(Integer.toString(IMAGE_PANEL_SIZE));
        fieldImageHeight.setText(Integer.toString(IMAGE_PANEL_SIZE));
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