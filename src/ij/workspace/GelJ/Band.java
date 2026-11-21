/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ;

import ij.gui.Roi;
import java.io.Serializable;

/**
 *
 * @author joheras
 */
public class Band implements Serializable{

    private Roi roi;
    private int lane;
    
    public Band(Roi roi,int lane) {
        this.roi = roi;
        this.lane = lane;
    }

    public Roi getRoi() {
        return roi;
    }

    public int getLane() {
        return lane;
    }

    
    public static Roi[] getroifrombands (Band[] bands){
        Roi[] res = new Roi[bands.length];    
        for(int i = 0; i<bands.length;i++){
         res[i] = bands[i].getRoi();
        }   
        return res;
    }
    
    private static int bandsofalanenumber (Band[] bands,int lane){
        int n = 0;
        for (Band b : bands){
            if (b.getLane() == lane){
                n++;
            }
        }
        return n;
    }
    
    
    public static Band[] bandsofalane (Band[] bands,int lane){
        int n = bandsofalanenumber(bands,lane);
        Band[] res  = new Band[n];
        
        int i = 0;
        for (Band b : bands){
            if (b.getLane() == lane){
                res[i] = b;
                i++;
            }
        }
        return res;
    }
    
    
    
    
    
}
