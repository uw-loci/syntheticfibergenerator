package syntheticfibergenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


class DistributionDialog extends JDialog {

    Distribution distribution;

    private JComboBox<String> comboBox;
    private JLabel label1;
    private JLabel label2;
    private JTextField field1;
    private JTextField field2;

    DistributionDialog(Distribution distribution) {
        super();
        this.distribution = distribution;
        initialize();
    }

    private void initialize() {
        setModal(true);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 0;
        String[] options = {Gaussian.typename, Uniform.typename};
        comboBox = new JComboBox<>(options);
        add(comboBox, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;

        gbc.gridy++;
        add(new JLabel("Lower Bound"), gbc);
        gbc.gridy++;
        add(new JLabel("Upper Bound"), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx++;
        gbc.gridy--;
        JTextField lowerBoundField = new JTextField();
        lowerBoundField.setEnabled(false);
        add(lowerBoundField, gbc);

        gbc.gridy++;
        JTextField upperBoundField = new JTextField();
        upperBoundField.setEnabled(false);
        add(upperBoundField, gbc);

        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx--;
        gbc.gridy++;
        label1 = new JLabel("Mean");
        add(label1, gbc);
        gbc.gridy++;
        label2 = new JLabel("Sigma");
        add(label2, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx++;
        gbc.gridy--;
        field1 = new JTextField();
        add(field1, gbc);
        gbc.gridy++;
        field2 = new JTextField();
        add(field2, gbc);

        gbc.gridx--;
        gbc.gridy++;
        JButton okButton = new JButton("Okay");
        add(okButton, gbc);
        gbc.gridx++;
        JButton cancelButton = new JButton("Cancel");
        add(cancelButton, gbc);

        okButton.setPreferredSize(cancelButton.getPreferredSize());

        pack();
        setResizable(false);

        okButton.addActionListener((ActionEvent e) -> okPressed());
        cancelButton.addActionListener((ActionEvent e) -> cancelPressed());

        comboBox.addActionListener((ActionEvent e) -> setDistribution(comboBox.getSelectedItem().toString()));

        lowerBoundField.setText(Double.toString(distribution.lowerBound));
        upperBoundField.setText(Double.toString(distribution.upperBound));

        if (distribution instanceof Gaussian) {
            setDistribution(Gaussian.typename);
        } else if (distribution instanceof Uniform) {
            setDistribution(Uniform.typename);
        }

        setVisible(true);
    }

    private void okPressed() {
        try {
            if (comboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(null, "Defaults file not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (comboBox.getSelectedItem().toString().equals(Gaussian.typename)) {
                double mean = Double.parseDouble(field1.getText()); // TODO: add error checking
                double sigma = Double.parseDouble(field2.getText());
                this.distribution = new Gaussian(mean, sigma, this.distribution);

            } else if (comboBox.getSelectedItem().toString().equals(Uniform.typename)){
                double min = Double.parseDouble(field1.getText());
                double max = Double.parseDouble(field2.getText());
                this.distribution = new Uniform(min, max, this.distribution);
            }
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid parameters");
        }
    }


    private void cancelPressed() {
        dispose();
    }


    private void refresh()
    {
        if (distribution instanceof Gaussian)
        {
            Gaussian gaussian = (Gaussian) distribution;
            field1.setText(Double.toString(gaussian.mean));
            field2.setText(Double.toString(gaussian.sdev));
            label1.setText("Mean");
            label2.setText("Sigma");
        }
        else if (distribution instanceof Uniform)
        {
            Uniform uniform = (Uniform) distribution;
            field1.setText(Double.toString(uniform.min));
            field2.setText(Double.toString(uniform.max));
            label1.setText("Minimum");
            label2.setText("Maximum");
        }
    }


    private void setDistribution(String name)
    {
        if (!name.equals(distribution.getType()))
        {
            if (name.equals(Gaussian.typename)) {
                distribution = new Gaussian(distribution.lowerBound, distribution.upperBound);
            } else if (name.equals(Uniform.typename)) {
                distribution = new Uniform(distribution.lowerBound, distribution.upperBound);
            }
        }
        refresh();
    }
}
