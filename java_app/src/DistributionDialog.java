import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class DistributionDialog
{
    JDialog dialog;

    Distribution distribution;

    JComboBox<String> comboBox;

    JLabel label1;
    JLabel label2;

    JTextField field1;
    JTextField field2;

    DistributionDialog(Distribution distribution)
    {
        this.distribution = distribution;
    }

    private void initialize()
    {
        dialog = new JDialog();
        dialog.setModal(true);

        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 0;
        String[] options = {"Gaussian", "Uniform"};
        comboBox = new JComboBox<>(options);
        if (distribution instanceof Gaussian)
        {
            comboBox.setSelectedItem("Gaussian");
        }
        else if (distribution instanceof Uniform)
        {
            comboBox.setSelectedItem("Uniform");
        }
        dialog.add(comboBox, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = 1;

        gbc.gridy++;
        dialog.add(new JLabel("Lower Bound"), gbc);
        gbc.gridy++;
        dialog.add(new JLabel("Upper Bound"), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx++;
        gbc.gridy--;
        JTextField lowerBoundField = new JTextField(Double.toString(distribution.lowerBound));
        lowerBoundField.setEnabled(false);
        dialog.add(lowerBoundField, gbc);
        gbc.gridy++;
        JTextField upperBoundField = new JTextField(Double.toString(distribution.upperBound));
        upperBoundField.setEnabled(false);
        dialog.add(upperBoundField, gbc);

        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx--;
        gbc.gridy++;
        label1 = new JLabel("Mean");
        dialog.add(label1, gbc);
        gbc.gridy++;
        label2 = new JLabel("Sigma");
        dialog.add(label2, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx++;
        gbc.gridy--;
        field1 = new JTextField();
        dialog.add(field1, gbc);
        gbc.gridy++;
        field2 = new JTextField();
        dialog.add(field2, gbc);

        gbc.gridx--;
        gbc.gridy++;
        JButton okButton = new JButton("Okay");
        dialog.add(okButton, gbc);
        gbc.gridx++;
        JButton cancelButton = new JButton("Cancel");
        dialog.add(cancelButton, gbc);

        okButton.setPreferredSize(cancelButton.getPreferredSize());

        if (distribution instanceof Gaussian)
        {
            setDistribution("Gaussian");
        }
        else if (distribution instanceof Uniform)
        {
            setDistribution("Uniform");
        }

        dialog.pack();
        dialog.setResizable(false);

        okButton.addActionListener((ActionEvent e) -> okPressed());
        cancelButton.addActionListener((ActionEvent e) -> cancelPressed());

        comboBox.addActionListener((ActionEvent e) -> setDistribution(comboBox.getSelectedItem().toString()));
    }

    void showDialog()
    {
        initialize();
        dialog.setVisible(true);
    }

    private void okPressed()
    {
        try
        {
            if (comboBox.getSelectedItem().toString().equals("Gaussian"))
            {
                double mean = Double.parseDouble(field1.getText());
                double sigma = Double.parseDouble(field2.getText());
                this.distribution = new Gaussian(mean, sigma, this.distribution);

            }
            else
            {
                double min = Double.parseDouble(field1.getText());
                double max = Double.parseDouble(field2.getText());
                this.distribution = new Uniform(min, max, this.distribution);
            }
            dialog.dispose();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Invalid parameters");
        }
    }


    private void cancelPressed()
    {
        dialog.dispose();
    }


    private void setDistribution(String name)
    {
        if (name.equals("Gaussian"))
        {
            if (distribution instanceof Gaussian)
            {
                Gaussian gaussian = (Gaussian) distribution;
                field1.setText(Double.toString(gaussian.mean));
                field2.setText(Double.toString(gaussian.sigma));
            }
            else
            {
                field1.setText("");
                field2.setText("");
            }
            label1.setText("Mean");
            label2.setText("Sigma");
        }
        else if (name.equals("Uniform"))
        {
            if (distribution instanceof Uniform)
            {
                Uniform uniform = (Uniform) distribution;
                field1.setText(Double.toString(uniform.min));
                field2.setText(Double.toString(uniform.max));
            }
            else
            {
                field1.setText("");
                field2.setText("");
            }
            label1.setText("Minimum");
            label2.setText("Maximum");
        }
    }
}
