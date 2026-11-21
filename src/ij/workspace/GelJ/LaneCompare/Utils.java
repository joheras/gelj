/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.LaneCompare;

import ij.workspace.GelJ.LaneCompare.PairBoolInt;
import static java.lang.Math.abs;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author joheras
 */
public final class Utils {
    
    public static int commonbands (double[] l1, double l2[], double tolerance){
       int commonbands = 0;
       double[] lane1=l1;
       double[] lane2=l2;
       PairBoolInt pbi;
       
       for(int i=0;i<lane1.length; i++){     
           pbi = belongswithtolerance(lane1[i],lane2,tolerance);
           if(pbi.getBoolPairBoolInt()){
               commonbands++;
               lane2 = ArrayUtils.remove(lane2,pbi.getIntPairBoolInt());
           }
       }
       return commonbands;
    }
    
    
    public static PairBoolInt belongswithtolerance(double pos, double l[],double tolerance){
        PairBoolInt p;
        for (int i=0;i<l.length;i++){
            if(pos - tolerance <= l[i] && l[i] <= pos + tolerance){
               p = new PairBoolInt(true,i);
               return p;
            }
        }
        p = new PairBoolInt(false,0);
        return p;
    }
    
    
       
    public static double sumArray (double[] a){
        double res = 0;
        for(int i = 0; i<a.length;i++){
            res = res + a[i];
        }
        return res;
    }
    
    
    public static double sumArraysquare (double[] a){
        double res = 0;
        for(int i = 0; i<a.length;i++){
            res = res + (a[i]*a[i]);
        }
        return res;
    }
    
    public static double sumArrayabs (double[] a){
        double res = 0;
        for(int i = 0; i<a.length;i++){
            res = res + (abs(a[i]));
        }
        return res;
    }
    
    public static double[] productarrays (double[] a1, double[] a2){
        int maxlength;
        if (a1.length<a2.length){
            maxlength = a2.length;
        } else{
           maxlength = a1.length;
        } 
        double[] res = new double[maxlength];
        
        if (a1.length<a2.length){
            for(int i=0;i<a1.length; i++){
               res[i]=a1[i]*a2[i];
            }
            System.arraycopy(a2, a1.length, res, a1.length, a2.length - a1.length);
        }else{
            for(int i=0;i<a2.length; i++){
               res[i]=a1[i]*a2[i];
            }
            System.arraycopy(a1, a2.length, res, a2.length, a1.length - a2.length);
            /* The above line is equivalent to 
            for(int i=a2.length;i<a1.length; i++){
               res[i]=a1[i];
            }*/
        }
        return res;
    }
    
    public static double[] differencearrays (double [] a1, double[] a2){
        int maxlength;
        if (a1.length<a2.length){
            maxlength = a2.length;
        } else{
           maxlength = a1.length;
        } 
        double[] res = new double[maxlength];
        
        if (a1.length<a2.length){
            for(int i=0;i<a1.length; i++){
               res[i]=a1[i]-a2[i];
            }
            System.arraycopy(a2, a1.length, res, a1.length, a2.length - a1.length);
        }else{
            for(int i=0;i<a2.length; i++){
               res[i]=a1[i]-a2[i];
            }
            System.arraycopy(a1, a2.length, res, a2.length, a1.length - a2.length);
            /* The above line is equivalent to 
            for(int i=a2.length;i<a1.length; i++){
               res[i]=a1[i];
            }*/
        }
        return res;
    }
    
    
    
    
}
