package plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import workspace.profiles.ProfilePlotMaximum;

/**
 *
 * @author joheras
 */
public class BrailleFotos implements PlugIn {

    private Hashtable letters = new Hashtable();
    private int distancepoints = 6;
    private int distancewords = 12;

    @Override
    public void run(String arg) {

        initialisehashtable();

        ImagePlus imp = IJ.getImage();

        IJ.run(imp, "8-bit", "");
        IJ.setThreshold(imp, 0, 150, "Black & White");
        IJ.run(imp, "Convert to Mask", "");
        imp.updateAndDraw();

        int w = imp.getWidth();
        int h = imp.getHeight();

        imp.setRoi(0, 0, w, h);
        IJ.run(imp, "Invert", "");
        ProfilePlotMaximum pp = new ProfilePlotMaximum(imp);
        double[] profile = pp.getProfile();

        IJ.run(imp, "Rotate 90 Degrees Left", "");
        imp.setRoi(0, 0, h, w);
        ProfilePlotMaximum pp_inv = new ProfilePlotMaximum(imp);

        double[] profile_inv = pp_inv.getProfile();
        IJ.run(imp, "Rotate 90 Degrees Right", "");

        int[] maxima = MaximumFinder.findMaxima(profile_inv, 0, true);
        Arrays.sort(maxima);
        IJ.run(imp, "Invert", "");

        ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();

        int i = 0;
        for (int j = 0; j < maxima.length; j++) {

            if (i == 2) {
                rectangles.add(new Rectangle(0,(int)profile_inv[maxima[j-2]], w,
                            (int)(profile_inv[maxima[j]]-profile_inv[maxima[j-2]])));
                i=0;
            } else {
                i++;
            }

        }
        RoiManager lm = new RoiManager();
        for(int k=0;k<rectangles.size();k++){
            lm.add(imp,new Roi(rectangles.get(k)),k);
        }

    }

    private String readline(int w, int h, ImagePlus imp) {
        String text = "";

        imp.setRoi(0, 0, w, h);
        IJ.run(imp, "Invert", "");
        ProfilePlotMaximum pp = new ProfilePlotMaximum(imp);
        double[] profile = pp.getProfile();

        IJ.run(imp, "Rotate 90 Degrees Left", "");
        imp.setRoi(0, 0, h, w);
        ProfilePlotMaximum pp_inv = new ProfilePlotMaximum(imp);

        double[] profile_inv = pp_inv.getProfile();
        IJ.run(imp, "Rotate 90 Degrees Right", "");

        int[] maxima = MaximumFinder.findMaxima(profile, 0, true);
        Arrays.sort(maxima);
        // int[] maxima = MaximumFinder.findMaxima(avgprofile, 4, false);

        double[] xvalues = new double[profile.length];
        for (int i = 0; i < xvalues.length; i++) {
            xvalues[i] = i;
        }

        int croptop = higherthanvalue(profile_inv, 1, true);
        int cropbottom = higherthanvalue(profile_inv, 1, false);

        int first = maxima[0];
        int adding = 0;
        int line = 0;
        int letter = 0;

        Line l = new Line(maxima[0], croptop, maxima[0], cropbottom);
        int temp = whichletter(l, imp, line);
        letter = temp;

        line = line + 3;

        RoiManager lm = new RoiManager();

        for (int j = 1; j < maxima.length; j++) {

            if (maxima[j] - maxima[j - 1] < distancepoints) {

                adding = adding + maxima[j] - maxima[j - 1];

                Line l1 = new Line(maxima[j], croptop, maxima[j], cropbottom);
                temp = whichletter(l1, imp, line);

                letter = letter + temp;

                if (j == maxima.length - 1) {
//                    l1 = new Line(maxima[j], croptop, maxima[j], cropbottom);
//                    temp = whichletter(l1, imp, line);;
//                    letter = letter + temp;

                    String temp2 = (String) letters.get(letter);

                    if (temp2 != null) {
                        text = text + temp2;
                    }

                    Rectangle r = new Rectangle(first, croptop, adding + 1, cropbottom - croptop);
                    lm.add(imp, new Roi(r), j - 1);
                }

                line = line + 3;

            } else {
//                IJ.error(""+letter);
                String temp2 = (String) letters.get(letter);

                if (temp2 != null) {
                    text = text + temp2;
                }
                if (maxima[j] - maxima[j - 1] >= distancewords) {
                    text = text + " ";
                }

                Rectangle r = new Rectangle(first, croptop, adding + 1, cropbottom - croptop);
                lm.add(imp, new Roi(r), j - 1);
                first = maxima[j];
                adding = 0;
                line = 0;
                letter = 0;

                if (j == maxima.length - 1) {
                    Rectangle r1 = new Rectangle(first, croptop, adding + 1, cropbottom - croptop);
                    lm.add(imp, new Roi(r1), j);
                } else {
                    Line l1 = new Line(maxima[j], croptop, maxima[j], cropbottom);
                    letter = letter + whichletter(l1, imp, line);
                    line = line + 3;
                }

            }

//            
//             lm.add(imp,l,j);
        }
        lm.close();
        IJ.run(imp, "Invert", "");
        return text;

    }

    private int whichletter(Line l, ImagePlus imp, int n) {

        int letter = 0;
//        IJ.run(imp, "Invert", "");
        imp.setRoi((Roi) l);

        ProfilePlotMaximum pp = new ProfilePlotMaximum(imp);
        double[] profile = pp.getProfile();
        int[] maxima = MaximumFinder.findMaxima(profile, 0, false);
        Arrays.sort(maxima);
//        IJ.run(imp, "Invert", "");

        for (int i = 0; i < maxima.length; i++) {

            if (maxima[i] < 4) {
                letter = letter + (int) Math.pow(2, n);
            } else if (maxima[i] < 8) {
                letter = letter + (int) Math.pow(2, n + 1);
            } else {
                letter = letter + (int) Math.pow(2, n + 2);
            }

        }

        return letter;

    }

    private void initialisehashtable() {
        letters.put(1, "a");
        letters.put(3, "b");
        letters.put(9, "c");
        letters.put(25, "d");
        letters.put(17, "e");
        letters.put(11, "f");
        letters.put(27, "g");
        letters.put(19, "h");
        letters.put(10, "i");
        letters.put(26, "j");
        letters.put(5, "k");
        letters.put(7, "l");
        letters.put(13, "m");
        letters.put(29, "n");
        letters.put(59, "ñ");
        letters.put(21, "o");
        letters.put(15, "p");
        letters.put(31, "q");
        letters.put(23, "r");
        letters.put(14, "s");
        letters.put(30, "t");
        letters.put(37, "u");
        letters.put(39, "v");
        letters.put(58, "w");
        letters.put(45, "x");
        letters.put(61, "y");
        letters.put(53, "z");
    }

    private int higherthanvalue(double[] v, double value, boolean from_end) {

        if (from_end) {
            int pos = 0;
            for (int i = 0; i < v.length; i++) {
                if (value < v[i]) {
                    return i;
                }
            }
            return pos;
        } else {
            int pos = v.length - 1;
            for (int i = v.length - 1; i >= 0; i--) {
                if (value < v[i]) {
                    return i;
                }
            }
            return pos;

        }
    }

}
