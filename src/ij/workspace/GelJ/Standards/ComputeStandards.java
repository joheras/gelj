/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ.Standards;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;
import static ij.process.ImageProcessor.BICUBIC;
import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.Lane.indexesstandardlanes;
import ij.workspace.GelJ.Menu;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardfromfile;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardinfofromfile;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardposfromfile;
import java.io.IOException;
import umontreal.iro.lecuyer.functionfit.SmoothingCubicSpline;

/**
 *
 * @author joheras
 */
public class ComputeStandards {

    public static CurveFitter[] computerflines(Lane[] lanes) {

        int[] indexes = indexesstandardlanes(lanes);
        int numberofstdlanes = indexes.length;
        CurveFitter[] curves = new CurveFitter[lanes[indexes[0]].getStdpositionbands().length];

        // For each standard position, we compute the rf-line 
        for (int i = 0; i < lanes[indexes[0]].getStdpositionbands().length; i++) {
            double[] x = new double[indexes.length];
            double[] y = new double[indexes.length];

            for (int j = 0; j < indexes.length; j++) {
                x[j] = lanes[indexes[j]].getRoi().getBounds().x
                        + (lanes[indexes[j]].getRoi().getBounds().width / 2);
                y[j] = lanes[indexes[j]].getPositionbands()[i];

            }

            curves[i] = new CurveFitter(x, y);
            curves[i].doFit(numberofstdlanes - 2);

        }

        return curves;

    }

    public static void computestandardpositionsLane(Lane l, double[] standard, CurveFitter[] curves, int method, double offset) {

        double[] y = new double[curves.length];
        double x = l.getRoi().getBounds().x + (l.getRoi().getBounds().width / 2);
        double[] xs = new double[curves.length];

        // First, we compute the intersection points of the backbone of the 
        // lane with the curves.
        for (int i = 0; i < curves.length; i++) {
            y[i] = curves[i].f(curves[i].getParams(), x);
        }

        // From the obtained values, we compute the curve that fits those values 
        // with the standard positions.
        CurveFitter curve = new CurveFitter(y, standard);
        curve.doFit(method);
        l.setCurve(curve);
        l.setCurvemethod(method);
        double[] params = curve.getParams();

        double[] stdspositions = new double[l.getPositionbands().length];

        for (int j = 0; j < stdspositions.length; j++) {
            stdspositions[j] = curve.f(params, l.getPositionbands()[j]);
        }
        l.setOffset(offset - y[0]);
        l.setStdpositionbands(stdspositions);

    }

    private static void computestandardpositionsLane(Lane l) {

        CurveFitter curve = l.getCurve();
        double[] params = curve.getParams();

        double[] stdspositions = new double[l.getPositionbands().length];

        for (int j = 0; j < stdspositions.length; j++) {
            stdspositions[j] = curve.f(params, l.getPositionbands()[j]);
        }

        l.setStdpositionbands(stdspositions);
    }

    public static void computestdlanes(Lane[] lanes, int method, double offset) {
        CurveFitter[] curves = computerflines(lanes);
        int[] indexes = indexesstandardlanes(lanes);

        for (Lane l : lanes) {
            if (!l.isIsstandard()) {

                if (l.getCurve() == null) {
                    computestandardpositionsLane(l,
                            lanes[indexes[0]].getStdpositionbands(), curves, method,
                            offset);
                } else {
                    computestandardpositionsLane(l);
                }
            }
        }

    }

    public static void computestdlanes(Menu m) throws IOException {
        Lane[] lanes = m.finallanes;
        int[] indexes = indexesstandardlanes(lanes);

        // We distinguish whether there are one or more than one markers
        if(indexes.length > 1){
        /// Compute the rflines
        CurveFitter[] curves = computerflines(lanes);

        /// We compute where are the molecular weights of the reference markers
        /// in the rest of the lanes.
        for (Lane l : lanes) {
            if (l.isIsstandard()) {
                l.setMarkerpositionbands(l.getPositionbands());
            } else {
                int[] y = new int[curves.length];
                double x = l.getRoi().getBounds().x + (l.getRoi().getBounds().width / 2);
                for (int i = 0; i < curves.length; i++) {
                    y[i] = (int) curves[i].f(curves[i].getParams(), x);
                }
                l.setMarkerpositionbands(y);
            }
        }
        }else{
            int[] bandspos = lanes[indexes[0]].getPositionbands();
            for(Lane l : lanes){
                l.setMarkerpositionbands(bandspos);
            }        
        }

        /// We generate the migration model given from the perfect standard
        double[] infomarker = readstandardinfofromfile(m.getMarkername());
        double[] posmarker = readstandardposfromfile(m.getMarkername());
        double[] mwmarker = readstandardfromfile(m.getMarkername());
        CurveFitter model = new CurveFitter(posmarker, mwmarker);
        model.doFit((int) infomarker[2]);

        /// We find the molecular weights positions that have been used in the gel
        double[] mws = lanes[indexes[0]].getStdpositionbands();
        boolean[] inmarker = new boolean[posmarker.length];
        for (int i = 0; i < inmarker.length; i++) {
            inmarker[i] = member(mwmarker[i], mws);
        }

        double[] markerpos = new double[mws.length];
        int i = 0;
        for (int j = 0; j < inmarker.length; j++) {
            if (inmarker[j]) {
                markerpos[i] = posmarker[j];
                i++;
            }
        }

        // Now, we can normalise the lanes
        for (int j = 0; j < lanes.length; j++) {
            normaliselane(lanes[j], j, (int) infomarker[0], (int) infomarker[1], markerpos, model, m.getinvertcolour());
            m.setLane(lanes[j].getRoi(), j);
        }

    }

    public static void computemwlanes(Menu m) throws IOException {
        Lane[] lanes = m.finallanes;
        int[] indexes = indexesstandardlanes(lanes);

        /// We generate the migration model given from the perfect standard
        double[] infomarker = readstandardinfofromfile(m.getMarkername());
        double[] posmarker = readstandardposfromfile(m.getMarkername());
        double[] mwmarker = readstandardfromfile(m.getMarkername());
        CurveFitter model = new CurveFitter(posmarker, mwmarker);
        model.doFit((int) infomarker[2]);

        /// We find the molecular weights positions that have been used in the gel
        double[] mws = lanes[indexes[0]].getStdpositionbands();
        boolean[] inmarker = new boolean[posmarker.length];
        for (int i = 0; i < inmarker.length; i++) {
            inmarker[i] = member(mwmarker[i], mws);
        }

        double[] markerpos = new double[mws.length];
        int i = 0;
        for (int j = 0; j < inmarker.length; j++) {
            if (inmarker[j]) {
                markerpos[i] = posmarker[j];
                i++;
            }
        }

        // Now, we can normalise the lanes
        for (Lane l : lanes) {
            computemwlane(l, (int) infomarker[0], (int) infomarker[1], markerpos, model, m.getinvertcolour());
        }

    }

    public static void computestdlane(Lane lane, String marker, boolean invert) throws IOException {
      

        /// We generate the migration model given from the perfect standard
        double[] infomarker = readstandardinfofromfile(marker);
        double[] posmarker = readstandardposfromfile(marker);
        double[] mwmarker = readstandardfromfile(marker);
        CurveFitter model = new CurveFitter(posmarker, mwmarker);
        model.doFit((int) infomarker[2]);

        double[] mws = new double[lane.getPositionbands().length];
        double[] params = model.getParams();
        int[] bands2 = new int[lane.getPositionbands().length];
        for (int i = 0; i < mws.length; i++) {
            bands2[i] = (int) (lane.getPositionbands()[i]);
            mws[i] = model.f(params, lane.getPositionbands()[i]);
        }
        lane.setStdpositionbands(mws);
        lane.setPositionbands2(bands2);
    }

    public static void computestdlanes(Lane[] lanes, String marker, boolean invert) throws IOException {
        int[] indexes = indexesstandardlanes(lanes);

        /// Compute the rflines
        CurveFitter[] curves = computerflines(lanes);

        /// We compute where are the molecular weights of the reference markers
        /// in the rest of the lanes.
        for (Lane l : lanes) {
            if (l.isIsstandard()) {
                l.setMarkerpositionbands(l.getPositionbands());
            } else {
                int[] y = new int[curves.length];
                double x = l.getRoi().getBounds().x + (l.getRoi().getBounds().width / 2);
                for (int i = 0; i < curves.length; i++) {
                    y[i] = (int) curves[i].f(curves[i].getParams(), x);
                }
                l.setMarkerpositionbands(y);
            }
        }

        /// We generate the migration model given from the perfect standard
        double[] infomarker = readstandardinfofromfile(marker);
        double[] posmarker = readstandardposfromfile(marker);
        double[] mwmarker = readstandardfromfile(marker);
        CurveFitter model = new CurveFitter(posmarker, mwmarker);
        model.doFit((int) infomarker[2]);

        /// We find the molecular weights positions that have been used in the gel
        double[] mws = lanes[indexes[0]].getStdpositionbands();
        boolean[] inmarker = new boolean[posmarker.length];
        for (int i = 0; i < inmarker.length; i++) {
            inmarker[i] = member(mwmarker[i], mws);
        }

        double[] markerpos = new double[mws.length];
        int i = 0;
        for (int j = 0; j < inmarker.length; j++) {
            if (inmarker[j]) {
                markerpos[i] = posmarker[j];
                i++;
            }
        }

        // Now, we can normalise the lanes
        for (int j = 0; j < lanes.length; j++) {
            normaliselane(lanes[j], j, (int) infomarker[0], (int) infomarker[1], markerpos, model, invert);
        }

    }

    private static boolean member(double d, double[] ds) {

        for (double d1 : ds) {
            if (d1 == d) {
                return true;
            }
        }

        return false;
    }

    private static void computemwlane(Lane l, int w, int h, double[] markerpos, CurveFitter model, boolean invert) {

        /// we compute where the marker positions are found
        double[] heights = new double[l.getMarkerpositionbands().length];
        for (int i = 0; i < heights.length; i++) {
            heights[i] = l.getMarkerpositionbands()[i];
        }

        //// Now, we compute the molecular weights of the bands of the lane
        if (!l.isIsstandard()) {
            double[] mws = new double[l.getPositionbands().length];
            double[] params = model.getParams();
            int[] bands2 = new int[l.getPositionbands().length];
            for (int i = 0; i < mws.length; i++) {
                bands2[i] = (int) (l.getPositionbands()[i]);
                mws[i] = model.f(params, l.getPositionbands()[i]);
            }
            l.setStdpositionbands(mws);
            l.setPositionbands2(bands2);
        } else {
            int[] bands2 = new int[l.getPositionbands().length];
            for (int i = 0; i < bands2.length; i++) {
                bands2[i] = (int) (l.getPositionbands()[i]);
            }
            l.setPositionbands2(bands2);
        }

    }

    private static void normaliselane(Lane l, int pos, int w, int h, double[] markerpos, CurveFitter model, boolean invert) {

        /// First, we scale the image
        ImagePlus imp = l.getImp();

        ImageProcessor ip0 = imp.getProcessor();
        double scalefactor = (double) h / (double) ip0.getHeight();
        ip0.setInterpolationMethod(BICUBIC);
        ImageProcessor ip = ip0.resize(w, h, true);
        imp.setProcessor(ip);
        //??
        imp.updateAndDraw();
        //to remove
        Roi r = new Roi(pos * w, 0, w, h);
        r.setName(l.getRoi().getName());
        l.setRoi(r);
        /// we compute where the marker positions are found
        double[] heights = new double[l.getMarkerpositionbands().length];
        for (int i = 0; i < heights.length; i++) {
            heights[i] = l.getMarkerpositionbands()[i] * scalefactor;
        }

        // We compute the shifts of each position
        double[] shifts = new double[heights.length];
        for (int i = 0; i < heights.length; i++) {
            shifts[i] = markerpos[i] - heights[i];
        }

        /// Image normalisation
        SmoothingCubicSpline fit = new SmoothingCubicSpline(heights, shifts, 1);
        ImagePlus imp2;
        if (invert) {
            imp2 = IJ.createImage(imp.getTitle(), "" + imp.getType() + "-white", imp.getWidth(), imp.getHeight(), 1);
        } else {
            imp2 = IJ.createImage(imp.getTitle(), "" + imp.getType() + "-black", imp.getWidth(), imp.getHeight(), 1);
        }
        ImageProcessor ip2 = imp2.getProcessor();

        for (int i = 0; i < imp.getHeight(); i++) {
            for (int j = 0; j < imp.getWidth(); j++) {
                if (i > 0) {
                    int jump = (int) (i + fit.evaluate(i)) - (int) (i - 1 + fit.evaluate(i - 1));
                    if (jump > 1) {
                        for (int k = 1; k < jump; k++) {
                            ip2.putPixelValue(j, (int) (i - 1 + fit.evaluate(i - 1)) + k, ip.getPixelValue(j, i - 1));
                        }
                    }
                }
                ip2.putPixelValue(j, (int) (i + fit.evaluate(i)), ip.getPixelValue(j, i));
            }
        }
        imp2.updateAndDraw();
        l.setImp(imp2);

        //// Now, we compute the molecular weights of the bands of the lane
        if (!l.isIsstandard()) {
            double[] mws = new double[l.getPositionbands().length];
            double[] params = model.getParams();
            int[] bands2 = new int[l.getPositionbands().length];
            for (int i = 0; i < mws.length; i++) {
                bands2[i] = (int) (l.getPositionbands()[i] * scalefactor + fit.evaluate(l.getPositionbands()[i] * scalefactor));
                mws[i] = model.f(params, l.getPositionbands()[i] * scalefactor + fit.evaluate(l.getPositionbands()[i] * scalefactor));
            }
            l.setStdpositionbands(mws);
            l.setPositionbands2(bands2);
        } else {
            int[] bands2 = new int[l.getPositionbands().length];
            for (int i = 0; i < bands2.length; i++) {
                bands2[i] = (int) (l.getPositionbands()[i] * scalefactor + fit.evaluate(l.getPositionbands()[i] * scalefactor));
            }
            l.setPositionbands2(bands2);
        }

        IJ.run(imp2, "Rotate 90 Degrees Left", "");
        IJ.run(imp2, "Select All", "");
        ProfilePlot pp = new ProfilePlot(imp2);
        double[] profile = pp.getProfile();
        l.setProfile(profile);
        IJ.run(imp2, "Rotate 90 Degrees Right", "");

        l.setScalefactor(scalefactor);

    }

}
