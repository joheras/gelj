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
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.AWTEvent;
import java.awt.Scrollbar;
import java.util.Vector;

/**
 *
 * @author joheras
 */
public class LaneBackground implements ExtendedPlugInFilter, DialogListener {

    private int nPasses = 1;
    private ImagePlus dup;
    private ImagePlus imp1;
    private Roi r;
    private int radius = 50;
    private static int FLAGS = DOES_ALL | SUPPORTS_MASKING | KEEP_PREVIEW | FINAL_PROCESSING;
    private boolean lightBackground = false;
    private BackgroundSubtracter bs;
    private boolean changedbg;
    private LaneManager lm;

    public LaneBackground(ImagePlus dup, ImagePlus imp1, Roi r, boolean background, boolean changedbg, LaneManager lm) {
        this.dup = dup;
        this.r = r;
        this.imp1 = imp1;
        lightBackground = background;
        this.changedbg = changedbg;
        this.lm = lm;
    }

    @Override
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {

        GenericDialog gd = new GenericDialog("Background Subtraction");
        gd.addSlider("Radius", 0, 100, 50);
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
        gd.showDialog();
        if (gd.wasCanceled()) {
            imp.close();
            return DONE;
        }

        if (gd.wasOKed()) {
            imp1.setRoi(r);
            IJ.run(imp, "Select All", "");
            IJ.run(imp, "Copy", "");
            imp.changes = false;
            changedbg = true;
            lm.changedbg=true;
            imp.close();
        }

        return IJ.setupDialog(imp, FLAGS);

    }

    @Override
    public void setNPasses(int nPasses) {
        this.nPasses = nPasses;
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp1 = imp;
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        bs = new BackgroundSubtracter();

        bs.subtractBackround(ip, radius);
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        Vector sliders = gd.getSliders();
        radius = ((Scrollbar) sliders.elementAt(0)).getValue();
        lightBackground = gd.getNextBoolean();
        return true;
    }

    public void rollingBallBackground(ImageProcessor ip, double radius,
            boolean lightBackground) {
        boolean invertedLut = ip.isInvertedLut();
        boolean invert = (invertedLut && !lightBackground) || (!invertedLut && lightBackground);
        RollingBall ball = new RollingBall(radius);
        FloatProcessor fp = null;
        for (int channelNumber = 0; channelNumber < ip.getNChannels(); channelNumber++) {
            fp = ip.toFloat(channelNumber, fp);
            fp.snapshot();  //float images need a snapshot to subtract
            float[] bgPixels = (float[]) fp.getPixels(); //currently holds the background
            if (ip instanceof FloatProcessor) {         //here ip and fp are the same (bgPixels will be output)
                float[] snapshotPixels = (float[]) fp.getSnapshotPixels(); //original data in the snapshot
                for (int p = 0; p < bgPixels.length; p++) {
                    bgPixels[p] = snapshotPixels[p] - bgPixels[p];
                }
                //for all others, the image data are in ip, the background is in fp
            } else if (ip instanceof ShortProcessor) {
                float offset = invert ? 65535.5f : 0.5f;//includes 0.5 for rounding when converting float to short
                short[] pixels = (short[]) ip.getPixels();
                for (int p = 0; p < bgPixels.length; p++) {
                    float value = (pixels[p] & 0xffff) - bgPixels[p] + offset;
                    if (value < 0f) {
                        value = 0f;
                    }

                    if (value > 65535f) {
                        value = 65535f;
                    }

                    pixels[p] = (short) (value);
                }
            } else if (ip instanceof ByteProcessor) {
                float offset = invert ? 255.5f : 0.5f;  //includes 0.5 for rounding when converting float to byte
                byte[] pixels = (byte[]) ip.getPixels();
                for (int p = 0; p < bgPixels.length; p++) {
                    float value = (pixels[p] & 0xff) - bgPixels[p] + offset;
                    if (value < 0f) {
                        value = 0f;
                    }

                    if (value > 255f) {
                        value = 255f;
                    }

                    pixels[p] = (byte) (value);
                }
            } else if (ip instanceof ColorProcessor) {
                float offset = invert ? 255.5f : 0.5f;
                int[] pixels = (int[]) ip.getPixels();
                int shift = 16 - 8 * channelNumber;

                int byteMask = 255 << shift;
                int resetMask = 0xffffffff ^ (255 << shift);

                for (int p = 0; p < bgPixels.length; p++) {
                    int pxl = pixels[p];
                    float value = ((pxl & byteMask) >> shift) - bgPixels[p] + offset;
                    if (value < 0f) {
                        value = 0f;
                    }

                    if (value > 255f) {
                        value = 255f;
                    }
                    pixels[p] = (pxl & resetMask) | ((int) value << shift);

                }
            }

        }
    }

    class RollingBall {

        float[] data;
        int width;
        int shrinkFactor;

        RollingBall(double radius) {
            int arcTrimPer;
            if (radius <= 10) {
                shrinkFactor = 1;
                arcTrimPer = 24; // trim 24% in x and y
            } else if (radius <= 30) {
                shrinkFactor = 2;
                arcTrimPer = 24; // trim 24% in x and y
            } else if (radius <= 100) {
                shrinkFactor = 4;
                arcTrimPer = 32; // trim 32% in x and y
            } else {
                shrinkFactor = 8;
                arcTrimPer = 40; // trim 40% in x and y
            }
            buildRollingBall(radius, arcTrimPer);
        }

        /**
         * Computes the location of each point on the rolling ball patch
         * relative to the center of the sphere containing it. The patch is
         * located in the top half of this sphere. The vertical axis of the
         * sphere passes through the center of the patch. The projection of the
         * patch in the xy-plane below is a square.
         */
        void buildRollingBall(double ballradius, int arcTrimPer) {
            double rsquare;     // rolling ball radius squared
            int xtrim;          // # of pixels trimmed off each end of ball to make patch
            int xval, yval;     // x,y-values on patch relative to center of rolling ball
            double smallballradius; // radius of rolling ball (downscaled in x,y and z when image is shrunk)
            int halfWidth;      // distance in x or y from center of patch to any edge (patch "radius")

            this.shrinkFactor = shrinkFactor;
            smallballradius = ballradius / shrinkFactor;
            if (smallballradius < 1) {
                smallballradius = 1;
            }
            rsquare = smallballradius * smallballradius;
            xtrim = (int) (arcTrimPer * smallballradius) / 100; // only use a patch of the rolling ball
            halfWidth = (int) Math.round(smallballradius - xtrim);
            width = 2 * halfWidth + 1;
            data = new float[width * width];

            for (int y = 0, p = 0; y < width; y++) {
                for (int x = 0; x < width; x++, p++) {
                    xval = x - halfWidth;
                    yval = y - halfWidth;
                    double temp = rsquare - xval * xval - yval * yval;
                    data[p] = temp > 0. ? (float) (Math.sqrt(temp)) : 0f;
                    //-Float.MAX_VALUE might be better than 0f, but gives different results than earlier versions
                }
            }
        //IJ.log(ballradius+"\t"+smallballradius+"\t"+width); //###
            //IJ.log("half patch width="+halfWidth+", size="+data.length);
        }

    }

}
