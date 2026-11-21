/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins;

import ij.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.plugin.frame.RoiManager;


/**
 *
 * @author joheras
 */
public class LaneSegmentation implements PlugInFilter {

    ImagePlus imp;
    private RoiManager rm = new RoiManager();
    
    

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        
        
        
        Roi r0 = imp.getRoi();
        int x0 = r0.getBounds().x+r0.getBounds().width/2;
        int y0 = r0.getBounds().y;
        int x1 = r0.getBounds().x+r0.getBounds().width/2;
        int y1 = r0.getBounds().y+r0.getBounds().height;
        
        ip.rotateLeft();
        imp.updateAndDraw();
        /*Line l = new Line(y0,x0,y1,x1);
        imp.setRoi(l);
        
        ProfilePlot pp = new ProfilePlot(imp);
        double[] profile = pp.getProfile();
        ip.rotateRight();
        imp.updateAndDraw();
        // This image is cropped to remove the empty regions that can be found 
        // in the borders of the image. To this aim, we use the intensity 
        // profile to obtain the point where interesting information starts to 
        // arise. We fix to 80 the value for the minimum intensity. We also
        // crop the image from the top and the bottom. 
        
         
        
        // We smooth the profile applying an average filter.
        double[] avgprofile = movingaveragefilter(profile, 3);
        
        // We obtain the local minimum of the smoothed profile, and short such 
        // an array.
        int[] maxima = MaximumFinder.findMaxima(avgprofile, 4, false);
        Arrays.sort(maxima);
        // int[] maxima = MaximumFinder.findMaxima(avgprofile, 4, false);
        // Now, we select the regions of interest that are given by the local
        // minimum of the profile. These regions of interest are added to the 
        // RoiManager.
        int r0x = r0.getBounds().x;
        int r0y = r0.getBounds().y;  
        int w = r0.getBounds().width;
        IJ.error(""+ maxima.length);
        
        for (int j = 0; j < maxima.length; j++) {
        
            Line line = new Line(r0x,r0y+maxima[j],r0x+w,r0y+maxima[j]);
            rm.add(imp,line,j);
            
        }*/
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
    
    
    private int findmaximumbetweentwopoints (double[] profile,int p1, int p2){
        int max = p1;
        for (int i = p1;i<p2;i++){
            if (profile[max]< profile[i]){
                max = i;
            }    
        }
        return max;
    }
    
    private int find70percentleft (double[] profile,int min, int max){
        double cutvalue = profile[min] + (profile[max] - profile[min])*0.05;

        for (int i = min;i < min + (max - min)/2;i++){
            if (profile[i]> cutvalue){
                return i;
            }    
        }
        return min + (max - min)/2;
    }
    
    private int find70percentright (double[] profile,int min, int max){
        double cutvalue = profile[min] + (profile[max] - profile[min])*0.05;

        for (int i = max;i < max + (min - max)/2;i++){
            if (profile[i]> cutvalue){
                return i;
            }    
        }
        return min;
    }
 

}
