/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.LaneCompare;

import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.LaneCompare.Utils.differencearrays;
import static ij.workspace.GelJ.LaneCompare.Utils.sumArraysquare;
import static java.lang.Math.sqrt;

/**
 *
 * @author joheras
 */
public class EuclideanDistance extends LaneComparison {

    @Override
    public double Compare2Lanes(Lane l1, Lane l2) {
        double[] x = l1.getProfile();
        double[] y = l2.getProfile();
        
        double[] x_minus_y = differencearrays(x,y);
        double sum_x_minus_y_x_minus_y = sumArraysquare(x_minus_y);
        
        return (1- sqrt(sum_x_minus_y_x_minus_y));
    }

    
    
}
