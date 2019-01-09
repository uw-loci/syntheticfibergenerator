package syntheticfibergenerator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class MiscUtilityTest {

    @Test
    void testGuiName() {
        Param<Integer> testParam = new Param<>();
        testParam.setName("ordinary name");
        assertEquals(MiscUtility.guiName(testParam), "Ordinary name:");
    }

    @Test
    void testEmptyGuiName() {
        Param<Integer> testParam = new Param<>();
        testParam.setName("");
        assertEquals(MiscUtility.guiName(testParam), ":");
    }

    @Test
    void testNonAlphaGuiName() {
        Param<Integer> testParam = new Param<>();
        testParam.setName("$weird name");
        assertEquals(MiscUtility.guiName(testParam), "$weird name:");
    }

    @Test
    void testToFromDeltas() {
        ArrayList<Vector> points = new ArrayList<>();
        points.add(new Vector(0.0, 0.0));
        points.add(new Vector(1.0, 2.0));
        points.add(new Vector(-3.0, 3.0));
        points.add(new Vector(1.5, 0.0));
        ArrayList<Vector> recon = MiscUtility.fromDeltas(MiscUtility.toDeltas(points), points.get(0));
        assertEquals(points, recon);
    }

    @Test
    void testEmptyToDeltas() {
        ArrayList<Vector> points = new ArrayList<>();
        ArrayList<Vector> deltas = MiscUtility.toDeltas(points);
        assertTrue(deltas.isEmpty());
    }

    @Test
    void testEmptyFromDeltas() {
        ArrayList<Vector> deltas = new ArrayList<>();
        Vector start = new Vector(-1.0, 2.0);
        ArrayList<Vector> points = MiscUtility.fromDeltas(deltas, start);
        assertEquals(points.size(), 1);
        assertEquals(points.get(0), start);
    }
}
