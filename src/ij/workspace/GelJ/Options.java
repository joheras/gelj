/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.PlugInFrame;
import static ij.workspace.GelJ.Lane.biggerlane;
import static ij.workspace.GelJ.Lane.indexesstandardlanes;
import static ij.workspace.GelJ.Lane.lanesnames;
import ij.workspace.GelJ.OptionsMenu.ModifyStandards;
import ij.workspace.GelJ.OptionsMenu.SimilarityTable;
import ij.workspace.GelJ.Protocols.Protocols;
import ij.workspace.GelJ.rfcalibration.rftable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class Options extends PlugInFrame {

    private javax.swing.JButton jButtonModifyStandards;
    private javax.swing.JButton jButtonMWTable;
    private javax.swing.JButton jButtonSimilarityTable;
    private javax.swing.JButton jButtonProtocols;
   // private javax.swing.JButton jButtonrflines;
    //  private javax.swing.JButton jButton3DViewer;
    private Menu menu;
    private ResultsTable mwtable;

    public Options(Menu menu) {
        super("Options");
        this.menu = menu;
        showWindow();
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    void showWindow() {
        jButtonModifyStandards = new javax.swing.JButton();
        jButtonModifyStandards.setText("Edit Standards");
        jButtonModifyStandards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifyStandardsActionPerformed(evt);
            }
        });

        jButtonMWTable = new javax.swing.JButton();
        jButtonMWTable.setText("Molecular Weight Table");
        jButtonMWTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMWTableActionPerformed(evt);
            }
        });

        jButtonSimilarityTable = new javax.swing.JButton();
        jButtonSimilarityTable.setText("Similarity Table");
        jButtonSimilarityTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSimilarityTableActionPerformed(evt);
            }
        });

        jButtonProtocols = new javax.swing.JButton();
        jButtonProtocols.setText("Edit Standards");
        jButtonProtocols.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProtocolsActionPerformed(evt);
            }
        });

        /* jButtonrflines = new javax.swing.JButton(); 
         jButtonrflines.setText("Calibrate using rf-lines");
         jButtonrflines.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
         jButtonrflinesActionPerformed(evt);
         }
         });
        
        
        
         jButton3DViewer = new javax.swing.JButton(); 
         jButton3DViewer.setText("3D Viewer");
         jButton3DViewer.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
         jButton3DViewerActionPerformed(evt);
         }
         });*/
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jButtonModifyStandards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonMWTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSimilarityTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonProtocols, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        //.addComponent(jButtonrflines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        //.addComponent(jButton3DViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonModifyStandards)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonMWTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSimilarityTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonProtocols)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                /* .addComponent(jButtonrflines)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)    
                 .addComponent(jButton3DViewer)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED) */
                )
        );

        setSize(250, 122);
        setResizable(false);

        show();

    }

    private void jButtonProtocolsActionPerformed(ActionEvent evt) {
        GenericDialog gd = new GenericDialog("Edit Protocols");
        Protocols p = new Protocols();
        p.addprotocols();
        gd.add(p);
        gd.addMessage("");
        gd.showDialog();

        if (gd.wasCanceled()) {
            gd.dispose();
        }
        if (gd.wasOKed()) {
            gd.dispose();
        }
    
    }

    private void jButtonModifyStandardsActionPerformed(ActionEvent evt) {
        GenericDialog gd = new GenericDialog("Edit standards");
        ModifyStandards ms = new ModifyStandards();
        ms.addstandards();
        gd.add(ms);
        gd.addMessage("");
        gd.showDialog();

        if (gd.wasCanceled()) {
            gd.dispose();
        }
        if (gd.wasOKed()) {
            gd.dispose();
        }
    }

    private void jButtonMWTableActionPerformed(ActionEvent evt) {
        if (!menu.normalisation) {
            IJ.error("You have not performed the normalisation step.");
        } else {
            Lane[] lanes = menu.externalcomputestdlanes();

            if (mwtable == null) {
                mwtable = new ResultsTable();
            } else {
                mwtable.show("Molecular Weight Table");
            }

            for (int i = 0; i < lanes.length; i++) {
                mwtable.setHeading(i, lanes[i].getName());
            }

            int bl = biggerlane(lanes);

            for (int i = 0; i < bl; i++) {
                mwtable.incrementCounter();
            }

            for (Lane l : lanes) {
                double[] stds = l.getStdpositionbands();
                for (int i = 0; i < stds.length; i++) {
                    mwtable.setValue(l.getName(), i, stds[i]);
                }
                for (int i = stds.length; i < bl; i++) {
                    mwtable.setValue(l.getName(), i, "-");
                }
            }

            mwtable.show("Molecular Weight Table");
        }
    }

    private void jButtonSimilarityTableActionPerformed(ActionEvent evt) {
        SimilarityTable st = new SimilarityTable(menu.similaritytable, menu.similaritymethod, lanesnames(menu.selectedlanes));

    }

    private void jButtonrflinesActionPerformed(ActionEvent evt) {
        if (!menu.normalisation) {
            IJ.error("You have not performed the normalisation step.");
        } else {
            String[][] data = extractbandnamesfromlanes(menu.finallanes);
            paintrflines(data);
            rftable rf = new rftable(data, menu, WindowManager.getCurrentImage());
        }
    }

    private String[][] extractbandnamesfromlanes(Lane[] finallanes) {

        ArrayList<double[]> lanes = new ArrayList<double[]>();
        // We add the mw positions of each lane to the arraylist lanes
        for (int i = 0; i < finallanes.length; i++) {
            lanes.add(finallanes[i].getStdpositionbands());
        }

        ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<Integer>> usedindexes = new ArrayList<ArrayList<Integer>>();

        for (int i = 0; i < finallanes.length; i++) {
            res.add(new ArrayList<String>());
            usedindexes.add(new ArrayList<Integer>());
        }

        /// We obtain the tolerance value
        int[] indexes = indexesstandardlanes(finallanes);
        double[] stds = finallanes[indexes[0]].getStdpositionbands();
        double tolerance = (stds[0] - stds[stds.length - 1]) * 0.03;

        // We create an array that keeps track of the current band position
        // in each lane
        int[] bandposition = initarrayones(finallanes.length);

        double val = takefirstmaximum(lanes);
        int pos = takefirstmaximumposition(lanes);

        while (val != -10000) {

            // We either go to the left of the max position
            for (int j = pos; j >= 0; j--) {
                if (lanes.get(j) == null) {
                    res.get(j).add("");
                } else if (val - tolerance < lanes.get(j)[0]
                        && lanes.get(j)[0] < val + tolerance) {
                    val = lanes.get(j)[0];
                    // Add the band to the result 
                    res.get(j).add("Band " + bandposition[j]);
                    // Remove the first element of the lane
                    lanes.set(j, removefirstelementarray(lanes.get(j)));
                    // Increase by 1 the band position
                    bandposition[j] = bandposition[j] + 1;
                } else {
                    res.get(j).add("");
                }
            }
            // or to the right
            for (int j = pos + 1; j < lanes.size(); j++) {
                if (lanes.get(j) == null) {
                    res.get(j).add("");
                } else if (val - tolerance < lanes.get(j)[0]
                        && lanes.get(j)[0] < val + tolerance) {

                    val = lanes.get(j)[0];
                    // Add the band to the result 
                    res.get(j).add("Band " + bandposition[j]);
                    // Remove the first element of the lane
                    lanes.set(j, removefirstelementarray(lanes.get(j)));
                    // Increase by 1 the band position
                    bandposition[j] = bandposition[j] + 1;
                } else {
                    res.get(j).add("");
                }
            }

            val = takefirstmaximum(lanes);
            pos = takefirstmaximumposition(lanes);
        }
        return matrixconversion(res);
    }

    private String[][] matrixconversion(ArrayList<ArrayList<String>> data) {
        String[][] res = new String[data.get(0).size()][data.size()];

        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(0).size(); j++) {
                res[j][i] = data.get(i).get(j);
            }
        }
        return res;
    }

    private int getbiggerarray(ArrayList<double[]> lanes) {
        int max = 0;
        for (int i = 0; i < lanes.size(); i++) {
            if (max < lanes.get(i).length) {
                max = lanes.get(i).length;
            }
        }
        return max;

    }

    private int[] initarrayones(int size) {
        int[] res = new int[size];

        for (int i = 0; i < size; i++) {
            res[i] = 1;
        }
        return res;
    }

    private double takefirstmaximum(ArrayList<double[]> lanes) {

        double max = -10000;
        for (int i = 0; i < lanes.size(); i++) {
            if (lanes.get(i) == null || lanes.get(i).length == 0) {

            } else {
                if (max < lanes.get(i)[0]) {
                    max = lanes.get(i)[0];
                }
            }
        }
        return max;
    }

    private int takefirstmaximumposition(ArrayList<double[]> lanes) {

        int max = -10000;
        int pos = -1;
        for (int i = 0; i < lanes.size(); i++) {
            if (lanes.get(i) == null || lanes.get(i).length == 0) {

            } else {
                if (max < lanes.get(i)[0]) {
                    pos = i;
                }
            }
        }
        return pos;
    }

    private double[] removefirstelementarray(double[] array) {
        if (array == null || array.length == 0) {
            return array;
        } else if (array.length == 1) {
            return null;
        } else {
            double[] res = new double[array.length - 1];
            for (int i = 1; i < array.length; i++) {
                res[i - 1] = array[i];
            }
            return res;
        }
    }

    public void paintrflines(String[][] data) {
        ImagePlus imp = WindowManager.getCurrentImage();
        Overlay o = imp.getOverlay();
        for (int i = 0; i < data.length; i++) {
            ArrayList<Integer> xpoints = new ArrayList<Integer>();
            ArrayList<Integer> ypoints = new ArrayList<Integer>();
            for (int j = 0; j < data[i].length; j++) {
                if (data[i][j] != "") {
                    int k = Integer.parseInt(data[i][j].substring(5));
                    PointRoi p = new PointRoi(menu.finallanes[j].getRoi().getBounds().x
                            + (menu.finallanes[j].getRoi().getBounds().width / 2),
                            menu.finallanes[j].getPositionbands()[k - 1]);
                    imp.setRoi(p);
                    IJ.run("Add Selection...");
                    xpoints.add(menu.finallanes[j].getRoi().getBounds().x
                            + (menu.finallanes[j].getRoi().getBounds().width / 2));
                    ypoints.add(menu.finallanes[j].getPositionbands()[k - 1]);
                }
            }
            PolygonRoi polygonRoi = new PolygonRoi(transformintarraylisttoarray(xpoints),
                    transformintarraylisttoarray(ypoints), xpoints.size(),
                    Roi.POLYLINE);
            o = imp.getOverlay();
            o.addElement(polygonRoi);
        }
        IJ.run("Show Overlay", "");

    }

    private int[] transformintarraylisttoarray(ArrayList<Integer> a) {

        int[] res = new int[a.size()];

        for (int i = 0; i < a.size(); i++) {
            res[i] = a.get(i);
        }
        return res;

    }

    /*
     private void jButton3DViewerActionPerformed(ActionEvent evt) {
        
     GenericDialog gd = new GenericDialog("3D Viewer");
     Viewer3D v3d = new Viewer3D();
        
     if(menu.finallanes==null|| menu.finallanes.length == 0){
     v3d.disablelaneoption();
     }else{
     String[] names=lanesnames(menu.finallanes);
     v3d.setlistoflanes(names);
     }
        
        
     gd.addMessage("");
     gd.showDialog();
        
     if(gd.wasCanceled()){
     gd.dispose();
     }
     if(gd.wasOKed()){
     if(v3d.iswhole3dselected()){
     // do something
     }else{
     int pos = v3d.getselection();
     Lane l = menu.finallanes[pos];
     // Do something with l
                
                
            
     }
     }  
        
        
        
     }*/
}
