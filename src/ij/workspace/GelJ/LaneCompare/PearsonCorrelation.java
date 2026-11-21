/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.LaneCompare;

import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.LaneCompare.Utils.productarrays;
import static ij.workspace.GelJ.LaneCompare.Utils.sumArray;
import static ij.workspace.GelJ.LaneCompare.Utils.sumArraysquare;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;


/**
 *
 * @author joheras
 */
public class PearsonCorrelation extends LaneComparison {

    @Override
    public double Compare2Lanes(Lane l1, Lane l2) {
        double[] x = l1.getProfile();
        double[] y = l2.getProfile();
        
        double[] productxy = productarrays(x, y);
        double sumproductxy = sumArray(productxy);
        
        double sumx = sumArray(x);
        double sumxx = sumArraysquare(x);
        double sumxsumx = sumx * sumx;
        double sumxsumx_div_n = sumxsumx / x.length;
        double sqrt_sumxx_minus_sumxsumx_div_n = sqrt(sumxx - sumxsumx_div_n);
        
        double sumy = sumArray(y);
        double sumyy = sumArraysquare(y);
        double sumysumy = sumy * sumy;
        double sumysumy_div_n = sumysumy / y.length;
        double sqrt_sumyy_minus_sumysumy_div_n = sqrt(sumyy - sumysumy_div_n);
        
        double sumxsumy = sumx * sumy;
        int n = max(x.length,y.length);
        double sumxsumy_div_n = sumxsumy/n;
  
        double cor = (sumproductxy - sumxsumy_div_n)/(sqrt_sumxx_minus_sumxsumx_div_n * sqrt_sumyy_minus_sumysumy_div_n);
        
        if (cor < 0){
            return 1;
        }else{
            // cor is a value between 0 and 1 that indicates the correlation 
            // (a value close to 1 means that the 2 lanes are similar, and a 
            // value close to 0 means that the 2 lanes are different).
            // Clustering algorithms works with distance between points
            // instead of similarity; therefore, we need to perform the operation
            // 1 minus cor to obtain the distance from the correlation.
            
            return 1 - cor;
        }
    }
    
    

    
}
