/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ;

import ij.*;
import ij.gui.*;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.*;
import ij.workspace.GelJ.BandManager;
import ij.workspace.GelJ.Lane;
import ij.workspace.GelJ.Menu;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import java.awt.AWTEvent;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;


/**
 *
 * @author joheras
 */
public class BandSegmentation implements ExtendedPlugInFilter, DialogListener {

    private ImagePlus imp;
    private BandManager bm;
    private boolean darkbackground;
    public static final int BLUE=0, RED=1, GREEN=2, MAGENTA=3, CYAN=4, YELLOW = 5, ORANGE=6;
    private static int FLAGS = DOES_ALL|SUPPORTS_MASKING|KEEP_PREVIEW|FINAL_PROCESSING;
    private Roi[] lanes;
    private int nPasses = 1;
    private PlugInFilterRunner pfr;
    private double tolerance = 150;
    private Lane[] finallanes;
    private Menu m;
    private Color c = Color.BLUE;

    
    public BandSegmentation (ImagePlus imp,BandManager bm,boolean bg, Roi[] l, Menu m){
        this.imp = imp;
        this.bm = bm;
        this.lanes = l;
        darkbackground = bg;
        this.finallanes = m.finallanes;
        this.m = m;
        c=bm.getColour();
        //run(imp.getProcessor());
        //showDialog(imp, null, pfr);
    }
    
    
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        GenericDialog gd = new GenericDialog(command);
        gd.addMessage("Fix the tolerance to find bands");
        gd.addSlider("Tolerance", 25, 255, 150);
        String[] colours = {"blue","red","green","magenta","cyan","yellow","orange"};
        gd.addChoice("Colour", colours, "blue");
        this.imp = imp;
        
        this.pfr = pfr;
        gd.addPreviewCheckbox(pfr);
       // 
        gd.addDialogListener(this);
        ((Checkbox)gd.getCheckboxes().elementAt(0)).setState(true);
        gd.showDialog();                    // input by the user (or macro) happens here
        if (gd.wasCanceled()){
            bm.delete_without_asking(true);
            ImagePlus imp2 = WindowManager.getCurrentImage();
            removeOverlay(imp2);
            return DONE;
        } 
        if(gd.wasOKed()){
            bm.setColour(c);
        }
        
        IJ.register(this.getClass());
        return IJ.setupDialog(imp, FLAGS);
    }
    
   
    
    
    
    
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        Vector sliders = gd.getSliders();
        tolerance = ((Scrollbar)sliders.elementAt(0)).getValue();
        int colour = gd.getNextChoiceIndex();
        switch (colour){
            case BLUE:
                c = Color.BLUE;
                break;
            case RED:
                c = Color.RED;
                break;
            case GREEN:
                c = Color.GREEN;
                break;
            case MAGENTA:
                c = Color.MAGENTA;
                break;
            case CYAN:
                c = Color.CYAN;
                break;
            case YELLOW:
                c = Color.YELLOW;
                break;
            case ORANGE:
                c = Color.ORANGE;
                break;      
            default:
                c = Color.BLUE;
                break;
        }
        return true;
    }
    

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {

        Prefs.verticalProfile = true;
        bm.delete_without_asking(true);
        bm.restart_bands();
        
        for (int i =0; i<m.finallanes.length;i++) {
            Roi r0 = m.finallanes[i].getRoi();
            int x0 = r0.getBounds().x + r0.getBounds().width / 2;
            int y0 = r0.getBounds().y;
            int x1 = r0.getBounds().x + r0.getBounds().width / 2;
            int y1 = r0.getBounds().y + r0.getBounds().height;

            Line l0 = new Line(x0, y0, x1, y1);
            l0.setWidth(15);
            imp.setRoi(l0);

            // Finding the peaks of the profile
            if ((!darkbackground && !imp.isInvertedLut()) || imp.isInvertedLut()){
                ip.invert();
            }
            //ProfilePlot pp = new ProfilePlot(imp);
            double[] profile = finallanes[i].getProfile();
            if ((!darkbackground && !imp.isInvertedLut()) || imp.isInvertedLut()){
                ip.invert();
            }
            l0.setWidth(1);
            int[] maxima = MaximumFinder.findMaxima(profile, 2, true);
            int[] htt = higherthantolerance(maxima, profile, tolerance);
            Arrays.sort(htt);

            // Showing the lines
            int r0x = r0.getBounds().x;
            int r0y = r0.getBounds().y;
            int w = r0.getBounds().width;
            
            int[] positionsy = new int[htt.length];
            for (int k = 0; k<htt.length;k++){
                positionsy[k]=r0y+htt[k];
            }
            

            for (int j = 0; j < htt.length; j++) {

                /*Line line = new Line(r0x, r0y + htt[j], r0x + w, r0y + htt[j]);
                Line lineintersection = lineintersectionroi(r0,line);*/
                Rectangle r = new Rectangle(r0x, r0y + htt[j], w, 2);
                
                Roi r1 = rectangleintersectionroi(r0, r);
                r1.setFillColor(c);
                bm.add(imp, r1, j,i);
                
                //bm.add(imp, line, j);

            }
           
        //m.finallanes[i].setProfile(profile);
        //m.finallanes[i].setPositionbands(positionsy);
        
            
        }

        bm.showAll(0);
        //m.finallanes = finallanes;
    }
    
    
    
    
    private Line lineintersectionroi (Roi r, Line l){
       Line line; 
       int[] xpointsl = l.getPoints().xpoints;
       int[] ypointsl = l.getPoints().ypoints;
       int len = xpointsl.length;
       Boolean inside = false;
       int x0 = xpointsl[0];
       int y0 = ypointsl[0];  
       int x1 = xpointsl[len-1];
       int y1 = ypointsl[len-1];
       
       for (int i=1;i<len; i++){
           if (!inside){
               if(r.contains(xpointsl[i], ypointsl[i])){
                 x0 = xpointsl[i];
                 y0 = ypointsl[i];
                 inside = true;
               }
           } else{
               if(!r.contains(xpointsl[i], ypointsl[i])){
                 x1 = xpointsl[i];
                 y1 = ypointsl[i];
                 line = new Line(x0, y0, x1, y1);
                 return line;
               }
           }   
       }
        
       line = new Line(x0, y0, x1, y1);
       return line;
       
    }
    
    
    
    private int[] higherthantolerance (int[] v, double[] p, double tolerance){
        ArrayList<Integer> res = new ArrayList<Integer>();

        for (int i = 0; i < v.length; i++) {
            if (p[v[i]] > tolerance) {
                res.add(v[i]);
            }
        }

        int[] ret = new int[res.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = res.get(i);
        }
        return ret;
    }

    
    // Function that applies the average filter to a vector v using the value of
    // avgpoints as diameter of the circle where the filter is applied. 

    private double[] movingaveragefilter(double[] v, int avgpoints) {

        int lv = v.length;
        double[] res = new double[lv];
        int sp; // starting point 
        int ep; // end point 

        for (int i = 0; i < lv; i++) {

            sp = i - (avgpoints - 1) / 2;
            if (sp < 0) {
                sp = 0;
            }
            ep = i + (avgpoints - 1) / 2;
            if (ep >= lv) {
                ep = lv - 1;
            }

            double partial_sum = 0;
            for (int j = sp; j <= ep; j++) {
                partial_sum = partial_sum + v[j];
            }

            res[i] = partial_sum / (ep - sp + 1);

        }

        return res;

    }

    // Given a vector v, a double value and a boolean. This function searches 
    // the first or the last position in v such that v[pos] is smaller than
    // value. The boolean indicates if the search starts from the end or the 
    // beginning. 
    
    private int higherthanvalue (double[] v, double value, boolean from_end){
    
    if (from_end){
       int pos=0;
       for (int i = 0; i<v.length;i++){
            if(value<v[i]){return i;}
        }
       return pos;
    }
    else{
      int pos=v.length-1;
       for (int i = v.length - 1; i>=0;i--){
            if(value<v[i]){return i;}
        }
       return pos;
    
    }
    }


    public void setNPasses(int nPasses) {
        this.nPasses = nPasses;
    }

    private void removeOverlay(ImagePlus imp) {
        if (imp != null && imp.getCanvas() != null) {
            setOverlay(imp, null);
        }
    }
    
    private void setOverlay(ImagePlus imp, Overlay overlay) {
        if (imp == null) {
            return;
        }
        ImageCanvas ic = imp.getCanvas();
        if (ic == null) {
            imp.setOverlay(overlay);
            return;
        }
        ic.setShowAllList(overlay);
        imp.draw();
    }
 

}
