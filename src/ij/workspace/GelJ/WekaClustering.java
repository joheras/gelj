/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ;

import ij.workspace.GelJ.Assistant2.PleaseWait;
import static ij.workspace.GelJ.Lane.lanesnames;
import ij.workspace.GelJ.Weka.ClusterDraw;
import ij.workspace.GelJ.Weka.HierarchyVisualizer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka2.clusterers.HierarchicalClusterer;
import static weka2.clusterers.HierarchicalClusterer.TAGS_LINK_TYPE;

/**
 *
 * @author joheras
 */
public class WekaClustering {

    public static final int SINGLE = 0, COMPLETE = 1, UPGMA = 2, MEAN = 3,
            UPGMC = 4, WARD = 5, NEIGHBOR_JOINING = 7;

    private int linktype;
    private double[][] data;
    private String[] names;
    private Lane[] finallanes;
    private String link;
    private int method;
    private boolean invert;
    private int output;
    private Menu m = null;
    private Color c;
    private ArrayList<String> fields;
    
    public WekaClustering(int linktype, double[][] data, Lane[] finallanes, int method, boolean invert, int output, Color c, ArrayList<String> fields) {
        this.linktype = linktype;
        this.data = data;
        this.finallanes = finallanes;
        this.method = method;
        this.invert = invert;
        this.output = output;
        this.c = c;
        this.fields=fields;
        names = lanesnames(this.finallanes);
        switch (linktype) {
            case SINGLE:
                link = "Single linkage";
                break;
            case COMPLETE:
                link = "Complete linkage";
                break;
            case UPGMA:
                link = "UPGMA";
                break;
            case MEAN:
                link = "Mean linkage";
                break;
            case UPGMC:
                link = "UPGMC";
                break;
            case WARD:
                link = "Ward";
                break;
            default:
                break;
        }

    }

    public WekaClustering(int linktype, double[][] data, Lane[] finallanes, int method, boolean invert, int output, Menu m,ArrayList<String> fields) {
        this.linktype = linktype;
        this.data = data;
        this.finallanes = finallanes;
        this.method = method;
        this.invert = invert;
        this.output = output;
        this.m = m;
        this.c = m.getColour();
        this.fields=fields;
        names = lanesnames(this.finallanes);
        switch (linktype) {
            case SINGLE:
                link = "Single linkage";
                break;
            case COMPLETE:
                link = "Complete linkage";
                break;
            case UPGMA:
                link = "UPGMA";
                break;
            case MEAN:
                link = "Mean linkage";
                break;
            case UPGMC:
                link = "UPGMC";
                break;
            case WARD:
                link = "Ward";
                break;
            default:
                break;
        }

    }

    public String computeandvisualisecluster(PleaseWait pw) throws Exception {

        

        int len = data.length;

        // We create a feature vector and the attributes to the feature vector
        FastVector fvWekaAttributes = new FastVector(len);

        for (int i = 0; i < len; i++) {
            fvWekaAttributes.addElement(new Attribute(names[i]));
        }

        // We create an empty set of instances
        Instances ins_data = new Instances("Rel", fvWekaAttributes, len);

        // We create the instances and add them to the set of instances
        for (int j = 0; j < len; j++) {
            Instance ins = new DenseInstance(len);
            for (int k = 0; k < len; k++) {
                ins.setValue((Attribute) fvWekaAttributes.elementAt(k), data[j][k]);
            }
            ins_data.add(ins);
        }

        // We define the cluster instance
        // Define the cluster instance
        HierarchicalClusterer hc = new HierarchicalClusterer();
        hc.setNumClusters(1);
        hc.setLinkType(new SelectedTag(linktype, TAGS_LINK_TYPE));
        hc.setDistanceFunction(new EuclideanDistance());

        hc.buildClusterermatrix(ins_data, data);
        String graph = hc.graph();

        String graph1 = changespanishdot(graph);
        //hierarchicaltreevisualise(graph);
        //String[] temp = namesinorder(graph.substring(8));
        String[] newnames = namesinorder(graph1.substring(8));
        Lane[] reorderlanes = reorderlanes(finallanes, newnames);

        /*
         Montage m = new Montage(IJ.getImage(),getImagesfromfinallanes(reorderlanes,method));
        

        
        
         switch (linktype){
         case SINGLE:
         m.createMontage("Reordered Lanes using Single Linkage");
         break;
         case COMPLETE:
         m.createMontage("Reordered Lanes using Complete Linkage");
         break;    
         case UPGMA:
         m.createMontage("Reordered Lanes using UPGMA");
         break;     
         case MEAN:
         m.createMontage("Reordered Lanes using Mean Linkage");
         break;
         case UPGMC:
         m.createMontage("Reordered Lanes using UPGMC");
         break; 
         case WARD:
         m.createMontage("Reordered Lanes using WARD");
         break; 
         default:
         break;
         }
         */
        ClusterDraw cd = new ClusterDraw(graph1, reorderlanes, method, invert, output, c,fields);

        cd.showDiagram(pw);
        
        return graph1;

    }

    public void hierarchicaltreevisualise(String graph) {

        final javax.swing.JFrame jf = new javax.swing.JFrame(
                link + " clustering");

        jf.setSize(400, 500);
        jf.getContentPane().setLayout(new BorderLayout());
        HierarchyVisualizer tv = new HierarchyVisualizer(graph.substring(7));
        jf.getContentPane().add(tv, BorderLayout.CENTER);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {

                jf.dispose();

            }
        });
        jf.setVisible(true);
        tv.fitToScreen();

    }

    private String changespanishdot(String s) {
        boolean remains = true;
        String temp = s;
        String res = "";

        while (remains) {
            if (temp.contains(":0,")) {
                int pos = temp.indexOf(":0,");
                char c = temp.charAt(pos + 3);
                if (c >= '0' && c <= '9') {
                    res = res + temp.substring(0, pos) + ":0.";
                    temp = temp.substring(pos + 3);
                } else {
                    res = res + temp.substring(0, pos) + ":0,";
                    temp = temp.substring(pos + 3);
                }

            } else {
                res = res + temp;
                remains = false;
            }

        }

        return res;

    }

    private String removecommafromname(String s) {
        if (s.contains(",")) {
            return s.substring(s.indexOf(",") + 1);
        } else {
            return s;
        }

    }

    private String[] removenameswithcommas(String[] ls) {
        String[] res = new String[ls.length];
        for (int i = 0; i < ls.length; i++) {
            res[i] = removecommafromname(ls[i]);
        }
        return res;
    }

    private String[] namesinorder(String str) {
        boolean remains = true;
        String[] res = new String[finallanes.length];
        int i = 0;
        String temp = str;
        while (remains) {
            if (temp.startsWith("(")) {
                temp = temp.substring(1);
            } else if (temp.contains(":") && (temp.charAt(temp.indexOf(":") - 1) == ')')) {
                temp = temp.substring(temp.indexOf(":") + 1);
            } else if (temp.contains(":")) {
                String temp1 = temp.substring(0, temp.indexOf(":"));
                if (temp1.contains("(")) {
                    if (temp1.contains(",")) {
                        if (temp1.indexOf(",") < temp1.indexOf("(")) {
                            res[i] = removeinitialbrakets(temp1.substring(temp1.indexOf("(") + 1));
                        } else {
                            res[i] = removeinitialbrakets(temp1.substring(temp1.indexOf(",") + 1));
                        }
                    } else {
                        res[i] = removeinitialbrakets(temp1.substring(temp1.indexOf("(") + 1));
                    }
                } else if (temp1.contains(",")) {
                    res[i] = removeinitialbrakets(temp1.substring(temp1.indexOf(",") + 1));
                } else {
                    res[i] = removeinitialbrakets(temp1);
                }
                i++;
                temp = temp.substring(temp.indexOf(":") + 1);
            } else {
                remains = false;
            }
        }
        return res;
    }

    private String removeinitialbrakets(String str) {
        String temp = str;
        while (temp.startsWith("(")) {
            temp = temp.substring(1);
        }
        return temp;
    }

    private Lane[] reorderlanes(Lane[] l, String[] names) {
        Lane[] res = new Lane[l.length];
        for (int i = 0; i < l.length; i++) {
            res[i] = lanewithname(l, names[i]);
        }
        return res;
    }

    private Lane lanewithname(Lane[] l, String name) {
        for (Lane l1 : l) {
            if (l1.getName().equals(name)) {
                return l1;
            }
        }
        return null;
    }

}
