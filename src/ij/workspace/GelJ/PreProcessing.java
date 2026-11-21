/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.PlugInFrame;
import java.awt.event.ActionEvent;

/**
 *
 * @author joheras
 */
public class PreProcessing extends PlugInFrame {
    
   //private javax.swing.JButton jButtonAdjustL;
    private javax.swing.JButton jButtonBackground;
    private javax.swing.JButton jButtonCB;
    private javax.swing.JButton jButtonFilter;
    private javax.swing.JButton jButtonGamma;
    private javax.swing.JButton jButtonFlipH;
    private javax.swing.JButton jButtonFlipV;
    private javax.swing.JButton jButtonInvert;
    private javax.swing.JButton jButtonRotate;
    private javax.swing.JButton jButtonRotateL;
    private javax.swing.JButton jButtonRotateR;
    private javax.swing.JButton jButtonCrop;
    private javax.swing.JButton jButtonDone;
    private PlugInFilterRunner pfr;
    private Menu m;
    

    

    public PreProcessing(Menu m) {
        super("Preprocessing");
        showWindow();
        this.m = m;
    }
    
    
    void showWindow(){
        jButtonCrop = new javax.swing.JButton();
        jButtonCB = new javax.swing.JButton();
        jButtonGamma = new javax.swing.JButton();
       // jButtonAdjustL = new javax.swing.JButton();
        jButtonBackground = new javax.swing.JButton();
        jButtonFilter = new javax.swing.JButton();
        jButtonFlipH = new javax.swing.JButton();
        jButtonFlipV = new javax.swing.JButton();
        jButtonRotateL = new javax.swing.JButton();
        jButtonRotateR = new javax.swing.JButton();
        jButtonRotate = new javax.swing.JButton();
        jButtonInvert = new javax.swing.JButton();
        
        jButtonCrop.setText("Crop");
        jButtonCrop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCropActionPerformed(evt);
            }
        });
        
        jButtonFlipH.setText("Flip Horizontally");
        jButtonFlipH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFlipHActionPerformed(evt);
            }
        });

        jButtonFlipV.setText("Flip Vertically");
        jButtonFlipV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFlipVActionPerformed(evt);
            }
        });

        jButtonRotateL.setText("Rotate 90º Left");
        jButtonRotateL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRotateLActionPerformed(evt);
            }
        });

        jButtonRotateR.setText("Rotate 90º Right");
        jButtonRotateR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRotateRActionPerformed(evt);
            }
        });

        jButtonRotate.setText("Rotate");
        jButtonRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRotateActionPerformed(evt);
            }
        });

        jButtonInvert.setText("Invert");
        jButtonInvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvertActionPerformed(evt);
            }
        });

        jButtonCB.setText("Brightness/Contrast");
        jButtonCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCBActionPerformed(evt);
            }
        });

        jButtonGamma.setText("Gamma Correction");
        jButtonGamma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGammaActionPerformed(evt);
            }
        });

        /*jButtonAdjustL.setText("Adjust Linear Histogram");
        jButtonBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdjustLActionPerformed(evt);
            }
        });*/

        jButtonBackground.setText("Global Background Removal");
        jButtonBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBackgroundActionPerformed(evt);
            }
        });

        jButtonFilter.setText("Apply Filter");
        jButtonFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFilterActionPerformed(evt);
            }
        });
        
 

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jButtonCrop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonFlipH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonFlipV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonRotateL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonRotateR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonRotate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonInvert, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonGamma, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonBackground, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButtonCrop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonFlipH)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonFlipV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRotateL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRotateR)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRotate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonInvert)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonGamma)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonBackground)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonFilter))
        );
        
        setSize(250, 380);
        setResizable(false);
        
        show();
        
        
    }
    
    
   
    
    
    private void jButtonCBActionPerformed(java.awt.event.ActionEvent evt) {                                          
        IJ.run("Brightness/Contrast...","");
        
    }                                         

    private void jButtonGammaActionPerformed(java.awt.event.ActionEvent evt) {                                             
        IJ.run("Gamma...", "");
    }                                            

    private void jButtonBackgroundActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        IJ.run("Subtract Background...", "");
        }                                                 

    private void jButtonFilterActionPerformed(java.awt.event.ActionEvent evt) {                                              
        
        ImagePlus imp = IJ.getImage();
        Filters f = new Filters(); 
        pfr=new PlugInFilterRunner(f, "Filters", "");
        
    }  
    
    private void jButtonFlipHActionPerformed(java.awt.event.ActionEvent evt) { 
        ImagePlus imp = IJ.getImage();
        IJ.run(imp, "Flip Horizontally", "");
    }                                            

    private void jButtonFlipVActionPerformed(java.awt.event.ActionEvent evt) {                                             
        ImagePlus imp = IJ.getImage();
        IJ.run(imp, "Flip Vertically", "");
    }                                            

    private void jButtonRotateLActionPerformed(java.awt.event.ActionEvent evt) {                                               
        ImagePlus imp = IJ.getImage();
        IJ.run(imp, "Rotate 90 Degrees Left", "");
    }                                              

    private void jButtonRotateRActionPerformed(java.awt.event.ActionEvent evt) {                                               
        ImagePlus imp = IJ.getImage();
       IJ.run(imp, "Rotate 90 Degrees Right", "");
    }                                              

    private void jButtonRotateActionPerformed(java.awt.event.ActionEvent evt) {                                              
        ImagePlus imp = IJ.getImage();
        IJ.run(imp, "Rotate... ", "");
    }                                             

    private void jButtonInvertActionPerformed(java.awt.event.ActionEvent evt) {                                              
        ImagePlus imp = IJ.getImage();
        IJ.run(imp, "Invert", "");
        m.changeinvertcolour();
    }      
    
    private void jButtonCropActionPerformed(ActionEvent evt) {
                ImagePlus imp = IJ.getImage();
        Roi r = imp.getRoi();
        IJ.run(imp, "Crop", "");
    }
    
   /* private void jButtonAdjustLActionPerformed(java.awt.event.ActionEvent evt) {                                          
        ImagePlus imp = IJ.getImage();
        LinearAdjustmentHistogram lah = new LinearAdjustmentHistogram();
        
    }  */
    
    
    
}
