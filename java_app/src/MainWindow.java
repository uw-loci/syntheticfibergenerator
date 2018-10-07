import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow
{
    private JPanel mainPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JButton generateButton;
    private JTextField nFiberField;
    private JTextField fiberLengthField;
    private JLabel currentImage;
    private JLabel nFiberLabel;
    private JLabel fiberLengthLabel;
    private JPanel rightPanel;
    private JPanel leftPanel;
    private JPanel prevNextPanel;
    private JPanel settingsPanel;
    private JLabel imageDisplay;


    private MainWindow()
    {
        generateButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("Chose to generate " + nFiberField.getText() + " fibers");
                System.out.println("Chose an average length of " + fiberLengthField.getText());
                // TODO: Call fiber generator here
            }
        });

        prevButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // currentImage.setIcon(new ImageIcon("SampleImage.jpg"));
                System.out.println("Registered previous button press");
            }
        });

        nextButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // currentImage.setIcon(new ImageIcon("SampleImage.jpg"));
                System.out.println("Registered next button press");
            }
        });
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Fiber Generator");
        MainWindow window = new MainWindow();
        window.currentImage.setIcon(new ImageIcon("resources/SampleImage.jpg")); // Only GIF, JPG, and PNG are supported
        frame.setContentPane(window.mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}