/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ;

import db.Classes.Comparison;
import db.Classes.Experiment;
import db.Classes.LaneDB;
import db.Classes.Marker;
import db.Classes.Study;
import db.Classes.User;
import static db.Queries.ComparisonQueries.insertComparison;
import static db.Queries.ComparisonQueries.insertComparisonLane;
import static db.Queries.ComparisonQueries.lastComparisonid;
import static db.Queries.ExperimentQueries.insertExperiment;
import static db.Queries.ExperimentQueries.lastExperimentid;
import static db.Queries.LaneQueries.insertLane;
import static db.Queries.LaneQueries.lastLaneid;
import static db.Queries.MarkerQueries.getMarker;
import static db.Queries.MarkerQueries.lastMarkerid;
import static db.Queries.StudyQueries.getStudy;
import static db.Queries.StudyQueries.insertStudy;
import static db.Queries.StudyQueries.lastStudyid;
import static db.Queries.UserQueries.getUser;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.PlugInFrame;
import ij.workspace.GelJ.Assistant.Assistant;
import ij.workspace.GelJ.Assistant2.PleaseWait;
import static ij.workspace.GelJ.Lane.getstandardlanes;
import static ij.workspace.GelJ.Lane.indexesstandardlanes;
import static ij.workspace.GelJ.Lane.lanesnames;
import static ij.workspace.GelJ.Lane.numberofbandslanes;
import static ij.workspace.GelJ.Lane.roisfromlanes;
import static ij.workspace.GelJ.Lane.whatarestandardlanes;
import ij.workspace.GelJ.LaneCompare.BandDifference;
import ij.workspace.GelJ.LaneCompare.CosineCorrelation;
import ij.workspace.GelJ.LaneCompare.Dice;
import ij.workspace.GelJ.LaneCompare.EuclideanDistance;
import ij.workspace.GelJ.LaneCompare.Jaccard;
import ij.workspace.GelJ.LaneCompare.Jeffrey;
import ij.workspace.GelJ.LaneCompare.LaneComparison;
import ij.workspace.GelJ.LaneCompare.ManhattanDistance;
import ij.workspace.GelJ.LaneCompare.Ochiai;
import ij.workspace.GelJ.LaneCompare.PearsonCorrelation;
import ij.workspace.GelJ.SaveLoad.Load;
import ij.workspace.GelJ.SaveLoad.Save;
import static ij.workspace.GelJ.Standards.ComputeStandards.computestdlane;
import static ij.workspace.GelJ.Standards.ComputeStandards.computestdlanes;
import ij.workspace.GelJ.Standards.CurveCalibration;
import ij.workspace.GelJ.Standards.ListofValues;
import static ij.workspace.GelJ.Standards.StandardUtils.createStandard;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardfromfile;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardsavailable;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import ij.workspace.GelJ.Standards.StandardsPanel;
import static ij.workspace.GelJ.Utils.Utilities.allequal;
import static ij.workspace.GelJ.Utils.Utilities.intarraytodoublearray;
import static ij.workspace.GelJ.Utils.Utilities.selectlanes;
import static ij.workspace.GelJ.Utils.Utilities.selectstandards;
import static ij.workspace.GelJ.Utils.Utilities.trueinbooleanarray;
import static ij.workspace.GelJ.WekaClustering.COMPLETE;
import static ij.workspace.GelJ.WekaClustering.MEAN;
import static ij.workspace.GelJ.WekaClustering.SINGLE;
import static ij.workspace.GelJ.WekaClustering.UPGMA;
import static ij.workspace.GelJ.WekaClustering.UPGMC;
import static ij.workspace.GelJ.WekaClustering.WARD;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author joheras
 */
public class Menu extends PlugInFrame implements ActionListener, ItemListener, MouseListener, MouseWheelListener, ListSelectionListener {

    private javax.swing.JButton jButtonCrop;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonOpen;
    private javax.swing.JButton jButtonProcessing;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButton1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton jButtonLanes;
    private javax.swing.JButton jButtonDendogram;
    private javax.swing.JButton jButtonHelp;
    private PlugInFilterRunner pfr;
    private LaneManager lm;
    private BandManager bm;
    private Roi[] lanes;
    //public Roi[] standards;
    private Band[] bands;
    public Lane[] finallanes;
    private PreProcessing f;
    private Options o;
    private JButton jButtonStandards;
    private int calibrationmethod;
    private javax.swing.JButton jButtonOptions;
    public boolean normalisation = false;
    public double similaritytable[][];
    public int similaritymethod;
    public Lane[] selectedlanes;
    private double initialoffset;
    private boolean invertcolour = false;
    private boolean saved = false;
    private Color colour = Color.BLUE;
    private boolean first_time = true;
    private String windowname;
    private boolean changes = false;

    /// To connect with the database
    private boolean newstudy;
    private int studyid;
    private String experimentname;
    private String experimentcomments;
    private int user;
    private String marker;
    private boolean[] selectedlanesforcomparison;
    private String simmethod = "";
    private String linkage = "UPGMA";
    private int tol;
    private boolean firstcomparison = true;
    private int laneid;
    private int experimentid;
    private Experiment e;
    private String username;
    private Hashtable lanesinfo;

    private void cleanall() {
        lm = null;
        bm = null;
        lanes = null;
        bands = null;
        finallanes = null;
        f = null;
        o = null;
        calibrationmethod = 0;
        normalisation = false;
        similaritytable = null;
        similaritymethod = 0;
        selectedlanes = null;
        initialoffset = 0;
        invertcolour = false;
        saved = false;
        jButtonLanes.setEnabled(false);
        jButtonProcessing.setEnabled(false);
        jButtonSave.setEnabled(false);
        jButton1.setEnabled(false);
        jButtonStandards.setEnabled(false);
        jButtonDendogram.setEnabled(false);
        jButtonOptions.setEnabled(false);
        firstcomparison = true;
    }

    public Menu() {

        super("GelJ");
        showWindow();
    }

    void showWindow() {

        ImagePlus imp = WindowManager.getCurrentImage();

        boolean initialimage;

        if (imp != null) {
            initialimage = true;
        } else {
            initialimage = false;
        }

        jToolBar1 = new javax.swing.JToolBar();
        jButtonOpen = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonProcessing = new javax.swing.JButton();
        jButtonLanes = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButtonStandards = new javax.swing.JButton();
        jButtonDendogram = new javax.swing.JButton();
        jButtonOptions = new javax.swing.JButton();
        jButtonHelp = new javax.swing.JButton();

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButtonOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/folder_open.png"))); // NOI18N
        jButtonOpen.setToolTipText("Open");
        jButtonOpen.setFocusable(false);
        jButtonOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    jButtonOpenActionPerformed(evt);
                } catch (IOException ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jToolBar1.add(jButtonOpen);

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/save.png"))); // NOI18N
        jButtonSave.setToolTipText("Save");
        jButtonSave.setFocusable(false);
        jButtonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        if (!initialimage) {
            jButtonSave.setEnabled(false);
        }

        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    jButtonSaveActionPerformed(evt);
                } catch (IOException ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jToolBar1.add(jButtonSave);

        jButtonProcessing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/processing.png"))); // NOI18N
        jButtonProcessing.setToolTipText("Preprocessing");
        jButtonProcessing.setFocusable(false);
        jButtonProcessing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonProcessing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        if (!initialimage) {
            jButtonProcessing.setEnabled(false);
        }

        jButtonProcessing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcessingActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonProcessing);

        jButtonLanes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/lanes.png"))); // NOI18N
        jButtonLanes.setToolTipText("Lane detection");
        jButtonLanes.setFocusable(false);
        jButtonLanes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonLanes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        if (!initialimage) {
            jButtonLanes.setEnabled(false);
        }

        jButtonLanes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLanesActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonLanes);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/bands.png"))); // NOI18N
        jButton1.setToolTipText("Band detection");
        jButton1.setEnabled(false);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButtonStandards.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/standards.png"))); // NOI18N
        jButtonStandards.setToolTipText("Normalisation");
        jButtonStandards.setEnabled(false);
        jButtonStandards.setFocusable(false);
        jButtonStandards.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonStandards.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonStandards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    jButtonStandardsActionPerformed(evt);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jToolBar1.add(jButtonStandards);

        jButtonDendogram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/dendogram.png"))); // NOI18N
        jButtonDendogram.setToolTipText("Generate Dendogram");
        jButtonDendogram.setEnabled(false);
        jButtonDendogram.setFocusable(false);
        jButtonDendogram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonDendogram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDendogram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    jButtonDendogramActionPerformed(evt);
                } catch (Exception ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jToolBar1.add(jButtonDendogram);

        jButtonOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/options.png"))); // NOI18N
        jButtonOptions.setToolTipText("Options");

        if (!initialimage) {
            jButtonOptions.setEnabled(false);
        }

        jButtonOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOptionsActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonOptions);

        jButtonHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/help.png"))); // NOI18N
        jButtonHelp.setToolTipText("Help");
        jButtonHelp.setFocusable(false);
        jButtonHelp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonHelp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonHelp);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3))
        );

        setSize(420, 75);
        setResizable(false);

//        show();
    }

    public void setExperimentid(int experimentid) {
        this.experimentid = experimentid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private void jButtonOpenActionPerformed(ActionEvent evt) throws IOException, FileNotFoundException, ClassNotFoundException {

        IJ.setTool("rectangle");
        ImagePlus imp0 = WindowManager.getCurrentImage();
        if (imp0 != null) {
            GenericDialog gd = new GenericDialog("Save");
            gd.enableYesNoCancel();
            gd.hideCancelButton();
            gd.addMessage("Do you want to save the current study?");
            gd.showDialog();

            if (gd.wasOKed()) {
                Save s = new Save(imp0, finallanes, bands, normalisation, initialoffset, invertcolour,marker);
                s.saveinfile();
            }

            imp0.changes = false;
            imp0.close();
            cleanall();
        }

        if (first_time) {
            Load ld = new Load(this);
            ld.loadimageorstudy();
        } else {
            GenericDialog gd = new GenericDialog("GelJ Assistant");
            gd.addMessage("Do you want to use the assistant?");

            gd.hideCancelButton();
            gd.enableYesNoCancel();
            gd.addMessage("");
            // gd.addHelp("");
            // gd.setHelpLabel("Back");

            gd.showDialog();

            if (gd.wasOKed()) {
                Assistant a = new Assistant(this);
                gd.dispose();
                a.openimageassitant();
            } else {
                Load ld = new Load(this);
                ld.loadimageorstudy();
            }

        }

        ImagePlus imp = WindowManager.getCurrentImage();

        boolean initialimage;

        if (imp != null) {
            initialimage = true;
        } else {
            initialimage = false;
        }

        if (initialimage) {
            windowname = imp.getTitle();
            jButtonLanes.setEnabled(true);
            jButtonProcessing.setEnabled(true);
            jButtonSave.setEnabled(true);
            jButtonOptions.setEnabled(true);
        }

        first_time = false;
    }

    public void enableall() {

        jButtonLanes.setEnabled(true);
        jButtonProcessing.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonOptions.setEnabled(true);

    }

    private void jButtonSaveActionPerformed(ActionEvent evt) throws IOException {

        if (changes) {
            ImagePlus imp = IJ.getImage();
            Save s = new Save(imp, finallanes, bands, normalisation, initialoffset, invertcolour,marker);
            s.saveinfile();
            saved = true;
        }
    }

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {
        IJ.setTool("rectangle");
        ImagePlus imp = IJ.getImage();
        ImageEdit panelOptions = new ImageEdit(imp, this);
        GenericDialog gdG = new GenericDialog("Image Editing");
        gdG.add(panelOptions);
        gdG.addMessage("");
        gdG.showDialog();
    }

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {
        ImagePlus imp = IJ.getImage();
        IJ.setTool("rectangle");
    }

    private void jButtonCropActionPerformed(java.awt.event.ActionEvent evt) {
        ImagePlus imp = IJ.getImage();
        Roi r = imp.getRoi();
        IJ.run(imp, "Crop", "");
        changes = true;
    }

    private void jButtonProcessingActionPerformed(java.awt.event.ActionEvent evt) {
        ImagePlus imp = IJ.getImage();
        if (f == null) {
            f = new PreProcessing(this);
        } else {
            f.showWindow();
        }

    }

    private void jButtonLanesActionPerformed(java.awt.event.ActionEvent evt) {
        changes = true;
        lm = new LaneManager(this);
        IJ.setTool("rectangle");
        ImagePlus imp = WindowManager.getCurrentImage();
        LaneManagerCanvas cc = new LaneManagerCanvas(imp);
        new ImageWindow(imp, cc);
        cc.requestFocus();

        if (lanes != null) {
            for (Roi lane : lanes) {
                lm.addRoi(lane);
            }

            if (getstandardlanes(finallanes) != null) {
                lm.fix_standards(roisfromlanes(getstandardlanes(finallanes)));
            }
        }

        lm.requestFocus();
    }

    // Band Manager button
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        /*ImagePlus imp = IJ.getImage();
         Overlay o = imp.getOverlay();
         lanes = o.toArray();*/

        if (finallanes == null || finallanes.length == 0) {
            IJ.error("As a first step, you must detect the lanes of the image");
        } else {
            IJ.setTool("point");
            bm = new BandManager(this);
            ImagePlus imp = WindowManager.getCurrentImage();
            BandManagerCanvas cc = new BandManagerCanvas(imp);
            ImageWindow imageWindow = new ImageWindow(imp, cc);

            if (finallanes != null) {
                for (int i = 0; i < finallanes.length; i++) {
                    int[] positionbands = finallanes[i].getPositionbands();
                    if (positionbands != null) {
                        for (int b : positionbands) {
                            Roi r0 = finallanes[i].getRoi();
                            int r0x = r0.getBounds().x;
                            int r0y = r0.getBounds().y;
                            int w = r0.getBounds().width;
                            Rectangle r = new Rectangle(r0x, r0y + b, w, 2);
                            Roi r1 = rectangleintersectionroi(r0, r);
                            r1.setFillColor(colour);
                            bm.add(imp, r1, 0, i);
                            bm.showAll(0);
                        }
                    }
                }
            }

            bm.requestFocus();

            /*if (bands!=null){
             for (Band band : bands) {
             bm.add(imp,band.getRoi(),0,band.getLane());
             bm.showAll(0);
             }   
             }*/
        }
    }

    private void jButtonStandardsActionPerformed(ActionEvent evt) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        // If no standard has been previously defined, we ask the user whether
        // she wants to define a standard.
        Lane[] stds = getstandardlanes(finallanes);

        if ((stds == null) || (stds.length == 0)) {
            GenericDialog gdG = new GenericDialog("No Standards");
            gdG.addMessage("You have not defined any standard yet, do you want to add them now?.");
            gdG.showDialog();
            if (gdG.wasOKed()) {
                boolean[] selectstandards = selectstandards(lanesnames(finallanes),
                        whatarestandardlanes(finallanes));

                if (selectstandards.length < 2) {
                    return;
                } else {
                    for (int i = 0; i < selectstandards.length; i++) {
                        finallanes[i].setIsstandard(selectstandards[i]);
                    }
                }

            } else {
                return;
            }
        }
        if (stds.length == 1) {
            GenericDialog gdG = new GenericDialog("No Standards");
            gdG.addMessage("You have only defined one lane as reference lane, and you"
                    + " need to define at least two lanes as reference lanes. "
                    + "Would you like to add them now?");
            gdG.showDialog();
            if (gdG.wasOKed()) {
                boolean[] selectstandards = selectstandards(lanesnames(finallanes),
                        whatarestandardlanes(finallanes));

                if (selectstandards.length < 2) {
                    IJ.error("Select at least two lanes as reference lanes");
                    return;
                } else {
                    for (int i = 0; i < selectstandards.length; i++) {
                        finallanes[i].setIsstandard(selectstandards[i]);
                    }
                }
            }
            if (gdG.wasCanceled()) {
                return;
            }
        }
        // If the standard lanes have different number of bands, we ask the user
        // whether she wants to modify the number of bands in the standards.
        Lane[] stdlanes = getstandardlanes(finallanes);

        int[] indexesstd = numberofbandslanes(stdlanes);

        if (!allequal(indexesstd)) {
            GenericDialog gdG = new GenericDialog("No Standards");
            gdG.addMessage("The number of bands in the reference lanes is different, "
                    + "all the reference lanes must have the same number of bands. ");

            for (int i = 0; i < stdlanes.length; i++) {
                gdG.addMessage(stdlanes[i].getName()
                        + " contains " + stdlanes[i].getPositionbands().length + ".");

            }
            gdG.addMessage("Would you like to modify the number of bands?");

            gdG.showDialog();
            if (gdG.wasOKed()) {
                bm = new BandManager(this);
                ImagePlus imp = WindowManager.getCurrentImage();
                BandManagerCanvas cc = new BandManagerCanvas(imp);
                new ImageWindow(imp, cc);
                cc.requestFocus();

                for (Band band : bands) {
                    bm.add(imp, band.getRoi(), 0, band.getLane());
                    bm.showAll(0);
                }
                return;
            }
            if (gdG.wasCanceled()) {
                return;
            }
        }

        // We show the dialog asking the user whether she wants to use a 
        // previously defined standard or create a new one. 
        GenericDialog gdG = new GenericDialog("Standards");
        StandardsPanel sp = new StandardsPanel();
        ArrayList<String> availablestd = readstandardsavailable();

        if (availablestd == null || 0 == availablestd.size()) {
            sp.disableavailablestd();
        }
        sp.setlistofstandards(availablestd);
        gdG.add(sp);
        gdG.addMessage("");
        gdG.showDialog();

        if (gdG.wasCanceled()) {
            gdG.dispose();
            return;
        }

        int[] indexes = indexesstandardlanes(finallanes);

        // If she chooses to create a new standard, she has to introduce a name,
        // and the molecular wheights for the bands of the standard.
        if (sp.isnewstandardselected()) {

            /////////////////////////////////////////////////////////////
            ImagePlus imp = WindowManager.getCurrentImage();
            NormaliseManager rm = new NormaliseManager();

            int[] positionbands = finallanes[indexes[0]].getPositionbands();
            if (positionbands != null) {
                for (int b : positionbands) {
                    Roi r0 = finallanes[indexes[0]].getRoi();
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

            double[] res = createStandard(finallanes[indexes[0]].getPositionbands().length);
            rm.close();
            if (res == null) {
                return;
            } else {
                for (int i = 0; i < indexes.length; i++) {
                    finallanes[indexes[i]].setStdpositionbands(res);
                }
            }

        } else {
            // If she chooses an available standard, we check that the standard contains
            // as much molecular weights as bands in the image. In such a case, we assign
            // the values; otherwise, we ask the user to remove/add the molecular weights that 
            // do not appear in the image. 
            String file = sp.nameoptionmenu();
            marker = file;
            double[] stdvalues = readstandardfromfile(file);
            if (stdvalues.length == finallanes[indexes[0]].getPositionbands().length) {
                for (int i = 0; i < indexes.length; i++) {
                    finallanes[indexes[i]].setStdpositionbands(stdvalues);
                }
            } else {
                IJ.error("The standard contains " + stdvalues.length + " elements,"
                        + " but the reference lane contains "
                        + finallanes[indexes[0]].getPositionbands().length
                        + " bands. Please, add or remove the necessary values.");

                /////////////////////////////////////////////////////////////
                ImagePlus imp = WindowManager.getCurrentImage();
                NormaliseManager rm = new NormaliseManager();

                int[] positionbands = finallanes[indexes[0]].getPositionbands();
                if (positionbands != null) {
                    for (int b : positionbands) {
                        Roi r0 = finallanes[indexes[0]].getRoi();
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

                // Problem here both with correct number of values and when it is
                // required to ask the user again for values.
                while (stdvalues.length != finallanes[indexes[0]].getPositionbands().length) {

                    if (gdG2.wasCanceled()) {
                        gdG2.dispose();
                        return;
                    }
                    if (gdG2.wasOKed()) {
                        stdvalues = lp.getelements();
                        if (stdvalues.length != finallanes[indexes[0]].getPositionbands().length) {
                            IJ.error("The standard contains " + stdvalues.length + " elements,"
                                    + " but the reference lane contains "
                                    + finallanes[indexes[0]].getPositionbands().length
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
                initialoffset = finallanes[indexes[0]].getPositionbands()[0];
                double tempoffset = 0;
                for (int i = 0; i < indexes.length; i++) {
                    finallanes[indexes[i]].setStdpositionbands(stdvalues);
                    tempoffset = initialoffset
                            - finallanes[indexes[i]].getPositionbands()[0];
                    finallanes[indexes[i]].setOffset(tempoffset);
                }

            }

        }
        // Finally, we ask the user to select the calibration method. 
        CurveCalibration cc = new CurveCalibration(
                intarraytodoublearray(finallanes[indexes[0]].getPositionbands()),
                finallanes[indexes[0]].getStdpositionbands(),
                this);

        normalisation = true;

        /*GenericDialog gdG = new GenericDialog("Checking");
         for(int i=0;i<finallanes.length;i++){
         gdG.addMessage(finallanes[i].getName() + " has "+ 
         finallanes[i].getPositionbands().length + " bands.");
         }
         gdG.showDialog();
         */
    }

    private void jButtonDendogramActionPerformed(java.awt.event.ActionEvent evt) throws Exception {

        if (finallanes == null || finallanes.length == 0) {
            IJ.error("In order to create a dendogram, you must (at least) detect the "
                    + "lanes of the image.");
        } else {

            ImagePlus imp = IJ.getImage();
            Dendogram panelOptions = new Dendogram(imp);

            double tolerance_aux = 100;

            if (!normalisation) {
                IJ.error("You have not performed the normalisation step; therefore "
                        + "you will only be able to use curve-based methods for the "
                        + "dendrograms.");
            } else {
                panelOptions.activatebandbased();
                computestdlanes(finallanes, calibrationmethod, initialoffset);
                int[] indexes = indexesstandardlanes(finallanes);
                double[] stds = finallanes[indexes[0]].getStdpositionbands();
                tolerance_aux = stds[0] - stds[stds.length - 1];

            }

            boolean[] temp = selectlanes(lanesnames(finallanes));
            selectedlanesforcomparison = temp;
            if (temp == null || temp.length == 0) {
                return;
            }

            selectedlanes = new Lane[trueinbooleanarray(temp)];
            int j = 0;

            for (int i = 0; i < temp.length; i++) {
                if (temp[i]) {
                    selectedlanes[j] = finallanes[i];
                    j++;
                }

            }

            GenericDialog gdG = new GenericDialog("Cluster Analysis");
            gdG.add(panelOptions);
            gdG.addMessage("");
            gdG.showDialog();
            if (gdG.wasCanceled()) {
                return;
            }
            similaritymethod = panelOptions.getSimilarity();
            int clustering_alg = panelOptions.getClustering();
            tol = panelOptions.getTolerance();
            double tolerance = tolerance_aux * panelOptions.getTolerance() / 100;
            int output = panelOptions.getOutput();

            LaneComparison lc;
            switch (similaritymethod) {
                case 0:
                    lc = new Dice(tolerance);
                    simmethod = "Dice";
                    break;
                case 1:
                    lc = new Jaccard(tolerance);
                    simmethod = "Jaccard";
                    break;
                case 2:
                    lc = new Jeffrey(tolerance);
                    simmethod = "Jeffrey's X";
                    break;
                case 3:
                    lc = new Ochiai(tolerance);
                    simmethod = "Ochiai";
                    break;
                case 4:
                    lc = new BandDifference(tolerance);
                    simmethod = "Band difference";
                    break;
                case 5:
                    lc = new PearsonCorrelation();
                    simmethod = "Pearson correlation";
                    break;
                case 6:
                    lc = new CosineCorrelation();
                    simmethod = "Cosine coefficient";
                    break;
                case 7:
                    lc = new EuclideanDistance();
                    simmethod = "Euclidean distance";
                    break;
                case 8:
                    lc = new ManhattanDistance();
                    simmethod = "Manhattan distance";
                    break;
                default:
                    lc = new PearsonCorrelation();
                    simmethod = "Pearson correlation";
                    break;
            }

            switch (clustering_alg) {
                case SINGLE:
                    linkage = "Single linkage";
                    break;
                case COMPLETE:
                    linkage = "Complete linkage";
                    break;
                case UPGMA:
                    linkage = "UPGMA";
                    break;
                case MEAN:
                    linkage = "Mean linkage";
                    break;
                case UPGMC:
                    linkage = "UPGMC";
                    break;
                case WARD:
                    linkage = "Ward";
                    break;
                default:
                    break;
            }

            similaritytable = lc.CompareLanes(selectedlanes);
            PleaseWait pw = new PleaseWait((JDialog) null, false);
            pw.setVisible(true);
            pw.toFront();
            WekaClustering wc = new WekaClustering(clustering_alg, similaritytable,
                    selectedlanes, similaritymethod, invertcolour, output, colour,null);
            wc.computeandvisualisecluster(pw);

            if (firstcomparison) {
                createnewstudy();
                firstcomparison = false;
            } else {
                createnewcomparison();
            }

        }
    }

    public Lane[] externalcomputestdlanes() {
        computestdlanes(finallanes, calibrationmethod, initialoffset);
        return finallanes;
    }

    private void jButtonOptionsActionPerformed(java.awt.event.ActionEvent evt) {

        ImagePlus imp = IJ.getImage();

        if (normalisation) {
            computestdlanes(finallanes, calibrationmethod, initialoffset);
        }

        if (o == null) {
            o = new Options(this);
        } else {
            o.setMenu(this);
            o.showWindow();
        }

    }

    private void jButtonHelpActionPerformed(ActionEvent evt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setLanes(Roi[] lanes) {
        this.lanes = lanes;
    }

    /* public void setStandards(Roi[] stds) {
     this.standards = stds;
     }*/
    public Roi[] getLanes() {
        return lanes;
    }
    
    public void setLane(Roi lane, int pos){
        lanes[pos]=lane;
    }

    public void setBands(Band[] bands) {
        this.bands = bands;
    }

    public Band[] getBands() {
        return bands;
    }

    public void makebuttonsavailable() {
        jButton1.setEnabled(true);
        jButtonDendogram.setEnabled(true);
    }

    public void makebuttonstdavailable() {
        jButtonStandards.setEnabled(true);
    }

    public void setCalibrationmethod(int calibrationmethod) {
        this.calibrationmethod = calibrationmethod;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }

    public void setNormalisation(boolean normalisation) {
        this.normalisation = normalisation;
    }

    public void setInitialoffset(double initialoffset) {
        this.initialoffset = initialoffset;
    }

    public void changeinvertcolour() {
        this.invertcolour = !this.invertcolour;
    }

    public boolean getinvertcolour() {
        return invertcolour;
    }

    public void setInvertcolour(boolean invertcolour) {
        this.invertcolour = invertcolour;
    }

    @Override
    public void close() {
        if (!saved) {
            GenericDialog gd = new GenericDialog("Save Study");
            gd.enableYesNoCancel();
            gd.hideCancelButton();
            gd.addMessage("Do you want to save the study for further use?");
            gd.addMessage("");
            gd.showDialog();

            if (gd.wasOKed()) {
                ImagePlus imp = WindowManager.getCurrentImage();
                imp.changes = false;
                Save s = new Save(imp, finallanes, bands, normalisation,
                        initialoffset, invertcolour,marker);
                try {
                    s.saveinfile();
                } catch (IOException ex) {
                    Logger.getLogger(WekaClustering.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        IJ.getInstance().quit();
        super.close();

    }

    public double getInitialoffset() {
        return initialoffset;
    }

    public int getCalibrationmethod() {
        return calibrationmethod;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public Color getColour() {
        return colour;
    }

    public void sortfinallanes() {

        for (int i = 0; i < finallanes.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < finallanes.length; j++) {
                if (finallanes[j].getRoi().getBounds().x < finallanes[min].getRoi().getBounds().x) {
                    min = j;
                }
            }
            Lane temp = finallanes[i];
            finallanes[i] = finallanes[min];
            finallanes[min] = temp;
        }

    }

    public void setFirst_time(boolean first_time) {
        this.first_time = first_time;
    }

    public String getWindowname() {
        return windowname;
    }

    public void setWindowname(String windowname) {
        this.windowname = windowname;
    }

    public void setChanges(boolean changes) {
        this.changes = changes;
    }

    public int getStudyid() {
        return studyid;
    }

    public void setStudyid(int studyid) {
        this.studyid = studyid;
    }

    public boolean isNewstudy() {
        return newstudy;
    }

    public void setNewstudy(boolean newstudy) {
        this.newstudy = newstudy;
    }

    public String getExperimentname() {
        return experimentname;
    }

    public void setExperimentname(String experimentname) {
        this.experimentname = experimentname;
    }

    public String getExperimentcomments() {
        return experimentcomments;
    }

    public void setExperimentcomments(String experimentcomments) {
        this.experimentcomments = experimentcomments;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public void setSelectedlanesforcomparison(boolean[] selectedlanesforcomparison) {
        this.selectedlanesforcomparison = selectedlanesforcomparison;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setSimmethod(String simmethod) {
        this.simmethod = simmethod;
    }

    public void setLinkage(String linkage) {
        this.linkage = linkage;
    }

    public void setTol(int tol) {
        this.tol = tol;
    }

    public void changefirstcomparison() {
        firstcomparison = false;
    }

    public void createnewstudy() throws FileNotFoundException, UnsupportedEncodingException {

        /// Creation of the study
        studyid = lastStudyid();
        String name = "Study-" + studyid;
        String comments = "";

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date date = new Date(utilDate.getTime());

        // Creating folders
        File folder0 = new File(System.getProperty("user.dir") + "/GelJDBImages/");

        if (!folder0.exists()) {
            folder0.mkdir();
        }

        username = getUser(user).getUsername();
        File folderuser = new File(System.getProperty("user.dir") + "/GelJDBImages/"
                + studyid);

        if (!folderuser.exists()) {
            folderuser.mkdir();
        }

        File folder = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username);
        folder.mkdir();
        File folderexp = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/experiments/");
        folderexp.mkdir();
        File foldercomp = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/comparisons/");
        foldercomp.mkdir();

        String path = System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username;
        User u = getUser(user);
        Study s = new Study(studyid, name, date, path, comments, u);

        insertStudy(s);

        //// Creation of the experiment
        experimentid = lastExperimentid();
        experimentname = "Experiment-" + experimentid;
        Marker m = null;
        if (marker != null) {
            m = getMarker(marker);
        } else {
            m = getMarker(lastMarkerid() - 1);
        }

        e = new Experiment(experimentid, experimentname, date, windowname, calibrationmethod, "", s, m, u);
        insertExperiment(e);

        // Creation of the folder where the experiment image will be saved.
        File folderexp1 = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/experiments/" + experimentid + "/");
        folderexp1.mkdir();

        ImagePlus imp = WindowManager.getImage(windowname);
        String pathimage = folderexp1 + "/" + windowname;
        IJ.save(imp, pathimage);

        //// Creation of the lanes to store them in the database
        // Creation of the folder where the lanes will be saved.
        File folderexplanes = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/experiments/" + experimentid + "/lanes/");
        folderexplanes.mkdir();

        // For each lane, we create a lanedb instance that is added to the database
        laneid = lastLaneid();
        updatelanesdb();

        //// Creation of the comparison
        int compid = lastComparisonid();
        String compname = "Comparison-" + compid;

        Comparison c = new Comparison(compid, compname, date, simmethod, linkage, tol, "", s);
        insertComparison(c);
        /// Adding the selected lanes to the comparison
        for (int i = 0; i < selectedlanesforcomparison.length; i++) {
            if (selectedlanesforcomparison[i]) {
                insertComparisonLane(compid, laneid + i);
            }

        }

    }

    public Experiment storeexperiment() throws FileNotFoundException, UnsupportedEncodingException {

        username = getUser(user).getUsername();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date date = new Date(utilDate.getTime());

        //// Creation of the experiment
        experimentid = lastExperimentid();
        experimentname = "Experiment-" + experimentid;
        Marker m = null;
        if (marker != null) {
            m = getMarker(marker);
        } else {
            m = getMarker(lastMarkerid() - 1);
        }

        e = new Experiment(experimentid, experimentname, date, windowname, calibrationmethod, "", getStudy(studyid), m, getUser(user));
        insertExperiment(e);

        // Creation of the folder where the experiment image will be saved.
        File folderexp1 = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/experiments/" + experimentid + "/");
        folderexp1.mkdir();

        ImagePlus imp = WindowManager.getCurrentImage();
        imp.deleteRoi();
        imp.setOverlay(null);
        String pathimage = folderexp1 + "/" + imp.getTitle();
        IJ.save(imp, pathimage);

        //// Creation of the lanes to store them in the database
        // Creation of the folder where the lanes will be saved.
        File folderexplanes = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/experiments/" + experimentid + "/lanes/");
        folderexplanes.mkdir();

        // For each lane, we create a lanedb instance that is added to the database
        laneid = lastLaneid();
        updatelanesdb();

        return e;

    }

    public void createnewcomparison() throws FileNotFoundException, UnsupportedEncodingException {

        Study s = getStudy(studyid);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date date = new Date(utilDate.getTime());

        // We update the lanes information just in case
        updatelanesdb();

        //// Creation of the comparison
        int compid = lastComparisonid();
        String compname = "Comparison-" + compid;

        Comparison c = new Comparison(compid, compname, date, simmethod, linkage, tol, "", s);
        insertComparison(c);
        /// Adding the selected lanes to the comparison
        for (int i = 0; i < selectedlanesforcomparison.length; i++) {
            if (selectedlanesforcomparison[i]) {
                insertComparisonLane(compid, laneid + i);
            }

        }

    }

    private void updatelanesdb() throws FileNotFoundException, UnsupportedEncodingException {

        for (int i = 0; i < finallanes.length; i++) {
            if (firstcomparison) {
                String label = finallanes[i].getName();
                LaneInfo l = (LaneInfo) lanesinfo.get(label);
                String strainnumber = "" + (i + 1);
                if (!l.getStrainNumber().equals("")) {
                    strainnumber = l.getStrainNumber();
                }
                LaneDB lanedb = new LaneDB(laneid + i, l.getGenus(), l.getSpecies(), "" + strainnumber, l.getCountry(), "", finallanes[i].isIsstandard(), "", e);
                insertLane(lanedb);
            }
            /// Create folder
            String folderlanepath = System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/experiments/" + experimentid + "/lanes/" + (laneid + i) + "/";
            File folderlane = new File(folderlanepath);
            folderlane.mkdir();
            /// Add files to folder
            // Saving the image
            IJ.saveAsTiff(finallanes[i].getImp(), folderlanepath + (laneid + i) + ".tiff");
            // Saving the molecular weights
            String lanemws = folderlanepath + (laneid + i) + ".mw";
            PrintWriter writermws = new PrintWriter(lanemws, "UTF-8");
            for (int j = 0; j < finallanes[i].getStdpositionbands().length; j++) {
                writermws.println(finallanes[i].getStdpositionbands()[j]);
            }
            writermws.close();
            // Saving the band positions
            String lanebands = folderlanepath + (laneid + i) + ".bands";
            PrintWriter writerbands = new PrintWriter(lanebands, "UTF-8");
            for (int j = 0; j < finallanes[i].getPositionbands().length; j++) {
                writerbands.println(finallanes[i].getPositionbands()[j]);
            }
            writerbands.close();
            // Saving the curve
            String lanecurve = folderlanepath + (laneid + i) + ".curve";
            PrintWriter writercurve = new PrintWriter(lanecurve, "UTF-8");
            for (int j = 0; j < finallanes[i].getProfile().length; j++) {
                writercurve.println(finallanes[i].getProfile()[j]);
            }
            writercurve.close();

            writermws.close();
            // Saving the band positions
            String lanebands2 = folderlanepath + (laneid + i) + ".bands2";
            PrintWriter writerbands2 = new PrintWriter(lanebands2, "UTF-8");
            for (int j = 0; j < finallanes[i].getPositionbands2().length; j++) {
                writerbands2.println(finallanes[i].getPositionbands2()[j]);
            }
            writerbands2.close();

            RoiEncoder.save(finallanes[i].getRoi(), folderlanepath + (laneid + i) + ".roi");

            // Saving the roi
//            String laneroi = folderlanepath + (laneid + i) + ".roi";
//            PrintWriter writerroi = new PrintWriter(laneroi, "UTF-8");
//            
//            Roi p = finallanes[i].getRoi();          
//            for (int j = 0; j < finallanes[i].getRoi().getPolygon().npoints; j++) {
//                writerroi.println(finallanes[i].getRoi().getPolygon().xpoints[j] + "," + 
//                        finallanes[i].getRoi().getPolygon().ypoints[j]);
//            }
//            writerroi.close();
//            
        }

    }

    public void updatelanedb(int id, Lane[] markers) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        Lane[] newlanes = new Lane[markers.length + 1];
        System.arraycopy(markers, 0, newlanes, 0, markers.length);
        newlanes[markers.length] = finallanes[0];
        computestdlane(finallanes[0], marker,invertcolour);
        /// Create folder
        String folderlanepath = System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + username + "/experiments/" + experimentid + "/lanes/" + id + "/";
           /// Add files to folder

        // Saving the molecular weights
        String lanemws = folderlanepath + id + ".mw";
        PrintWriter writermws = new PrintWriter(lanemws, "UTF-8");
        for (int j = 0; j < finallanes[0].getStdpositionbands().length; j++) {
            writermws.println(finallanes[0].getStdpositionbands()[j]);
        }
        writermws.close();
        // Saving the band positions
        String lanebands = folderlanepath + id + ".bands";
        PrintWriter writerbands = new PrintWriter(lanebands, "UTF-8");
        for (int j = 0; j < finallanes[0].getPositionbands().length; j++) {
            writerbands.println(finallanes[0].getPositionbands()[j]);
        }
        writerbands.close();

        // Saving the band positions
        String lanebands2 = folderlanepath + id + ".bands2";
        PrintWriter writerbands2 = new PrintWriter(lanebands2, "UTF-8");
        for (int j = 0; j < finallanes[0].getPositionbands2().length; j++) {
            writerbands2.println(finallanes[0].getPositionbands2()[j]);
        }
        writerbands2.close();

        // Saving the curve
        String lanecurve = folderlanepath + id + ".curve";
        PrintWriter writercurve = new PrintWriter(lanecurve, "UTF-8");
        for (int j = 0; j < finallanes[0].getProfile().length; j++) {
            writercurve.println(finallanes[0].getProfile()[j]);
        }
        writercurve.close();

    }

    public BandManager bandmanagerfunctionality() {
        if (finallanes == null || finallanes.length == 0) {
            IJ.error("As a first step, you must detect the lanes of the image");
        } else {
            IJ.setTool("point");
            bm = new BandManager(this, true);
            ImagePlus imp = WindowManager.getCurrentImage();
            BandManagerCanvas cc = new BandManagerCanvas(imp);
            ImageWindow imageWindow = new ImageWindow(imp, cc);

            if (finallanes != null) {
                for (int i = 0; i < finallanes.length; i++) {
                    int[] positionbands = finallanes[i].getPositionbands();
                    if (positionbands != null) {
                        for (int b : positionbands) {
                            Roi r0 = finallanes[i].getRoi();
                            int r0x = r0.getBounds().x;
                            int r0y = r0.getBounds().y;
                            int w = r0.getBounds().width;
                            Rectangle r = new Rectangle(r0x, r0y + b, w, 2);
                            Roi r1 = rectangleintersectionroi(r0, r);
                            r1.setFillColor(colour);
                            bm.add(imp, r1, 0, i);
                            bm.showAll(0);
                        }
                    }
                }
            }

            bm.requestFocus();

            /*if (bands!=null){
             for (Band band : bands) {
             bm.add(imp,band.getRoi(),0,band.getLane());
             bm.showAll(0);
             }   
             }*/
        }

        return bm;
    }

    public void setLm(LaneManager lm) {
        this.lm = lm;
    }

    public Hashtable getLanesinfo() {
        return lanesinfo;
    }

    public void setLanesinfo(Hashtable lanesinfo) {
        this.lanesinfo = lanesinfo;
    }

    public String getMarkername() {
        return marker;
    }

    public void savestandardadditionalinfo() throws FileNotFoundException, UnsupportedEncodingException {
        int[] indexes = indexesstandardlanes(finallanes);

        double[] info = {28, 289, 9};
        String workingfile2 = System.getProperty("user.dir") + "/standardfiles/info/" + marker + ".info";
        PrintWriter writer2 = new PrintWriter(workingfile2, "UTF-8");
        writer2.println(finallanes[indexes[0]].getImp().getWidth());
        writer2.println(finallanes[indexes[0]].getImp().getHeight());
        writer2.println(calibrationmethod);
        writer2.close();
        int[] pos = finallanes[indexes[0]].getPositionbands();
        workingfile2 = System.getProperty("user.dir") + "/standardfiles/info/" + marker + ".pos";
        writer2 = new PrintWriter(workingfile2, "UTF-8");
        for (int i = 0; i < pos.length; i++) {
            writer2.println(pos[i]);
        }
        writer2.close();

    }

}
