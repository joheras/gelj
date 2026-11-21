/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author joheras
 */
public class Lane implements Serializable{
    
    private ImagePlus imp;
    private String name;
    private Roi roi;
    private double[] profile;
    private int[] positionbands;
    private int[] positionbands2;
    private double[] stdpositionbands;
    private int[] markerpositionbands;
    private boolean isstandard;
    private double offset = 0;
    private CurveFitter curve = null;
    private int curvemethod = 0;
 
    private String genus="";
    private String species="";
    private String country="";
    private String MLST="";
    private String StrainNumber="";
    private double scalefactor=1.0;
    
    
    public Lane (ImagePlus imp, String name, Roi roi,boolean isstandard){
        this.imp = imp;
        this.name = name;
        this.roi = roi;
        this.isstandard = isstandard;
        
    }
    
    public Lane (ImagePlus imp, String name, Roi roi,boolean isstandard,String genus,String species, String country, String MLST, String StrainNumber){
        this.imp = imp;
        this.name = name;
        this.roi = roi;
        this.isstandard = isstandard;
        this.genus = genus;
        this.species = species;
        this.country = country;
        this.MLST = MLST;
        this.StrainNumber = StrainNumber;
        
    }
    
    public Lane (ImagePlus imp, String name, Roi roi, double[] profile, int[] positionbands, double[] stdpositionbands){
        this.imp = imp;
        this.name = name;
        this.roi = roi;
        this.profile = profile;
        this.positionbands = positionbands;
        this.stdpositionbands = stdpositionbands;
    }

 
    
    public void setIsstandard(boolean isstandard) {
        this.isstandard = isstandard;
    }

    public ImagePlus getImp() {
        return imp;
    }

    public void setImp(ImagePlus imp) {
        this.imp = imp;
    }
    
    
    
    public boolean isIsstandard() {
        return isstandard;
    }


    public double[] getProfile() {
        return profile;
    }

    public int[] getPositionbands() {
        return positionbands;
    }

    public double[] getStdpositionbands() {
        return stdpositionbands;
    }

    public String getName() {
        return name;
    }

    public Roi getRoi() {
        return roi;
    }

    public void setRoi(Roi roi) {
        this.roi = roi;
    }

    
    
    public void setProfile(double[] profile) {
        this.profile = profile;
    }

    public void setPositionbands(int[] positionbands) {
        this.positionbands = positionbands;
    }

    public void setStdpositionbands(double[] stdpositionbands) {
        this.stdpositionbands = stdpositionbands;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
    
    public void showprofile (){
        double[] xValues = new double[profile.length];
        for(int i=0;i<profile.length;i++){
            xValues[i]=i;
        }   
        Plot plot = new Plot("Profile of "+name, "", "", xValues, profile);
	plot.show();
        
    
    }
    
    
    
    
    
    private static int numberofstdlanes(Lane[] lanes){
       int res = 0;
       for(Lane l : lanes){
          if(l.isIsstandard()){
              res++;
          }  
        }
        return res;
        
    }
    
    public static int[] numberofbandslanes (Lane[] lanes){
        int[] res = new int[lanes.length];
        int i = 0;
        for(Lane l : lanes){
              res[i] =l.getPositionbands().length;
              i++;
        } 
        
        return res;
    
    }
    
    
    public static Lane[] getstandardlanes(Lane[] lanes){
        Lane[] res = new Lane[numberofstdlanes(lanes)];
        int i = 0;
        for(Lane l : lanes){
            if(l.isIsstandard()){
              res[i] =l;
              i++;
          } 
        
        }
        return res;
    
    }
    
    public static int[] indexesstandardlanes (Lane[] lanes){
        int[] res = new int[numberofstdlanes(lanes)];
        int i = 0;
        for(int j=0;j<lanes.length;j++){
            if(lanes[j].isIsstandard()){
              res[i] =j;
              i++;
          } 
        
        }
        return res;
    
    }
    
     public static int[] indexesnonstandardlanes (Lane[] lanes){
        int[] res = new int[lanes.length - numberofstdlanes(lanes)];
        int i = 0;
        for(int j=0;j<lanes.length;j++){
            if(!lanes[j].isIsstandard()){
              res[i] =j;
              i++;
          } 
        
        }
        return res;
    
    }
    
    
    
    public static String[] lanesnames (Lane[] l){
        String[] res = new String[l.length];
        for(int i = 0; i<l.length;i++){
            res[i] = l[i].getName();
        }
        return res;
    }
    
    public static Roi[] roisfromlanes (Lane[] l){
        Roi[] res = new Roi[l.length];
        for(int i = 0; i<l.length;i++){
            res[i] = l[i].getRoi();
        }
        return res;
    }
    
    
    public static int posroiinlanes (Lane[] l,Roi r){
        for(int i =0;i<l.length;i++){
            if(l[i].getRoi().equals(r)){
                return i;
            }
        }
        return -1;
    }
    
    public static boolean[] whatarestandardlanes(Lane[] lanes){
        boolean[] res = new boolean[lanes.length];
        for(int i =0;i<lanes.length;i++){
            res[i] = lanes[i].isIsstandard();
        }
        return res;
    
    }
    
    
    public static int biggerlane (Lane[] lanes){
        int res = 0;
        for(Lane l : lanes){
            if(res<l.getStdpositionbands().length){
                res = l.getStdpositionbands().length;
            }
        }
        return res;
    }
    
    public static ImagePlus[] getImagesfromfinallanes (Lane[] lanes,int method,Color c,Boolean invert){
        ImagePlus[] res = new ImagePlus[lanes.length];
        if(method<5){
            
            
            for(int i=0;i<lanes.length;i++){
            ImagePlus imp = lanes[i].getImp();
            IJ.run(imp, "RGB Color", "");
            ImagePlus imp2 = imp.duplicate();
            int[] pos = lanes[i].getPositionbands2();
            ImageProcessor ip = imp2.getProcessor();
            if(invert){ip.invert();}
            ip.setColor(c);
            for(int j=0;j<pos.length;j++){
                ip.drawRect(0, pos[j],imp2.getWidth(), 2);
            }
            
            
            //ip.translate(0, lanes[i].getOffset());
            
            res[i] = imp2;
        }
        }else{    
            for(int i=0;i<lanes.length;i++){
            ImagePlus imp = lanes[i].getImp();
            res[i] = imp;
         }
        }
        return res;
    }
    
    public static ImagePlus[] getonlyImagesfromfinallanes (Lane[] lanes){
        ImagePlus[] res = new ImagePlus[lanes.length];
        for(int i=0;i<lanes.length;i++){
            ImagePlus imp = lanes[i].getImp();
            res[i] = imp;
        }
        return res;
    }
    
    
    public static ImagePlus[] getonlybandsfromfinallanes (Lane[] lanes,boolean invert){
        ImagePlus[] res = new ImagePlus[lanes.length];
            
            
        for(int i=0;i<lanes.length;i++){
            ImagePlus imp = lanes[i].getImp();
            
            ImagePlus imp2 = IJ.createImage(lanes[i].getName(), imp.getWidth(), 
                    imp.getHeight(), 0, 32);
                    
            int[] pos = lanes[i].getPositionbands2();
            ImageProcessor ip = imp2.getProcessor();
            if(invert){ip.invert();}
            ip.setColor(Color.blue);
            for(int j=0;j<pos.length;j++){
                ip.drawRect(0, pos[j],imp2.getWidth(), 2);
            }
            
            
            //ip.translate(0, lanes[i].getOffset());
            
            res[i] = imp2;
        }
        
        return res;
    }
    
    
    
    public static ImagePlus[] getemptyimages (Lane[] lanes){
        ImagePlus[] res = new ImagePlus[lanes.length];
        double scalefactor = 1;
        if (lanes[0].getImp().getHeight() < 600) {
            scalefactor = 600 / (double) lanes[0].getImp().getHeight();
        }
        
        for(int i=0;i<lanes.length;i++){
            res[i]=IJ.createImage("", "white-8",(int)(lanes[i].getImp().getWidth()*scalefactor), 2, 0);
        }
        return res;
    }

    public CurveFitter getCurve() {
        return curve;
    }

    public void setCurve(CurveFitter curve) {
        this.curve = curve;
    }

    public int getCurvemethod() {
        return curvemethod;
    }

    public void setCurvemethod(int curvemethod) {
        this.curvemethod = curvemethod;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMLST() {
        return MLST;
    }

    public void setMLST(String MLST) {
        this.MLST = MLST;
    }

    public String getStrainNumber() {
        return StrainNumber;
    }

    public void setStrainNumber(String StrainNumber) {
        this.StrainNumber = StrainNumber;
    }

    public int[] getMarkerpositionbands() {
        return markerpositionbands;
    }

    public void setMarkerpositionbands(int[] markerpositionbands) {
        this.markerpositionbands = markerpositionbands;
    }

    public double getScalefactor() {
        return scalefactor;
    }

    public void setScalefactor(double scalefactor) {
        this.scalefactor = scalefactor;
    }

    public int[] getPositionbands2() {
        return positionbands2;
    }

    public void setPositionbands2(int[] positionbands2) {
        this.positionbands2 = positionbands2;
    }
    
    
    
    
    
    
}
