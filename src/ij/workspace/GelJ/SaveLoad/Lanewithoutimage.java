/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.SaveLoad;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.workspace.GelJ.Lane;
import java.io.Serializable;

/**
 *
 * @author joheras
 */
public class Lanewithoutimage implements Serializable{
    private String name;
    //private Roi roi;
    private double[] profile;
    private int[] positionbands;
    private double[] stdpositionbands;
    private boolean isstandard;
    private double offset = 0;

    public Lanewithoutimage(String name, Roi roi, double[] profile, int[] positionbands, 
            double[] stdpositionbands, boolean isstandard, double offset) {
            this.name = name;
            //this.roi = roi;
            this.profile = profile;
            this.positionbands = positionbands;
            this.stdpositionbands = stdpositionbands;
            this.isstandard = isstandard;
            this.offset = offset;
    }

    public String getName() {
        return name;
    }

/*    public Roi getnewRoi() {
        return roi;
    }
*/
    public double[] getProfile() {
        return profile;
    }

    public int[] getPositionbands() {
        return positionbands;
    }

    public double[] getStdpositionbands() {
        return stdpositionbands;
    }

    public boolean getisIsstandard() {
        return isstandard;
    }

    public double getOffset() {
        return offset;
    }

    public void setIsstandard(boolean isstandard) {
        this.isstandard = isstandard;
    }
    
    
    
    
    
    private static Lanewithoutimage LanetoLanewithoutimage (Lane l){
        return new Lanewithoutimage(l.getName(),l.getRoi(),l.getProfile(),l.getPositionbands(),
        l.getStdpositionbands(),l.isIsstandard(),l.getOffset());
        
    }
    
    
    public static Lanewithoutimage[] LanestoLaneswithoutimage (Lane[] lanes){
        Lanewithoutimage[] res = new Lanewithoutimage[lanes.length];
        for(int i = 0; i<lanes.length;i++){
            res[i] = LanetoLanewithoutimage(lanes[i]);
        }
        return res;
    }

    
    public static Lane LanewitoutimagetoLane (Lanewithoutimage l, ImagePlus imp, Roi r){
        
        Lane res = new Lane(null,l.name,r,l.isstandard);
        
        res.setPositionbands(l.positionbands);
        res.setStdpositionbands(l.stdpositionbands);
        res.setProfile(l.profile);
        res.setOffset(l.offset);
        
        imp.setRoi(r);
        ImagePlus dup = imp.duplicate();
        res.setImp(dup);
        return res;
    
    }
    
    
    public static Lane[] LaneswitoutimagetoLanes (Lanewithoutimage[] lanes, 
            ImagePlus imp, Roi[] rois){
        Lane[] res = new Lane[lanes.length];
        for(int i = 0;i<lanes.length;i++){
            res[i] = LanewitoutimagetoLane(lanes[i],imp,rois[i]);
        }
        return res;
    }
    
    

}    