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
import static ij.workspace.GelJ.Lane.getstandardlanes;
import static ij.workspace.GelJ.Lane.indexesstandardlanes;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

/**
 *
 * @author joheras
 */
public class Step3_1 extends javax.swing.JFrame {

    /**
     * Creates new form crop
     */
    private boolean close = true;
    private ImagePlus imp;
    private Image_Assistant ia;
    private BandManager bm;
    private boolean darkbg;
    private int tolerance;
    private Color c;

    public Step3_1( ImagePlus imp) {
        super();
        initComponents();
        this.imp = imp;
    }

    public Step3_1( ImagePlus imp, Image_Assistant aThis, boolean darkbg) {
        super();
        initComponents();
        this.imp = imp;
        this.ia = aThis;
        this.darkbg = darkbg;
        bm = new BandManager(ia.m);
        bm.setbgcheckboxstate(darkbg);
        bm.setVisible(false);
        this.c = Color.BLUE;
        this.tolerance = 100;
        IJ.setTool("point");

        bm.updateShowAll();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        setVisible(true);
        setLocation((int) width - 515, 0);
        imp.getWindow().setAlwaysOnTop(true);
        setAlwaysOnTop(true);
        toFront();
        JSlider[] sliders = {jSlider1, jSlider2, jSlider3, jSlider4, jSlider5, jSlider6, jSlider7, jSlider8, jSlider9, jSlider10};
        JLabel[] labels = {jLabel5, jLabel9, jLabel10, jLabel11, jLabel12, jLabel13, jLabel14, jLabel15, jLabel16, jLabel17};
        
        int[] indexes = indexesstandardlanes(ia.m.finallanes);
        initiatenecessaryscrollbars(sliders, labels, indexes.length, tolerance);
        

       

    }

    
    private void initiatenecessaryscrollbars(JSlider[] scrollbars, JLabel[] labels, int n, int tolerance) {

        for (int i = 0; i < n; i++) {
            scrollbars[i].setValue(tolerance);
        }

        for (int i = n; i < scrollbars.length; i++) {
            scrollbars[i].setVisible(false);
            labels[i].setVisible(false);
        }
    }
    
    private int[] gettolerances(JSlider[] scrollbars, int n) {

        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = scrollbars[i].getValue();
        }
        return res;

    }
    
    
    private void localselectbands() {
        JSlider[] sliders = {jSlider1, jSlider2, jSlider3, jSlider4, jSlider5, jSlider6, jSlider7, jSlider8, jSlider9, jSlider10};
        
        int[] indexes = indexesstandardlanes(ia.m.finallanes);
        int[] tolerances = gettolerances(sliders, indexes.length);
        Prefs.verticalProfile = true;
        bm.delete_without_asking(true);
        bm.restart_bands();
        ImageProcessor ip = imp.getProcessor();
        
        

        for (int i = 0; i < indexes.length; i++) {

            Roi r0 = ia.m.finallanes[indexes[i]].getRoi();
            int x0 = r0.getBounds().x + r0.getBounds().width / 2;
            int y0 = r0.getBounds().y;
            int x1 = r0.getBounds().x + r0.getBounds().width / 2;
            int y1 = r0.getBounds().y + r0.getBounds().height;

            Line l0 = new Line(x0, y0, x1, y1);
            l0.setWidth(15);
            imp.setRoi(l0);

            // Finding the peaks of the profile
            if ((!darkbg && !imp.isInvertedLut()) || imp.isInvertedLut()) {
                ip.invert();
            }
            //ProfilePlot pp = new ProfilePlot(imp);
            double[] profile = ia.m.finallanes[indexes[i]].getProfile();
            if ((!darkbg && !imp.isInvertedLut()) || imp.isInvertedLut()) {
                ip.invert();
            }
            l0.setWidth(1);
            int[] maxima = MaximumFinder.findMaxima(profile, 2, true);
            int[] htt = higherthantolerance(maxima, profile, tolerances[i]);
            Arrays.sort(htt);

            // Showing the lines
            int r0x = r0.getBounds().x;
            int r0y = r0.getBounds().y;
            int w = r0.getBounds().width;

            int[] positionsy = new int[htt.length];
            for (int k = 0; k < htt.length; k++) {
                positionsy[k] = r0y + htt[k];
            }

            for (int j = 0; j < htt.length; j++) {

                /*Line line = new Line(r0x, r0y + htt[j], r0x + w, r0y + htt[j]);
                 Line lineintersection = lineintersectionroi(r0,line);*/
                Rectangle r = new Rectangle(r0x, r0y + htt[j], w, 2);

                Roi r1 = rectangleintersectionroi(r0, r);
                if(ia.m.finallanes[indexes[i]].isIsstandard()){
                    if(c==Color.RED){
                        r1.setFillColor(Color.BLUE);
                    }else{
                        r1.setFillColor(Color.RED);
                    }
                }
                else{
                    r1.setFillColor(c);
                }
                bm.add(imp, r1, j, indexes[i]);

                //bm.add(imp, line, j);
            }

        }
        //m.finallanes[i].setProfile(profile);
        //m.finallanes[i].setPositionbands(positionsy);
        bm.showAll(0);
        //m.finallanes = finallanes;

    }

    private Line lineintersectionroi(Roi r, Line l) {
        Line line;
        int[] xpointsl = l.getPoints().xpoints;
        int[] ypointsl = l.getPoints().ypoints;
        int len = xpointsl.length;
        Boolean inside = false;
        int x0 = xpointsl[0];
        int y0 = ypointsl[0];
        int x1 = xpointsl[len - 1];
        int y1 = ypointsl[len - 1];

        for (int i = 1; i < len; i++) {
            if (!inside) {
                if (r.contains(xpointsl[i], ypointsl[i])) {
                    x0 = xpointsl[i];
                    y0 = ypointsl[i];
                    inside = true;
                }
            } else {
                if (!r.contains(xpointsl[i], ypointsl[i])) {
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

    private int[] higherthantolerance(int[] v, double[] p, double tolerance) {
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
    private int higherthanvalue(double[] v, double value, boolean from_end) {

        if (from_end) {
            int pos = 0;
            for (int i = 0; i < v.length; i++) {
                if (value < v[i]) {
                    return i;
                }
            }
            return pos;
        } else {
            int pos = v.length - 1;
            for (int i = v.length - 1; i >= 0; i--) {
                if (value < v[i]) {
                    return i;
                }
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
    
    public BandManager getBm() {
        return bm;
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabel9 = new javax.swing.JLabel();
        jSlider2 = new javax.swing.JSlider();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jSlider3 = new javax.swing.JSlider();
        jSlider4 = new javax.swing.JSlider();
        jSlider5 = new javax.swing.JSlider();
        jSlider6 = new javax.swing.JSlider();
        jSlider7 = new javax.swing.JSlider();
        jSlider8 = new javax.swing.JSlider();
        jSlider9 = new javax.swing.JSlider();
        jSlider10 = new javax.swing.JSlider();

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

        jLabel7.setText("Step 3. Normalisation");

        jLabel8.setText("Step 4. Band detection");

        jLabel2.setForeground(new java.awt.Color(0, 15, 255));
        jLabel2.setText("3.1. Markers threshold");

        jLabel3.setText("3.2. Markers picking");

        jLabel4.setText("3.3. Migration model");

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
        jTextArea1.setText("In this step, you can automatically\ndetect the bands of each marker\n(reference lane). You can fix a\ndifferent threshold for each \nmarker of the image.\n");
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
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

        jLabel5.setText("Marker 1:");

        jSlider1.setMaximum(255);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel9.setText("Marker 2:");

        jSlider2.setMaximum(255);
        jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider2StateChanged(evt);
            }
        });

        jLabel10.setText("Marker 3:");

        jLabel11.setText("Marker 4:");

        jLabel12.setText("Marker 5:");

        jLabel13.setText("Marker 6:");

        jLabel14.setText("Marker 7:");

        jLabel15.setText("Marker 8:");

        jLabel16.setText("Marker 9:");

        jLabel17.setText("Marker 10:");

        jSlider3.setMaximum(255);
        jSlider3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider3StateChanged(evt);
            }
        });

        jSlider4.setMaximum(255);
        jSlider4.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider4StateChanged(evt);
            }
        });

        jSlider5.setMaximum(255);
        jSlider5.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider5StateChanged(evt);
            }
        });

        jSlider6.setMaximum(255);
        jSlider6.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider6StateChanged(evt);
            }
        });

        jSlider7.setMaximum(255);
        jSlider7.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider7StateChanged(evt);
            }
        });

        jSlider8.setMaximum(255);
        jSlider8.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider8StateChanged(evt);
            }
        });

        jSlider9.setMaximum(255);
        jSlider9.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider9StateChanged(evt);
            }
        });

        jSlider10.setMaximum(255);
        jSlider10.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider10StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addContainerGap(187, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jSlider8, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jSlider7, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jSlider10, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jSlider9, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel13))
                                .addGap(33, 33, 33)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSlider4, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSlider5, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSlider6, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jSlider4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jSlider5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jSlider6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jSlider7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jSlider8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(jSlider9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jSlider10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1152, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel6);

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
                    .addComponent(jScrollPane2)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        try {
            if (close) {
                ia.saveUnfinishedExperiment(this, 6);
                close=false;
            } else {

            }
        } catch (IOException ex) {
            Logger.getLogger(Step3_1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowClosed


    private void jbBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBackActionPerformed
        close = false;
        int option = JOptionPane.showConfirmDialog(this, "If you go back, the current band selection\n will be lost, do you want to go back?", "Alert", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            imp.deleteRoi();
            bm.dispose();
            ImageWindow iw = imp.getWindow();
            LaneManagerCanvas cc = new LaneManagerCanvas(imp);
            ImageWindow iw2 = new ImageWindow(imp, cc);
            iw.hide();
            LaneManager lm = new LaneManager(ia.m);
            step2_3 s = new step2_3(imp, ia, darkbg, lm);
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
        close = false;
        Step3_2 s = new Step3_2(imp,ia,darkbg,bm);
        this.dispose();


    }//GEN-LAST:event_jButton2ActionPerformed

    private void jSlider2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider2StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider2StateChanged

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider1StateChanged

    private void jSlider3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider3StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider3StateChanged

    private void jSlider4StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider4StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider4StateChanged

    private void jSlider5StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider5StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider5StateChanged

    private void jSlider6StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider6StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider6StateChanged

    private void jSlider7StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider7StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider7StateChanged

    private void jSlider8StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider8StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider8StateChanged

    private void jSlider9StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider9StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider9StateChanged

    private void jSlider10StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider10StateChanged
        localselectbands();
    }//GEN-LAST:event_jSlider10StateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider10;
    private javax.swing.JSlider jSlider2;
    private javax.swing.JSlider jSlider3;
    private javax.swing.JSlider jSlider4;
    private javax.swing.JSlider jSlider5;
    private javax.swing.JSlider jSlider6;
    private javax.swing.JSlider jSlider7;
    private javax.swing.JSlider jSlider8;
    private javax.swing.JSlider jSlider9;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton jbBack;
    // End of variables declaration//GEN-END:variables
}
