package ij.workspace.GelJ.Assistant2;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.workspace.GelJ.LaneManager;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class Step2FixStandard extends ImageWindow implements ActionListener {

   private java.awt.Button Back;
    private javax.swing.ButtonGroup buttonGroup1;
    private java.awt.Button buttonnext;
    private java.awt.Canvas canvas1;
    private java.awt.Button deletereference;
    private java.awt.Button fixreference;
    private java.awt.Panel jPanel1;
    private java.awt.Panel jPanel2;
    private java.awt.Panel jPanel3;
    private java.awt.Panel jPanel4;
    private java.awt.Panel jPanel5;
    private java.awt.Label label1;
    private java.awt.Label label10;
    private java.awt.Label label12;
    private java.awt.Label label13;
    private java.awt.Label label14;
    private java.awt.Label label15;
    private java.awt.Label label6;
    private java.awt.Label label7;
    private java.awt.Label label8;
    private java.awt.Label label9;
    
    private ArrayDeque<ImagePlus> history = new ArrayDeque<>();
    private LaneManager lm;
    private boolean darkbg;
    private ImageAssistant ia;

    public Step2FixStandard(ImagePlus imp, ImageCanvas ic, ImageAssistant ia, boolean darkbg,LaneManager lm) {
        super(imp, ic);
        this.darkbg = darkbg;
        this.ia = ia;
        this.lm = lm;
        lm.updateShowAll();
        removeAll();
        IJ.setTool("rectangle");
        addPanel(ic);
        imp.updateAndDraw();
        ic.requestFocus();
        this.setLocation(0, 0);
         IJ.run(imp, "In [+]", "");
        IJ.run(imp, "Out [-]", "");
    }

    private void exitForm(java.awt.event.WindowEvent evt) throws IOException {
        imp.changes = false;
        ia.saveUnfinishedExperiment(this, 5);
    }

    void addPanel(ImageCanvas ic) {

         buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new java.awt.Panel();
        label1 = new java.awt.Label();
        label6 = new java.awt.Label();
        label13 = new java.awt.Label();
        label14 = new java.awt.Label();
        label15 = new java.awt.Label();
        label7 = new java.awt.Label();
        label8 = new java.awt.Label();
        jPanel2 = new java.awt.Panel();
        canvas1 = ic;
        jPanel4 = new java.awt.Panel();
        buttonnext = new java.awt.Button();
        Back = new java.awt.Button();
        jPanel5 = new java.awt.Panel();
        fixreference = new java.awt.Button();
        deletereference = new java.awt.Button();
        jPanel3 = new java.awt.Panel();
        label9 = new java.awt.Label();
        label10 = new java.awt.Label();
        label12 = new java.awt.Label();

        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                try {
                    exitForm(evt);
                } catch (IOException ex) {
                    Logger.getLogger(Step2FixStandard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        label1.setText("Step 1. Image Processing");

        label6.setForeground(new java.awt.Color(0, 0, 0));
        label6.setText("Step 2. Lane Detection");

        label13.setForeground(new java.awt.Color(0, 0, 0));
        label13.setText("2.1. Dark/Light background");

        label14.setForeground(new java.awt.Color(0, 0, 0));
        label14.setText("2.2. Lane edition");

        label15.setForeground(new java.awt.Color(0, 17, 255));
        label15.setText("2.3. Fix standards");

        label7.setText("Step 3. Band Detection");

        label8.setText("Step 4. Normalisation");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(label13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(label14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(label15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        fixreference.setLabel("Fix Reference Lane [f]");
        fixreference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixreferenceActionPerformed(evt);
            }
        });

        deletereference.setLabel("Delete Reference Lane");
        deletereference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletereferenceActionPerformed(evt);
            }
        });

//        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Help"));

        
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        
        
        label9.setText("In this window, you must fix ");

        label10.setText("at least two reference lanes.");

        label12.setText("Shortcuts are between brackets.");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fixreference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(deletereference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fixreference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deletereference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void buttonnextActionPerformed(java.awt.event.ActionEvent evt) {
        
        
        
        if (lm.getnumberofstds() < 2) {
            JOptionPane.showMessageDialog(this, "You must select (at least) two reference lanes","Reference Lanes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        lm.close();
        ia.m.setLm(null);
        
        BandManagerCanvas cc = new BandManagerCanvas(imp);
        Step3 s1i = new Step3(imp,cc,ia,darkbg);    
        s1i.setHistory(history);
//        s1i.getBm().auto_detect();
        
//        s1i.getBm().activatehistogramdisplay();

        this.dispose();
        
        
        
        
        

    }

    private void BackActionPerformed(java.awt.event.ActionEvent evt) {
        ImagePlus imp2 = null;
        if(history.isEmpty()){
            imp2 = imp;
        }else{
            imp2 = history.remove();
        }
        
        LaneManagerCanvas cc = new LaneManagerCanvas(imp2);
        Step2Edit s1c = new Step2Edit(imp2, cc, ia,darkbg,lm);
        s1c.setTitle(this.getTitle());
        s1c.setHistory(history);
        this.dispose();
        
    }

    private void autoActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.auto_detect();
    }

    private void addActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.runCommand("add");
    }

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.delete(false);
    }

    private void renameActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.rename(null);
    }

    private void adjustActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "RGB Color", "");
        lm.adjustcontrastbrightness(imp);
    }

    private void backgroundActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.backgroundremoval(imp);
    }

    private void fixreferenceActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.fix_standard();
    }

    private void deletereferenceActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.delete_standard();
    }

    private void showreferenceActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.run(imp, "8-bit", "");
        lm.show_standards();
    }

    private void rectangleActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.setTool("rectangle");
    }

    private void polygonActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.setTool("polygon");
    }

    public void setHistory(ArrayDeque<ImagePlus> history) {
        this.history = history;
    }

    public LaneManager getLm() {
        return lm;
    }
    
    
    

}
