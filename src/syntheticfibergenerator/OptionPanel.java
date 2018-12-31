package syntheticfibergenerator;

import javax.swing.*;
import java.awt.*;

class OptionPanel extends JPanel {

    private static final int FIELD_W = 5;
    private static final int INNER_BUFF = 5;

    private int y = 0;

    OptionPanel(String borderText) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(borderText));
    }

    JButton addButtonLine(String labelText, String buttonText) {
        addLabel(labelText);
        return addButton(buttonText);
    }

    JButton addButton(String labelText) {
        GridBagConstraints gbc = gbcRight();
        JButton button = new JButton(labelText);
        add(button, gbc);
        y++;
        return button;
    }

    JTextField addFieldLine(String labelText) {
        addLabel(labelText);
        return addField();
    }

    JTextField addField() {
        GridBagConstraints gbc = gbcRight();
        JTextField field = new JTextField(FIELD_W);
        add(field, gbc);
        y++;
        return field;
    }

    JTextField addDisplayField() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = y;
        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        field.setOpaque(false);
        field.setEditable(false);
        add(field, gbc);
        y++;
        return field;
    }

    JLabel addLabel(String labelText) {
        GridBagConstraints gbc = gbcLeft();
        JLabel label = new JLabel(labelText);
        add(label, gbc);
        return label;
    }

    JCheckBox addCheckBox(String checkBoxText) {
        GridBagConstraints gbc = gbcLeft();
        JCheckBox box = new JCheckBox(checkBoxText);
        add(box, gbc);
        return box;
    }

    private GridBagConstraints gbcLeft() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, INNER_BUFF);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = y;
        return gbc;
    }

    private GridBagConstraints gbcRight() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, INNER_BUFF, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = y;
        return gbc;
    }
}
