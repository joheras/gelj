/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ.Utils;

import db.Interface.SelectUser;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import java.awt.GridLayout;
import java.awt.Panel;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;

/**
 *
 * @author joheras
 */
public class Utilities {

    public static boolean isintersectionempty(Roi roi1, Roi roi2) {
        ShapeRoi sroi1 = new ShapeRoi(roi1);
        ShapeRoi sroi2 = new ShapeRoi(roi2);
        ShapeRoi and = sroi1.and(sroi2);
        double l = and.getLength();

        return (l == 0);
    }

    public static boolean intersectwithany(Roi roi1, Roi[] r) {
        for (int i = 0; i < r.length; i++) {
            if (!isintersectionempty(roi1, r[i])) {
                return true;
            }
        }
        return false;
    }

    public static Roi belongstoany(int x, int y, Roi[] r) {
        for (int i = 0; i < r.length; i++) {
            if (r[i].contains(x, y)) {
                return r[i];
            }
        }
        return null;

    }

    public static int roibelonging(int x, int y, Roi[] r) {
        for (int i = 0; i < r.length; i++) {
            if (r[i].contains(x, y)) {
                return i;
            }
        }
        return -1;

    }

    public static boolean allequal(int[] a) {

        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] != a[i + 1]) {
                return false;
            }
        }
        return true;

    }

    public static double[] intarraytodoublearray(int[] a) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = a[i];
        }
        return res;
    }

    public static boolean[] selectstandards(String[] labels, boolean[] checked) {
        GenericDialog gdG = new GenericDialog("Select the reference lanes:");
        boolean[] res = new boolean[checked.length];
        List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
        gdG.hideCancelButton();
        Panel panel = new Panel();
        panel.setLayout(new GridLayout(labels.length, 1, 6, 0));

        for (int i = 0; i < labels.length; i++) {
            JCheckBox check = new JCheckBox(labels[i]);
            check.setSelected(checked[i]);
            panel.add(check);
            checkBoxes.add(check);

        }

        gdG.add(panel);
        gdG.addMessage("");
        gdG.showDialog();

        if (gdG.wasOKed()) {

            for (int j = 0; j < checkBoxes.size(); j++) {
                res[j] = checkBoxes.get(j).isSelected();
            }

            if (res.length < 2) {
                gdG.dispose();
                IJ.error("You have to select (at least) two lanes as standards");
                return selectstandards(labels, res);

            } else {
                return res;
            }
        }

        if (gdG.wasCanceled()) {
            return null;
        }

        return res;

    }

    public static boolean[] recoverstandards(ArrayList<String> labels) {
        GenericDialog gdG = new GenericDialog("Select the standards to recover:");
        List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
        boolean[] res = new boolean[labels.size()];
        Panel panel = new Panel();
        panel.setLayout(new GridLayout(labels.size(), 1, 6, 0));

        for (int i = 0; i < labels.size(); i++) {
            JCheckBox check = new JCheckBox(labels.get(i));
            panel.add(check);
            checkBoxes.add(check);

        }

        gdG.add(panel);
        gdG.addMessage("");
        gdG.showDialog();

        if (gdG.wasOKed()) {

            for (int j = 0; j < checkBoxes.size(); j++) {
                res[j] = checkBoxes.get(j).isSelected();
            }

            return res;
        }

        if (gdG.wasCanceled()) {
            return null;
        }

        return res;

    }

    public static boolean[] selectlanes(String[] labels) {
        GenericDialog gdG = new GenericDialog("Lanes to cluster:");
        List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
        boolean[] res = new boolean[labels.length];
        Panel panel = new Panel();
        panel.setLayout(new GridLayout(labels.length, 1, 6, 0));

        for (int i = 0; i < labels.length; i++) {
            JCheckBox check = new JCheckBox(labels[i]);
            check.setSelected(true);
            panel.add(check);
            checkBoxes.add(check);

        }

        gdG.add(panel);
        gdG.addMessage("");
        gdG.showDialog();

        if (gdG.wasOKed()) {

            for (int j = 0; j < checkBoxes.size(); j++) {
                res[j] = checkBoxes.get(j).isSelected();
            }

            return res;
        }

        if (gdG.wasCanceled()) {
            return null;
        }

        return res;

    }

    public static int trueinbooleanarray(boolean[] a) {
        int n = 0;
        for (boolean b : a) {
            if (b) {
                n++;
            }
        }
        return n;

    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            // The directory is now empty so delete it 
            return dir.delete();
        }
        return dir.delete();
    }

    public static void restartApplication() throws IOException {
        StringBuilder cmd = new StringBuilder();
        cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
        for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            cmd.append(jvmArg + " ");
        }
        cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
        cmd.append(SelectUser.class.getName()).append(" ");
        Runtime.getRuntime().exec(cmd.toString());
        System.exit(0);
    }

}
