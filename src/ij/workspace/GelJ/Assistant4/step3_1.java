/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.Assistant4;


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
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class step3_1 extends javax.swing.JFrame {

    /**
     * Creates new form crop
     */
    private boolean close = true;
    private ImagePlus imp;
    private Image_Assistant ia;
    private BandManager bm;
    private boolean darkbg;
    
    public step3_1(ImagePlus imp) {
        super();
        initComponents();
        this.imp = imp;
    }

    public step3_1(ImagePlus imp, Image_Assistant aThis, boolean darkbg) {
        super();
        initComponents();
        this.imp = imp;
        this.ia =aThis;
        this.darkbg=darkbg;
        IJ.setTool("point");
        bm = new BandManager(ia.m);
        bm.setbgcheckboxstate(darkbg);
        bm.setVisible(false);
        
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        setVisible(true);
        setLocation((int)width-515, 0);
        imp.getWindow().setAlwaysOnTop(true);
        setAlwaysOnTop(true);
        toFront();
        
         globalselectbands();
        
    }

    public BandManager getBm() {
        return bm;
    }
    
    
    private void globalselectbands (){
        double tolerance = jSlider1.getValue();
        int colour = jComboBox1.getSelectedIndex();
        Color c;
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
                
                if(ia.m.finallanes[i].isIsstandard()){
                    if(colour==RED){
                        r1.setFillColor(Color.BLUE);
                    }else{
                            r1.setFillColor(Color.RED);
                    }
                    
                }
                else{
                    r1.setFillColor(c);
                }
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
    
    
    
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel4 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jbBack = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Image analysis");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton2.setText("Next>");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jbBack.setText("<Back");
        jbBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jbBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jbBack)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Steps")));
        jPanel5.setForeground(new java.awt.Color(0, 17, 255));

        jLabel1.setText("Step 1. Image preprocessing");

        jLabel6.setText("Step 2. Lane detection");

        jLabel7.setText("Step 3. Band detection");

        jLabel8.setText("Step 4. Normalisation");

        jLabel2.setForeground(new java.awt.Color(0, 15, 255));
        jLabel2.setText("3.1. Global threshold");

        jLabel3.setText("3.2. Lane threshold");

        jLabel4.setText("3.3. Band picking");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Help"));

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("In this step, you can fix a global \nthreshold to automatically locate\nthe bands of the image. Just move\nthe above scrollbar.\n\nYou can also select the colour of\nthe bands.");
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSlider1.setMaximum(255);
        jSlider1.setValue(150);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel5.setText("Threshold:");

        jLabel9.setText("Colour:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "blue", "red", "green", "magenta", "cyan", "yellow", "orange" }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(198, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        try {
          if(close){
                ia.saveUnfinishedExperiment(this, 6);
                close=false;
          }else{
          
          }
          } catch (IOException ex) {
            Logger.getLogger(step3_1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowClosed

    
    
    
    
    
    private void jbBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBackActionPerformed
        close=false;
         int option = JOptionPane.showConfirmDialog(this, "If you go back, the current band selection\n will be lost, do you want to go back?", "Alert", JOptionPane.YES_NO_OPTION);
        if(option==JOptionPane.YES_OPTION){
        imp.deleteRoi();
        bm.dispose();
        ImageWindow iw = imp.getWindow();
                iw.setLocked(false);
        LaneManagerCanvas cc = new LaneManagerCanvas(imp);
        ImageWindow iw2 = new ImageWindow(imp, cc);
        iw2.setLocked(true);
        iw.dispose();
        LaneManager lm = new LaneManager(ia.m);
        step2_3 s = new step2_3( imp, ia,darkbg,lm);
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
        lm.setLanesinfo(ia.m.getLanesinfo());
        
        this.dispose();
        }
        
        
        
        
    }//GEN-LAST:event_jbBackActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        close=false;
        Color c = Color.BLUE;
        switch (jComboBox1.getSelectedIndex()){
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
        step3_2 s = new step3_2( imp, ia, darkbg,bm,jSlider1.getValue(),c);
        this.dispose();
                
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
       globalselectbands();
    }//GEN-LAST:event_jSlider1StateChanged

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
       globalselectbands();
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton jbBack;
    // End of variables declaration//GEN-END:variables
}
