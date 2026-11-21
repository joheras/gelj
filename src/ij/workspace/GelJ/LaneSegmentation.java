/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ;


import ij.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.MaximumFinder;
import ij.process.*;


import java.util.Arrays;

/**
 *
 * @author joheras
 */
public class LaneSegmentation implements PlugInFilter {

    ImagePlus imp;
    private LaneManager lm;
    private boolean darkbackground;
    
    public LaneSegmentation (ImagePlus imp,LaneManager lm,boolean bg){
        this.imp = imp;
        this.lm = lm;
        darkbackground = bg;
        run(imp.getProcessor());
    }
    

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {

        
        // We obtain the width and the height of the image.
        int w = imp.getWidth();
        int h = imp.getHeight();

        // We select the whole image as region of interest
        imp.setRoi(0, 0, w, h);
        
        // We obtain the intensity profile of the image, and the intensity 
        // profile of the image rotated 90 degrees to the left. 
        if ((!darkbackground && !imp.isInvertedLut()) || imp.isInvertedLut()){
            IJ.run(imp, "Invert", "");
        }
        ProfilePlot pp = new ProfilePlot(imp);
        double[] profile = pp.getProfile();
        
        IJ.run(imp, "Rotate 90 Degrees Left", "");
        imp.setRoi(0, 0, h, w);
        ProfilePlot pp_inv = new ProfilePlot(imp);
        
        
        double[] profile_inv = pp_inv.getProfile();
        IJ.run(imp, "Rotate 90 Degrees Right", "");
        
        if ((!darkbackground && !imp.isInvertedLut()) || imp.isInvertedLut()){
            IJ.run(imp, "Invert", "");
        }
        // This image is cropped to remove the empty regions that can be found 
        // in the borders of the image. To this aim, we use the intensity 
        // profile to obtain the point where interesting information starts to 
        // arise. We fix to 80 the value for the minimum intensity. We also
        // crop the image from the top and the bottom. 
        
        int cropleft = higherthanvalue(profile, 1,true);
        int cropright = higherthanvalue(profile, 1,false);
        int croptop = higherthanvalue(profile_inv, 1,true);
        int cropbottom = higherthanvalue(profile_inv, 1,false);
         
        // The profile is also cropped. 
        profile = Arrays.copyOfRange(profile, cropleft, cropright);
        
        // We smooth the profile applying an average filter.
        double[] avgprofile = movingaveragefilter(profile, 11);
        
        // We obtain the local minimum of the smoothed profile, and short such 
        // an array.
        int[] minima = MaximumFinder.findMinima(avgprofile, 1, true);
        Arrays.sort(minima);
        // int[] maxima = MaximumFinder.findMaxima(avgprofile, 4, false);
        
        double[] xvalues = new double[avgprofile.length];
        for (int i =0;i<xvalues.length;i++){
            xvalues[i] = i;
        }
        
        
        
        //IJ.error("" + minima.length);
        
        // Now, we select the regions of interest that are given by the local
        // minimum of the profile. These regions of interest are added to the 
        // RoiManager.
        
                
        
        for (int j = 0; j < minima.length; j++) {
        
            if (j==0){
                float[] xPoints = {cropleft+2,cropleft+2,cropleft+2,
                    cropleft+minima[0]-2, cropleft+minima[0]-2, 
                    cropleft+minima[0]-2};
                float[] yPoints = {croptop,(cropbottom - croptop)/2,cropbottom,
                                   cropbottom,(cropbottom - croptop)/2,croptop};
                PolygonRoi pr = new PolygonRoi(xPoints,yPoints,Roi.POLYGON);
                lm.add(imp,pr,j);
                
            } else if (j==(minima.length - 1)){
                float[] xPoints2 = {cropleft+minima[j-1]+2,cropleft+minima[j-1]+2,
                    cropleft+minima[j-1]+2, cropleft+minima[j]-2,
                    cropleft+minima[j]-2,cropleft+minima[j]-2};
                float[] yPoints2 =  {croptop,(cropbottom - croptop)/2,cropbottom,
                                   cropbottom,(cropbottom - croptop)/2,croptop};
                PolygonRoi pr2 = new PolygonRoi(xPoints2,yPoints2,Roi.POLYGON);
                lm.add(imp,pr2,j);
                float[] xPoints = {cropleft+minima[j]+2,cropleft+minima[j]+2,
                    cropleft+minima[j]+2, cropright-2, cropright-2,cropright-2};
                float[] yPoints =  {croptop,(cropbottom - croptop)/2,cropbottom,
                                   cropbottom,(cropbottom - croptop)/2,croptop};
                PolygonRoi pr = new PolygonRoi(xPoints,yPoints,Roi.POLYGON);
                lm.add(imp,pr,j+1);
            } else{
                float[] xPoints = {cropleft+minima[j-1]+2,cropleft+minima[j-1]+2,
                    cropleft+minima[j-1]+2, cropleft+minima[j]-2,
                    cropleft+minima[j]-2,cropleft+minima[j]-2};
                float[] yPoints =  {croptop,(cropbottom - croptop)/2,cropbottom,
                                   cropbottom,(cropbottom - croptop)/2,croptop};
                PolygonRoi pr = new PolygonRoi(xPoints,yPoints,Roi.POLYGON);
                lm.add(imp,pr,j);
            }
            
        }

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
 

}
