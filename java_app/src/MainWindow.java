import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.io.*;

public class MainWindow extends JFrame
{
    private JPanel panelMain;
    private JButton prevButton;
    private JButton nextButton;
    private JButton buttonGenerate;
    private JTextField fieldNFiber;
    private JTextField fieldMeanLength;
    private JLabel currentImage;
    private JLabel labelNFiber;
    private JLabel labelMeanLength;
    private JPanel panelRight;
    private JPanel panelLeft;
    private JPanel panelNextPrev;
    private JPanel panelSettings;
    private JTextField fieldImageWidth;
    private JTextField fieldImageHeight;
    private JLabel labelImageWidth;
    private JLabel labelImageHeight;
    private JTextField fieldNImage;
    private JLabel labelNImage;
    private JLabel imageDisplay;

    ArrayList<BufferedImage> imageStack;
    int displayedImage;

    private final int IMAGE_PANEL_SIZE = 400;
    private final String IMAGE_PREFIX = "images" + File.separator;
    private final String DATA_PREFIX = "data" + File.separator;

    private int tryParseInt(String text, boolean positiveRequired) throws IllegalArgumentException
    {
        int output;
        try
        {
            output = Integer.parseInt(text);
        }
        catch (NumberFormatException exception)
        {
            JOptionPane.showMessageDialog(null, "Parse error: " + text + " is not a valid integer",
                    "Error", JOptionPane.ERROR_MESSAGE);
            throw exception;
        }
        if (positiveRequired && output <= 0)
        {
            JOptionPane.showMessageDialog(null, "Invalid argument: " + text + " must be positive",
                    "Error", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("Argument must be positive");
        }
        return output;
    }

    private double tryParseDouble(String text) throws IllegalArgumentException
    {
        try
        {
            return Double.parseDouble(text);
        }
        catch (NumberFormatException exception)
        {
            JOptionPane.showMessageDialog(null, "Parse error: " + text + " is not a valid double",
                    "Error", JOptionPane.ERROR_MESSAGE);
            throw exception;
        }
    }

    private void displayImage(BufferedImage image)
    {
        AffineTransform transform = new AffineTransform();
        double xScale = (double) IMAGE_PANEL_SIZE / (double) image.getWidth();
        double yScale = (double) IMAGE_PANEL_SIZE / (double) image.getHeight();
        double scale = Math.min(xScale, yScale);
        transform.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(transform,
                AffineTransformOp.TYPE_BILINEAR);
        BufferedImage scaled = scaleOp.filter(image, null);

        Icon icon = new ImageIcon(scaled);
        currentImage.setText(null);
        currentImage.setIcon(icon);
        pack();
    }

    private void saveImage(BufferedImage image, String name) throws IOException
    {
        File outFile = new File(name);
        ImageIO.write(image, "png", outFile);
    }

    private void saveData(AllFibers fibers, String name)
    {
        try
        {
            FileWriter writer = new FileWriter(name);
            writer.write(fibers.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException ex)
        {
            // TODO: Do something better here
            System.out.println("Unable to write to output file " + name);
        }
    }

    private MainWindow()
    {
        super("Fiber Generator");
        currentImage.setPreferredSize(new Dimension(IMAGE_PANEL_SIZE, IMAGE_PANEL_SIZE));
        setContentPane(panelMain);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        imageStack = new ArrayList<>();

        fieldNImage.setText("10");
        fieldNFiber.setText("50");
        fieldMeanLength.setText(Integer.toString(IMAGE_PANEL_SIZE / 2));
        fieldImageWidth.setText(Integer.toString(IMAGE_PANEL_SIZE));
        fieldImageHeight.setText(Integer.toString(IMAGE_PANEL_SIZE));

        pack();
        setVisible(true);

        buttonGenerate.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int nImages;
                ImageParams imageParams = new ImageParams();
                try
                {
                    nImages = tryParseInt(fieldNImage.getText(), true);
                    imageParams.nFibers = tryParseInt(fieldNFiber.getText(), true);
                    imageParams.meanLength = tryParseDouble(fieldMeanLength.getText());
                    imageParams.imageHeight = tryParseInt(fieldImageHeight.getText(), true);
                    imageParams.imageWidth = tryParseInt(fieldImageWidth.getText(), true);
                }
                catch (IllegalArgumentException exception)
                {
                    return;
                }

                imageStack.clear();
                for (int i = 0; i < nImages; i++)
                {
                    AllFibers allFibers = new AllFibers(imageParams);
                    allFibers.generate();
                    String dataName = DATA_PREFIX + "data" + i + ".json";
                    saveData(allFibers, dataName);
                    BufferedImage image = allFibers.drawFibers();
                    imageStack.add(image);
                }

                for (int i = 0; i < nImages; i++)
                {
                    String imageName =  IMAGE_PREFIX + "image" + i + ".png";
                    try
                    {
                        saveImage(imageStack.get(i), imageName);
                    }
                    catch (IOException exception)
                    {
                        JOptionPane.showMessageDialog(null, "Error: Unable to write file " + imageName,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }

                displayedImage = 0;
                displayImage(imageStack.get(displayedImage));
            }
        });

        prevButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (imageStack.isEmpty() || displayedImage == 0)
                {
                    return;
                }
                else
                {
                    displayedImage--;
                    displayImage(imageStack.get(displayedImage));
                }
            }
        });

        nextButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (imageStack.isEmpty() || displayedImage == imageStack.size() - 1)
                {
                    return;
                }
                else
                {
                    displayedImage++;
                    displayImage(imageStack.get(displayedImage));
                }
            }
        });
    }

    public static void main(String[] args)
    {
        new MainWindow();
    }
}