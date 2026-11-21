/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.LaneCompare;

import ij.workspace.GelJ.Lane;

/**
 *
 * @author joheras
 */
public abstract class LaneComparison {
    
    
    // public static final int DICE=0, JACCARD=1, PEARSON=2, COSINE=3;
    
    
    public abstract double Compare2Lanes (Lane l1, Lane l2);
    
    
    
    public double[][] CompareLanes (Lane[] l){
        double[][] res = new double[l.length][l.length];
        
        for (int i =0;i<l.length;i++){
            for(int j=0;j<l.length;j++){
                if (i==j){
                    res[i][j] = Compare2Lanes(l[i],l[j]);
                }else if (j<i){
                   res[i][j] = res[j][i];
                } else{
                   res[i][j] = Compare2Lanes(l[i],l[j]); 
                }     
            }
        }
        
        return res;
    
    }
     
}
