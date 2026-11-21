/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.Utils;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Rectangle;

/**
 *
 * @author joheras
 */
public class Montage {
    
    ImagePlus[] images;
    ImagePlus imp;
    
    public Montage (ImagePlus imp,ImagePlus[] images){
        this.imp = imp;
        this.images = images;
    }
    
    public void createMontage (String title){
        
        
        
        int w = sumwidth(images) + (5 * images.length);
        int h = imp.getHeight();
        int x;
        Rectangle r;
        Roi roi;
        
        ImageProcessor ip = imp.getProcessor().convertToRGB();
        ImageProcessor montage = ip.createProcessor(w,h);
        ImagePlus imp2 = new ImagePlus(title, montage);
	imp2.setCalibration(imp.getCalibration());
	montage = imp2.getProcessor();
        montage.setColor(Color.black);
	montage.fill();
        
        int pos = 0;
        
        for(int i = 0; i<images.length;i++){
            ImageProcessor ip1 = images[i].getProcessor();
            montage.insert(ip1, pos, 0);
            pos = images[i].getWidth() + 5 + pos;
            
        }
        imp2.updateAndDraw();
        imp2.show();

    }
    
    
    
    private int getMaximumWidth (ImagePlus[] rects){
       int max = rects[0].getWidth();
       for(int i = 1; i <rects.length;i++){
           if(max< rects[i].getWidth()){
              max = rects[i].getWidth();
           }
       }
       return max;
    }
    
    private int sumwidth (ImagePlus[] rects){
       int sum = 0;
       for(int i = 0; i <rects.length;i++){
           sum = sum + rects[i].getWidth();
       }
       return sum;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
