package ij.workspace.GelJ.Assistant2;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.workspace.GelJ.BandManager;
import static ij.workspace.GelJ.Lane.indexesstandardlanes;
import ij.workspace.GelJ.NormaliseManager;
import ij.workspace.GelJ.Standards.CurveCalibration;
import ij.workspace.GelJ.Standards.ListofValues;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardfromfile;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardsavailable;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import static ij.workspace.GelJ.Utils.Utilities.intarraytodoublearray;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joheras
 */
public class Step4 extends ImageWindow implements ActionListener {

    private java.awt.Button Back;
    private javax.swing.ButtonGroup buttonGroup1;
    private java.awt.Button buttonnext;
    private java.awt.Canvas canvas1;
    private java.awt.Panel jPanel1;
    private java.awt.Panel jPanel2;
    private java.awt.Panel jPanel3;
    private java.awt.Panel jPanel4;
    private java.awt.Panel jPanel5;
    private javax.swing.JRadioButton jrbNew;
    private javax.swing.JRadioButton jrbavailable;
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
    private java.awt.Choice stdschoice;

    private ArrayDeque<ImagePlus> history = new ArrayDeque<>();
    private boolean darkbg;
    private ImageAssistant ia;
    private ArrayList<String> availablestd;

    public Step4(ImagePlus imp, ImageCanvas ic, ImageAssistant ia, boolean darkbg) {
        super(imp, ic);
        this.darkbg = darkbg;
        this.ia = ia;
        IJ.setTool("point");
        removeAll();
        addPanel(ic);
        imp.updateAndDraw();
        ic.requestFocus();
        availablestd = readstandardsavailable();
        if (availablestd == null || 0 == availablestd.size()) {
            stdschoice.setEnabled(false);
        } else {
            for (String std1 : availablestd) {
                stdschoice.addItem(std1);
            }
        }
        this.setLocation(0, 0);
        IJ.run(imp, "In [+]", "");
        IJ.run(imp, "Out [-]", "");
    }

    private void exitForm(java.awt.event.WindowEvent evt) throws IOException {
        imp.changes = false;
        ia.saveUnfinishedExperiment(this, 7);
    }

    void addPanel(ImageCanvas ic) {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new java.awt.Panel();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        label3 = new java.awt.Label();
        label4 = new java.awt.Label();
        label5 = new java.awt.Label();
        label6 = new java.awt.Label();
        label7 = new java.awt.Label();
        label8 = new java.awt.Label();
        label11 = new java.awt.Label();
        label12 = new java.awt.Label();
        jPanel2 = new java.awt.Panel();
        canvas1 = ic;
        jPanel4 = new java.awt.Panel();
        buttonnext = new java.awt.Button();
        Back = new java.awt.Button();
        jPanel5 = new java.awt.Panel();
        jPanel3 = new java.awt.Panel();
        label9 = new java.awt.Label();
        label10 = new java.awt.Label();
        jrbavailable = new javax.swing.JRadioButton();
        stdschoice = new java.awt.Choice();
        jrbNew = new javax.swing.JRadioButton();

        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                try {
                    exitForm(evt);
                } catch (IOException ex) {
                    Logger.getLogger(Step4.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        label1.setText("Step 1. Image Processing");

        label2.setForeground(new java.awt.Color(0, 0, 0));
        label2.setText("1.1. Crop");

        label3.setForeground(new java.awt.Color(0, 0, 0));
        label3.setText("1.2. Invert");

        label4.setForeground(new java.awt.Color(0, 0, 0));
        label4.setText("1.3. Auto Adjust Background");

        label5.setText("1.4. Other operations");

        label6.setForeground(new java.awt.Color(0, 0, 0));
        label6.setText("Step 2. Lane Detection");

        label7.setForeground(new java.awt.Color(0, 0, 0));
        label7.setText("Step 3. Band Detection");

        label8.setForeground(new java.awt.Color(0, 17, 255));
        label8.setText("Step 4. Normalisation");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        buttonnext.setLabel("Next >");
        buttonnext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    buttonnextActionPerformed(evt);
                } catch (IOException ex) {
                    Logger.getLogger(Step4.class.getName()).log(Level.SEVERE, null, ex);
                }
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

        label9.setText("In this window, you can normalise");

        label11.setText("the image. Select an available");

        label12.setText("marker or create a new one.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        buttonGroup1.add(jrbavailable);
        jrbavailable.setSelected(true);
        jrbavailable.setText("Use available standard");

        buttonGroup1.add(jrbNew);
        jrbNew.setText("Define new standard");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(stdschoice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(jrbNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jrbavailable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jrbavailable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stdschoice, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jrbNew)
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
                        .addGap(6, 6, 6)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void buttonnextActionPerformed(java.awt.event.ActionEvent evt) throws IOException {
        int[] indexes = indexesstandardlanes(ia.m.finallanes);

        // If she chooses to create a new standard, she has to introduce a name,
        // and the molecular wheights for the bands of the standard.
        if (jrbNew.isSelected()) {

            imp.deleteRoi();
            CustomCanvas cc = new CustomCanvas(imp);
            Step4new s1i = new Step4new(imp, cc, ia, darkbg);
            this.dispose();
            return;
            } else {
            // If she chooses an available standard, we check that the standard contains
            // as much molecular weights as bands in the image. In such a case, we assign
            // the values; otherwise, we ask the user to remove/add the molecular weights that 
            // do not appear in the image. 
            String file = stdschoice.getSelectedItem();
            ia.m.setMarker(file);
            double[] stdvalues = readstandardfromfile(file);
            if (stdvalues.length == ia.m.finallanes[indexes[0]].getPositionbands().length) {
                for (int i = 0; i < indexes.length; i++) {
                    ia.m.finallanes[indexes[i]].setStdpositionbands(stdvalues);
                }
            } else {
                IJ.error("The standard contains " + stdvalues.length + " elements,"
                        + " but the reference lane contains "
                        + ia.m.finallanes[indexes[0]].getPositionbands().length
                        + " bands. Please, add or remove the necessary values.");

                /////////////////////////////////////////////////////////////
                ImagePlus imp = WindowManager.getCurrentImage();
                NormaliseManager rm = new NormaliseManager();

                int[] positionbands = ia.m.finallanes[indexes[0]].getPositionbands();
                if (positionbands != null) {
                    for (int b : positionbands) {
                        Roi r0 = ia.m.finallanes[indexes[0]].getRoi();
                        int r0x = r0.getBounds().x;
                        int r0y = r0.getBounds().y;
                        int w = r0.getBounds().width;
                        Rectangle r = new Rectangle(r0x, r0y + b, w, 2);
                        Roi r1 = rectangleintersectionroi(r0, r);
                        rm.add(imp, r1, 0);
                        rm.showAll(0);
                    }
                }
                /////////////////////////////////////////////////////////////////// 

                GenericDialog gdG2 = new GenericDialog("List of values");
                ListofValues lp = new ListofValues();
                lp.init();
                lp.addelements(stdvalues);
                gdG2.add(lp);
                gdG2.addMessage("");
                gdG2.showDialog();

                while (stdvalues.length != ia.m.finallanes[indexes[0]].getPositionbands().length) {

                    if (gdG2.wasCanceled()) {
                        gdG2.dispose();
                        return;
                    }
                    if (gdG2.wasOKed()) {
                        stdvalues = lp.getelements();
                        if (stdvalues.length != ia.m.finallanes[indexes[0]].getPositionbands().length) {
                            IJ.error("The standard contains " + stdvalues.length + " elements,"
                                    + " but the reference lane contains "
                                    + ia.m.finallanes[indexes[0]].getPositionbands().length
                                    + " bands.");

                            gdG2.dispose();
                            gdG2 = new GenericDialog("List of values");
                            lp = new ListofValues();
                            lp.init();
                            lp.addelements(stdvalues);
                            gdG2.add(lp);
                            gdG2.addMessage("");
                            gdG2.showDialog();
                        }

                    }

                }
                ia.m.setInitialoffset(ia.m.finallanes[indexes[0]].getPositionbands()[0]);
                double tempoffset = 0;
                for (int i = 0; i < indexes.length; i++) {
                    ia.m.finallanes[indexes[i]].setStdpositionbands(stdvalues);
                    tempoffset = ia.m.getInitialoffset()
                            - ia.m.finallanes[indexes[i]].getPositionbands()[0];
                    ia.m.finallanes[indexes[i]].setOffset(tempoffset);
                }

            }

        }

        // Finally, we ask the user to select the calibration method. 

        PleaseWait pw = new PleaseWait(this, false);
        pw.setVisible(true);
        pw.toFront();
        
        CurveCalibration cc = new CurveCalibration(
                intarraytodoublearray(ia.m.finallanes[indexes[0]].getPositionbands()),
                ia.m.finallanes[indexes[0]].getStdpositionbands(),
                ia.m, ia, this);
        
        pw.dispose();

        ia.m.normalisation = true;

    }

    private void BackActionPerformed(ActionEvent evt) {

        imp.deleteRoi();
        BandManagerCanvas cc = new BandManagerCanvas(imp);
        Step3manual s1i = new Step3manual(imp, cc, ia, darkbg);
        BandManager bm = s1i.getBm();
        bm.delete_without_asking(false);

        if (ia.m.finallanes != null) {
            for (int i = 0; i < ia.m.finallanes.length; i++) {
                int[] positionbands = ia.m.finallanes[i].getPositionbands();
                if (positionbands != null) {
                    for (int b : positionbands) {
                        Roi r0 = ia.m.finallanes[i].getRoi();
                        int r0x = r0.getBounds().x;
                        int r0y = r0.getBounds().y;
                        int w = r0.getBounds().width;
                        Rectangle r = new Rectangle(r0x, r0y + b, w, 2);
                        Roi r1 = rectangleintersectionroi(r0, r);
                        r1.setFillColor(ia.m.getColour());
                        bm.add(imp, r1, 0, i);
                        bm.showAll(0);
                    }
                }
            }
        }

        bm.requestFocus();

        s1i.setHistory(history);
        this.dispose();

    }

    public void setHistory(ArrayDeque<ImagePlus> history) {
        this.history = history;
    }

}
