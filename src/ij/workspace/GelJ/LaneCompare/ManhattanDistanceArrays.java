/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.LaneCompare;

import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.LaneCompare.Utils.differencearrays;
import static ij.workspace.GelJ.LaneCompare.Utils.sumArrayabs;

/**
 *
 * @author joheras
 */
public class ManhattanDistance  extends LaneComparison {

    @Override
    public double Compare2Lanes(Lane l1, Lane l2) {
        double[] x = l1.getProfile();
        double[] y = l2.getProfile();
        
        double[] x_minus_y = differencearrays(x,y);
        double sum_abs_x_minus_y = sumArrayabs(x_minus_y);
        
        return 1 - sum_abs_x_minus_y;
    }
    
}
