package syntheticfibergenerator; // TODO: Cleaned up

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


class DistributionDialog extends JDialog {

    Distribution distribution;

    // Saves the original distribution in case "Cancel" is pressed
    private Distribution original;

    // GUI elements
    private JComboBox<String> comboBox;
    private JLabel label1;
    private JTextField field1;
    private JLabel label2;
    private JTextField field2;
    private JButton okayButton;
    private JButton cancelButton;


    DistributionDialog(Distribution distribution) {
        super();
        this.original = distribution;
        this.distribution = distribution;
        initGUI();
        displayDistribution();
        setVisible(true);
    }

    private void initGUI() {
        setModal(true);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = GUIUtility.newGBC();

        String[] options = {Gaussian.typename, Uniform.typename};
        comboBox = new JComboBox<>(options);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        add(comboBox, gbc);

        gbc = GUIUtility.newGBC();

        OptionPanel panel = new OptionPanel();
        gbc.gridwidth = 2;
        gbc.gridy = 1;
        add(panel, gbc);

        panel.addLabel("Lower bound:");
        panel.addReadOnlyField().setText(Double.toString(distribution.lowerBound));
        panel.addLabel("Upper bound:");
        panel.addReadOnlyField().setText(Double.toString(distribution.upperBound));
        label1 = panel.addLabel("");
        field1 = panel.addField();
        label2 = panel.addLabel("");
        field2 = panel.addField();

        gbc = GUIUtility.newGBC();

        cancelButton = new JButton("Cancel");
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 100;
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(cancelButton, gbc);

        okayButton = new JButton("Okay");
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        okayButton.setPreferredSize(cancelButton.getPreferredSize());
        add(okayButton, gbc);

        setupListeners();
        setResizable(false);
        pack();
    }

    private void displayDistribution() {
        comboBox.setSelectedItem(distribution.getType());
        if (distribution instanceof Gaussian) {
            Gaussian gaussian = (Gaussian) distribution;
            label1.setText(GUIUtility.guiName(gaussian.mean));
            field1.setText(gaussian.mean.getString());
            label2.setText(GUIUtility.guiName(gaussian.sigma));
            field2.setText(gaussian.sigma.getString());
        } else if (distribution instanceof Uniform) {
            Uniform uniform = (Uniform) distribution;
            label1.setText(GUIUtility.guiName(uniform.min));
            field1.setText(uniform.min.getString());
            label2.setText(GUIUtility.guiName(uniform.max));
            field2.setText(uniform.max.getString());
        }
    }

    private void setupListeners() {
        comboBox.addActionListener((ActionEvent e) -> selectionChanged());
        okayButton.addActionListener((ActionEvent e) -> okayPressed());
        cancelButton.addActionListener((ActionEvent e) -> cancelPressed());
    }

    private void selectionChanged() {
        if (comboBox.getSelectedItem() != null) {
            String selection = comboBox.getSelectedItem().toString();
            if (!selection.equals(distribution.getType())) {
                if (selection.equals(Gaussian.typename)) {
                    distribution = new Gaussian(distribution.lowerBound, distribution.upperBound);
                } else if (selection.equals(Uniform.typename)) {
                    distribution = new Uniform(distribution.lowerBound, distribution.upperBound);
                }
            }
            displayDistribution();
        }
    }

    private void okayPressed() {
        if (comboBox.getSelectedItem() == null) {
            GUIUtility.showError("No distribution type selected");
        } else {
            String selection = comboBox.getSelectedItem().toString();
            try {
                if (selection.equals(Gaussian.typename)) {
                    Gaussian gaussian = (Gaussian) distribution;
                    gaussian.mean.parse(field1.getText(), Double::parseDouble);
                    gaussian.sigma.parse(field2.getText(), Double::parseDouble);
                } else if (selection.equals(Uniform.typename)) {
                    Uniform uniform = (Uniform) distribution;
                    uniform.min.parse(field1.getText(), Double::parseDouble);
                    uniform.max.parse(field2.getText(), Double::parseDouble);
                }
                dispose();
            } catch (IllegalArgumentException e) {
                GUIUtility.showError(e.getMessage());
            }
        }
    }

    private void cancelPressed() {
        this.distribution = original;
        dispose();
    }
}
