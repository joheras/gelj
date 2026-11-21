/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.LaneCompare;

import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.LaneCompare.Utils.commonbands;

/**
 *
 * @author joheras
 */
public class BandDifference extends LaneComparison {
    
    private double tolerance;
    
    
    public BandDifference (double tolerance){
        this.tolerance = tolerance;
    }
    
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public double Compare2Lanes(Lane l1, Lane l2) {
        double cor;
        double[] stdl1 = l1.getStdpositionbands();
        double[] stdl2 = l2.getStdpositionbands();
        
        double b1 = stdl1.length;
        double b2 = stdl2.length;
        double b1b2 = commonbands(stdl1,stdl2, tolerance);        

        // cor is a value between 0 and 1 that indicates the correlation 
        // (a value close to 1 means that the 2 lanes are similar, and a 
        // value close to 0 means that the 2 lanes are different).
        // Clustering algorithms works with distance between points
        // instead of similarity; therefore, we need to perform the operation
        // 1 minus cor to obtain the distance from the correlation.
        
        cor =  1 - ((b1 + b2 - 2 * b1b2)/ (b1 + b2 - b1b2));
        
        return 1 - cor;
        
        
    }
}
