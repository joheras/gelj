/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.workspace.GelJ.Assistant3.Image_Assistant;
import ij.workspace.GelJ.Menu;






/*import ij.Prefs;
import ij.WindowManager;
import ij.gui.Arrow;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import static ij.gui.Roi.MOVING;
import static ij.gui.Roi.NORMAL;
import static ij.gui.Roi.OVAL;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;

import ij.plugin.frame.Recorder;
import ij.plugin.frame.RoiManager;
import ij.plugin.tool.PixelInspectionTool;
import ij.plugin.tool.PlugInTool;

import ij.process.ImageProcessor;
import ij.workspace.GelJ.LaneManager;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
/*import weka.clusterers.HierarchicalClusterer;
import static weka.clusterers.HierarchicalClusterer.TAGS_LINK_TYPE;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;*/

/**
 *
 * @author joheras
 */
public class Test implements PlugIn {
   
    
    public Test(){}
    
    private ImagePlus imp;
    
   
    public void removebg (ImagePlus imp, Roi r){
        imp.deleteRoi();
        ImagePlus imp2 = imp.duplicate();
        imp2.setRoi(r);
        
        for (int i=0;i<imp2.getWidth();i++){
            for(int j =0;j<imp2.getHeight();j++){
                if(!r.contains(i, j)){
                    imp2.getProcessor().putPixel(i, j, 255);
                }
            }
        }
         imp2.updateAndDraw();
        ImagePlus imp3 = imp2.duplicate();
       imp3.show();
    
    
    }

  
    public void run(String args) {
        
     Menu m = new Menu();
        
     Image_Assistant ia = new Image_Assistant(m,null);
     ia.runAssistant();
        
        
        
        
        
        
        
        
        
        
        
        
//        ColorChooser cc = new ColorChooser("Pick a colour", Color.blue, true);
//        cc.getColor();
//        
        
        
          
            //HierarchyVisualizer a = new HierarchyVisualizer("(((human:2.0,(chimp:1.0,bonobo:1.0):1.0):1.0,gorilla:3.0):1.0,siamang:4.0)");
            /*JFrame f;
            f = new JFrame();
            Container contentPane = f.getContentPane();
            contentPane.add(a);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setSize(500, 500);
            f.setVisible(true);
            a.fitToScreen();
            a.getGraphics();*/
            /*double[] x = {62,73,83,94,107,120,134,149,168,184,227,251,278,306};
            double[] y = {727.5,679,630,582,533.5,485,436,388,339,291,194,145,97,48.5};
            new CurveCalibration(x, y, 0, new CurveFitter(x, y));
            */
            /*double[] x = {62,73,83,94,107,120,134,149,168,184,227,251,278,306};
            double[] y = {727.5,679,630,582,533.5,485,436,388,339,291,194,145,97,48.5};
            CurveFitter cf = new CurveFitter(x, y);
            cf.doFit(0);
            System.out.println(cf.getStatusString());
            char pChar = 'a';
            double[] pVal = cf.getParams();
            for (int i = 0; i < cf.getNumParams(); i++) {
            System.out.println(pChar + " = " + IJ.d2s(pVal[i],5,9));
            pChar++;
            }
            plot(cf);
            */
            /* try {
            float[] x = imp.getRoi().getFloatPolygon().xpoints;
            float[] y = imp.getRoi().getFloatPolygon().ypoints;
            float[] xpos = new float[(int)(imp.getHeight()-y[0])];
            float[] ypos = new float[(int)(imp.getHeight()-y[0])];
            xpos[0] = (int)x[0];
            ypos[0] = (int)y[0];
            float t1, t2, t3;
            imp.setRoi(new PointRoi(xpos[0],ypos[0]));
            Thread.sleep(50);
            for (int i=1;i<(int)(imp.getHeight()-y[0]);i++){
            try {
            t1 = ip.getPixel((int)xpos[i-1], (int)ypos[i-1]+1);
            t2 = ip.getPixel((int)xpos[i-1]-1, (int)ypos[i-1]+1);
            t3 = ip.getPixel((int)xpos[i-1]+1, (int)ypos[i-1]+1);
            if (minofthree(t1,t2,t3)==t1){
            xpos[i] = xpos[i-1];
            } else if (minofthree(t1,t2,t3)==t2){
            xpos[i] = xpos[i-1]-1;
            } else{
            xpos[i] = xpos[i-1]+1;
            }
            ypos[i] = ypos[i-1]+1;
            imp.setRoi(new PointRoi(xpos[i],ypos[i]));
            Thread.sleep(50);
            } catch (InterruptedException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            } catch (InterruptedException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
        
    
       
        
        
        
    }
    

}
