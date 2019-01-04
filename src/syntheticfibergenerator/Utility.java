package syntheticfibergenerator;

import javax.swing.*;
import java.awt.*;

class Utility {

    static GridBagConstraints newGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static String guiName(Param param) {
        String name = param.getName();
        String uppercase = name.substring(0, 1).toUpperCase() + name.substring(1);
        return uppercase + ":";
    }

    static double sq(double val) {
        return val * val;
    }
}
