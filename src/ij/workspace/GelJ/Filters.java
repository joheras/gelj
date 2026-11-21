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
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;

/**
 *
 * @author joheras
 */
public class Filters implements ExtendedPlugInFilter, DialogListener {
    
    public static final int MEAN=0, MIN=1, MAX=2, VARIANCE=3, MEDIAN=4, Gaussian = 5;
    private static int FLAGS = DOES_ALL|SUPPORTS_MASKING|KEEP_PREVIEW|FINAL_PROCESSING;
    private double radius = 2;
    private int filter = 0;
    private int nPasses = 1;
    private RankFilters rf = new RankFilters();
    private PlugInFilterRunner pfr;
    private ImagePlus imp;
    

    @Override
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        GenericDialog gd = new GenericDialog(command);
        String[] filters = {"Mean", "Minimum", "Maximum", 
                            "Variance", "Median", "Gaussian"};
        gd.addChoice("Filter:", filters, "Mean");
        gd.addNumericField("Radius:", radius, 2);
        this.imp = imp;
        this.pfr = pfr;
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
        gd.showDialog();                    // input by the user (or macro) happens here
        if (gd.wasCanceled()) return DONE;
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
        if (filter < 5 )
            rf.rank(imp.getProcessor(),radius,filter);
        ip.blurGaussian(radius);
       
    }    

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        radius = gd.getNextNumber();
        filter = gd.getNextChoiceIndex();
        if (radius < 0 || gd.invalidNumber())
            return false;
        return true;    
    }
    
}
