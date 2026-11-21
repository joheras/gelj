/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;
import java.awt.Checkbox;
import java.awt.Color;

/**
 *
 * @author joheras
 */
public class ColourPicker implements ExtendedPlugInFilter, DialogListener {
    
    public static final int BLUE=0, RED=1, GREEN=2, MAGENTA=3, CYAN=4, YELLOW = 5, ORANGE=6;
    private static int FLAGS = DOES_ALL|SUPPORTS_MASKING|KEEP_PREVIEW|FINAL_PROCESSING;
    private int colour = 0;
    private int nPasses = 1;
    private PlugInFilterRunner pfr;
    private ImagePlus imp;
    private Roi[] rois;
    private BandManager bm;
    private Color c = Color.BLUE;
    private Color originalcolour;
    
    public ColourPicker(Roi[] rois, BandManager bm){
        this.rois = rois;
        this.bm=bm;
        originalcolour = bm.getColour();
    }

    @Override
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        GenericDialog gd = new GenericDialog(command);
        String[] colours = {"blue","red","green","magenta","cyan","yellow","orange"};
        gd.addChoice("Colour:", colours, "blue");
        this.imp = imp;
        this.pfr = pfr;
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
        ((Checkbox)gd.getCheckboxes().elementAt(0)).setState(true);
        gd.showDialog();                    // input by the user (or macro) happens here
        if (gd.wasCanceled()) {
            for (Roi r : rois){
            r.setFillColor(originalcolour);
        }
            return DONE;}
        if (gd.wasOKed()) bm.setColour(c);
        IJ.register(this.getClass());
        return IJ.setupDialog(imp, FLAGS);
    
    }

    @Override
    public void setNPasses(int nPasses) {
        this.nPasses = nPasses;
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return FLAGS;
    }

    @Override
    public void run(ImageProcessor ip) { 
        switch (colour){
            case BLUE:
                c = Color.BLUE;
                break;
            case RED:
                c = Color.RED;
                break;
            case GREEN:
                c = Color.GREEN;
                break;
            case MAGENTA:
                c = Color.MAGENTA;
                break;
            case CYAN:
                c = Color.CYAN;
                break;
            case YELLOW:
                c = Color.YELLOW;
                break;
            case ORANGE:
                c = Color.ORANGE;
                break;      
            default:
                c = Color.BLUE;
                break;
        }
        for (Roi r : rois){
            r.setFillColor(c);
        }
    }    

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        colour = gd.getNextChoiceIndex();
        return true;    
    }
    
}
