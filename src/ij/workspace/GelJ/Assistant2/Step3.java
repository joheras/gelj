package ij.workspace.GelJ.Assistant2;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;
import ij.workspace.GelJ.BandManager;
import static ij.workspace.GelJ.BandSegmentation.BLUE;
import static ij.workspace.GelJ.BandSegmentation.CYAN;
import static ij.workspace.GelJ.BandSegmentation.GREEN;
import static ij.workspace.GelJ.BandSegmentation.MAGENTA;
import static ij.workspace.GelJ.BandSegmentation.ORANGE;
import static ij.workspace.GelJ.BandSegmentation.RED;
import static ij.workspace.GelJ.BandSegmentation.YELLOW;
import static ij.workspace.GelJ.Lane.getstandardlanes;
import static ij.workspace.GelJ.Lane.roisfromlanes;
import ij.workspace.GelJ.LaneManager;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class Step3 extends ImageWindow implements ActionListener {

    private java.awt.Button Back;
    private javax.swing.ButtonGroup buttonGroup1;
    private java.awt.Button buttonnext;
    private java.awt.Canvas canvas1;
    private java.awt.Choice choice1;
    private java.awt.Panel jPanel1;
    private java.awt.Panel jPanel2;
    private java.awt.Panel jPanel3;
    private java.awt.Panel jPanel4;
    private java.awt.Panel jPanel5;
    private java.awt.Label label1;
    private java.awt.Label label10;
    private java.awt.Label label11;
    private java.awt.Label label12;
    private java.awt.Label label2;
    private java.awt.Label label3;
    private java.awt.Label label4;
    private java.awt.Label label5;
    private java.awt.Label label6;
    private java.awt.Label label7;
    private java.awt.Label label8;
    private java.awt.Label label9;
    private java.awt.Scrollbar scrollbar1;

    private ArrayDeque<ImagePlus> history = new ArrayDeque<>();
    private BandManager bm;
    private boolean darkbg;
    private ImageAssistant ia;


    public Step3(ImagePlus imp, ImageCanvas ic, ImageAssistant ia, boolean darkbg) {
        super(imp, ic);
        this.darkbg = darkbg;
        this.ia = ia;
        IJ.setTool("point");
        removeAll();
        addPanel(ic);
        imp.updateAndDraw();
        ic.requestFocus();
        bm = new BandManager(ia.m);
        bm.setbgcheckboxstate(darkbg);
//        bm.auto_detect();
        bm.setVisible(false);
        String[] colours = {"blue","red","green","magenta","cyan","yellow","orange"};
        for(String s : colours){
            choice1.add(s);
        }
        choice1.select(0);
        
        this.setLocation(0, 0);
        globalselectbands();
         IJ.run(imp, "In [+]", "");
        IJ.run(imp, "Out [-]", "");

    }

    private void exitForm(java.awt.event.WindowEvent evt) throws IOException {
        imp.changes = false;
        ia.saveUnfinishedExperiment(this, 6);
    }

    void addPanel(ImageCanvas ic) {

       buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new java.awt.Panel();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        label3 = new java.awt.Label();
        label4 = new java.awt.Label();
        label6 = new java.awt.Label();
        label7 = new java.awt.Label();
        label8 = new java.awt.Label();
        jPanel2 = new java.awt.Panel();
        canvas1 = ic;
        jPanel4 = new java.awt.Panel();
        buttonnext = new java.awt.Button();
        Back = new java.awt.Button();
        jPanel5 = new java.awt.Panel();
        jPanel3 = new java.awt.Panel();
        label9 = new java.awt.Label();
        label10 = new java.awt.Label();
        label11 = new java.awt.Label();
        label12 = new java.awt.Label();
        label5 = new java.awt.Label();
        scrollbar1 = new java.awt.Scrollbar();
        choice1 = new java.awt.Choice();

        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                try {
                    exitForm(evt);
                } catch (IOException ex) {
                    Logger.getLogger(Step3.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        label1.setText("Step 1. Image Processing");

        label2.setForeground(new java.awt.Color(0, 17, 255));
        label2.setText("3.1. Global threshold");

        label3.setForeground(new java.awt.Color(0, 0, 0));
        label3.setText("3.2. Lane by Lane threshold");

        label4.setForeground(new java.awt.Color(0, 0, 0));
        label4.setText("3.3. Manual band picking");

        label6.setForeground(new java.awt.Color(0, 0, 0));
        label6.setText("Step 2. Lane Detection");

        label7.setForeground(new java.awt.Color(0, 0, 0));
        label7.setText("Step 3. Band Detection");

        label8.setText("Step 4. Normalisation");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        canvas1.setMinimumSize(new java.awt.Dimension(10, 10));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas1, ic.getWidth(), ic.getWidth(),ic.getWidth())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas1, javax.swing.GroupLayout.Alignment.TRAILING, ic.getHeight(),ic.getHeight(),ic.getHeight())
        );

        buttonnext.setLabel("Next >");
        buttonnext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonnextActionPerformed(evt);
            }
        });

        Back.setLabel("< Back");
        Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Back, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonnext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonnext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Back, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel5.setMaximumSize(new java.awt.Dimension(100, 181));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        label9.setText("In this window, you can fix the");

        label10.setText("threshold to find bands in the");

        label11.setText("scrollbar.");

        label12.setText("image. Just move the above");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        label5.setText("Fix the tolerance to find bands.");

        scrollbar1.setMaximum(255);
        scrollbar1.setMinimum(25);
        scrollbar1.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar1.setValue(150);
        scrollbar1.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar1AdjustmentValueChanged(evt);
            }
        });

        choice1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                choice1ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(label5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollbar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollbar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }

    public void actionPerformed(ActionEvent e) {

    }

    private void buttonnextActionPerformed(java.awt.event.ActionEvent evt) {

        
        BandManagerCanvas cc = new BandManagerCanvas(imp);
        Color c = Color.BLUE;
        switch (choice1.getSelectedIndex()){
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
        Step3local s1i = new Step3local(imp, cc, ia, darkbg,bm,scrollbar1.getValue(),c);
        s1i.setHistory(history);

        this.dispose();

    }

    private void BackActionPerformed(java.awt.event.ActionEvent evt) {
         int option = JOptionPane.showConfirmDialog(this, "If you go back, the current band selection\n will be lost, do you want to go back?", "Alert", JOptionPane.YES_NO_OPTION);
        if(option==JOptionPane.YES_OPTION){
        imp.deleteRoi();
        bm.dispose();
        LaneManagerCanvas cc = new LaneManagerCanvas(imp);
        LaneManager lm = new LaneManager(ia.m);
        Step2FixStandard s1i = new Step2FixStandard(imp, cc, ia, darkbg,lm);
        lm.delete_without_asking(false);
        Roi[] lanes = ia.m.getLanes();
        if (lanes != null) {
            for (Roi lane : lanes) {
                lm.addRoi(lane);
            }

            if (getstandardlanes(ia.m.finallanes) != null) {
                lm.fix_standards(roisfromlanes(getstandardlanes(ia.m.finallanes)));
            }
        }
        s1i.setHistory(history);
        this.dispose();
        }

    }

    private void autoActionPerformed(java.awt.event.ActionEvent evt) {
        bm.auto_detect();
    }

    private void addActionPerformed(java.awt.event.ActionEvent evt) {
        bm.add_band();
    }

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {
        bm.delete(false);
    }

    private void PointActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.setTool("point");
    }

    private void ZoomActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.setTool("zoom");
    }

    private void HandActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.setTool("hand");
    }

    private void PickColourActionPerformed(java.awt.event.ActionEvent evt) {
        bm.pick_colour();
    }

    private void UndoActionPerformed(java.awt.event.ActionEvent evt) {
        bm.undo();
    }

    private void RedoActionPerformed(java.awt.event.ActionEvent evt) {
        bm.redo();
    }

    public BandManager getBm() {
        return bm;
    }

    public void setHistory(ArrayDeque<ImagePlus> history) {
        this.history = history;
    }

//    private void checkbox1ItemStateChanged(java.awt.event.ItemEvent evt) {                                           
//        bm.activatehistogramdisplay(checkbox1.getState());
//    }  
    
    private void scrollbar1AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {                                                  
        
        globalselectbands();
    }                                                 

    private void choice1ItemStateChanged(java.awt.event.ItemEvent evt) {                                         
        
        globalselectbands();
    }  
    
    
    private void globalselectbands (){
        double tolerance = scrollbar1.getValue();
        int colour = choice1.getSelectedIndex();
        Color c = Color.BLUE;
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
        
        
        Prefs.verticalProfile = true;
        bm.delete_without_asking(true);
        bm.restart_bands();
        ImageProcessor ip = imp.getProcessor();
        
        for (int i =0; i<ia.m.finallanes.length;i++) {
            Roi r0 = ia.m.finallanes[i].getRoi();
            int x0 = r0.getBounds().x + r0.getBounds().width / 2;
            int y0 = r0.getBounds().y;
            int x1 = r0.getBounds().x + r0.getBounds().width / 2;
            int y1 = r0.getBounds().y + r0.getBounds().height;

            Line l0 = new Line(x0, y0, x1, y1);
            l0.setWidth(15);
            imp.setRoi(l0);

            // Finding the peaks of the profile
            if ((!darkbg && !imp.isInvertedLut()) || imp.isInvertedLut()){
                ip.invert();
            }
            //ProfilePlot pp = new ProfilePlot(imp);
            double[] profile = ia.m.finallanes[i].getProfile();
            if ((!darkbg && !imp.isInvertedLut()) || imp.isInvertedLut()){
                ip.invert();
            }
            l0.setWidth(1);
            int[] maxima = MaximumFinder.findMaxima(profile, 2, true);
            int[] htt = higherthantolerance(maxima, profile, tolerance);
            Arrays.sort(htt);

            // Showing the lines
            int r0x = r0.getBounds().x;
            int r0y = r0.getBounds().y;
            int w = r0.getBounds().width;
            
            int[] positionsy = new int[htt.length];
            for (int k = 0; k<htt.length;k++){
                positionsy[k]=r0y+htt[k];
            }
            

            for (int j = 0; j < htt.length; j++) {

                /*Line line = new Line(r0x, r0y + htt[j], r0x + w, r0y + htt[j]);
                Line lineintersection = lineintersectionroi(r0,line);*/
                Rectangle r = new Rectangle(r0x, r0y + htt[j], w, 2);
                
                Roi r1 = rectangleintersectionroi(r0, r);
                r1.setFillColor(c);
                bm.add(imp, r1, j,i);
                
                //bm.add(imp, line, j);

            }
           
        //m.finallanes[i].setProfile(profile);
        //m.finallanes[i].setPositionbands(positionsy);
        
            
        }

        bm.showAll(0);
    
    
    }
    
    
    private Line lineintersectionroi (Roi r, Line l){
       Line line; 
       int[] xpointsl = l.getPoints().xpoints;
       int[] ypointsl = l.getPoints().ypoints;
       int len = xpointsl.length;
       Boolean inside = false;
       int x0 = xpointsl[0];
       int y0 = ypointsl[0];  
       int x1 = xpointsl[len-1];
       int y1 = ypointsl[len-1];
       
       for (int i=1;i<len; i++){
           if (!inside){
               if(r.contains(xpointsl[i], ypointsl[i])){
                 x0 = xpointsl[i];
                 y0 = ypointsl[i];
                 inside = true;
               }
           } else{
               if(!r.contains(xpointsl[i], ypointsl[i])){
                 x1 = xpointsl[i];
                 y1 = ypointsl[i];
                 line = new Line(x0, y0, x1, y1);
                 return line;
               }
           }   
       }
        
       line = new Line(x0, y0, x1, y1);
       return line;
       
    }
    
    
    
    private int[] higherthantolerance (int[] v, double[] p, double tolerance){
        ArrayList<Integer> res = new ArrayList<Integer>();

        for (int i = 0; i < v.length; i++) {
            if (p[v[i]] > tolerance) {
                res.add(v[i]);
            }
        }

        int[] ret = new int[res.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = res.get(i);
        }
        return ret;
    }

    
    // Function that applies the average filter to a vector v using the value of
    // avgpoints as diameter of the circle where the filter is applied. 

    private double[] movingaveragefilter(double[] v, int avgpoints) {

        int lv = v.length;
        double[] res = new double[lv];
        int sp; // starting point 
        int ep; // end point 

        for (int i = 0; i < lv; i++) {

            sp = i - (avgpoints - 1) / 2;
            if (sp < 0) {
                sp = 0;
            }
            ep = i + (avgpoints - 1) / 2;
            if (ep >= lv) {
                ep = lv - 1;
            }

            double partial_sum = 0;
            for (int j = sp; j <= ep; j++) {
                partial_sum = partial_sum + v[j];
            }

            res[i] = partial_sum / (ep - sp + 1);

        }

        return res;

    }

    // Given a vector v, a double value and a boolean. This function searches 
    // the first or the last position in v such that v[pos] is smaller than
    // value. The boolean indicates if the search starts from the end or the 
    // beginning. 
    
    private int higherthanvalue (double[] v, double value, boolean from_end){
    
    if (from_end){
       int pos=0;
       for (int i = 0; i<v.length;i++){
            if(value<v[i]){return i;}
        }
       return pos;
    }
    else{
      int pos=v.length-1;
       for (int i = v.length - 1; i>=0;i--){
            if(value<v[i]){return i;}
        }
       return pos;
    
    }
    }


    private void removeOverlay(ImagePlus imp) {
        if (imp != null && imp.getCanvas() != null) {
            setOverlay(imp, null);
        }
    }
    
    private void setOverlay(ImagePlus imp, Overlay overlay) {
        if (imp == null) {
            return;
        }
        ImageCanvas ic = imp.getCanvas();
        if (ic == null) {
            imp.setOverlay(overlay);
            return;
        }
        ic.setShowAllList(overlay);
        imp.draw();
    }
    
    
    
    
}
