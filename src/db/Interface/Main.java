/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db.Interface;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import db.Classes.Comparison;
import db.Classes.Experiment;
import db.Classes.Lab;
import db.Classes.LaneDB;
import static db.Classes.LaneDB.allsamemarker;
import static db.Classes.LaneDB.searchlanesstring;
import db.Classes.Marker;
import db.Classes.Study;
import db.Classes.UnfinishedExperiment;
import db.Classes.User;
import static db.Interface.EditViewLaneDB.LaneDBtoLaneIJ;
import db.NewComparisonWizard.NewComparison;
import db.NewComparisonWizard.Step1;
import static db.Queries.BackupQueries.createbackup;
import static db.Queries.BackupQueries.restorebackup;
import static db.Queries.BackupQueries.unzipFunction;
import static db.Queries.ComparisonQueries.deletecomparison;
import static db.Queries.ComparisonQueries.getComparisons;
import static db.Queries.ComparisonQueries.insertComparison;
import static db.Queries.ComparisonQueries.insertComparisonLane;
import static db.Queries.ComparisonQueries.lastComparisonid;
import static db.Queries.ExperimentQueries.deletecomparisonsexperiment;
import static db.Queries.ExperimentQueries.deleteexperiment;
import static db.Queries.ExperimentQueries.existscomparisonsexperiment;
import static db.Queries.ExperimentQueries.exportexperiment;
import static db.Queries.ExperimentQueries.getExperiments;
import static db.Queries.ExperimentQueries.getExperimentsofOtherStudies;
import static db.Queries.ExperimentQueries.insertExperiment;
import static db.Queries.ExperimentQueries.lastExperimentid;
import static db.Queries.LabQueries.existslab_id;
import static db.Queries.LabQueries.insertLab;
import static db.Queries.LabQueries.lastLabid;
import static db.Queries.LaneQueries.containadditionalfield;
import static db.Queries.LaneQueries.deletelanecomparison;
import static db.Queries.LaneQueries.getAdditionalFields;
import static db.Queries.LaneQueries.getAdditionalFieldsExp;
import static db.Queries.LaneQueries.getLane;
import static db.Queries.LaneQueries.getLanes;
import static db.Queries.LaneQueries.getLanesComparison;
import static db.Queries.LaneQueries.getlanesexperiment;
import static db.Queries.LaneQueries.insertLane;
import static db.Queries.LaneQueries.insertLaneAddedInfo;
import static db.Queries.LaneQueries.lastLaneid;
import static db.Queries.LaneQueries.updateLane;
import static db.Queries.MarkerQueries.existsmarker_id;
import static db.Queries.MarkerQueries.insertMarker;
import static db.Queries.MarkerQueries.lastMarkerid;
import static db.Queries.StudyQueries.deletestudy;
import static db.Queries.StudyQueries.getStudies;
import static db.Queries.StudyQueries.getStudy;
import db.Queries.UnfinishedExperimentsQueries;
import static db.Queries.UnfinishedExperimentsQueries.getUnfinishedExperiments;
import static db.Queries.UserQueries.existssuser;
import static db.Queries.UserQueries.getUser;
import static db.Queries.UserQueries.insertUser;
import static db.Queries.UserQueries.lastUserid;
import ij.IJ;
import ij.ImageJ;
import static ij.ImageJ.NO_SHOW;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.io.RoiDecoder;
import ij.io.SaveDialog;
import ij.plugin.Memory;
import ij.text.TextWindow;
import ij.workspace.GelJ.Assistant4.Image_Assistant;
import ij.workspace.GelJ.Lane;
import ij.workspace.GelJ.MarkerSelection.MarkerSelection_1;
import ij.workspace.GelJ.MarkerSelection.ViewMarkers;
import ij.workspace.GelJ.Menu;
import ij.workspace.GelJ.SaveLoad.Load;
import static ij.workspace.GelJ.Standards.StandardUtils.readcomparisonconfig;
import ij.workspace.GelJ.Utils.Utilities;
import static ij.workspace.GelJ.Utils.Utilities.deleteDir;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Component;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author joheras
 */
public class Main extends javax.swing.JFrame implements TreeSelectionListener, TableModelListener {

    /**
     * Creates new form Main
     */
    public int studyid;
    public int userid;
    private ArrayList<Experiment> experiments = new ArrayList<>();
    private ArrayList<Comparison> comparisons = new ArrayList<>();
    private ArrayList<LaneDB> lanes = new ArrayList<>();
    private ArrayList<LaneDB> templanes = new ArrayList<>();
    private ArrayList<String> additionalfields = new ArrayList<>();
    private boolean firsttime = true;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNodeComp;
    private DefaultTreeModel treeModelComp;
    private ImageJ ij;
    private boolean twiceinarow = false;

    public Main(int userid) {
        initComponents();

        jsMainPane.setDividerLocation(0.3);
        jspLeftPane.setDividerLocation(0.7);
        jspRightPane.setDividerLocation(1.0);
//        initialiseexperimentstree();
//        initialisecomparisonstree();

        //        this.studyid = studyid;       
//        updateexperiments(true);
//        updatecomparisons(true);
//        updatelanes(true);
        this.userid = userid;
        ArrayList<Study> studies = getStudies(userid);
        if (studies.isEmpty()) {
            jmOpenStudy.setEnabled(false);
//            jMenu3.setEnabled(false);
        }
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        activateemptytoolbar();

    }

    private void initialiseexperimentstree() {

        rootNode = new DefaultMutableTreeNode("Experiments");
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new MyTreeModelListener());
        jtExperiments.setModel(treeModel);
        jtExperiments.setEditable(true);
        jtExperiments.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jtExperiments.addTreeSelectionListener(this);
        jtExperiments.setShowsRootHandles(true);
        jtExperiments.setRootVisible(true);
        jtExperiments.setCellRenderer(new ExperimentsRenderer());
        ToolTipManager.sharedInstance().registerComponent(jtExperiments);
        // Mouse moved
//        jtExperiments.addMouseMotionListener(new MouseMotionAdapter() {
//            public void mouseMoved(MouseEvent e) {
//                int x = (int) e.getPoint().getX();
//                int y = (int) e.getPoint().getY();
//                TreePath path = jtExperiments.getPathForLocation(x, y);
//                Rectangle pathBounds = jtExperiments.getUI().getPathBounds(jtExperiments, path);
//                if (pathBounds != null && pathBounds.contains(e.getX(), e.getY())) {
//                    JPopupMenu menu = new JPopupMenu();
//                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
//                    Experiment experiment = (Experiment) node.getUserObject();
//                    JTextArea jt = new JTextArea(experiment.printInfo());
//                    jt.setEditable(false);
//                    menu.add(jt);
//                    menu.show(jtExperiments, pathBounds.x, pathBounds.y + pathBounds.height);
//                }
//            }
//
//        });
    }

    private void initialisecomparisonstree() {

        rootNodeComp = new DefaultMutableTreeNode("Comparisons");
        treeModelComp = new DefaultTreeModel(rootNodeComp);
        treeModelComp.addTreeModelListener(new MyTreeModelListener());

        jtComparisons.setModel(treeModelComp);
        jtComparisons.setEditable(true);
        jtComparisons.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jtComparisons.addTreeSelectionListener(this);
        jtComparisons.setShowsRootHandles(true);
        jtComparisons.setRootVisible(true);

        jtComparisons.setCellRenderer(new ComparisonRenderer());
        ToolTipManager.sharedInstance().registerComponent(jtComparisons);
//        jtComparisons.addMouseMotionListener(new MouseMotionAdapter() {
//            public void mouseMoved(MouseEvent e) {
//                int x = (int) e.getPoint().getX();
//                int y = (int) e.getPoint().getY();
//                TreePath path = jtComparisons.getPathForLocation(x, y);
//                Rectangle pathBounds = jtComparisons.getUI().getPathBounds(jtComparisons, path);
//                if (pathBounds != null && pathBounds.contains(e.getX(), e.getY())) {
//                    JPopupMenu menu = new JPopupMenu();
//                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
//                    Comparison comp = (Comparison) node.getUserObject();
//                    JTextArea jt = new JTextArea(comp.printInfo());
//                    jt.setEditable(false);
//                    menu.add(jt);
//                    menu.show(jtComparisons, pathBounds.x, pathBounds.y + pathBounds.height);
//                }
//            }
//
//        });
    }

    public DefaultMutableTreeNode addExperiment(DefaultMutableTreeNode parent,
            Object child,
            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode
                = new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }

        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent,
                parent.getChildCount());

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            jtExperiments.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    public DefaultMutableTreeNode addComparison(DefaultMutableTreeNode parent,
            Object child,
            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode
                = new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNodeComp;
        }

        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModelComp.insertNodeInto(childNode, parent,
                parent.getChildCount());

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            jtComparisons.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    public void updateexperiments(boolean all, boolean clean) {

        if (all) {
            experiments = getExperiments(studyid);
            clearExp();
        }

        if (experiments.isEmpty()) {
            jtExperiments.setRootVisible(false);
            disablenoexperiment();
        } else {
            jtExperiments.setRootVisible(true);
        }

        for (Experiment e : experiments) {
            addExperiment(rootNode, e, true);
        }

        if (clean) {
            cleanlanes();
        }
    }

    public void updatecomparisons(boolean all, boolean clean) {

        if (all) {
            comparisons = getComparisons(studyid);
            clearComp();
        }

        if (comparisons.isEmpty()) {
            jtComparisons.setRootVisible(false);
            if (!experiments.isEmpty()) {
                disablenocomparison();
            } else {
                disablenoexperiment();
            }
        } else {
            jtComparisons.setRootVisible(true);
        }

        for (Comparison c : comparisons) {
            addComparison(rootNodeComp, c, true);
        }

        if (clean) {
            cleanlanes();
            activateemptytoolbar();
        }
    }

    public void clearComp() {
        rootNodeComp.removeAllChildren();
        treeModelComp.reload();
    }

    public void clearExp() {
        rootNode.removeAllChildren();
        treeModel.reload();
    }

    public void updateselectedlanes() {

        for (int i = 0; i < lanes.size(); i++) {
            lanes.set(i, getLane(lanes.get(i).getId()));
        }

        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

        String[] names = new String[names0.length + additionalfields.size()];
        for (int i = 0; i < names0.length; i++) {
            names[i] = names0[i];
        }
        for (int i = 0; i < additionalfields.size(); i++) {
            names[i + names0.length] = additionalfields.get(i);
        }

        Object[][] data = new Object[lanes.size()][names.length];

        // We update the table
        for (int i = 0; i < lanes.size(); i++) {
            data[i][0] = lanes.get(i).getGenus();
            data[i][1] = lanes.get(i).getSpecies();
            data[i][2] = lanes.get(i).getStrainNumber();
            data[i][3] = lanes.get(i).getCountry();
            data[i][4] = lanes.get(i).getMSLT();
            data[i][5] = lanes.get(i).isIsMarker();
            data[i][6] = lanes.get(i).getExperiment().getName();
            data[i][7] = lanes.get(i).getComments();
            for (int j = 0; j < additionalfields.size(); j++) {
                data[i][8 + j] = lanes.get(i).getLaneaddedinfo().get(additionalfields.get(j));
            }
        }

        // Operations to change the model, we disable the edition of the date
        DefaultTableModel model = new DefaultTableModel(data, names) {
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                if (mColIndex == 6 || mColIndex == 5) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return java.lang.Boolean.class;
                } else {
                    return java.lang.Object.class;
                }

            }

        };

        model.addTableModelListener(this);
        jtLanes.setModel(model);

        jtLanes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (jtLanes.getSelectedRow() > -1) {
                    if (!twiceinarow) {
                        if (jtComparisons.getSelectionPath() == null) {
                            viewlane();
                        }
                    }
                    twiceinarow = !twiceinarow;
                }
                if (jtLanes.getSelectedRow() > 0) {

                    jbEditlane.setEnabled(true);
                    jbDeleteLane.setEnabled(true);
                    jbShowMW.setEnabled(true);
                    jbFindSimilarLanes.setEnabled(true);
                }
            }
        });

        TableColumn countriescolumn = jtLanes.getColumnModel().getColumn(3);
        countriescolumn.setCellEditor(new DefaultCellEditor(generatecountriescombobox()));

    }

    public void cleanlanes() {

        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

        String[] names = new String[names0.length + additionalfields.size()];
        for (int i = 0; i < names0.length; i++) {
            names[i] = names0[i];
        }
        for (int i = 0; i < additionalfields.size(); i++) {
            names[i + names0.length] = additionalfields.get(i);
        }

        Object[][] data = new Object[lanes.size()][names.length];

        // Operations to change the model, we disable the edition of the date
        DefaultTableModel model = new DefaultTableModel(data, names);

        model.addTableModelListener(this);
        jtLanes.setModel(model);

    }

    public void activateemptytoolbar() {
        jbeditexpcomp.setVisible(false);
        jbDeleteLane.setVisible(false);
        jbEditlane.setVisible(false);
        jbShowMW.setVisible(false);
//        jbViewLane.setVisible(false);
        jbAddField.setVisible(false);
        jbdeletefield.setVisible(false);
        jbFindSimilarLanes.setVisible(false);
        jxsearchlane.setVisible(false);
        jbAdvancedSearchLanes.setVisible(false);
        jSeparator1.setVisible(false);
        jSeparator2.setVisible(false);
        jSeparator3.setVisible(false);
//        jSeparator4.setVisible(false);

        jbexport.setVisible(false);
        jbexport.setEnabled(false);
        jbDeleteExp.setVisible(false);
        jbDeleteExp.setEnabled(false);
        jbViewComp.setVisible(false);
        jbViewComp.setEnabled(false);
        jbExportNewick.setVisible(false);
        jbExportNewick.setEnabled(false);
        jbSimMatrix.setVisible(false);
        jbSimMatrix.setEnabled(false);

        jbeditexpcomp.setEnabled(false);
        jbDeleteLane.setEnabled(false);
        jbEditlane.setEnabled(false);
        jbShowMW.setEnabled(false);
//        jbViewLane.setEnabled(false);
        jbAddField.setEnabled(false);
        jbdeletefield.setEnabled(false);
        jbFindSimilarLanes.setEnabled(false);
        jxsearchlane.setEnabled(false);
        jbAdvancedSearchLanes.setEnabled(false);

        jToolBar3.setVisible(false);
        jtLanes.setVisible(false);
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jtExperiments.clearSelection();
        jtComparisons.clearSelection();

    }

    public void activateexperimentstoolbar() {
        jbeditexpcomp.setVisible(true);
        jbeditexpcomp.setEnabled(true);
        jbeditexpcomp.setToolTipText("Modify properties of the experiment");
        jbDeleteExp.setVisible(true);
        jbDeleteExp.setEnabled(true);

        jbmodifycomp.setVisible(false);
        jbmodifycomp.setEnabled(false);
        jbdeletecomp.setVisible(false);
        jbdeletecomp.setEnabled(false);

        jbDeleteLane.setVisible(false);
        jbEditlane.setVisible(true);
        jbEditlane.setEnabled(false);
        jbShowMW.setVisible(true);
        jbShowMW.setEnabled(false);
//        jbViewLane.setVisible(true);
        jbAddField.setVisible(true);
        jbdeletefield.setVisible(true);
        jbFindSimilarLanes.setVisible(true);
        jbFindSimilarLanes.setEnabled(false);
        jxsearchlane.setVisible(true);

        jbAdvancedSearchLanes.setVisible(true);
        jSeparator1.setVisible(true);
        jSeparator2.setVisible(true);
        jSeparator3.setVisible(true);
//        jSeparator4.setVisible(true);

        jbexport.setVisible(true);
        jbexport.setEnabled(true);

        jbViewComp.setVisible(false);
        jbViewComp.setEnabled(false);
        jbExportNewick.setVisible(false);
        jbExportNewick.setEnabled(false);
        jbSimMatrix.setVisible(false);
        jbSimMatrix.setEnabled(false);

        jbeditexpcomp.setEnabled(true);
//        jbDeleteLane.setEnabled(false);
//        jbEditlane.setEnabled(true);
//        jbViewLane.setEnabled(true);
        jbAddField.setEnabled(true);
        jbdeletefield.setEnabled(true);
//        jbFindSimilarLanes.setEnabled(true);
        jxsearchlane.setEnabled(true);
        jbAdvancedSearchLanes.setEnabled(true);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtExperiments.getLastSelectedPathComponent();
        Experiment exp = (Experiment) node.getUserObject();
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Experiment " + exp.getName()));
        jbDeleteExp.setToolTipText("Delete experiment " + exp.getName());
        jbexport.setToolTipText("Export experiment " + exp.getName());

        jToolBar3.setVisible(true);
        jtLanes.setVisible(true);
    }

    public void activatecomparisontoolbar() {
        jbeditexpcomp.setVisible(false);
        jbeditexpcomp.setEnabled(false);
        jbDeleteExp.setVisible(false);
        jbDeleteExp.setEnabled(false);

        jbmodifycomp.setVisible(true);
        jbmodifycomp.setEnabled(true);
        jbdeletecomp.setVisible(true);
        jbdeletecomp.setEnabled(true);
        jbDeleteLane.setVisible(true);
        jbDeleteLane.setEnabled(false);
        jbEditlane.setVisible(false);
        jbShowMW.setVisible(false);
//        jbViewLane.setVisible(true);
        jbAddField.setVisible(false);
        jbdeletefield.setVisible(false);
        jbFindSimilarLanes.setVisible(true);
        jbFindSimilarLanes.setEnabled(false);
        jxsearchlane.setVisible(true);
        jbViewComp.setVisible(true);
        jbViewComp.setEnabled(true);
        jbExportNewick.setVisible(true);
        jbExportNewick.setEnabled(true);
        jbSimMatrix.setVisible(true);
        jbSimMatrix.setEnabled(true);

        jbAdvancedSearchLanes.setVisible(true);
        jSeparator1.setVisible(true);
        jSeparator2.setVisible(true);
        jSeparator3.setVisible(false);
//        jSeparator4.setVisible(true);

        jbexport.setVisible(false);
        jbexport.setEnabled(false);

        jbeditexpcomp.setEnabled(true);
//        jbDeleteLane.setEnabled(true);
//        jbEditlane.setEnabled(false);
//        jbViewLane.setEnabled(true);
        jbAddField.setEnabled(false);
        jbdeletefield.setEnabled(false);
//        jbFindSimilarLanes.setEnabled(true);
        jxsearchlane.setEnabled(true);
        jbAdvancedSearchLanes.setEnabled(true);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
        Comparison comp = (Comparison) node.getUserObject();
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Comparison " + comp.getName()));

        jbDeleteExp.setToolTipText("Delete comparison " + comp.getName());

        jToolBar3.setVisible(true);
        jtLanes.setVisible(true);
    }

    public void updatelanes(boolean all) {

        if (all) {
            lanes = getLanes(studyid);
            templanes = lanes;
        }

//        additionalfields = getAdditionalFields(studyid);
        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

        String[] names = new String[names0.length + additionalfields.size()];
        for (int i = 0; i < names0.length; i++) {
            names[i] = names0[i];
        }
        for (int i = 0; i < additionalfields.size(); i++) {
            names[i + names0.length] = additionalfields.get(i);
        }

        Object[][] data = new Object[lanes.size()][names.length];

        // We update the table
        for (int i = 0; i < lanes.size(); i++) {
            data[i][0] = lanes.get(i).getGenus();
            data[i][1] = lanes.get(i).getSpecies();
            data[i][2] = lanes.get(i).getStrainNumber();
            data[i][3] = lanes.get(i).getCountry();
            data[i][4] = lanes.get(i).getMSLT();
            data[i][5] = lanes.get(i).isIsMarker();
            data[i][6] = lanes.get(i).getExperiment().getName();
            data[i][7] = lanes.get(i).getComments();
            for (int j = 0; j < additionalfields.size(); j++) {
                data[i][8 + j] = lanes.get(i).getLaneaddedinfo().get(additionalfields.get(j));
            }
        }

        // Operations to change the model, we disable the edition of the date
        DefaultTableModel model = new DefaultTableModel(data, names) {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                if (mColIndex == 6 || mColIndex == 5) {
                    return false;
                } else {
                    return true;
                }
            }

            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return java.lang.Boolean.class;
                } else {
                    return java.lang.Object.class;
                }

            }
        };

        model.addTableModelListener(this);
        jtLanes.setModel(model);

        jtLanes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (jtLanes.getSelectedRow() > -1) {
                    if (!twiceinarow) {
                        if (jtComparisons.getSelectionPath() == null) {
                            viewlane();
                        }

                    }
                    twiceinarow = !twiceinarow;
                }
                if (jtLanes.getSelectedRow() > 0) {

                    jbEditlane.setEnabled(true);
                    jbDeleteLane.setEnabled(true);
                    jbShowMW.setEnabled(true);
                    jbFindSimilarLanes.setEnabled(true);
                }
            }
        });

        TableColumn countriescolumn = jtLanes.getColumnModel().getColumn(3);
        countriescolumn.setCellEditor(new DefaultCellEditor(generatecountriescombobox()));

    }

    public void updatelanes(ArrayList<LaneDB> newlanes) {

        additionalfields = getAdditionalFields(studyid);

        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

        String[] names = new String[names0.length + additionalfields.size()];
        for (int i = 0; i < names0.length; i++) {
            names[i] = names0[i];
        }
        for (int i = 0; i < additionalfields.size(); i++) {
            names[i + names0.length] = additionalfields.get(i);
        }

        Object[][] data = new Object[newlanes.size()][names.length];

        // We update the table
        for (int i = 0; i < newlanes.size(); i++) {
            data[i][0] = newlanes.get(i).getGenus();
            data[i][1] = newlanes.get(i).getSpecies();
            data[i][2] = newlanes.get(i).getStrainNumber();
            data[i][3] = newlanes.get(i).getCountry();
            data[i][4] = newlanes.get(i).getMSLT();
            data[i][5] = newlanes.get(i).isIsMarker();
            data[i][6] = newlanes.get(i).getExperiment().getName();
            data[i][7] = newlanes.get(i).getComments();
            for (int j = 0; j < additionalfields.size(); j++) {
                data[i][8 + j] = newlanes.get(i).getLaneaddedinfo().get(additionalfields.get(j));
            }
        }

        // Operations to change the model, we disable the edition of the date
        DefaultTableModel model = new DefaultTableModel(data, names) {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                if (mColIndex == 6 || mColIndex == 5) {
                    return false;
                } else {
                    return true;
                }
            }

            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return java.lang.Boolean.class;
                } else {
                    return java.lang.Object.class;
                }

            }
        };

        model.addTableModelListener(this);
        jtLanes.setModel(model);

        jtLanes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (jtLanes.getSelectedRow() > -1) {
                    if (!twiceinarow) {
                        if (jtComparisons.getSelectionPath() == null) {
                            viewlane();
                        }

                    }
                    twiceinarow = !twiceinarow;
                }

                if (jtLanes.getSelectedRow() > 0) {

                    jbEditlane.setEnabled(true);
                    jbDeleteLane.setEnabled(true);
                    jbShowMW.setEnabled(true);
                    jbFindSimilarLanes.setEnabled(true);
                }
            }
        });

        TableColumn countriescolumn = jtLanes.getColumnModel().getColumn(3);
        countriescolumn.setCellEditor(new DefaultCellEditor(generatecountriescombobox()));

    }

    private JComboBox generatecountriescombobox() {
        JComboBox comboBox = new JComboBox();
        String[] locales = Locale.getISOCountries();
        ArrayList<String> countries = new ArrayList<>();
        for (String countryCode : locales) {
            Locale obj = new Locale("", countryCode);
            countries.add(obj.getDisplayCountry());
        }
        Collections.sort(countries);
        for (String country : countries) {
            comboBox.addItem(country);
        }

        return comboBox;
    }

    @Override
    public void tableChanged(TableModelEvent e) {

        /// Modification in Lanes table 
        if (jtLanes.getModel().toString().equals(e.getSource().toString())) {

            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column <= 7) {
                Object cellValue = jtLanes.getValueAt(row, column);
                updateLane(lanes.get(row).getId(), cellValue, column);
            } else {
                String cellValue = String.valueOf(jtLanes.getValueAt(row, column));
                lanes.get(row).addnewinfo(additionalfields.get(column - 8), cellValue);
                if (containadditionalfield(lanes.get(row).getId(), additionalfields.get(column - 8))) {
                    updateLane(lanes.get(row));
                } else {
                    insertLaneAddedInfo(lanes.get(row).getId(), additionalfields.get(column - 8), cellValue);
                }

            }

            updateselectedlanes();

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jsMainPane = new javax.swing.JSplitPane();
        jspLeftPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtExperiments = new javax.swing.JTree();
        jToolBar1 = new javax.swing.JToolBar();
        jbNewExp = new javax.swing.JButton();
        jbDuplicateExp = new javax.swing.JButton();
        jbimportexpfile = new javax.swing.JButton();
        jbImport = new javax.swing.JButton();
        jbSearchExp = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jbNewComp = new javax.swing.JButton();
        jbDuplicateComp = new javax.swing.JButton();
        jbSearchComp = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtComparisons = new javax.swing.JTree();
        jspRightPane = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtLanes = new javax.swing.JTable();
        jToolBar3 = new javax.swing.JToolBar();
        jbeditexpcomp = new javax.swing.JButton();
        jbDeleteExp = new javax.swing.JButton();
        jbexport = new javax.swing.JButton();
        jbmodifycomp = new javax.swing.JButton();
        jbdeletecomp = new javax.swing.JButton();
        jbViewComp = new javax.swing.JButton();
        jbExportNewick = new javax.swing.JButton();
        jbSimMatrix = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jbEditlane = new javax.swing.JButton();
        jbDeleteLane = new javax.swing.JButton();
        jbFindSimilarLanes = new javax.swing.JButton();
        jbShowMW = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jbAddField = new javax.swing.JButton();
        jbdeletefield = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jxsearchlane = new org.jdesktop.swingx.JXSearchField();
        jbAdvancedSearchLanes = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jScrollPane4 = new javax.swing.JScrollPane();
        jPanel5 = new javax.swing.JPanel();
        jToolBar5 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSave = new javax.swing.JButton();
        jXImageView2 = new org.jdesktop.swingx.JXImageView();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jmiNewStudy = new javax.swing.JMenuItem();
        jmOpenStudy = new javax.swing.JMenuItem();
        jmiEditStudy = new javax.swing.JMenuItem();
        jmiDeleteStudy = new javax.swing.JMenuItem();
        jmiCloseStudy = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jmiExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jmannotate = new javax.swing.JMenu();
        jmiUnfinished = new javax.swing.JMenuItem();
        jAnnotateItem = new javax.swing.JMenuItem();
        jMenuGenetic = new javax.swing.JMenu();
        jmiAddPopulationField = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jmiExportGenetics = new javax.swing.JMenuItem();
        jmMarkers = new javax.swing.JMenu();
        jmiaddmarker = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuMemory = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GelJ");

        jsMainPane.setDividerLocation(150);

        jspLeftPane.setDividerLocation(200);
        jspLeftPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Experiments"));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jtExperiments.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jtExperiments.setRootVisible(false);
        jScrollPane2.setViewportView(jtExperiments);

        jToolBar1.setRollover(true);

        jbNewExp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/experiment-add1b.png"))); // NOI18N
        jbNewExp.setToolTipText("New Experiment");
        jbNewExp.setEnabled(false);
        jbNewExp.setFocusable(false);
        jbNewExp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbNewExp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbNewExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbNewExpActionPerformed(evt);
            }
        });
        jToolBar1.add(jbNewExp);

        jbDuplicateExp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/experiment-duplicate.png"))); // NOI18N
        jbDuplicateExp.setToolTipText("Duplicate Experiment");
        jbDuplicateExp.setEnabled(false);
        jbDuplicateExp.setFocusable(false);
        jbDuplicateExp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbDuplicateExp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbDuplicateExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDuplicateExpActionPerformed(evt);
            }
        });
        jToolBar1.add(jbDuplicateExp);

        jbimportexpfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/experiment-importb.png"))); // NOI18N
        jbimportexpfile.setToolTipText("Import Experiment from File");
        jbimportexpfile.setEnabled(false);
        jbimportexpfile.setFocusable(false);
        jbimportexpfile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbimportexpfile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbimportexpfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbimportexpfileActionPerformed(evt);
            }
        });
        jToolBar1.add(jbimportexpfile);

        jbImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/experiment-import2b.png"))); // NOI18N
        jbImport.setToolTipText("Import Experiment from Study");
        jbImport.setEnabled(false);
        jbImport.setFocusable(false);
        jbImport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbImport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbImportActionPerformed(evt);
            }
        });
        jToolBar1.add(jbImport);

        jbSearchExp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png"))); // NOI18N
        jbSearchExp.setToolTipText("Search Experiments");
        jbSearchExp.setEnabled(false);
        jbSearchExp.setFocusable(false);
        jbSearchExp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbSearchExp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbSearchExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSearchExpActionPerformed(evt);
            }
        });
        jToolBar1.add(jbSearchExp);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
        );

        jspLeftPane.setTopComponent(jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Comparisons"));

        jToolBar2.setRollover(true);

        jbNewComp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/comparison-addb.png"))); // NOI18N
        jbNewComp.setToolTipText("New Comparison");
        jbNewComp.setEnabled(false);
        jbNewComp.setFocusable(false);
        jbNewComp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbNewComp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbNewComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbNewCompActionPerformed(evt);
            }
        });
        jToolBar2.add(jbNewComp);

        jbDuplicateComp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/comparison-duplicate.png"))); // NOI18N
        jbDuplicateComp.setToolTipText("Duplicate Comparison");
        jbDuplicateComp.setEnabled(false);
        jbDuplicateComp.setFocusable(false);
        jbDuplicateComp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbDuplicateComp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbDuplicateComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDuplicateCompActionPerformed(evt);
            }
        });
        jToolBar2.add(jbDuplicateComp);

        jbSearchComp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png"))); // NOI18N
        jbSearchComp.setToolTipText("Search Comparisons");
        jbSearchComp.setEnabled(false);
        jbSearchComp.setFocusable(false);
        jbSearchComp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbSearchComp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbSearchComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSearchCompActionPerformed(evt);
            }
        });
        jToolBar2.add(jbSearchComp);

        treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jtComparisons.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jtComparisons.setMaximumSize(new java.awt.Dimension(20, 38));
        jtComparisons.setRootVisible(false);
        jScrollPane3.setViewportView(jtComparisons);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane3)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
        );

        jspLeftPane.setRightComponent(jPanel2);

        jsMainPane.setLeftComponent(jspLeftPane);

        jspRightPane.setDividerLocation(500);
        jspRightPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Lanes"));

        jtLanes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"
            }
        ));
        jScrollPane1.setViewportView(jtLanes);

        jToolBar3.setRollover(true);

        jbeditexpcomp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/experiment-edit.png"))); // NOI18N
        jbeditexpcomp.setToolTipText("Modify Experiment Properties");
        jbeditexpcomp.setEnabled(false);
        jbeditexpcomp.setFocusable(false);
        jbeditexpcomp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbeditexpcomp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbeditexpcomp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbeditexpcompActionPerformed(evt);
            }
        });
        jToolBar3.add(jbeditexpcomp);

        jbDeleteExp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/experiment-delete.png"))); // NOI18N
        jbDeleteExp.setToolTipText("Delete Experiment");
        jbDeleteExp.setEnabled(false);
        jbDeleteExp.setFocusable(false);
        jbDeleteExp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbDeleteExp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbDeleteExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteExpActionPerformed(evt);
            }
        });
        jToolBar3.add(jbDeleteExp);

        jbexport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/experiment-export.png"))); // NOI18N
        jbexport.setToolTipText("Export experiment");
        jbexport.setEnabled(false);
        jbexport.setFocusable(false);
        jbexport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbexport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbexport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbexportActionPerformed(evt);
            }
        });
        jToolBar3.add(jbexport);

        jbmodifycomp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/comparison-edit.png"))); // NOI18N
        jbmodifycomp.setToolTipText("Edit comparison");
        jbmodifycomp.setEnabled(false);
        jbmodifycomp.setFocusable(false);
        jbmodifycomp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbmodifycomp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbmodifycomp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbmodifycompActionPerformed(evt);
            }
        });
        jToolBar3.add(jbmodifycomp);

        jbdeletecomp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/comparison-delete.png"))); // NOI18N
        jbdeletecomp.setToolTipText("Delete comparison");
        jbdeletecomp.setEnabled(false);
        jbdeletecomp.setFocusable(false);
        jbdeletecomp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbdeletecomp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbdeletecomp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbdeletecompActionPerformed(evt);
            }
        });
        jToolBar3.add(jbdeletecomp);

        jbViewComp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/comparison-view.png"))); // NOI18N
        jbViewComp.setToolTipText("View Comparison");
        jbViewComp.setEnabled(false);
        jbViewComp.setFocusable(false);
        jbViewComp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbViewComp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbViewComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbViewCompActionPerformed(evt);
            }
        });
        jToolBar3.add(jbViewComp);

        jbExportNewick.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/newick.png"))); // NOI18N
        jbExportNewick.setToolTipText("Export Dendrogram to Newick format");
        jbExportNewick.setEnabled(false);
        jbExportNewick.setFocusable(false);
        jbExportNewick.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbExportNewick.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbExportNewick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbExportNewickActionPerformed(evt);
            }
        });
        jToolBar3.add(jbExportNewick);

        jbSimMatrix.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/simmatrix.png"))); // NOI18N
        jbSimMatrix.setToolTipText("Export Similarity Matrix");
        jbSimMatrix.setEnabled(false);
        jbSimMatrix.setFocusable(false);
        jbSimMatrix.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbSimMatrix.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbSimMatrix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSimMatrixActionPerformed(evt);
            }
        });
        jToolBar3.add(jbSimMatrix);
        jToolBar3.add(jSeparator1);

        jbEditlane.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/lane_edit.png"))); // NOI18N
        jbEditlane.setToolTipText("Edit bands of a lane");
        jbEditlane.setEnabled(false);
        jbEditlane.setFocusable(false);
        jbEditlane.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbEditlane.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbEditlane.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbEditlaneActionPerformed(evt);
            }
        });
        jToolBar3.add(jbEditlane);

        jbDeleteLane.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/lane_delete.png"))); // NOI18N
        jbDeleteLane.setToolTipText("Remove Lane from Comparison");
        jbDeleteLane.setEnabled(false);
        jbDeleteLane.setFocusable(false);
        jbDeleteLane.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbDeleteLane.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbDeleteLane.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteLaneActionPerformed(evt);
            }
        });
        jToolBar3.add(jbDeleteLane);

        jbFindSimilarLanes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/lane-similarity.png"))); // NOI18N
        jbFindSimilarLanes.setToolTipText("Find Similar Lanes");
        jbFindSimilarLanes.setEnabled(false);
        jbFindSimilarLanes.setFocusable(false);
        jbFindSimilarLanes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbFindSimilarLanes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbFindSimilarLanes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFindSimilarLanesActionPerformed(evt);
            }
        });
        jToolBar3.add(jbFindSimilarLanes);

        jbShowMW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/standards.png"))); // NOI18N
        jbShowMW.setToolTipText("Show Molecular Weights");
        jbShowMW.setEnabled(false);
        jbShowMW.setFocusable(false);
        jbShowMW.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbShowMW.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbShowMW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbShowMWActionPerformed(evt);
            }
        });
        jToolBar3.add(jbShowMW);
        jToolBar3.add(jSeparator2);

        jbAddField.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/addfieldb.png"))); // NOI18N
        jbAddField.setToolTipText("Add Field");
        jbAddField.setEnabled(false);
        jbAddField.setFocusable(false);
        jbAddField.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbAddField.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbAddField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddFieldActionPerformed(evt);
            }
        });
        jToolBar3.add(jbAddField);

        jbdeletefield.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/deletefield.png"))); // NOI18N
        jbdeletefield.setToolTipText("Delete Field");
        jbdeletefield.setEnabled(false);
        jbdeletefield.setFocusable(false);
        jbdeletefield.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbdeletefield.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbdeletefield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbdeletefieldActionPerformed(evt);
            }
        });
        jToolBar3.add(jbdeletefield);
        jToolBar3.add(jSeparator3);

        jxsearchlane.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jxsearchlane.setEnabled(false);
        jxsearchlane.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jxsearchlaneActionPerformed(evt);
            }
        });
        jxsearchlane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jxsearchlaneKeyReleased(evt);
            }
        });
        jToolBar3.add(jxsearchlane);

        jbAdvancedSearchLanes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png"))); // NOI18N
        jbAdvancedSearchLanes.setToolTipText("Advanced Search of Lanes");
        jbAdvancedSearchLanes.setEnabled(false);
        jbAdvancedSearchLanes.setFocusable(false);
        jbAdvancedSearchLanes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbAdvancedSearchLanes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbAdvancedSearchLanes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAdvancedSearchLanesActionPerformed(evt);
            }
        });
        jToolBar3.add(jbAdvancedSearchLanes);
        jToolBar3.add(jSeparator6);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)
            .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addContainerGap())
        );

        jspRightPane.setTopComponent(jPanel3);

        jToolBar5.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom_in.png"))); // NOI18N
        jButton1.setToolTipText("Zoom in");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar5.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom_out.png"))); // NOI18N
        jButton2.setToolTipText("Zoom out");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar5.add(jButton2);

        jSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GelJImages/save.png"))); // NOI18N
        jSave.setEnabled(false);
        jSave.setFocusable(false);
        jSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveActionPerformed(evt);
            }
        });
        jToolBar5.add(jSave);

        javax.swing.GroupLayout jXImageView2Layout = new javax.swing.GroupLayout(jXImageView2);
        jXImageView2.setLayout(jXImageView2Layout);
        jXImageView2Layout.setHorizontalGroup(
            jXImageView2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jXImageView2Layout.setVerticalGroup(
            jXImageView2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 227, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar5, javax.swing.GroupLayout.DEFAULT_SIZE, 783, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jXImageView2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jToolBar5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jXImageView2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane4.setViewportView(jPanel5);

        jspRightPane.setRightComponent(jScrollPane4);

        jsMainPane.setRightComponent(jspRightPane);

        jMenu1.setText("File");

        jmiNewStudy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jmiNewStudy.setText("New Study");
        jmiNewStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiNewStudyActionPerformed(evt);
            }
        });
        jMenu1.add(jmiNewStudy);

        jmOpenStudy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jmOpenStudy.setText("Open Study");
        jmOpenStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmOpenStudyActionPerformed(evt);
            }
        });
        jMenu1.add(jmOpenStudy);

        jmiEditStudy.setText("Edit Study");
        jmiEditStudy.setEnabled(false);
        jmiEditStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiEditStudyActionPerformed(evt);
            }
        });
        jMenu1.add(jmiEditStudy);

        jmiDeleteStudy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, java.awt.event.InputEvent.CTRL_MASK));
        jmiDeleteStudy.setText("Delete Study");
        jmiDeleteStudy.setEnabled(false);
        jmiDeleteStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiDeleteStudyActionPerformed(evt);
            }
        });
        jMenu1.add(jmiDeleteStudy);

        jmiCloseStudy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jmiCloseStudy.setText("Close Study");
        jmiCloseStudy.setEnabled(false);
        jmiCloseStudy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiCloseStudyActionPerformed(evt);
            }
        });
        jMenu1.add(jmiCloseStudy);
        jMenu1.add(jSeparator5);

        jmiExit.setText("Exit");
        jmiExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiExitActionPerformed(evt);
            }
        });
        jMenu1.add(jmiExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tools");

        jmannotate.setText("Experiments");

        jmiUnfinished.setText("Unfinished Experiments");
        jmiUnfinished.setEnabled(false);
        jmiUnfinished.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiUnfinishedActionPerformed(evt);
            }
        });
        jmannotate.add(jmiUnfinished);

        jAnnotateItem.setText("Annotate Experiment");
        jAnnotateItem.setEnabled(false);
        jAnnotateItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAnnotateItemActionPerformed(evt);
            }
        });
        jmannotate.add(jAnnotateItem);

        jMenu2.add(jmannotate);

        jMenuGenetic.setText("Population Genetics");

        jmiAddPopulationField.setText("Add population field");
        jmiAddPopulationField.setEnabled(false);
        jmiAddPopulationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiAddPopulationFieldActionPerformed(evt);
            }
        });
        jMenuGenetic.add(jmiAddPopulationField);
        jMenuGenetic.add(jSeparator4);

        jmiExportGenetics.setText("Export Data (occurrence matrix)");
        jmiExportGenetics.setEnabled(false);
        jmiExportGenetics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiExportGeneticsActionPerformed(evt);
            }
        });
        jMenuGenetic.add(jmiExportGenetics);

        jMenu2.add(jMenuGenetic);

        jmMarkers.setText("Markers");

        jmiaddmarker.setText("Add Marker");
        jmiaddmarker.setEnabled(false);
        jmiaddmarker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiaddmarkerActionPerformed(evt);
            }
        });
        jmMarkers.add(jmiaddmarker);

        jMenuItem5.setText("View Markers");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jmMarkers.add(jMenuItem5);

        jMenu2.add(jmMarkers);

        jMenu4.setText("Backups");

        jMenuItem1.setText("Create backup");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem1);

        jMenuItem2.setText("Restore backup");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem2);

        jMenu2.add(jMenu4);

        jMenuMemory.setText("Increase Memory");
        jMenuMemory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuMemoryActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuMemory);

        jMenuBar1.add(jMenu2);

        jMenu5.setText("Help");

        jMenuItem7.setText("GelJ webpage");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem7);

        jMenuItem8.setText("GelJ manual");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem8);

        jMenuItem6.setText("About");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem6);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jsMainPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jsMainPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbDeleteExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteExpActionPerformed

        deleteexp();


    }//GEN-LAST:event_jbDeleteExpActionPerformed

    private void jbDuplicateCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDuplicateCompActionPerformed
        duplicatecomparison();
        activateemptytoolbar();
    }//GEN-LAST:event_jbDuplicateCompActionPerformed

    private void deletecomp() {

        TreePath currentSelection = jtComparisons.getSelectionPath();
        if (currentSelection != null) {
            int reply = JOptionPane.showConfirmDialog(this, "Do you want to remove this comparison? ", "Delete comparison", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
                MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
                if (parent != null) {
                    Comparison comp = (Comparison) currentNode.getUserObject();
                    deletecomparison(comp.getId());
                    lanes = new ArrayList<>();

                    lanes = getLanesComparison(comp.getId());
                    updatelanes(false);
//                    treeModelComp.removeNodeFromParent(currentNode);
                    updatecomparisons(true, true);
                    cleanlanes();
                    disablelaneoptions();

                    double scale = jXImageView2.getScale();
                    jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
                    jXImageView2.setScale(scale);
                    jToolBar3.setVisible(false);
                    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
                }
            }
        }
    }

    private void deleteexp() {
        TreePath currentSelection = jtExperiments.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
            if (parent != null) {

                Experiment exp = (Experiment) currentNode.getUserObject();

                boolean ce = existscomparisonsexperiment(exp.getId());
                if (ce) {
                    int reply = JOptionPane.showConfirmDialog(this, "Some comparisons include lanes of this experiment.\n"
                            + " If you remove the experiment, such comparisons will also be removed. \n"
                            + "Do you want to remove this experiment?", "Delete experiment", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {

                        deletecomparisonsexperiment(exp.getId());
                        deleteexperiment(exp.getId());
                        updateexperiments(true, true);
                        updatecomparisons(true, true);
                        File folder = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + getUser(userid).getUsername() + "/experiments/" + exp.getId());
                        Utilities.deleteDir(folder);

                        cleanlanes();
                        double scale = jXImageView2.getScale();
                        jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
                        jXImageView2.setScale(scale);
                        jToolBar3.setVisible(false);
                        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
                    }

                } else {
                    int reply = JOptionPane.showConfirmDialog(this, "Do you want to remove this experiment? "
                            + "This will remove all the lanes of the experiment", "Delete experiment", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {
                        deleteexperiment(exp.getId());
                        lanes = new ArrayList<>();

                        lanes = getLanesComparison(exp.getId());
                        updatelanes(false);
                        updateexperiments(true, true);
                        File folder = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + getUser(userid).getUsername() + "/experiments/" + exp.getId());
                        Utilities.deleteDir(folder);

                        cleanlanes();
                        double scale = jXImageView2.getScale();
                        jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
                        jXImageView2.setScale(scale);
                        jToolBar3.setVisible(false);
                        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
//                        treeModel.removeNodeFromParent(currentNode);
                    }
                }

            }
        }

    }


    private void jxsearchlaneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jxsearchlaneKeyReleased
        searchlanes();
    }//GEN-LAST:event_jxsearchlaneKeyReleased

    private void jbAddFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddFieldActionPerformed
        String fieldName = JOptionPane.showInputDialog("Introduce the name of the new field");
        if (additionalfields.contains(fieldName)) {
            JOptionPane.showMessageDialog(this, "There is already a field with that name", "", INFORMATION_MESSAGE, null);
        } else {
            additionalfields.add(fieldName);
            updatelanes(false);
        }


    }//GEN-LAST:event_jbAddFieldActionPerformed

    private void jbdeletefieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbdeletefieldActionPerformed
        String[] fields = new String[additionalfields.size()];
        if (additionalfields.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There is not any additional field to remove", "Field removal", JOptionPane.INFORMATION_MESSAGE);
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = additionalfields.get(i);
        }
        String selectedfield = (String) JOptionPane.showInputDialog(this, "Select the field to remove", "", INFORMATION_MESSAGE, null, fields, fields[0]);
        additionalfields.remove(selectedfield);
        updatelanes(false);
    }//GEN-LAST:event_jbdeletefieldActionPerformed

    private void showall() {
        updatelanes(true);
        jtComparisons.setSelectionPath(null);
        jtExperiments.setSelectionPath(null);

//        jbAdd.setEnabled(true);
        jbDeleteLane.setEnabled(false);
        jbDeleteLane.setVisible(false);
    }


    private void jbAdvancedSearchLanesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAdvancedSearchLanesActionPerformed

        SearchLanes sl = new SearchLanes(this);
        sl.setVisible(true);


    }//GEN-LAST:event_jbAdvancedSearchLanesActionPerformed

    private void addlanetocomparison() {
        int[] row = jtLanes.getSelectedRows();

        if (row.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least a lane to add", "", INFORMATION_MESSAGE, null);
            return;
        }

        ArrayList<LaneDB> selectedlanes = new ArrayList<>();
        for (int i = 0; i < row.length; i++) {
            selectedlanes.add(templanes.get(row[i]));
        }

        if (comparisons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First of all, you need to create a comparison.", "", INFORMATION_MESSAGE, null);
        } else {

            Comparison[] comps = new Comparison[comparisons.size()];
            for (int i = 0; i < comps.length; i++) {
                comps[i] = comparisons.get(i);
            }

            Comparison selected = (Comparison) JOptionPane.showInputDialog(this, "Select a comparison", "Select a comparison", INFORMATION_MESSAGE, null, comps, comps[0]);

            ArrayList<LaneDB> temp = selectedlanes;

            temp.addAll(selected.getLanes());

            if (!allsamemarker(temp)) {
                JOptionPane.showMessageDialog(this, "Not all the lanes have been normalised using the same marker", "", INFORMATION_MESSAGE, null);
                return;
            }

            insertComparisonLane(selected.getId(), selectedlanes);
            updatecomparisons(true, false);
        }

    }


    private void jbDeleteLaneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteLaneActionPerformed

        int row = jtLanes.getSelectedRow();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
        if (node == null) //Nothing is selected.     
        {
            return;
        }
        Comparison comp = (Comparison) node.getUserObject();

        int compid = comp.getId();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a lane first", "", INFORMATION_MESSAGE, null);
        } else {
            int reply = JOptionPane.showConfirmDialog(this, "Do you want to remove this lane? ", "Delete lane", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                deletelanecomparison(lanes.get(row).getId(), compid);
                lanes = getLanesComparison(compid);
                updatelanes(false);
            }
        }

        updatecomparisons(true, false);


    }//GEN-LAST:event_jbDeleteLaneActionPerformed

    private void jbSearchCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSearchCompActionPerformed
        SearchComparison sc = new SearchComparison(this);
        sc.setVisible(true);
    }//GEN-LAST:event_jbSearchCompActionPerformed

    private void jbNewCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbNewCompActionPerformed
        double scale = jXImageView2.getScale();
        jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
        jXImageView2.setScale(scale);
        try {
            createnewcomparison();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        activateemptytoolbar();
    }//GEN-LAST:event_jbNewCompActionPerformed

    private void jxsearchlaneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jxsearchlaneActionPerformed
        searchlanes();
    }//GEN-LAST:event_jxsearchlaneActionPerformed

    private void jmiExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jmiExitActionPerformed

    private void jmiNewStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiNewStudyActionPerformed

        NewStudy ns = new NewStudy(this, true, false, this);
        ns.setVisible(true);
        if (studyid == -1) {
            return;
        }
        enablefromopenornewstudy();
        initialiseexperimentstree();
        initialisecomparisonstree();
        updateexperiments(true, true);
        updatecomparisons(true, true);


    }//GEN-LAST:event_jmiNewStudyActionPerformed

    private void jmiCloseStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiCloseStudyActionPerformed
        disablefromclosestudy();

    }//GEN-LAST:event_jmiCloseStudyActionPerformed

    private void jmOpenStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmOpenStudyActionPerformed

        ArrayList<Study> studies = getStudies(userid);

        if (studies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have not created any study previously", "", JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }

        Study s = (Study) JOptionPane.showInputDialog(this, "Select a study", "Open study", JOptionPane.QUESTION_MESSAGE, null, studies.toArray(), studies.get(0));

        if (s == null) {
            return;
        }

        studyid = s.getId();
        enablefromopenornewstudy();
        initialiseexperimentstree();
        initialisecomparisonstree();

        updateexperiments(true, true);
        updatecomparisons(true, true);
        double scale = jXImageView2.getScale();
        jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
        jXImageView2.setScale(scale);
        this.setTitle("GelJ (" + s.getName() + " )");
    }//GEN-LAST:event_jmOpenStudyActionPerformed

    private void jmiEditStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiEditStudyActionPerformed

        NewStudy ns = new NewStudy(this, true, true, this);
        ns.setVisible(true);


    }//GEN-LAST:event_jmiEditStudyActionPerformed

    private void jmiDeleteStudyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiDeleteStudyActionPerformed
        int reply = JOptionPane.showConfirmDialog(this, "Do you want to delete this study?", "Delete study", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            int id = studyid;
            deletestudy(id);

            File folder = new File(System.getProperty("user.dir") + "/GelJDBImages/" + id);
            Utilities.deleteDir(folder);
            disablefromclosestudy();
            double scale = jXImageView2.getScale();
            jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
            jXImageView2.setScale(scale);
        }


    }//GEN-LAST:event_jmiDeleteStudyActionPerformed

    private void jbEditlaneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbEditlaneActionPerformed
        try {
            disableall();
            editlane();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jbEditlaneActionPerformed

    private void jbViewCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbViewCompActionPerformed
        viewcomparison();
    }//GEN-LAST:event_jbViewCompActionPerformed

    private void jbFindSimilarLanesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbFindSimilarLanesActionPerformed
        findsimilarlanes();
    }//GEN-LAST:event_jbFindSimilarLanesActionPerformed

    private void jmiUnfinishedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiUnfinishedActionPerformed
        try {
            continueunfinished();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jmiUnfinishedActionPerformed

    private void jbeditexpcompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbeditexpcompActionPerformed
        editexperiment();
    }//GEN-LAST:event_jbeditexpcompActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        double scale = jXImageView2.getScale();

        jXImageView2.setScale(scale * 1.25);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        double scale = jXImageView2.getScale();
        jXImageView2.setScale(scale * 0.75);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        createbackup();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        disablefromclosestudy();
        int option = JOptionPane.showConfirmDialog(this, "If you restore a backup, all the new changes introduced in the\n"
                + "database will be lost. Do you really want to restore a backup?", "Recover backup", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            try {
                restorebackup();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jbexportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbexportActionPerformed
        TreePath currentSelection = jtExperiments.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
            if (parent != null) {

                Experiment exp = (Experiment) currentNode.getUserObject();
                try {
                    exportexperiment(exp);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

    }//GEN-LAST:event_jbexportActionPerformed

    private void jbmodifycompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbmodifycompActionPerformed
        editcomparison();
    }//GEN-LAST:event_jbmodifycompActionPerformed

    private void jbdeletecompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbdeletecompActionPerformed
        deletecomp();
    }//GEN-LAST:event_jbdeletecompActionPerformed

    private void jAnnotateItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAnnotateItemActionPerformed
        TreePath currentSelection = jtExperiments.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
            if (parent != null) {

                Experiment experiment = (Experiment) currentNode.getUserObject();
                String image = System.getProperty("user.dir") + "/GelJDBImages/"
                        + experiment.getStudy().getId()
                        + "/" + experiment.getUser().getUsername()
                        + "/experiments/" + experiment.getId() + "/" + experiment.getImagename();

                if (ij == null) {
                    ij = new ImageJ(NO_SHOW);
                }

                ImagePlus imp = WindowManager.getCurrentImage();
                if (imp != null) {
                    imp.close();
                }

                imp = IJ.openImage(image);
                imp.show();
                ImageAnnotation ia = new ImageAnnotation(imp, this);

            }
        } else {
            JOptionPane.showMessageDialog(this, "You must select an experiment to annotate.");
        }
    }//GEN-LAST:event_jAnnotateItemActionPerformed

    private void jmiaddmarkerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiaddmarkerActionPerformed
        Menu m = new Menu();
        if (ij == null) {
            ij = new ImageJ(NO_SHOW);
        }

        disableall();
        OpenDialog od = new OpenDialog("Load an image...");
        Opener o = new Opener();

        if (od.getPath() != null) {
            ImagePlus imp1 = o.openImage(od.getPath());
            if (imp1 == null) {
                JOptionPane.showMessageDialog(m, "Select a valid image file", "Error", JOptionPane.ERROR_MESSAGE);
                enableall();
                return;
            }
            imp1.show();
            IJ.run(imp1, "Scale to Fit", "");
            imp1.getWindow().setLocked(true);

            m.setWindowname(imp1.getTitle());
            ImageWindow iw = imp1.getWindow();
            iw.setLocked(false);
            LaneManagerCanvas cc = new LaneManagerCanvas(imp1);
            ImageWindow iw2 = new ImageWindow(imp1, cc);
            iw2.setLocked(true);
            iw.dispose();

            MarkerSelection_1 s = new MarkerSelection_1(imp1, null, m, this);

            imp1.getWindow().toFront();

        } else {
            enableall();
        }


    }//GEN-LAST:event_jmiaddmarkerActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        ViewMarkers vm = new ViewMarkers(this, true);
        vm.setVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        About about = new About(this, true);
        about.setVisible(true);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        try {
            //Set your page url in this string. For eg, I m using URL for Google Search engine
            String url = "http://sourceforge.net/projects/gelj/";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        Manual manual = new Manual(this, true);
        manual.setVisible(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveActionPerformed
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
        if (node == null) //Nothing is selected.     
        {
            JOptionPane.showMessageDialog(this, "This option is only available to save dendrograms", "Save", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Comparison comp = (Comparison) node.getUserObject();

        // Here
        String image = System.getProperty("user.dir") + "/GelJDBImages/"
                + comp.getStudy().getId()
                + "/" + comp.getStudy().getUser().getUsername()
                + "/comparisons/" + comp.getId() + "/dendrogram.tif";

        ImagePlus imp = new ImagePlus(image);
        IJ.saveAsTiff(imp, "");
        imp.close();


    }//GEN-LAST:event_jSaveActionPerformed

    private void jbShowMWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbShowMWActionPerformed
        try {
            showMWofLane();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jbShowMWActionPerformed

    private void jMenuMemoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuMemoryActionPerformed
        Memory m = new Memory();
        m.run("");
    }//GEN-LAST:event_jMenuMemoryActionPerformed

    private void jbExportNewickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbExportNewickActionPerformed
        try {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
            if (node == null) //Nothing is selected.
            {
                JOptionPane.showMessageDialog(this, "Select a comparison to edit", "", INFORMATION_MESSAGE, null);
                return;
            }
            Comparison comp = (Comparison) node.getUserObject();

            String newickfile = System.getProperty("user.dir") + "/GelJDBImages/"
                    + comp.getStudy().getId()
                    + "/" + comp.getStudy().getUser().getUsername()
                    + "/comparisons/" + comp.getId() + "/newick.txt";

            File f = new File(newickfile);

            Path p = Paths.get(f.getPath());
            Charset c = Charset.defaultCharset();
            List<String> lines = Files.readAllLines(p, c);

            if (f.exists()) {

                SaveDialog sd = new SaveDialog("Export comparison to newick and nexus format...", "tree", ".txt");
                String dir = sd.getDirectory();
                String name = sd.getFileName();
                if (dir == null || name == null) {
                    return;
                }

                File fout = new File(dir + "newick_" + name);
                File nexusfile = new File(dir + "nexus_" + name);

                PrintWriter writer = new PrintWriter(dir + "nexus_" + name, "UTF-8");
                writer.println("#NEXUS");
                writer.println("BEGIN TREES;");
                writer.println("  TREE tree1 = " + lines.get(0));
                writer.println("END;");
                writer.close();

                try {
                    Files.copy(f.toPath(), fout.toPath(), REPLACE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                JOptionPane.showMessageDialog(null,
                        "This comparison was created with a previous version of GelJ\n"
                        + "and hence the newick file was not generated. To solve this problem\n "
                        + "use the \"Edit Comparison\" button but without modifying any parameter.\n"
                        + "This will create the newick file and you will be able to import it.", "Export dendrogram to newick", INFORMATION_MESSAGE, null);
                return;
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_jbExportNewickActionPerformed

    private void jbSimMatrixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSimMatrixActionPerformed
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
        if (node == null) //Nothing is selected.     
        {
            JOptionPane.showMessageDialog(this, "Select a comparison to edit", "", INFORMATION_MESSAGE, null);
            return;
        }
        Comparison comp = (Comparison) node.getUserObject();

        String[] options = {"Similarity matrix", "Distance matrix"};
        int choice = JOptionPane.showOptionDialog(this, "Type of matrix", "Export Matrix", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, "Similarity matrix");

        String matrix;
        String type;

        if (choice == 0) {
            matrix = System.getProperty("user.dir") + "/GelJDBImages/"
                    + comp.getStudy().getId()
                    + "/" + comp.getStudy().getUser().getUsername()
                    + "/comparisons/" + comp.getId() + "/simmatrix.xls";
            type = "similarityMatrix";
        } else {
            matrix = System.getProperty("user.dir") + "/GelJDBImages/"
                    + comp.getStudy().getId()
                    + "/" + comp.getStudy().getUser().getUsername()
                    + "/comparisons/" + comp.getId() + "/distancematrix.xls";
            type = "distanceMatrix";
        }

        File f = new File(matrix);

        if (f.exists()) {

            SaveDialog sd = new SaveDialog("Export matrix", type, ".xls");
            String dir = sd.getDirectory();
            String name = sd.getFileName();
            if (dir == null || name == null) {
                return;
            }

            File fout = new File(dir + name);
            try {
                Files.copy(f.toPath(), fout.toPath(), REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            JOptionPane.showMessageDialog(null,
                    "This comparison was created with a previous version of GelJ\n"
                    + "and hence the similarity matrix was not generated. To solve this problem\n "
                    + "use the \"Edit Comparison\" button but without modifying any parameter.\n"
                    + "This will create the similarity matrix and you will be able to import it.", "Export similarity matrix", INFORMATION_MESSAGE, null);
            return;
        }
    }//GEN-LAST:event_jbSimMatrixActionPerformed

    private void jmiExportGeneticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiExportGeneticsActionPerformed

        if (experiments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Create an experiment first", "", INFORMATION_MESSAGE, null);
            return;
        }

        NewComparison nc = new NewComparison(studyid);
        nc.setNonSelectedExperiments(experiments);
        ArrayList<String> fields = new ArrayList<>();

        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

        for (String s : names0) {
            fields.add(s);
        }
        for (String s : additionalfields) {
            fields.add(s);
        }

        nc.setNonSelectedFields(fields);

        db.PopulationGenetic.Step1 populationgenetics = new db.PopulationGenetic.Step1(nc, true);
        populationgenetics.setVisible(true);
    }//GEN-LAST:event_jmiExportGeneticsActionPerformed

    private void jmiAddPopulationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiAddPopulationFieldActionPerformed
        if (additionalfields.contains("Population")) {
            JOptionPane.showMessageDialog(this, "You have already added the Population field", "", INFORMATION_MESSAGE, null);
        } else {
            additionalfields.add("Population");
            updatelanes(false);
        }


    }//GEN-LAST:event_jmiAddPopulationFieldActionPerformed

    private void jbSearchExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSearchExpActionPerformed
        SearchExperiments se = new SearchExperiments(this);
        se.setVisible(true);
    }//GEN-LAST:event_jbSearchExpActionPerformed

    private void jbImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbImportActionPerformed

        if (getExperimentsofOtherStudies(studyid).isEmpty()) {
            JOptionPane.showMessageDialog(this, "There is not any experiment to import", "", INFORMATION_MESSAGE, null);
            return;
        }
        ImportExperiment ie = new ImportExperiment(studyid, this);
        ie.setVisible(true);
        enableall();
        activateemptytoolbar();
    }//GEN-LAST:event_jbImportActionPerformed

    private void jbimportexpfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbimportexpfileActionPerformed

        OpenDialog od = new OpenDialog("Import an experiment...");
        String path = od.getPath();
        if (!path.endsWith(".zip")) {
            JOptionPane.showMessageDialog(null, "You must select a zip file", "Import experiment", JOptionPane.ERROR_MESSAGE);
        } else {
            try {

                unzipFunction(path);
                File experiment = new File(System.getProperty("user.dir") + "/temp-geljdb/experiment.exp");
                File lanesfile = new File(System.getProperty("user.dir") + "/temp-geljdb/experiment.lanes");
                File marker = new File(System.getProperty("user.dir") + "/temp-geljdb/experiment.marker");
                File user = new File(System.getProperty("user.dir") + "/temp-geljdb/experiment.user");
                File addedinfo = new File(System.getProperty("user.dir") + "/temp-geljdb/experiment.adds");

                if (!experiment.exists() || !lanesfile.exists() || !marker.exists() || !user.exists()) {
                    JOptionPane.showMessageDialog(null, "This is not an experiment zip", "Import experiment", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                /// Reading user and lab
                Path p = Paths.get(user.getPath());
                Charset c = Charset.defaultCharset();
                List<String> linesuser = Files.readAllLines(p, c);
                String lineuser = linesuser.get(0).replace("\"", "");
                String[] partsuser = lineuser.split(",");

                Lab l = new Lab(Integer.parseInt(partsuser[5]), partsuser[6], partsuser[7], partsuser[8]);
                User u = new User(Integer.parseInt(partsuser[0]), partsuser[1], partsuser[2], partsuser[3], l);

                /// Reading marker
                p = Paths.get(marker.getPath());
                List<String> linemarker = Files.readAllLines(p, c);
                String markerline = linemarker.get(0).replace("\"", "");
                String[] partsmarker = markerline.split(",");
                File temp = new File(System.getProperty("user.dir") + "/standardfiles/" + partsmarker[1] + ".marker");
                Marker m1 = new Marker(Integer.parseInt(partsmarker[0]), partsmarker[1], temp.getPath());

                /// Reading experiment
                p = Paths.get(experiment.getPath());

                List<String> lines = Files.readAllLines(p, c);
                String expline = lines.get(0).replace("\"", "");
                String[] parts = expline.split(",");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = formatter.parse(parts[2]);
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                Experiment e = new Experiment(Integer.parseInt(parts[0]), parts[1], sqlDate, parts[3], Integer.parseInt(parts[4]), parts[5], getStudy(studyid), m1, u);

                /// Reading lanes added info
                ArrayList<String[]> lanesaddedinfo = new ArrayList<>();
                p = Paths.get(addedinfo.getPath());
                List<String> linesaddedinfolanes = Files.readAllLines(p, c);
                for (String s : linesaddedinfolanes) {
                    String[] partslanes = s.replace("\"", "").split(",");
                    lanesaddedinfo.add(partslanes);
                }

                /// Reading lanes
                p = Paths.get(lanesfile.getPath());
                List<String> lineslanes = Files.readAllLines(p, c);
                ArrayList<LaneDB> lanesDB = new ArrayList<>();

                for (String s : lineslanes) {
                    String[] partslanes = s.replace("\"", "").split(",");
                    LaneDB lane = new LaneDB(Integer.parseInt(partslanes[0]), partslanes[1], partslanes[2], partslanes[3],
                            partslanes[4], partslanes[5], Boolean.parseBoolean(partslanes[6]), partslanes[7], e);
                    for (int i = 0; i < lanesaddedinfo.size(); i++) {
                        if (Integer.parseInt(lanesaddedinfo.get(i)[3]) == lane.getId()) {
                            lane.addnewinfo(lanesaddedinfo.get(i)[1], lanesaddedinfo.get(i)[2]);
                        }
                    }
                    lanesDB.add(lane);
                }

                /// Folder where all information is originally saved
                File folderorigin = new File(System.getProperty("user.dir") + "/temp-geljdb/GelJDBImages/"
                        + parts[6] + "/" + u.getUsername() + "/experiments/" + e.getId() + "/");

                /// Adding lab to the database if it does not exists
                int labid = existslab_id(l.getName(), l.getCity(), l.getCountry());
                if (labid != -1) {
                    l.setId(labid);
                } else {
                    l.setId(lastLabid());
                    insertLab(l);
                }

                /// Adding user to the database if it does not exists
                User u2 = existssuser(u.getName(), u.getSurname());

                if (u2 != null) {
                    int option = JOptionPane.showConfirmDialog(null,
                            "The imported experiment was created by " + u.getName() + " " + u.getSurname()
                            + ".\nThe database already contains a user called " + u.getName() + " " + u.getSurname()
                            + " from " + u2.getLab().getName() + " (" + u2.getLab().getCity() + " - " + u2.getLab().getCountry() + ").\n"
                            + "Are they the same person?",
                            "Same user", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        u = u2;
                    } else {
                        u.setId(lastUserid());
                        insertUser(u);
                    }
                } else {
                    u.setId(lastUserid());
                    insertUser(u);
                }

                /// Adding marker to the database if it does not exists
                if (m1.getId() != 0 && m1.getId() != 1 && m1.getId() != 2) {

                    int mid = existsmarker_id(m1.getName());
                    if (mid != -1) {
                        m1.setId(mid);
                    } else {
                        m1.setId(lastMarkerid());
                        insertMarker(m1);
                        File markerorigin = new File(System.getProperty("user.dir") + "/temp-geljdb/standardfiles/" + m1.getName() + ".marker");
                        Files.copy(markerorigin.toPath(), temp.toPath());
                        File markerorigininfo = new File(System.getProperty("user.dir") + "/temp-geljdb/standardfiles/info/" + m1.getName() + ".info");
                        File temp1 = new File(System.getProperty("user.dir") + "/standardfiles/info/" + partsmarker[1] + ".info");
                        Files.copy(markerorigininfo.toPath(), temp1.toPath());
                        File markeroriginpos = new File(System.getProperty("user.dir") + "/temp-geljdb/standardfiles/info/" + m1.getName() + ".pos");
                        File temp2 = new File(System.getProperty("user.dir") + "/standardfiles/info/" + partsmarker[1] + ".pos");
                        Files.copy(markeroriginpos.toPath(), temp2.toPath());
                    }
                }

                /// Adding experiment to the database
                int expidoriginal = e.getId();
                e.setId(lastExperimentid());
                e.setName("Imported-" + e.getName());
                insertExperiment(e);
                /// Making experiment directory
                File folderexp = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + e.getUser().getUsername() + "/experiments/" + e.getId() + "/");
                folderexp.mkdir();
                /// Copying image file
                File imageorigin = new File(System.getProperty("user.dir") + "/temp-geljdb/GelJDBImages/"
                        + parts[6] + "/" + u.getUsername() + "/experiments/" + expidoriginal + "/" + e.getImagename());
                //                JOptionPane.showMessageDialog(null, imageorigin.getPath());

                File imagedestine = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + e.getUser().getUsername() + "/experiments/" + e.getId() + "/" + e.getImagename());
                //                JOptionPane.showMessageDialog(null, imagedestine.getPath());

                //// Error here
                Files.move(imageorigin.toPath(), imagedestine.toPath(), REPLACE_EXISTING);

                //                JOptionPane.showMessageDialog(null, "test");
                /// Lanes folder
                File folderlanes = new File(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + e.getUser().getUsername() + "/experiments/" + e.getId() + "/lanes/");
                folderlanes.mkdir();
                /// Adding lanes to the database
                int laneid = lastLaneid();

                for (LaneDB l0 : lanesDB) {
                    int lid = l0.getId();
                    LaneDB newlane = l0;
                    newlane.setId(laneid);
                    insertLane(newlane);

                    String folderoriginlane = System.getProperty("user.dir") + "/temp-geljdb/GelJDBImages/"
                            + parts[6] + "/" + u.getUsername() + "/experiments/" + expidoriginal + "/lanes/" + lid + "/";
                    String folderlanepath = System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + e.getUser().getUsername() + "/experiments/" + e.getId() + "/lanes/" + laneid + "/";
                    File folderlane = new File(folderlanepath);
                    folderlane.mkdir();

                    File curveorigin = new File(folderoriginlane + lid + ".curve");
                    File curvedestiny = new File(folderlanepath + laneid + ".curve");
                    Files.copy(curveorigin.toPath(), curvedestiny.toPath());

                    File mworigin = new File(folderoriginlane + lid + ".mw");
                    File mwdestiny = new File(folderlanepath + laneid + ".mw");
                    Files.copy(mworigin.toPath(), mwdestiny.toPath());

                    File bandsorigin = new File(folderoriginlane + lid + ".bands");
                    File bandsdestiny = new File(folderlanepath + laneid + ".bands");
                    Files.copy(bandsorigin.toPath(), bandsdestiny.toPath());

                    File bandsorigin2 = new File(folderoriginlane + lid + ".bands2");
                    File bandsdestiny2 = new File(folderlanepath + laneid + ".bands2");
                    Files.copy(bandsorigin2.toPath(), bandsdestiny2.toPath());

                    File imorigin = new File(folderoriginlane + lid + ".tiff");
                    File imdestiny = new File(folderlanepath + laneid + ".tiff");
                    Files.copy(imorigin.toPath(), imdestiny.toPath());

                    File roiorigin = new File(folderoriginlane + lid + ".roi");
                    File roidestiny = new File(folderlanepath + laneid + ".roi");
                    Files.copy(roiorigin.toPath(), roidestiny.toPath());

                    laneid++;
                }

                File tempgeldb = new File(System.getProperty("user.dir") + "/temp-geljdb");
                deleteDir(tempgeldb);

                updateexperiments(true, true);

                enableall();
                activateemptytoolbar();

            } catch (IOException ex) {
                Logger.getLogger(ImportExperiment.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(ImportExperiment.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_jbimportexpfileActionPerformed

    private void jbDuplicateExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDuplicateExpActionPerformed
        try {
            duplicateexperiment();
            double scale = jXImageView2.getScale();
            jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
            jXImageView2.setScale(scale);
            activateemptytoolbar();

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jbDuplicateExpActionPerformed

    private void jbNewExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbNewExpActionPerformed
        newexperiment();
        activateemptytoolbar();
    }//GEN-LAST:event_jbNewExpActionPerformed

    private void duplicateexperiment() throws IOException {
        TreePath currentSelection = jtExperiments.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
            if (parent != null) {
                Experiment exp = (Experiment) currentNode.getUserObject();
                ImportExperiment.duplicateexperiment(exp, studyid);
                updateexperiments(true, true);
            }
        }

    }

    // Backups
//    try {
//            createbackup();
//            java.text.SimpleDateFormat todaysDate
//                    = new java.text.SimpleDateFormat("yyyy-MM-dd");
//            JOptionPane.showMessageDialog(this, "Backup created on " + todaysDate.format((java.util.Calendar.getInstance()).getTime()), "Backup", JOptionPane.INFORMATION_MESSAGE);
//        } catch (SQLException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
    private void continueunfinished() throws IOException, FileNotFoundException, ClassNotFoundException, Exception {

        ArrayList<UnfinishedExperiment> exps = getUnfinishedExperiments(studyid);

        if (!exps.isEmpty()) {
            disableall();

            if (ij == null) {
                ij = new ImageJ(NO_SHOW);
            }
            UnfinishedExperiment ue = (UnfinishedExperiment) JOptionPane.showInputDialog(this, "Select the unfinished experiment", "Finish experiment", JOptionPane.QUESTION_MESSAGE, null, exps.toArray(), exps.get(0));
            if (ue != null) {
                Menu m = new Menu();
                disableall();
                m.setUser(userid);

                Load ld = new Load(m);
                String dir = System.getProperty("user.dir") + "/GelJDBImages/" + ue.getStudy().getId() + "/" + ue.getStudy().getUser().getUsername() + "/unfinished/";
                String name = ue.getId() + ".zip";

                ld.loadzipfile(dir + name);

//                 Assistant a = new Assistant(m, this);
                Image_Assistant a = new Image_Assistant(m, this);
                switch (ue.getStep()) {
                    case 0:
//                        a.cropimage();
                        a.recoverstep0(IJ.getImage());
                        break;
                    case 1:
//                        a.invertcolour();
                        a.recoverstep1(IJ.getImage());
                        break;
                    case 2:
//                        a.autoadjustbc();
                        a.recoverstep2(IJ.getImage());
                        break;
                    case 3:
//                        a.preprocessimage();
                        a.recoverstep3(IJ.getImage());
                        break;
                    case 4:
//                        a.lightdarkbackground();
                        a.recoverstep4(IJ.getImage());
                        break;
                    case 5:
//                        a.autolanedetection();
                        a.recoverstep4(IJ.getImage());
                        break;
                    case 6:
//                        a.autobanddetection();
                        a.recoverstep6(IJ.getImage());
                        break;
                    case 7:
//                        a.normalisegel();
                        a.recoverstep6(IJ.getImage());
                        break;
                    case 8:
//                        a.normalisegel();
                        a.recoverstep6(IJ.getImage());
                        break;
//                    case 9:
////                        a.normalisegel();
//                        a.recoverstep6b(IJ.getImage());
//                        break;
                    default:
//                        a.cropimage();
                        a.recoverstep0(IJ.getImage());
                        break;
                }
                UnfinishedExperimentsQueries.deleteunfinishedexperiment(ue.getId());
                IJ.getImage().getWindow().toFront();
            } else {
                enableall();
            }

        } else {
            JOptionPane.showMessageDialog(this, "There is not any unfinished experiment");
        }

    }

    private void findsimilarlanes() {

        if (ij != null) {
            ij.quit();
            ij = null;
        }

        if (IJ.getInstance() != null) {
            IJ.getInstance().quit();
            ij = null;
        }

        int[] row = jtLanes.getSelectedRows();

        if (row.length != 1) {
            JOptionPane.showMessageDialog(this, "Select a lane to find lanes that are similar", "", INFORMATION_MESSAGE, null);
            return;
        }

        LaneDB lane = templanes.get(row[0]);

        FindSimilarities fs = new FindSimilarities(lane);
        fs.setVisible(true);

    }

    private void viewcomparison() {

        if (ij != null) {
            ij.quit();
            ij = null;
        }

        if (IJ.getInstance() != null) {
            IJ.getInstance().quit();
            ij = null;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
        if (node == null) //Nothing is selected.     
        {
            JOptionPane.showMessageDialog(this, "Select a comparison to edit", "", INFORMATION_MESSAGE, null);
            return;
        }
        Comparison comp = (Comparison) node.getUserObject();
        NewComparison nc = comp.toNewComparison(experiments);
        ArrayList<String> fields = new ArrayList<>();

        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

        for (String s : names0) {
            fields.add(s);
        }
        for (String s : additionalfields) {
            fields.add(s);
        }

        nc.setNonSelectedFields(fields);
        ViewComparison vc = new ViewComparison(nc, comp);
        vc.setVisible(true);

    }

    private void editexperiment() {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtExperiments.getLastSelectedPathComponent();
        if (node == null) //Nothing is selected.     
        {
            JOptionPane.showMessageDialog(this, "Select an experiment to edit", "", INFORMATION_MESSAGE, null);
            return;
        }
        Experiment exp = (Experiment) node.getUserObject();
        NewExperiment ne = new NewExperiment(this, true, exp);
        ne.setVisible(true);
        lanes = getlanesexperiment(exp.getId(), true);
        templanes = lanes;
        updatelanes(false);
        activateexperimentstoolbar();

    }

    private void editlane() throws IOException {
        if (ij == null) {
            ij = new ImageJ(NO_SHOW);
        }

        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            imp.close();
        }

        int[] row = jtLanes.getSelectedRows();

        if (row.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one lane", "", INFORMATION_MESSAGE, null);
            return;
        }

        if (row.length > 1) {
            JOptionPane.showMessageDialog(this, "Select only one lane", "", INFORMATION_MESSAGE, null);
            return;
        }

        LaneDB lane = templanes.get(row[0]);

        if (lane.isIsMarker()) {
            JOptionPane.showMessageDialog(this, "Standard lanes cannot be edited", "", INFORMATION_MESSAGE, null);
            return;
        }

        IJ.open(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + getUser(userid).getUsername() + "/experiments/" + lane.getExperiment().getId() + "/" + lane.getExperiment().getImagename());

        try {
            EditViewLaneDB eldb = new EditViewLaneDB(lane, true, this);

        } catch (IOException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void showMWofLane() throws IOException {

        int[] row = jtLanes.getSelectedRows();

        if (row.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one lane", "", INFORMATION_MESSAGE, null);
            return;
        }

        if (row.length > 1) {
            JOptionPane.showMessageDialog(this, "Select only one lane", "", INFORMATION_MESSAGE, null);
            return;
        }

        LaneDB lane = templanes.get(row[0]);

        Lane mainlane = LaneDBtoLaneIJ(lane);

        double[] mws = mainlane.getStdpositionbands();

        ArrayList list = new ArrayList();
        String headings = "Lane\t";

        for (int i = 0; i < mws.length; i++) {
            String row1 = "" + mws[i];
            list.add(row1);
        }

        TextWindow textWindow = new TextWindow("Lane MWs", headings, list, 600, 400);
        textWindow.setVisible(true);

    }

    private void viewlane() {

        if (ij != null) {
            ij.quit();
            ij = null;
        }

        if (IJ.getInstance() != null) {
            IJ.getInstance().quit();
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            imp.close();
        }

        int[] row = jtLanes.getSelectedRows();

        if (row.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one lane", "", INFORMATION_MESSAGE, null);
            return;
        }

        Opener o = new Opener();
        imp = o.openImage(System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + getUser(userid).getUsername() + "/experiments/" + templanes.get(row[0]).getExperiment().getId() + "/" + templanes.get(row[0]).getExperiment().getImagename());

        LaneDB[] lanes = new LaneDB[row.length];

        for (int i = 0; i < row.length; i++) {
            lanes[i] = templanes.get(row[i]);
            String path = System.getProperty("user.dir") + "/GelJDBImages/" + studyid + "/" + getUser(userid).getUsername() + "/experiments/"
                    + lanes[i].getExperiment().getId() + "/lanes/" + lanes[i].getId() + "/" + lanes[i].getId() + ".roi";
            Roi roi = RoiDecoder.open(path);
            roi.setStrokeWidth(3);
            imp.setRoi(roi);
            IJ.run(imp, "Add Selection...", "");
            imp.deleteRoi();
        }

        ImagePlus imp2 = imp.flatten();
        imp.close();
//        imp2.show();

        double scale = jXImageView2.getScale();
        jXImageView2.setImage(imp2.getImage());
        jXImageView2.setScale(scale);
        imp2.close();
//        try {
//            for (int i = 0; i < row.length; i++) {
//                new EditViewLaneDB(lanes[i], false);
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        imp2.getWindow().setAlwaysOnTop(true);

    }

    private void newexperiment() {

        if (ij == null) {
            ij = new ImageJ(NO_SHOW);
        }

        ImagePlus imp1 = WindowManager.getCurrentImage();
        if (imp1 != null) {
            imp1.changes = false;
            imp1.close();
        }

        jtComparisons.setSelectionPath(null);

        Menu m = new Menu();
        disableall();
        m.setUser(userid);

        /// Change assistants
//        Image_Assistant a = new ij.workspace.GelJ.Assistant3.Image_Assistant(m, this);
        Image_Assistant a = new ij.workspace.GelJ.Assistant4.Image_Assistant(m, this);
        a.runAssistant();
//        ImageAssistant a = new ImageAssistant(m, this);
//        a.runAssistant();

    }

    private void searchlanes() {
        String s = jxsearchlane.getText();
        ArrayList<LaneDB> newlanes = searchlanesstring(lanes, s);
        templanes = newlanes;
        updatelanes(newlanes);

    }

    private void disablenoexperiment() {
//        jbEditExp.setEnabled(false);
        jbDeleteExp.setEnabled(false);
        jbSearchExp.setEnabled(false);
        jbNewComp.setEnabled(false);
//        jbEditComp.setEnabled(false);
        jbViewComp.setEnabled(false);
        jbExportNewick.setEnabled(false);
        jbSimMatrix.setEnabled(false);
        jbDuplicateExp.setEnabled(false);
        jbDuplicateComp.setEnabled(false);
//        jbDeleteComp.setEnabled(false);
        jbSearchComp.setEnabled(false);
        jbAddField.setEnabled(false);
        jbdeletefield.setEnabled(false);
        jbFindSimilarLanes.setEnabled(false);
        jxsearchlane.setEnabled(false);
        jbAdvancedSearchLanes.setEnabled(false);
//        jbViewLane.setEnabled(false);
        jbEditlane.setEnabled(false);
//        jmiComparison.setEnabled(false);
//        jbAdd.setEnabled(false);

        jbDeleteLane.setVisible(false);
        jbDeleteLane.setEnabled(false);
//        jbAll.setEnabled(false);
    }

    private void enablelaneoptionsexperiment() {
        jbAddField.setEnabled(true);
        jbdeletefield.setEnabled(true);
        jbFindSimilarLanes.setEnabled(true);
        jxsearchlane.setEnabled(true);
        jbAdvancedSearchLanes.setEnabled(true);
//        jbViewLane.setEnabled(true);
        jbEditlane.setEnabled(true);
        jbShowMW.setEnabled(true);
//        jmiComparison.setEnabled(true);
//        jbAdd.setEnabled(true);
        jbDeleteLane.setEnabled(false);
        jbDeleteLane.setVisible(false);
        jSave.setEnabled(true);
//        jbAll.setEnabled(true);
    }

    private void enablelaneoptionscomparison() {
        jbAddField.setEnabled(true);
        jbdeletefield.setEnabled(true);
        jbFindSimilarLanes.setEnabled(true);
        jxsearchlane.setEnabled(true);
        jbAdvancedSearchLanes.setEnabled(true);
//        jbViewLane.setEnabled(true);
        jbEditlane.setEnabled(true);
        jbShowMW.setEnabled(true);
//        jbAdd.setEnabled(false);
        jbDeleteLane.setEnabled(true);
        jbDeleteLane.setVisible(true);
        jSave.setEnabled(true);
//        jbAll.setEnabled(true);
    }

    private void disablelaneoptions() {
        jbAddField.setEnabled(false);
        jbdeletefield.setEnabled(false);
        jbFindSimilarLanes.setEnabled(false);
        jxsearchlane.setEnabled(false);
        jbAdvancedSearchLanes.setEnabled(false);
//        jbViewLane.setEnabled(false);
        jbEditlane.setEnabled(false);
        jbShowMW.setEnabled(false);
//        jbAdd.setEnabled(false);
        jbDeleteLane.setEnabled(false);
        jbDeleteLane.setVisible(false);
//        jbAll.setEnabled(false);
    }

    private void disablenocomparison() {
//        jbEditComp.setEnabled(false);
        jbViewComp.setEnabled(false);
        jbExportNewick.setEnabled(false);
        jbSimMatrix.setEnabled(false);
        jbDuplicateComp.setEnabled(false);
//        jbDeleteComp.setEnabled(false);
        jbSearchComp.setEnabled(false);
        jSave.setEnabled(false);

    }

    private void enablecomparison() {
//        jbEditComp.setEnabled(true);
        jbViewComp.setEnabled(true);
        jbExportNewick.setEnabled(true);
        jbSimMatrix.setEnabled(true);
        jbDuplicateComp.setEnabled(true);
//        jbDeleteComp.setEnabled(true);
        jbSearchComp.setEnabled(true);
        jSave.setEnabled(true);
    }

    private void enablefromopenornewstudy() {
//        jmEdit.setEnabled(true);
//        jMenu3.setEnabled(true);
        jmOpenStudy.setEnabled(true);
        jmiCloseStudy.setEnabled(true);
        jbNewExp.setEnabled(true);
        jbImport.setEnabled(true);
        jbimportexpfile.setEnabled(true);
//        jbEditExp.setEnabled(true);
        jbDeleteExp.setEnabled(true);
        jbSearchExp.setEnabled(true);
        jbDuplicateExp.setEnabled(true);
        jbNewComp.setEnabled(true);
//        jbEditComp.setEnabled(true);
        jbViewComp.setEnabled(true);
        jbExportNewick.setEnabled(true);
        jbSimMatrix.setEnabled(true);
        jbDuplicateComp.setEnabled(true);
//        jbDeleteComp.setEnabled(true);
        jbSearchComp.setEnabled(true);
        jmiUnfinished.setEnabled(true);
        jmiExportGenetics.setEnabled(true);
        jmiAddPopulationField.setEnabled(true);
        jAnnotateItem.setEnabled(true);
        jmMarkers.setEnabled(true);
        jmannotate.setEnabled(true);
        jmiaddmarker.setEnabled(true);
        jAnnotateItem.setEnabled(true);
        jSave.setEnabled(true);
//        jbAll.setEnabled(true);
//        jbAddField.setEnabled(true);
//        jbdeletefield.setEnabled(true);
//        jbFindSimilarLanes.setEnabled(true);
//        jxsearchlane.setEnabled(true);
//        jbAdvancedSearchLanes.setEnabled(true);
//        jbViewLane.setEnabled(true);
//        jbEditlane.setEnabled(true);
//        jmiNewExperiment.setEnabled(true);
//        jmiComparison.setEnabled(true);
        jmiDeleteStudy.setEnabled(true);
        jmiEditStudy.setEnabled(true);

    }

    private void disablefromclosestudy() {
//        jmEdit.setEnabled(false);
        jmiCloseStudy.setEnabled(false);
        jbNewExp.setEnabled(false);
        jbImport.setEnabled(false);
        jbimportexpfile.setEnabled(false);
//        jbEditExp.setEnabled(false);
        jbDeleteExp.setEnabled(false);
        jbSearchExp.setEnabled(false);
        jbNewComp.setEnabled(false);
        jbDuplicateExp.setEnabled(false);
//        jbEditComp.setEnabled(false);
        jbViewComp.setEnabled(false);
        jbExportNewick.setEnabled(false);
        jbSimMatrix.setEnabled(false);
        jbDuplicateComp.setEnabled(false);
//        jbDeleteComp.setEnabled(false);
        jbSearchComp.setEnabled(false);
        jmiUnfinished.setEnabled(false);
        jmiExportGenetics.setEnabled(false);
        jmiAddPopulationField.setEnabled(false);
        jAnnotateItem.setEnabled(false);
        jmannotate.setEnabled(false);
        jmMarkers.setEnabled(false);
//        jbAll.setEnabled(false);
//        jmiNewExperiment.setEnabled(false);
//        jmiComparison.setEnabled(false);
        jtExperiments.setModel(null);
        jtComparisons.setModel(null);
        jtExperiments.setRootVisible(false);
        jtComparisons.setRootVisible(false);
        jmiDeleteStudy.setEnabled(false);
        jbAddField.setEnabled(false);
        jbdeletefield.setEnabled(false);
        jbFindSimilarLanes.setEnabled(false);
        jxsearchlane.setEnabled(false);
        jbAdvancedSearchLanes.setEnabled(false);
//        jbViewLane.setEnabled(false);
        jbEditlane.setEnabled(false);
        jbShowMW.setEnabled(false);
//        jbAdd.setEnabled(false);
        jmiaddmarker.setEnabled(false);
        jbDeleteLane.setEnabled(false);
        jbDeleteLane.setVisible(false);
        jmiEditStudy.setEnabled(false);
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        cleanlanes();

        double scale = jXImageView2.getScale();
        jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
        jXImageView2.setScale(scale);
        jToolBar3.setVisible(false);

        this.setTitle("GelJDB");
    }

    private void disableall() {
//        jmEdit.setEnabled(false);
        jmiCloseStudy.setEnabled(false);
        jbNewExp.setEnabled(false);
        jbImport.setEnabled(false);
        jbimportexpfile.setEnabled(false);
//        jbEditExp.setEnabled(false);
        jbDeleteExp.setEnabled(false);
        jbSearchExp.setEnabled(false);
        jbNewComp.setEnabled(false);
        jbDuplicateExp.setEnabled(false);
//        jbEditComp.setEnabled(false);
        jbViewComp.setEnabled(false);
        jbExportNewick.setEnabled(false);
        jbSimMatrix.setEnabled(false);
        jbDuplicateComp.setEnabled(false);
//        jbDeleteComp.setEnabled(false);
        jbSearchComp.setEnabled(false);
        jmiEditStudy.setEnabled(false);
//        jbAll.setEnabled(false);
        jbAddField.setEnabled(false);
        jbdeletefield.setEnabled(false);
        jbFindSimilarLanes.setEnabled(false);
        jxsearchlane.setEnabled(false);
        jbAdvancedSearchLanes.setEnabled(false);
//        jmiNewExperiment.setEnabled(false);
//        jmiComparison.setEnabled(false);
//        jmNew.setEnabled(false);
        jmOpenStudy.setEnabled(false);
        jmiExit.setEnabled(false);
        jmiDeleteStudy.setEnabled(false);
//        jbViewLane.setEnabled(false);
        jbEditlane.setEnabled(false);
        jbShowMW.setEnabled(false);
        jmiEditStudy.setEnabled(false);
        jmiUnfinished.setEnabled(false);
        jmiExportGenetics.setEnabled(false);
        jmiAddPopulationField.setEnabled(false);
        jAnnotateItem.setEnabled(false);
        jmMarkers.setEnabled(false);
        jmannotate.setEnabled(false);
        jAnnotateItem.setEnabled(false);
        jSave.setEnabled(false);
    }

    public void enableall() {
//        jmEdit.setEnabled(true);
        jmiCloseStudy.setEnabled(true);
        jbNewExp.setEnabled(true);
        jbImport.setEnabled(true);
        jbimportexpfile.setEnabled(true);
//        jbEditExp.setEnabled(true);
        jbDeleteExp.setEnabled(true);
        jbSearchExp.setEnabled(true);
        jbDuplicateExp.setEnabled(true);
        jbNewComp.setEnabled(true);
//        jbEditComp.setEnabled(true);
        jbViewComp.setEnabled(true);
        jbExportNewick.setEnabled(true);
        jbSimMatrix.setEnabled(true);
        jbDuplicateComp.setEnabled(true);
//        jbDeleteComp.setEnabled(true);
        jbSearchComp.setEnabled(true);
        jmiUnfinished.setEnabled(true);
        jmiExportGenetics.setEnabled(true);
        jmiAddPopulationField.setEnabled(true);
        jAnnotateItem.setEnabled(true);
        jmannotate.setEnabled(true);
        jmMarkers.setEnabled(true);
//        jbAll.setEnabled(true);
        jbAddField.setEnabled(true);
        jbdeletefield.setEnabled(true);
//        jbFindSimilarLanes.setEnabled(true);
        jxsearchlane.setEnabled(true);
        jbAdvancedSearchLanes.setEnabled(true);
//        jbViewLane.setEnabled(true);
//        jbEditlane.setEnabled(true);
//        jmiNewExperiment.setEnabled(true);
//        jmiComparison.setEnabled(true);
//        jmNew.setEnabled(true);
        jmOpenStudy.setEnabled(true);
        jmiExit.setEnabled(true);
        jmiDeleteStudy.setEnabled(true);
        jmiEditStudy.setEnabled(true);
        jmiaddmarker.setEnabled(true);
        jSave.setEnabled(true);

        if (comparisons.isEmpty()) {
            if (!experiments.isEmpty()) {
                disablenocomparison();
            } else {
                disablenoexperiment();
            }
        } else {
            enablecomparison();
        }
    }

    public void editcomparison() {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
        if (node == null) //Nothing is selected.     
        {
            JOptionPane.showMessageDialog(this, "Select a comparison to edit", "", INFORMATION_MESSAGE, null);
            return;
        }
        Comparison comp = (Comparison) node.getUserObject();
        NewComparison nc = comp.toNewComparison(experiments);
        nc.setId(comp.getId());
        ArrayList<String> fields = new ArrayList<>();

        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

        for (String s : names0) {
            fields.add(s);
        }
        for (String s : additionalfields) {
            fields.add(s);
        }

        nc.setNonSelectedFields(fields);

        Step1 step1 = new Step1(nc, true);
        step1.setVisible(true);

        updatecomparisons(true, true);

    }

    public void createnewcomparison() throws IOException {

        if (ij != null) {
            ImagePlus imp1 = WindowManager.getCurrentImage();
            if (imp1 != null) {
                imp1.changes = false;
                imp1.close();
            }

            ij.quit();
            ij = null;
        }

        if (IJ.getInstance() != null) {
            IJ.getInstance().quit();
            ij = null;
        }

        if (experiments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Create an experiment first", "", INFORMATION_MESSAGE, null);
        } else {
            jtExperiments.setSelectionPath(null);
            NewComparison nc = new NewComparison(studyid);
            String[] vals = readcomparisonconfig();

            nc.setNonSelectedExperiments(experiments);
            ArrayList<String> fields = new ArrayList<>();

            String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MLST", "isMarker", "Experiment", "Comments"};

            for (String s : names0) {
                fields.add(s);
            }
            for (String s : additionalfields) {
                fields.add(s);
            }

            if (vals != null) {
                nc.setSimilaritymethod(vals[0]);
                nc.setLinkage(vals[1]);
                nc.setTolerance(Double.parseDouble(vals[2]));
                ArrayList<String> selectedfields = new ArrayList<String>();

                for (int i = 3; i < vals.length; i++) {
                    if (fields.contains(vals[i])) {
                        fields.remove(vals[i]);
                        selectedfields.add(vals[i]);
                    }

                }
                nc.setSelectedFields(selectedfields);

            }

            nc.setNonSelectedFields(fields);

            Step1 step1 = new Step1(nc, false);
            step1.setVisible(true);

            updatecomparisons(true, true);
            if (!comparisons.isEmpty()) {
                enablecomparison();
            }
        }

    }

    public void selectComparison(int id) {

        Enumeration<DefaultMutableTreeNode> e = rootNodeComp.depthFirstEnumeration();

        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (!node.getUserObject().toString().equals("Comparisons")) {
                if (((Comparison) node.getUserObject()).getId() == id) {
                    jtComparisons.setSelectionPath(new TreePath(node.getPath()));
                }
            }
        }

    }

    public void selectExperiment(int id) {

        Enumeration<DefaultMutableTreeNode> e = rootNode.depthFirstEnumeration();

        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (!node.getUserObject().toString().equals("Experiments")) {
                if (((Experiment) node.getUserObject()).getId() == id) {
                    jtExperiments.setSelectionPath(new TreePath(node.getPath()));
                }
            }
        }

    }

    private void duplicatecomparison() {
        TreePath currentSelection = jtComparisons.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
            if (parent != null) {
                Comparison comp = (Comparison) currentNode.getUserObject();
                comp.setId(lastComparisonid());
                comp.setName(comp.getName() + "-1");
                insertComparison(comp);
                updatecomparisons(true, true);
            }
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jAnnotateItem;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuGenetic;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuMemory;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton jSave;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar5;
    private org.jdesktop.swingx.JXImageView jXImageView2;
    private javax.swing.JButton jbAddField;
    private javax.swing.JButton jbAdvancedSearchLanes;
    private javax.swing.JButton jbDeleteExp;
    private javax.swing.JButton jbDeleteLane;
    private javax.swing.JButton jbDuplicateComp;
    private javax.swing.JButton jbDuplicateExp;
    private javax.swing.JButton jbEditlane;
    private javax.swing.JButton jbExportNewick;
    private javax.swing.JButton jbFindSimilarLanes;
    private javax.swing.JButton jbImport;
    private javax.swing.JButton jbNewComp;
    private javax.swing.JButton jbNewExp;
    private javax.swing.JButton jbSearchComp;
    private javax.swing.JButton jbSearchExp;
    private javax.swing.JButton jbShowMW;
    private javax.swing.JButton jbSimMatrix;
    private javax.swing.JButton jbViewComp;
    private javax.swing.JButton jbdeletecomp;
    private javax.swing.JButton jbdeletefield;
    private javax.swing.JButton jbeditexpcomp;
    private javax.swing.JButton jbexport;
    private javax.swing.JButton jbimportexpfile;
    private javax.swing.JButton jbmodifycomp;
    private javax.swing.JMenu jmMarkers;
    private javax.swing.JMenuItem jmOpenStudy;
    private javax.swing.JMenu jmannotate;
    private javax.swing.JMenuItem jmiAddPopulationField;
    private javax.swing.JMenuItem jmiCloseStudy;
    private javax.swing.JMenuItem jmiDeleteStudy;
    private javax.swing.JMenuItem jmiEditStudy;
    private javax.swing.JMenuItem jmiExit;
    private javax.swing.JMenuItem jmiExportGenetics;
    private javax.swing.JMenuItem jmiNewStudy;
    private javax.swing.JMenuItem jmiUnfinished;
    private javax.swing.JMenuItem jmiaddmarker;
    private javax.swing.JSplitPane jsMainPane;
    private javax.swing.JSplitPane jspLeftPane;
    private javax.swing.JSplitPane jspRightPane;
    private javax.swing.JTree jtComparisons;
    private javax.swing.JTree jtExperiments;
    private javax.swing.JTable jtLanes;
    private org.jdesktop.swingx.JXSearchField jxsearchlane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(TreeSelectionEvent e) {

        // Selection of a experiments node
        if (jtExperiments.getModel() == null) {
            activateemptytoolbar();
            double scale = jXImageView2.getScale();
            jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
            jXImageView2.setScale(scale);
            return;
        }

        if (jtExperiments.getModel().toString().equals(((JTree) e.getSource()).getModel().toString())
                && jtExperiments.getSelectionPath() != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtExperiments.getLastSelectedPathComponent();
            if (node == null) //Nothing is selected.     
            {
                activateemptytoolbar();
                double scale = jXImageView2.getScale();
                jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
                jXImageView2.setScale(scale);
                return;
            }
            Experiment experiment = (Experiment) node.getUserObject();

            // Here
            String image = System.getProperty("user.dir") + "/GelJDBImages/"
                    + experiment.getStudy().getId()
                    + "/" + experiment.getUser().getUsername()
                    + "/experiments/" + experiment.getId() + "/" + experiment.getImagename();

            if (image.endsWith(".tif") || image.endsWith(".tiff")) {
                FileSeekableStream stream = null;
                try {
                    stream = new FileSeekableStream(image);
                    TIFFDecodeParam decodeParam = new TIFFDecodeParam();
                    decodeParam.setDecodePaletteAsShorts(true);
                    ParameterBlock params = new ParameterBlock();
                    params.add(stream);
                    RenderedOp image1 = JAI.create("tiff", params);
                    BufferedImage img = image1.getAsBufferedImage();
                    double scale = jXImageView2.getScale();
                    jXImageView2.setImage(img);
                    jXImageView2.setEditable(false);
                    jXImageView2.setDragEnabled(false);
                    jXImageView2.setScale(scale);

                } catch (IOException ex) {
                    Logger.getLogger(Main.class
                            .getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        stream.close();

                    } catch (IOException ex) {
                        Logger.getLogger(Main.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } else {
                File f = new File(image);
                try {
                    double scale = jXImageView2.getScale();
                    jXImageView2.setImage(f);
                    jXImageView2.setEditable(false);
                    jXImageView2.setDragEnabled(false);
                    jXImageView2.setScale(scale);

                } catch (IOException ex) {
                    Logger.getLogger(Main.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }

            lanes = getlanesexperiment(experiment.getId(), true);
            templanes = lanes;
            ArrayList<String> info = getAdditionalFieldsExp(experiment.getId());
            for (String i : info) {
                if (!additionalfields.contains(i)) {
                    additionalfields.add(i);
                }
            }
            updatelanes(false);
            enablelaneoptionsexperiment();
            activateexperimentstoolbar();
            jtComparisons.setSelectionPath(null);
        }

        if (jtComparisons.getModel() == null) {
            activateemptytoolbar();
            double scale = jXImageView2.getScale();
            jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
            jXImageView2.setScale(scale);
            return;
        }

        // Selection of a comparison node
        if (jtComparisons.getModel().toString().equals(((JTree) e.getSource()).getModel().toString()) && jtComparisons.getSelectionPath() != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jtComparisons.getLastSelectedPathComponent();
            if (node == null) //Nothing is selected.     
            {
                activateemptytoolbar();
                double scale = jXImageView2.getScale();
                jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
                jXImageView2.setScale(scale);
                return;
            }
            Comparison comp = (Comparison) node.getUserObject();

            // Here
            String image = System.getProperty("user.dir") + "/GelJDBImages/"
                    + comp.getStudy().getId()
                    + "/" + comp.getStudy().getUser().getUsername()
                    + "/comparisons/" + comp.getId() + "/dendrogram.tif";

            File f = new File(image);
            if (f.exists()) {

                if (image.endsWith(".tif") || image.endsWith(".tiff")) {
                    FileSeekableStream stream = null;
                    try {
                        stream = new FileSeekableStream(image);
                        TIFFDecodeParam decodeParam = new TIFFDecodeParam();
                        decodeParam.setDecodePaletteAsShorts(true);
                        ParameterBlock params = new ParameterBlock();
                        params.add(stream);
                        RenderedOp image1 = JAI.create("tiff", params);
                        BufferedImage img = image1.getAsBufferedImage();
                        double scale = jXImageView2.getScale();
                        jXImageView2.setImage(img);
                        jXImageView2.setEditable(false);
                        jXImageView2.setDragEnabled(false);
                        jXImageView2.setScale(scale);

                    } catch (IOException ex) {
                        Logger.getLogger(Main.class
                                .getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            stream.close();

                        } catch (IOException ex) {
                            Logger.getLogger(Main.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } else {
                double scale = jXImageView2.getScale();
                jXImageView2.setImage(new BufferedImage(300, 200, TYPE_INT_ARGB));
                jXImageView2.setScale(scale);
            }

            lanes = getLanesComparison(comp.getId());
            templanes = lanes;
            updatelanes(false);
            enablelaneoptionscomparison();
            jtExperiments.setSelectionPath(null);
            activatecomparisontoolbar();

        }

    }

    class MyTreeModelListener implements TreeModelListener {

        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

            /*
             * If the event lists children, then the changed
             * node is the child of the node we've already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */
            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode) (node.getChildAt(index));

            System.out.println("The user has finished editing the node.");
            System.out.println("New value: " + node.getUserObject());
        }

        public void treeNodesInserted(TreeModelEvent e) {
        }

        public void treeNodesRemoved(TreeModelEvent e) {
        }

        public void treeStructureChanged(TreeModelEvent e) {
        }
    }

    public void setLanes(ArrayList<LaneDB> lanes) {
        this.lanes = lanes;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;

    }

    class ExperimentsRenderer extends DefaultTreeCellRenderer {

        public ExperimentsRenderer() {
        }

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (leaf) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

                if (node.getUserObject() instanceof String) {
                    setToolTipText(null);
                } else {
                    Experiment experiment = (Experiment) node.getUserObject();
                    String toolTip = experiment.printInfo();
                    setToolTipText(toolTip);
                }
            } else {
                setToolTipText(null);
            }

            return this;
        }

    }

    class ComparisonRenderer extends DefaultTreeCellRenderer {

        public ComparisonRenderer() {
        }

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (leaf) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

                if (node.getUserObject() instanceof String) {
                    setToolTipText(null);
                } else {
                    Comparison comp = (Comparison) node.getUserObject();
                    String toolTip = comp.printInfo();
                    setToolTipText(toolTip);
                }
            } else {
                setToolTipText(null);
            }

            return this;
        }

    }
}
