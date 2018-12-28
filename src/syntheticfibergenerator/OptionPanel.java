package syntheticfibergenerator;

import javax.swing.*;
import java.awt.*;

public class OptionPanel extends JPanel {

    private int yLeft = 0;
    private int yRight = 0; // TODO: Just have one y tracker and require a certain order on inserts

    OptionPanel(String name) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(name));
    }

    JTextField addFieldLine(String name) {
        addLabel(name);
        return addField();
    }

    JCheckBox addCheckbox(String name) { // TODO: Clean up duplicate code
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 5); // TODO: Extract to constant
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = yLeft;
        JCheckBox box = new JCheckBox(name);
        add(box, gbc);
        yLeft++;
        return box;
    }

    JTextField addField() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 0); // TODO: Extract to constant
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = yRight;
        JTextField field = new JTextField(5); // TODO: Extract to constant
        add(field, gbc);
        yRight++;
        return field;
    }

    JLabel addLabel(String name) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 5); // TODO: Extract to constant
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = yLeft;
        JLabel label = new JLabel(name);
        add(label, gbc);
        yLeft++;
        return label;
    }

    JButton addButton(String name) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 0); // TODO: Extract to constant
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = yRight;
        JButton button = new JButton(name);
        add(button, gbc);
        yRight++;
        return button;
    }

    /**
     * Note this will behave strangely if yLeft and yRight are not the same.
     * @param contents
     * @return
     */
    JTextField addFieldRow(String contents) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = yLeft;
        gbc.gridwidth = 2;
        JTextField field = new JTextField(contents);
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        field.setOpaque(false);
        field.setEditable(false);
        add(field, gbc);
        yLeft++;
        yRight++;
        return field;
    }
}
