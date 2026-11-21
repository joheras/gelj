/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.LaneCompare;

/**
 *
 * @author joheras
 */
public class PairBoolInt {
    
    private boolean b;
    private int i;
    
    
    PairBoolInt (boolean b,int i){
        this.b=b;
        this.i=i;
    }
    
    public boolean getBoolPairBoolInt (){
        return this.b;
    }
    
    public int getIntPairBoolInt (){
        return this.i;
    }
    
    
}
