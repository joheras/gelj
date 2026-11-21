/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db.Interface;

import db.Classes.Comparison;
import db.Classes.Experiment;
import db.Classes.LaneDB;
import static db.Classes.LaneDB.allsamemarker;
import db.Queries.ComparisonQueries;
import static db.Queries.ComparisonQueries.deletecomparison;
import static db.Queries.ComparisonQueries.getComparisons;
import static db.Queries.ComparisonQueries.insertComparison;
import static db.Queries.ComparisonQueries.lastComparisonid;
import static db.Queries.ExperimentQueries.deletecomparisonsexperiment;
import static db.Queries.ExperimentQueries.deleteexperiment;
import static db.Queries.ExperimentQueries.existscomparisonsexperiment;
import static db.Queries.ExperimentQueries.getExperiments;
import static db.Queries.ExperimentQueries.updateExperimentnameorcomments;
import static db.Queries.LaneQueries.containadditionalfield;
import static db.Queries.LaneQueries.deletecomparisonslane;
import static db.Queries.LaneQueries.deletelane;
import static db.Queries.LaneQueries.existscomparisonslane;
import static db.Queries.LaneQueries.getAdditionalFields;
import static db.Queries.LaneQueries.getLanes;
import static db.Queries.LaneQueries.getlanesexperiment;
import static db.Queries.LaneQueries.insertLaneAddedInfo;
import static db.Queries.LaneQueries.updateLane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author joheras
 */
public class StudyEdit extends javax.swing.JFrame implements TableModelListener {

    /**
     * Creates new form StudyEdit
     */
    public int studyid;
    private ArrayList<Experiment> experiments = new ArrayList<>();
    private ArrayList<Comparison> comparisons = new ArrayList<>();
    private ArrayList<LaneDB> lanes = new ArrayList<>();
    private ArrayList<String> additionalfields = new ArrayList<>();
    private boolean firsttime = true;

    public StudyEdit(int studyid) {
        initComponents();
        this.studyid = studyid;
        updateexperiments(true);
        updatecomparisons(true);
        updatelanes(true);
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

    public void updateexperiments(boolean all) {

        if (all) {
            experiments = getExperiments(studyid);
        }
        String[] names = {"Experiment", "Date", "Author", "Marker", "Comments"};
        String[][] data = new String[experiments.size()][5];

        // We update the table
        for (int i = 0; i < experiments.size(); i++) {
            data[i][0] = experiments.get(i).getName();
            data[i][1] = experiments.get(i).getDate().toString();
            data[i][2] = experiments.get(i).getUser().getUsername();
            data[i][3] = experiments.get(i).getMarker().getName();
            data[i][4] = experiments.get(i).getComments();
        }

        // Operations to change the model, we disable the edition of the date
        DefaultTableModel model = new DefaultTableModel(data, names) {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                if (mColIndex == 0 || mColIndex == 4) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        model.addTableModelListener(this);
        jtExperiments.setModel(model);

    }

    private JComboBox generatesimilaritycombobox() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("Dice");
        comboBox.addItem("Jaccard");
        comboBox.addItem("Band difference");
        comboBox.addItem("Jeffrey's X");
        comboBox.addItem("Ochiai");
        comboBox.addItem("Pearson correlation");
        comboBox.addItem("Cosine coefficient");
        comboBox.addItem("Euclidean distance");
        comboBox.addItem("Manhattan distance");
        return comboBox;

    }

    private JComboBox generatelinkagecombobox() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("UPGMA");
        comboBox.addItem("UPGMC");
        comboBox.addItem("Single linkage");
        comboBox.addItem("Complete linkage");
        comboBox.addItem("Mean linkage");
        comboBox.addItem("Ward");
        return comboBox;

    }

    public void updatecomparisons(boolean all) {

        if (all) {
            comparisons = getComparisons(studyid);
        }

        String[] names = {"Comparison", "Date", "Similarity", "Linkage", "Tolerance", "Comments"};
        Object[][] data = new Object[comparisons.size()][6];

        // We update the table
        for (int i = 0; i < comparisons.size(); i++) {
            data[i][0] = comparisons.get(i).getName();
            data[i][1] = comparisons.get(i).getDate().toString();
            data[i][2] = comparisons.get(i).getSimilarityMethod();
            data[i][3] = comparisons.get(i).getLinkage();
            data[i][4] = comparisons.get(i).getTolerance();
            data[i][5] = comparisons.get(i).getComments();
        }

        // Operations to change the model, we disable the edition of the date
        DefaultTableModel model = new DefaultTableModel(data, names) {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                if (mColIndex == 1) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        model.addTableModelListener(this);
        jtComparisons.setModel(model);
        TableColumn similaritycolumn = jtComparisons.getColumnModel().getColumn(2);
        similaritycolumn.setCellEditor(new DefaultCellEditor(generatesimilaritycombobox()));
        TableColumn linkagecolumn = jtComparisons.getColumnModel().getColumn(3);
        linkagecolumn.setCellEditor(new DefaultCellEditor(generatelinkagecombobox()));

    }

    public void updatelanes(boolean all) {

        if (all) {
            lanes = getLanes(studyid);
        }

        if (firsttime) {
            additionalfields = getAdditionalFields(studyid);
            firsttime = false;
        }

        String[] names0 = {"Genus", "Species", "Strain Number", "Country", "MSLT", "isMarker", "Experiment", "Comments"};

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
                if (mColIndex == 6) {
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

        TableColumn countriescolumn = jtLanes.getColumnModel().getColumn(3);
        countriescolumn.setCellEditor(new DefaultCellEditor(generatecountriescombobox()));

    }

    @Override
    public void tableChanged(TableModelEvent e) {

        /// Modification in Experiments table 
        if (jtExperiments.getModel().toString().equals(e.getSource().toString())) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            String cellValue = String.valueOf(jtExperiments.getValueAt(row, column));
            boolean b = (column == 0);
            updateExperimentnameorcomments(experiments.get(row).getId(), cellValue, b);

        }

        /// Modification in Comparisons table
        if (jtComparisons.getModel().toString().equals(e.getSource().toString())) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            Object cellValue = jtComparisons.getValueAt(row, column);

            switch (column) {
                case 0:
                    comparisons.get(row).setName(String.valueOf(cellValue));
                    ComparisonQueries.updateComparison(comparisons.get(row));
                    break;
                case 2:
                    comparisons.get(row).setSimilarityMethod(String.valueOf(cellValue));
                    ComparisonQueries.updateComparison(comparisons.get(row));
                    break;
                case 3:
                    comparisons.get(row).setLinkage(String.valueOf(cellValue));
                    ComparisonQueries.updateComparison(comparisons.get(row));
                    break;
                case 4:
                    comparisons.get(row).setTolerance(Integer.valueOf(String.valueOf(cellValue)));
                    ComparisonQueries.updateComparison(comparisons.get(row));
                    break;
                case 5:
                    comparisons.get(row).setComments(String.valueOf(cellValue));
                    ComparisonQueries.updateComparison(comparisons.get(row));
                    break;
                default:
                    break;
            }

        }

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

            updatelanes(true);

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
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane5 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtExperiments = new javax.swing.JTable();
        jbAddExp = new javax.swing.JButton();
        jbEditExp = new javax.swing.JButton();
        jbDeleteExp = new javax.swing.JButton();
        jbViewExp = new javax.swing.JButton();
        jbSearchexp = new javax.swing.JButton();
        jbShowAll = new javax.swing.JButton();
        jbAddtoComp = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtLanes = new javax.swing.JTable();
        jbViewLane = new javax.swing.JButton();
        jbEditLane = new javax.swing.JButton();
        jbDelete = new javax.swing.JButton();
        jbSearchLanes = new javax.swing.JButton();
        jbShowAllLanes = new javax.swing.JButton();
        jbAddtoCompLane = new javax.swing.JButton();
        jbSearchSim = new javax.swing.JButton();
        jbAddField = new javax.swing.JButton();
        jbDeleteField = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtComparisons = new javax.swing.JTable();
        jbNewComp = new javax.swing.JButton();
        jbEditComp = new javax.swing.JButton();
        jbRunComp = new javax.swing.JButton();
        jbDeleteComp = new javax.swing.JButton();
        jbDuplicateComp = new javax.swing.JButton();
        jbSearchComp = new javax.swing.JButton();
        jbShowAllComp = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jmiExport = new javax.swing.JMenuItem();
        jmiClose = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jmiStudyProps = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jtExperiments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "Date", "Author", "Marker", "Comments"
            }
        ));
        jScrollPane1.setViewportView(jtExperiments);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 385;
        gridBagConstraints.ipady = 455;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jbAddExp.setText("Add Experiment");
        jbAddExp.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 139;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jbAddExp, gridBagConstraints);

        jbEditExp.setText("Edit Experiment");
        jbEditExp.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 139;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jbEditExp, gridBagConstraints);

        jbDeleteExp.setText("Delete Experiment");
        jbDeleteExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteExpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 119;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jbDeleteExp, gridBagConstraints);

        jbViewExp.setText("View Experiment");
        jbViewExp.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 133;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jbViewExp, gridBagConstraints);

        jbSearchexp.setText("Search Experiments");
        jbSearchexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSearchexpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 112;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jbSearchexp, gridBagConstraints);

        jbShowAll.setText("Show All");
        jbShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbShowAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 193;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jbShowAll, gridBagConstraints);

        jbAddtoComp.setText("Add to Comparison");
        jbAddtoComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddtoCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 118;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jbAddtoComp, gridBagConstraints);

        jButton1.setText("Export Experiment");
        jButton1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jButton1, gridBagConstraints);

        jTabbedPane5.addTab("Experiments", jPanel1);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jtLanes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Genus", "Species", "Strain Number", "Country", "MSLT", "Marker", "Experiment", "Comments"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jtLanes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 430;
        gridBagConstraints.ipady = 455;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jScrollPane2, gridBagConstraints);

        jbViewLane.setText("View Lane");
        jbViewLane.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 196;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel2.add(jbViewLane, gridBagConstraints);

        jbEditLane.setText("Edit Lane");
        jbEditLane.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 202;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbEditLane, gridBagConstraints);

        jbDelete.setText("Delete Lane");
        jbDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 182;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbDelete, gridBagConstraints);

        jbSearchLanes.setText("Search Lanes");
        jbSearchLanes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSearchLanesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 175;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbSearchLanes, gridBagConstraints);

        jbShowAllLanes.setText("Show All");
        jbShowAllLanes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbShowAllLanesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 208;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbShowAllLanes, gridBagConstraints);

        jbAddtoCompLane.setText("Add to Comparison");
        jbAddtoCompLane.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddtoCompLaneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 133;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbAddtoCompLane, gridBagConstraints);

        jbSearchSim.setText("Find Similarities");
        jbSearchSim.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 156;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbSearchSim, gridBagConstraints);

        jbAddField.setText("Add Field");
        jbAddField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 201;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbAddField, gridBagConstraints);

        jbDeleteField.setText("Delete Field");
        jbDeleteField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.ipadx = 181;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jbDeleteField, gridBagConstraints);

        jTabbedPane5.addTab("Lanes", jPanel2);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jtComparisons.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Date", "Similarity Method", "Linkage Method", "Tolerance", "Comments"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jtComparisons);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 360;
        gridBagConstraints.ipady = 455;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jScrollPane3, gridBagConstraints);

        jbNewComp.setText("New Comparison");
        jbNewComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbNewCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 151;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel3.add(jbNewComp, gridBagConstraints);

        jbEditComp.setText("View Lanes of Comparison");
        jbEditComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbEditCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 83;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(jbEditComp, gridBagConstraints);

        jbRunComp.setText("Run Comparison");
        jbRunComp.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 154;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(jbRunComp, gridBagConstraints);

        jbDeleteComp.setText("Delete Comparison");
        jbDeleteComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 133;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(jbDeleteComp, gridBagConstraints);

        jbDuplicateComp.setText("Duplicate Comparison");
        jbDuplicateComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDuplicateCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 113;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(jbDuplicateComp, gridBagConstraints);

        jbSearchComp.setText("Search Comparison");
        jbSearchComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSearchCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 133;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(jbSearchComp, gridBagConstraints);

        jbShowAllComp.setText("Show All");
        jbShowAllComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbShowAllCompActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 208;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(jbShowAllComp, gridBagConstraints);

        jTabbedPane5.addTab("Comparisons", jPanel3);

        jMenu1.setText("File");

        jmiExport.setText("Export");
        jMenu1.add(jmiExport);

        jmiClose.setText("Close");
        jMenu1.add(jmiClose);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jmiStudyProps.setText("Study Properties");
        jMenu2.add(jmiStudyProps);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");
        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 687, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbAddFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddFieldActionPerformed

        String fieldName = JOptionPane.showInputDialog("Introduce the name of the new field");
        if (additionalfields.contains(fieldName)) {
            JOptionPane.showMessageDialog(this, "There is already a field with that name", "", INFORMATION_MESSAGE, null);
        } else {
            additionalfields.add(fieldName);
            updatelanes(true);
        }


    }//GEN-LAST:event_jbAddFieldActionPerformed

    private void jbDeleteFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteFieldActionPerformed

        String[] fields = new String[additionalfields.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = additionalfields.get(i);
        }

        String selectedfield = (String) JOptionPane.showInputDialog(this, "Select the field to remove", "", INFORMATION_MESSAGE, null, fields, fields[0]);
        additionalfields.remove(selectedfield);
        updatelanes(true);

    }//GEN-LAST:event_jbDeleteFieldActionPerformed

    private void jbDeleteExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteExpActionPerformed
        int row = jtExperiments.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an experiment first", "", INFORMATION_MESSAGE, null);
        } else {
            boolean ce = existscomparisonsexperiment(experiments.get(row).getId());
            if (ce) {
                int reply = JOptionPane.showConfirmDialog(this, "Some comparisons include lanes of this experiment."
                        + " If you remove the experiment, such comparisons will also be removed. "
                        + "Do you want to remove this experiment?", "Delete experiment", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {

                    deletecomparisonsexperiment(experiments.get(row).getId());
                    deleteexperiment(experiments.get(row).getId());
                    updateexperiments(true);
                    updatelanes(true);
                    updatecomparisons(true);
                }

            } else {
                int reply = JOptionPane.showConfirmDialog(this, "Do you want to remove this experiment? "
                        + "This will remove all the lanes of the experiment", "Delete experiment", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    deleteexperiment(experiments.get(row).getId());
                    updateexperiments(true);
                    updatelanes(true);
                }
            }

        }
    }//GEN-LAST:event_jbDeleteExpActionPerformed

    private void jbDeleteCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteCompActionPerformed
        int row = jtComparisons.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a comparison first", "", INFORMATION_MESSAGE, null);
        } else {
            int reply = JOptionPane.showConfirmDialog(this, "Do you want to remove this comparison? ", "Delete comparison", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                deletecomparison(comparisons.get(row).getId());
                updatecomparisons(true);
            }
        }
    }//GEN-LAST:event_jbDeleteCompActionPerformed

    private void jbDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteActionPerformed

        int row = jtLanes.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a lane first", "", INFORMATION_MESSAGE, null);
        } else {
            boolean ce = existscomparisonslane(lanes.get(row).getId());
            if (ce) {
                int reply = JOptionPane.showConfirmDialog(this, "Some comparisons include this lane."
                        + " If you remove the lane, such comparisons will also be removed. "
                        + "Do you want to remove this lane?", "Delete lane", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {

                    deletecomparisonslane(lanes.get(row).getId());
                    deletelane(lanes.get(row).getId());
                    updatelanes(true);
                    updatecomparisons(true);
                }

            } else {
                int reply = JOptionPane.showConfirmDialog(this, "Do you want to remove this lane? ", "Delete lane", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    deletelane(lanes.get(row).getId());
                    updatelanes(true);
                }
            }

        }

    }//GEN-LAST:event_jbDeleteActionPerformed

    private void jbShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbShowAllActionPerformed
        updateexperiments(true);
    }//GEN-LAST:event_jbShowAllActionPerformed

    private void jbShowAllLanesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbShowAllLanesActionPerformed
        updatelanes(true);
    }//GEN-LAST:event_jbShowAllLanesActionPerformed

    private void jbShowAllCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbShowAllCompActionPerformed
        updatecomparisons(true);
    }//GEN-LAST:event_jbShowAllCompActionPerformed

    private void jbDuplicateCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDuplicateCompActionPerformed
        int row = jtComparisons.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a comparison first", "", INFORMATION_MESSAGE, null);
        } else {
            Comparison c = comparisons.get(row);
            c.setId(lastComparisonid());
            c.setName(c.getName() + "-1");
            insertComparison(c);
            updatecomparisons(true);
        }
    }//GEN-LAST:event_jbDuplicateCompActionPerformed

    private void jbSearchexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSearchexpActionPerformed
        SearchExperiments se = new SearchExperiments(this);
        se.setVisible(true);


    }//GEN-LAST:event_jbSearchexpActionPerformed

    private void jbSearchLanesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSearchLanesActionPerformed
        SearchLanes sl = new SearchLanes(this);
        sl.setVisible(true);
    }//GEN-LAST:event_jbSearchLanesActionPerformed

    private void jbNewCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbNewCompActionPerformed
        NewComparisonGUI nc = new NewComparisonGUI(this, true, studyid, this);
        nc.setVisible(true);
    }//GEN-LAST:event_jbNewCompActionPerformed

    private void jbAddtoCompLaneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddtoCompLaneActionPerformed

        int[] row = jtLanes.getSelectedRows();

        if (row.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least a lane to add", "", INFORMATION_MESSAGE, null);
            return;
        }

        ArrayList<LaneDB> selectedlanes = new ArrayList<>();
        for (int i = 0; i < row.length; i++) {
            selectedlanes.add(lanes.get(row[i]));
        }

        if (!allsamemarker(selectedlanes)) {
            JOptionPane.showMessageDialog(this, "Not all the lanes have been normalised using the same marker", "", INFORMATION_MESSAGE, null);
            return;
        }

        AddtoComparison ac = new AddtoComparison(this, true, selectedlanes, studyid, this);
        ac.setVisible(true);


    }//GEN-LAST:event_jbAddtoCompLaneActionPerformed

    private void jbEditCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbEditCompActionPerformed
        int row = jtComparisons.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a comparison", "", INFORMATION_MESSAGE, null);
            return;
        }

        ViewLanesofComparison vlc = new ViewLanesofComparison(this, true, comparisons.get(row).getId(), comparisons.get(row).getName(), studyid);
        vlc.setVisible(true);


    }//GEN-LAST:event_jbEditCompActionPerformed

    private void jbSearchCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSearchCompActionPerformed
        SearchComparison sc = new SearchComparison(this);
        sc.setVisible(true);
    }//GEN-LAST:event_jbSearchCompActionPerformed

    private void jbAddtoCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddtoCompActionPerformed
        int row = jtExperiments.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an experiment", "", INFORMATION_MESSAGE, null);
            return;
        }

        int markers = JOptionPane.showConfirmDialog(this, "Do you want to include the markers of this experiment?");

        if (markers == JOptionPane.YES_OPTION) {
            ArrayList<LaneDB> selectedlanes = getlanesexperiment(experiments.get(row).getId(),true);
            AddtoComparison ac = new AddtoComparison(this, true, selectedlanes, studyid, this);
            ac.setVisible(true);
        } else if (markers == JOptionPane.NO_OPTION) {
            ArrayList<LaneDB> selectedlanes = getlanesexperiment(experiments.get(row).getId(),false);
            AddtoComparison ac = new AddtoComparison(this, true, selectedlanes, studyid, this);
            ac.setVisible(true);
        } else {
        }


    }//GEN-LAST:event_jbAddtoCompActionPerformed

    public int getStudyid() {
        return studyid;
    }

    public void setExperiments(ArrayList<Experiment> experiments) {
        this.experiments = experiments;
    }

    public void setLanes(ArrayList<LaneDB> lanes) {
        this.lanes = lanes;
    }

    public void setComparisons(ArrayList<Comparison> comparisons) {
        this.comparisons = comparisons;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JButton jbAddExp;
    private javax.swing.JButton jbAddField;
    private javax.swing.JButton jbAddtoComp;
    private javax.swing.JButton jbAddtoCompLane;
    private javax.swing.JButton jbDelete;
    private javax.swing.JButton jbDeleteComp;
    private javax.swing.JButton jbDeleteExp;
    private javax.swing.JButton jbDeleteField;
    private javax.swing.JButton jbDuplicateComp;
    private javax.swing.JButton jbEditComp;
    private javax.swing.JButton jbEditExp;
    private javax.swing.JButton jbEditLane;
    private javax.swing.JButton jbNewComp;
    private javax.swing.JButton jbRunComp;
    private javax.swing.JButton jbSearchComp;
    private javax.swing.JButton jbSearchLanes;
    private javax.swing.JButton jbSearchSim;
    private javax.swing.JButton jbSearchexp;
    private javax.swing.JButton jbShowAll;
    private javax.swing.JButton jbShowAllComp;
    private javax.swing.JButton jbShowAllLanes;
    private javax.swing.JButton jbViewExp;
    private javax.swing.JButton jbViewLane;
    private javax.swing.JMenuItem jmiClose;
    private javax.swing.JMenuItem jmiExport;
    private javax.swing.JMenuItem jmiStudyProps;
    private javax.swing.JTable jtComparisons;
    private javax.swing.JTable jtExperiments;
    private javax.swing.JTable jtLanes;
    // End of variables declaration//GEN-END:variables

}
