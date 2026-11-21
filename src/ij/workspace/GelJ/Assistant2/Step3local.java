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
import static ij.workspace.GelJ.Lane.getstandardlanes;
import static ij.workspace.GelJ.Lane.roisfromlanes;
import ij.workspace.GelJ.LaneManager;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Color;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.Scrollbar;
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
public class Step3local extends ImageWindow implements ActionListener {

    private java.awt.Button Back;
    private javax.swing.ButtonGroup buttonGroup1;
    private java.awt.Button buttonnext;
    private java.awt.Canvas canvas1;
    private java.awt.Panel jPanel1;
    private java.awt.Panel jPanel2;
    private java.awt.Panel jPanel3;
    private java.awt.Panel jPanel4;
    private java.awt.Panel jPanel5;
    private java.awt.Label label1;
    private java.awt.Label label10;
    private java.awt.Label label11;
    private java.awt.Label label12;
    private java.awt.Label label13;
    private java.awt.Label label14;
    private java.awt.Label label15;
    private java.awt.Label label16;
    private java.awt.Label label17;
    private java.awt.Label label18;
    private java.awt.Label label19;
    private java.awt.Label label2;
    private java.awt.Label label20;
    private java.awt.Label label21;
    private java.awt.Label label22;
    private java.awt.Label label23;
    private java.awt.Label label24;
    private java.awt.Label label25;
    private java.awt.Label label26;
    private java.awt.Label label27;
    private java.awt.Label label28;
    private java.awt.Label label29;
    private java.awt.Label label3;
    private java.awt.Label label30;
    private java.awt.Label label31;
    private java.awt.Label label32;
    private java.awt.Label label33;
    private java.awt.Label label34;
    private java.awt.Label label35;
    private java.awt.Label label36;
    private java.awt.Label label37;
    private java.awt.Label label38;
    private java.awt.Label label39;
    private java.awt.Label label4;
    private java.awt.Label label40;
    private java.awt.Label label41;
    private java.awt.Label label5;
    private java.awt.Label label6;
    private java.awt.Label label7;
    private java.awt.Label label8;
    private java.awt.Label label9;
    private java.awt.Panel panel1;
    private java.awt.ScrollPane scrollPane1;
    private java.awt.Scrollbar scrollbar1;
    private java.awt.Scrollbar scrollbar10;
    private java.awt.Scrollbar scrollbar11;
    private java.awt.Scrollbar scrollbar12;
    private java.awt.Scrollbar scrollbar13;
    private java.awt.Scrollbar scrollbar14;
    private java.awt.Scrollbar scrollbar15;
    private java.awt.Scrollbar scrollbar16;
    private java.awt.Scrollbar scrollbar17;
    private java.awt.Scrollbar scrollbar18;
    private java.awt.Scrollbar scrollbar19;
    private java.awt.Scrollbar scrollbar2;
    private java.awt.Scrollbar scrollbar20;
    private java.awt.Scrollbar scrollbar21;
    private java.awt.Scrollbar scrollbar22;
    private java.awt.Scrollbar scrollbar23;
    private java.awt.Scrollbar scrollbar24;
    private java.awt.Scrollbar scrollbar25;
    private java.awt.Scrollbar scrollbar26;
    private java.awt.Scrollbar scrollbar27;
    private java.awt.Scrollbar scrollbar28;
    private java.awt.Scrollbar scrollbar29;
    private java.awt.Scrollbar scrollbar3;
    private java.awt.Scrollbar scrollbar30;
    private java.awt.Scrollbar scrollbar4;
    private java.awt.Scrollbar scrollbar5;
    private java.awt.Scrollbar scrollbar6;
    private java.awt.Scrollbar scrollbar7;
    private java.awt.Scrollbar scrollbar8;
    private java.awt.Scrollbar scrollbar9;

    private ArrayDeque<ImagePlus> history = new ArrayDeque<>();
    private BandManager bm;
    private boolean darkbg;
    private ImageAssistant ia;
    private int tolerance;
    private Color c;

    public Step3local(ImagePlus imp, ImageCanvas ic, ImageAssistant ia, boolean darkbg, BandManager bm, int tolerance, Color c) {
        super(imp, ic);
        this.darkbg = darkbg;
        this.ia = ia;
        this.c = c;
        IJ.setTool("point");
        removeAll();
        addPanel(ic);
        imp.updateAndDraw();
        ic.requestFocus();
        this.bm = bm;
        bm.updateShowAll();
        this.setLocation(0, 0);
        Scrollbar[] scrollbars = {scrollbar1, scrollbar2, scrollbar3, scrollbar4, scrollbar5, scrollbar6, scrollbar7, scrollbar8, scrollbar9, scrollbar10, scrollbar11, scrollbar12, scrollbar13, scrollbar14, scrollbar15, scrollbar16, scrollbar17, scrollbar18, scrollbar19, scrollbar20, scrollbar21, scrollbar22, scrollbar23, scrollbar24, scrollbar25, scrollbar26, scrollbar27, scrollbar28, scrollbar29, scrollbar30};
        Label[] labels = {label5, label13, label14, label15, label16, label17, label18, label19, label20, label21, label22, label23, label24, label25, label26, label27, label28, label29, label30, label31, label32, label33, label34, label35, label36, label37, label38, label39, label40, label41};
        initiatenecessaryscrollbars(scrollbars, labels, ia.m.finallanes.length, tolerance);
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
        scrollPane1 = new java.awt.ScrollPane();
        panel1 = new java.awt.Panel();
        label5 = new java.awt.Label();
        scrollbar1 = new java.awt.Scrollbar();
        label13 = new java.awt.Label();
        scrollbar2 = new java.awt.Scrollbar();
        scrollbar4 = new java.awt.Scrollbar();
        label14 = new java.awt.Label();
        label15 = new java.awt.Label();
        scrollbar3 = new java.awt.Scrollbar();
        label16 = new java.awt.Label();
        scrollbar5 = new java.awt.Scrollbar();
        scrollbar6 = new java.awt.Scrollbar();
        label17 = new java.awt.Label();
        scrollbar7 = new java.awt.Scrollbar();
        label18 = new java.awt.Label();
        scrollbar8 = new java.awt.Scrollbar();
        label19 = new java.awt.Label();
        scrollbar9 = new java.awt.Scrollbar();
        label20 = new java.awt.Label();
        scrollbar10 = new java.awt.Scrollbar();
        label21 = new java.awt.Label();
        scrollbar11 = new java.awt.Scrollbar();
        label22 = new java.awt.Label();
        scrollbar12 = new java.awt.Scrollbar();
        label23 = new java.awt.Label();
        scrollbar13 = new java.awt.Scrollbar();
        label24 = new java.awt.Label();
        scrollbar14 = new java.awt.Scrollbar();
        label25 = new java.awt.Label();
        scrollbar15 = new java.awt.Scrollbar();
        label26 = new java.awt.Label();
        scrollbar16 = new java.awt.Scrollbar();
        label27 = new java.awt.Label();
        scrollbar17 = new java.awt.Scrollbar();
        label28 = new java.awt.Label();
        scrollbar18 = new java.awt.Scrollbar();
        label29 = new java.awt.Label();
        scrollbar19 = new java.awt.Scrollbar();
        label30 = new java.awt.Label();
        label31 = new java.awt.Label();
        scrollbar20 = new java.awt.Scrollbar();
        label32 = new java.awt.Label();
        scrollbar21 = new java.awt.Scrollbar();
        label33 = new java.awt.Label();
        scrollbar22 = new java.awt.Scrollbar();
        label34 = new java.awt.Label();
        scrollbar23 = new java.awt.Scrollbar();
        label35 = new java.awt.Label();
        scrollbar24 = new java.awt.Scrollbar();
        label36 = new java.awt.Label();
        scrollbar25 = new java.awt.Scrollbar();
        label37 = new java.awt.Label();
        scrollbar26 = new java.awt.Scrollbar();
        label38 = new java.awt.Label();
        scrollbar27 = new java.awt.Scrollbar();
        label39 = new java.awt.Label();
        scrollbar28 = new java.awt.Scrollbar();
        label40 = new java.awt.Label();
        scrollbar29 = new java.awt.Scrollbar();
        label41 = new java.awt.Label();
        scrollbar30 = new java.awt.Scrollbar();

        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                try {
                    exitForm(evt);
                } catch (IOException ex) {
                    Logger.getLogger(Step3local.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        label1.setText("Step 1. Image Processing");

        label2.setForeground(new java.awt.Color(0, 0, 0));
        label2.setText("3.1. Global threshold");

        label3.setForeground(new java.awt.Color(0, 17, 255));
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
                .addComponent(canvas1, ic.getWidth(), ic.getWidth(), ic.getWidth())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(canvas1, javax.swing.GroupLayout.Alignment.TRAILING, ic.getHeight(), ic.getHeight(), ic.getHeight())
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

        label9.setText("In this window, you can fix the");

        label10.setText("threshold to find bands in each ");

        label11.setText("above scrollbars.");

        label12.setText("lane of the image. Just move the ");

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
                        .addGap(0, 22, Short.MAX_VALUE))
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

        label5.setText("Lane 1:");

        scrollbar1.setMaximum(255);
        scrollbar1.setMinimum(25);
        scrollbar1.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar1.setValue(150);
        scrollbar1.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar1AdjustmentValueChanged(evt);
            }
        });

        label13.setText("Lane 2:");

        scrollbar2.setMaximum(255);
        scrollbar2.setMinimum(25);
        scrollbar2.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar2.setValue(150);
        scrollbar2.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar2AdjustmentValueChanged(evt);
            }
        });

        scrollbar4.setMaximum(255);
        scrollbar4.setMinimum(25);
        scrollbar4.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar4.setValue(150);
        scrollbar4.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar4AdjustmentValueChanged(evt);
            }
        });

        label14.setText("Lane 4:");

        label15.setText("Lane 3:");

        scrollbar3.setMaximum(255);
        scrollbar3.setMinimum(25);
        scrollbar3.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar3.setValue(150);
        scrollbar3.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar3AdjustmentValueChanged(evt);
            }
        });

        label16.setText("Lane 5:");

        scrollbar5.setMaximum(255);
        scrollbar5.setMinimum(25);
        scrollbar5.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar5.setValue(150);
        scrollbar5.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar5AdjustmentValueChanged(evt);
            }
        });

        scrollbar6.setMaximum(255);
        scrollbar6.setMinimum(25);
        scrollbar6.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar6.setValue(150);
        scrollbar6.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar6AdjustmentValueChanged(evt);
            }
        });

        label17.setText("Lane 6:");

        scrollbar7.setMaximum(255);
        scrollbar7.setMinimum(25);
        scrollbar7.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar7.setValue(150);
        scrollbar7.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar7AdjustmentValueChanged(evt);
            }
        });

        label18.setText("Lane 7:");

        scrollbar8.setMaximum(255);
        scrollbar8.setMinimum(25);
        scrollbar8.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar8.setValue(150);
        scrollbar8.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar8AdjustmentValueChanged(evt);
            }
        });

        label19.setText("Lane 8:");

        scrollbar9.setMaximum(255);
        scrollbar9.setMinimum(25);
        scrollbar9.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar9.setValue(150);
        scrollbar9.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar9AdjustmentValueChanged(evt);
            }
        });

        label20.setText("Lane 9:");

        scrollbar10.setMaximum(255);
        scrollbar10.setMinimum(25);
        scrollbar10.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar10.setValue(150);
        scrollbar10.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar10AdjustmentValueChanged(evt);
            }
        });

        label21.setText("Lane 10:");

        scrollbar11.setMaximum(255);
        scrollbar11.setMinimum(25);
        scrollbar11.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar11.setValue(150);
        scrollbar11.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar11AdjustmentValueChanged(evt);
            }
        });

        label22.setText("Lane 11:");

        scrollbar12.setMaximum(255);
        scrollbar12.setMinimum(25);
        scrollbar12.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar12.setValue(150);
        scrollbar12.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar12AdjustmentValueChanged(evt);
            }
        });

        label23.setText("Lane 12:");

        scrollbar13.setMaximum(255);
        scrollbar13.setMinimum(25);
        scrollbar13.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar13.setValue(150);
        scrollbar13.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar13AdjustmentValueChanged(evt);
            }
        });

        label24.setText("Lane 13:");

        scrollbar14.setMaximum(255);
        scrollbar14.setMinimum(25);
        scrollbar14.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar14.setValue(150);
        scrollbar14.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar14AdjustmentValueChanged(evt);
            }
        });

        label25.setText("Lane 14:");

        scrollbar15.setMaximum(255);
        scrollbar15.setMinimum(25);
        scrollbar15.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar15.setValue(150);
        scrollbar15.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar15AdjustmentValueChanged(evt);
            }
        });

        label26.setText("Lane 15:");

        scrollbar16.setMaximum(255);
        scrollbar16.setMinimum(25);
        scrollbar16.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar16.setValue(150);
        scrollbar16.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar16AdjustmentValueChanged(evt);
            }
        });

        label27.setText("Lane 16:");

        scrollbar17.setMaximum(255);
        scrollbar17.setMinimum(25);
        scrollbar17.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar17.setValue(150);
        scrollbar17.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar17AdjustmentValueChanged(evt);
            }
        });

        label28.setText("Lane 17:");

        scrollbar18.setMaximum(255);
        scrollbar18.setMinimum(25);
        scrollbar18.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar18.setValue(150);
        scrollbar18.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar18AdjustmentValueChanged(evt);
            }
        });

        label29.setText("Lane 18:");

        scrollbar19.setMaximum(255);
        scrollbar19.setMinimum(25);
        scrollbar19.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar19.setValue(150);
        scrollbar19.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar19AdjustmentValueChanged(evt);
            }
        });

        label30.setText("Lane 19:");

        label31.setText("Lane 20:");

        scrollbar20.setMaximum(255);
        scrollbar20.setMinimum(25);
        scrollbar20.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar20.setValue(150);
        scrollbar20.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar20AdjustmentValueChanged(evt);
            }
        });

        label32.setText("Lane 21:");

        scrollbar21.setMaximum(255);
        scrollbar21.setMinimum(25);
        scrollbar21.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar21.setValue(150);
        scrollbar21.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar21AdjustmentValueChanged(evt);
            }
        });

        label33.setText("Lane 22:");

        scrollbar22.setMaximum(255);
        scrollbar22.setMinimum(25);
        scrollbar22.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar22.setValue(150);
        scrollbar22.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar22AdjustmentValueChanged(evt);
            }
        });

        label34.setText("Lane 23:");

        scrollbar23.setMaximum(255);
        scrollbar23.setMinimum(25);
        scrollbar23.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar23.setValue(150);
        scrollbar23.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar23AdjustmentValueChanged(evt);
            }
        });

        label35.setText("Lane 24:");

        scrollbar24.setMaximum(255);
        scrollbar24.setMinimum(25);
        scrollbar24.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar24.setValue(150);
        scrollbar24.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar24AdjustmentValueChanged(evt);
            }
        });

        label36.setText("Lane 25:");

        scrollbar25.setMaximum(255);
        scrollbar25.setMinimum(25);
        scrollbar25.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar25.setValue(150);
        scrollbar25.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar25AdjustmentValueChanged(evt);
            }
        });

        label37.setText("Lane 26:");

        scrollbar26.setMaximum(255);
        scrollbar26.setMinimum(25);
        scrollbar26.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar26.setValue(150);
        scrollbar26.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar26AdjustmentValueChanged(evt);
            }
        });

        label38.setText("Lane 27:");

        scrollbar27.setMaximum(255);
        scrollbar27.setMinimum(25);
        scrollbar27.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar27.setValue(150);
        scrollbar27.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar27AdjustmentValueChanged(evt);
            }
        });

        label39.setText("Lane 28:");

        scrollbar28.setMaximum(255);
        scrollbar28.setMinimum(25);
        scrollbar28.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar28.setValue(150);
        scrollbar28.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar28AdjustmentValueChanged(evt);
            }
        });

        label40.setText("Lane 29:");

        scrollbar29.setMaximum(255);
        scrollbar29.setMinimum(25);
        scrollbar29.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar29.setValue(150);
        scrollbar29.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar29AdjustmentValueChanged(evt);
            }
        });

        label41.setText("Lane 30:");

        scrollbar30.setMaximum(255);
        scrollbar30.setMinimum(25);
        scrollbar30.setOrientation(java.awt.Scrollbar.HORIZONTAL);
        scrollbar30.setValue(150);
        scrollbar30.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollbar30AdjustmentValueChanged(evt);
            }
        });

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel1Layout.createSequentialGroup()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar12, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar13, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar14, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar15, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar16, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar17, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar18, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar19, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar20, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar21, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar22, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar23, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar24, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar25, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar26, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                        .addComponent(label38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar27, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar28, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar29, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addComponent(label41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(scrollbar30, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(label19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(20, 20, 20)
                                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(scrollbar1, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                                                .addComponent(scrollbar2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addComponent(scrollbar3, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                                                .addComponent(scrollbar4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addComponent(scrollbar5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addComponent(scrollbar6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addComponent(scrollbar7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addComponent(scrollbar9, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addComponent(scrollbar8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                        .addContainerGap()
                )
        );
        panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scrollbar30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
        );

        scrollPane1.add(panel1);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
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
        Step3manual s1i = new Step3manual(imp, cc, ia, darkbg, bm);
        s1i.setHistory(history);

        this.dispose();

    }

    private void BackActionPerformed(java.awt.event.ActionEvent evt) {

        int option = JOptionPane.showConfirmDialog(this, "If you go back, the current band selection\n will be lost, do you want to go back?", "Alert", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {

            imp.deleteRoi();
            bm.dispose();
            LaneManagerCanvas cc = new LaneManagerCanvas(imp);
            LaneManager lm = new LaneManager(ia.m);
            Step2FixStandard s1i = new Step2FixStandard(imp, cc, ia, darkbg, lm);
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

    private void scrollbar1AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar2AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar3AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar4AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar5AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar6AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar7AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar8AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar9AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar10AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar11AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar12AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar13AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar14AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar15AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar16AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar17AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar18AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar19AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar20AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar21AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar22AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar23AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar24AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar25AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar26AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar27AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar28AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar29AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    private void scrollbar30AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
        localselectbands();
    }

    public BandManager getBm() {
        return bm;
    }

    public void setHistory(ArrayDeque<ImagePlus> history) {
        this.history = history;
    }

    private void initiatenecessaryscrollbars(Scrollbar[] scrollbars, Label[] labels, int n, int tolerance) {

        for (int i = 0; i < n; i++) {
            scrollbars[i].setValue(tolerance);
        }

        for (int i = n; i < scrollbars.length; i++) {
            scrollbars[i].setVisible(false);
            labels[i].setVisible(false);
        }
    }

    private int[] gettolerances(Scrollbar[] scrollbars, int n) {

        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = scrollbars[i].getValue();
        }
        return res;

    }

    private void localselectbands() {
        Scrollbar[] scrollbars = {scrollbar1, scrollbar2, scrollbar3, scrollbar4, scrollbar5, scrollbar6, scrollbar7, scrollbar8, scrollbar9, scrollbar10, scrollbar11, scrollbar12, scrollbar13, scrollbar14, scrollbar15, scrollbar16, scrollbar17, scrollbar18, scrollbar19, scrollbar20, scrollbar21, scrollbar22, scrollbar23, scrollbar24, scrollbar25, scrollbar26, scrollbar27, scrollbar28, scrollbar29, scrollbar30};
        int[] tolerances = gettolerances(scrollbars, ia.m.finallanes.length);
        Prefs.verticalProfile = true;
        bm.delete_without_asking(true);
        bm.restart_bands();
        ImageProcessor ip = imp.getProcessor();

        for (int i = 0; i < ia.m.finallanes.length; i++) {

            Roi r0 = ia.m.finallanes[i].getRoi();
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
            double[] profile = ia.m.finallanes[i].getProfile();
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
                r1.setFillColor(c);
                bm.add(imp, r1, j, i);

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

}
