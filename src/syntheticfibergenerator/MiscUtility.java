package syntheticfibergenerator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


class MiscUtility {

    static GridBagConstraints newGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    static String guiName(Param param) {
        String name = param.getName();
        String uppercase = name.substring(0, 1).toUpperCase() + name.substring(1);
        return uppercase + ":";
    }

    static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static double sq(double val) {
        return val * val;
    }

    static int sq(int val) {
        return val * val;
    }

    static ArrayList<Vector> toDeltas(ArrayList<Vector> points) {
        ArrayList<Vector> deltas = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            deltas.add(points.get(i + 1).subtract(points.get(i)));
        }
        return deltas;
    }

    static ArrayList<Vector> fromDeltas(ArrayList<Vector> deltas, Vector start) {
        ArrayList<Vector> points = new ArrayList<>();
        points.add(start);
        for (int i = 0; i < deltas.size(); i++) {
            points.set(i + 1, points.get(i).add(deltas.get(i)));
        }
        return points;
    }
}
