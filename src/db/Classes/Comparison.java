package db.Classes;

import static db.Interface.EditViewLaneDB.LaneDBtoLaneIJ;
import db.NewComparisonWizard.NewComparison;
import static db.Queries.LaneQueries.getlanesexperiment;
import ij.IJ;
import ij.ImagePlus;
import ij.text.TextPanel;
import ij.text.TextWindow;
import ij.workspace.GelJ.Assistant2.PleaseWait;
import ij.workspace.GelJ.Lane;
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
import ij.workspace.GelJ.WekaClustering;
import static ij.workspace.GelJ.WekaClustering.COMPLETE;
import static ij.workspace.GelJ.WekaClustering.MEAN;
import static ij.workspace.GelJ.WekaClustering.SINGLE;
import static ij.workspace.GelJ.WekaClustering.UPGMA;
import static ij.workspace.GelJ.WekaClustering.UPGMC;
import static ij.workspace.GelJ.WekaClustering.WARD;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class Comparison {

    private int id;
    private String name;
    private Date date;
    private String similarityMethod;
    private String linkage;
    private double tolerance;
    private String comments;
    private Study study;
    private ArrayList<LaneDB> lanes = new ArrayList<>();

    public Comparison(int id, String name, Date date, String similarityMethod, String linkage, double tolerance, String comments, Study study) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.similarityMethod = similarityMethod;
        this.linkage = linkage;
        this.tolerance = tolerance;
        this.comments = comments;
        this.study = study;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSimilarityMethod() {
        return similarityMethod;
    }

    public void setSimilarityMethod(String similarityMethod) {
        this.similarityMethod = similarityMethod;
    }

    public String getLinkage() {
        return linkage;
    }

    public void setLinkage(String linkage) {
        this.linkage = linkage;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void addLane(LaneDB l) {
        if (lanes.contains(l)) {
        } else {
            lanes.add(l);
        }
    }

    public void deleteLane(LaneDB l) {
        lanes.remove(l);
    }

    public ArrayList<LaneDB> getLanes() {
        return lanes;
    }

    @Override
    public String toString() {
        return name;
    }

    public String printInfo() {
        String res = "<html>Comparison " + name + ":<br>";
        res = res + "   Date: " + date + "<br>";
        res = res + "   Similarity Method: " + similarityMethod + "<br>";
        res = res + "   Linkage Method: " + linkage + "<br>";
        res = res + "   Tolerance: " + tolerance + "%<br>";
        if (comments != null) {
            res = res + "   Comments: " + comments + "</html>";
        }
        return res;
    }

    public void setLanes(ArrayList<LaneDB> lanes) {
        this.lanes = lanes;
    }

    public NewComparison toNewComparison(ArrayList<Experiment> exps) {

        NewComparison nc = new NewComparison(study.getId());
        nc.setName(name);
        nc.setComments(comments);
        nc.setLinkage(linkage);
        nc.setSimilaritymethod(similarityMethod);
        nc.setTolerance(tolerance);
        nc.setNonSelectedExperiments(exps);

        ArrayList<Experiment> selectedexperiments = new ArrayList<Experiment>();

        for (LaneDB l : lanes) {
            for (Experiment e : exps) {
                if (l.getExperiment().getId() == e.getId() && !selectedexperiments.contains(e)) {
                    selectedexperiments.add(e);
                }
            }
        }

        nc.setSelectedExperiments(selectedexperiments);

        nc.setNonSelectedLanes(new ArrayList<LaneDB>());
        nc.setSelectedLanes(new ArrayList<LaneDB>());

        for (Experiment e : selectedexperiments) {
            ArrayList<LaneDB> newlanes = getlanesexperiment(e.getId(), true);
            for (LaneDB l : newlanes) {
                nc.getNonSelectedLanes().add(l);
                for (LaneDB l1 : lanes) {
                    if (l.getId() == l1.getId()) {
                        nc.getSelectedLanes().add(l);
                    }
                }
            }
        }

        nc.setMarkers(containmarkers(lanes));

        return nc;
    }

    private static boolean containmarkers(ArrayList<LaneDB> lanes) {

        for (LaneDB l : lanes) {
            if (l.isIsMarker()) {
                return true;
            }
        }
        return false;

    }

    public static void generateDendrogram(Comparison c, int output, ArrayList<String> fields, boolean simtable, PleaseWait pw, boolean invert) throws IOException, Exception {

        Lane[] selectedlanes = new Lane[c.getLanes().size()];

        for (int i = 0; i < selectedlanes.length; i++) {
            LaneDB l = c.getLanes().get(i);
            selectedlanes[i] = LaneDBtoLaneIJ(l);
            selectedlanes[i].setName("");
            for (String field : fields) {
                String name = selectedlanes[i].getName();
                switch (field) {
                    case "Genus":
                        selectedlanes[i].setName(name + l.getGenus() + "\t\t");
                        break;
                    case "Species":
                        selectedlanes[i].setName(name + l.getSpecies() + "\t\t");
                        break;
                    case "Strain Number":
                        selectedlanes[i].setName(name + l.getStrainNumber() + "\t\t");
                        break;
                    case "Country":
                        selectedlanes[i].setName(name + l.getCountry() + "\t\t");
                        break;
                    case "MLST":
                        selectedlanes[i].setName(name + l.getMSLT() + "\t\t");
                        break;
                    case "isMarker":
                        selectedlanes[i].setName(name + l.isIsMarker() + "\t\t");
                        break;
                    case "Experiment":
                        selectedlanes[i].setName(name + l.getExperiment().getName() + "\t\t");
                        break;
                    case "Comments":
                        selectedlanes[i].setName(name + l.getComments() + "\t\t");
                        break;
                    default:
                        selectedlanes[i].setName(name + l.getLaneaddedinfo().get(field) + "\t\t");
                        break;
                }

            }
            selectedlanes[i].setName("Exp. " + selectedlanes[i].getName() + l.getId());
        }

        double[] stds = selectedlanes[0].getStdpositionbands();
        double tolerance_aux = stds[0] - stds[stds.length - 1];
        double tolerance = tolerance_aux * c.getTolerance() / 100;

        int similaritymethod = 0;
        LaneComparison lc;
        switch (c.getSimilarityMethod()) {
            case "Dice":
                lc = new Dice(tolerance);
                similaritymethod = 0;
                break;
            case "Jaccard":
                lc = new Jaccard(tolerance);
                similaritymethod = 1;
                break;
            case "Jeffrey's X":
                lc = new Jeffrey(tolerance);
                similaritymethod = 2;
                break;
            case "Ochiai":
                lc = new Ochiai(tolerance);
                similaritymethod = 3;
                break;
            case "Band difference":
                lc = new BandDifference(tolerance);
                similaritymethod = 4;
                break;
            case "Pearson correlation":
                lc = new PearsonCorrelation();
                similaritymethod = 5;
                break;
            case "Cosine coefficient":
                lc = new CosineCorrelation();
                similaritymethod = 6;
                break;
            case "Euclidean distance":
                lc = new EuclideanDistance();
                simtable = true;
                similaritymethod = 7;
                break;
            case "Manhattan distance":
                lc = new ManhattanDistance();
                similaritymethod = 8;
                simtable = true;
                break;
            default:
                lc = new PearsonCorrelation();
                similaritymethod = 5;
                break;
        }

        int clustering_alg = 0;

        switch (c.getLinkage()) {
            case "Single linkage":
                clustering_alg = SINGLE;
                break;
            case "Complete linkage":
                clustering_alg = COMPLETE;
                break;
            case "UPGMA":
                clustering_alg = UPGMA;
                break;
            case "Mean linkage":
                clustering_alg = MEAN;
                break;
            case "UPGMC":
                clustering_alg = UPGMC;
                break;
            case "Ward":
                clustering_alg = WARD;
                break;
            default:
                break;
        }

        double[][] similaritytable = lc.CompareLanes(selectedlanes);

        ArrayList Similaritylist = new ArrayList();
        ArrayList Distancelist = new ArrayList();
        String headings = "Strain Number\t";
        for (int i = 0; i < similaritytable.length - 1; i++) {
            String name = selectedlanes[i].getName().replaceAll("\t", " ");
            headings = headings + name.substring(4, name.lastIndexOf(" ")) + "\t";
        }
        String name = selectedlanes[similaritytable.length - 1].getName().replaceAll("\t", " ");
        headings = headings + name.substring(4, name.lastIndexOf(" "));

        for (int i = 0; i < similaritytable[0].length; i++) {
            name = selectedlanes[i].getName().replaceAll("\t", " ");
            String row = name.substring(4, name.lastIndexOf(" ")) + "\t";
            String row1 = name.substring(4, name.lastIndexOf(" ")) + "\t";
            for (int j = 0; j < similaritytable.length - 1; j++) {
                row = row + (1 - similaritytable[i][j]) + "\t";
                row1 = row1 + similaritytable[i][j] + "\t";
            }
            row = row + (1 - similaritytable[i][similaritytable.length - 1]);
            row1 = row1 + similaritytable[i][similaritytable.length - 1];
            Similaritylist.add(row);
            Distancelist.add(row1);
        }

        
        if (simtable) {
            TextWindow textWindow = new TextWindow("Similarity Table", headings, Similaritylist, 600, 400);
            textWindow.setVisible(true);
        } 

        TextPanel textPanel = new TextPanel("Similarity Table");
        textPanel.setColumnHeadings(headings);
        textPanel.append(Similaritylist);
        
        TextPanel textPanelDistance = new TextPanel("Distance Table");
        textPanelDistance.setColumnHeadings(headings);
        textPanelDistance.append(Distancelist);

        if (similaritymethod != 7 && similaritymethod != 8) {
            WekaClustering wc = new WekaClustering(clustering_alg, similaritytable,
                    selectedlanes, similaritymethod, invert, output, Color.BLUE, fields);
            String newick = wc.computeandvisualisecluster(pw);

            String path = System.getProperty("user.dir") + "/GelJDBImages/"
                    + c.getStudy().getId()
                    + "/" + c.getStudy().getUser().getUsername()
                    + "/comparisons/" + c.getId() + "/";
            File f = new File(path);
            if (!f.exists()) {
                f.mkdir();
            }

            String simmatrix = path + "simmatrix.xls";
            textPanel.saveAs(simmatrix);
            
            String distancematrix = path + "distancematrix.xls";
            textPanelDistance.saveAs(distancematrix);
            
            
            String newickFile = path + "newick.txt";
            PrintWriter writer = new PrintWriter(newickFile, "UTF-8");
            writer.println(newick.substring(7)+";");
            writer.close();

            ImagePlus imp = IJ.getImage();
            IJ.saveAsTiff(imp, path + "dendrogram.tif");
        }

    }

    public static void generateOcurrenceMatrix(Comparison c, ArrayList<String> fields) throws IOException, Exception {

        Lane[] selectedlanes = new Lane[c.getLanes().size()];

        for (int i = 0; i < selectedlanes.length; i++) {
            LaneDB l = c.getLanes().get(i);
            selectedlanes[i] = LaneDBtoLaneIJ(l);
            selectedlanes[i].setName("");
            for (String field : fields) {
                String name = selectedlanes[i].getName();
                switch (field) {
                    case "Genus":
                        selectedlanes[i].setName(name + l.getGenus() + "\t\t");
                        break;
                    case "Species":
                        selectedlanes[i].setName(name + l.getSpecies() + "\t\t");
                        break;
                    case "Strain Number":
                        selectedlanes[i].setName(name + l.getStrainNumber() + "\t\t");
                        break;
                    case "Country":
                        selectedlanes[i].setName(name + l.getCountry() + "\t\t");
                        break;
                    case "MLST":
                        selectedlanes[i].setName(name + l.getMSLT() + "\t\t");
                        break;
                    case "isMarker":
                        selectedlanes[i].setName(name + l.isIsMarker() + "\t\t");
                        break;
                    case "Experiment":
                        selectedlanes[i].setName(name + l.getExperiment().getName() + "\t\t");
                        break;
                    case "Comments":
                        selectedlanes[i].setName(name + l.getComments() + "\t\t");
                        break;
                    default:
                        selectedlanes[i].setName(name + l.getLaneaddedinfo().get(field) + "\t\t");
                        break;
                }

            }
            selectedlanes[i].setName("Exp. " + selectedlanes[i].getName() + l.getId());
        }

        double[] stds = selectedlanes[0].getStdpositionbands();
        double tolerance_aux = stds[0] - stds[stds.length - 1];
        double tolerance = tolerance_aux * c.getTolerance() / 100;

        ArrayList<Double> mws = getMWs(selectedlanes, tolerance);
        int[][] similaritytable = createOccurrenceMatrix(selectedlanes, tolerance, mws);

        ArrayList list = new ArrayList();
        String headings = "mw \t";
        for (int i = 0; i < selectedlanes.length - 1; i++) {
            String name = selectedlanes[i].getName().replaceAll("\t", " ");
            headings = headings + name.substring(4, name.lastIndexOf(" ")) + "\t";
        }
        String name = selectedlanes[selectedlanes.length - 1].getName().replaceAll("\t", " ");
        headings = headings + name.substring(4, name.lastIndexOf(" "));

        for (int i = 0; i < mws.size(); i++) {
            name = "" + mws.get(i);
            String row = name + "\t";
            for (int j = 0; j < selectedlanes.length - 1; j++) {
                row = row + (1 - similaritytable[i][j]) + "\t";
            }
            row = row + similaritytable[i][selectedlanes.length - 1];
            list.add(row);
        }

        TextWindow textWindow = new TextWindow("Occurrence Matrix", headings, list, 600, 400);
        textWindow.setVisible(true);

    }

    private static ArrayList<Double> insertSortMWs(ArrayList<Double> mws, double mw, double tolerance) {
        ArrayList<Double> res = new ArrayList<Double>();
        if (mws.size() == 0) {
            res.add(mw);
            return res;
        }

        int i = 0;
        boolean b = true;

        while (b && (i < mws.size())) {
            if (mws.get(i) + tolerance < mw) {
                b = false;
                res.add(mw);
            } else {
                if (mws.get(i) - tolerance < mw) {
                    b = false;
                } else {
                    res.add(mws.get(i));
                    i++;
                }
            }
        }
        if (b == true) {
            res.add(mw);
        } else {

            for (int j = i; j < mws.size(); j++) {
                res.add(mws.get(j));
            }
        }
        return res;

    }

    private static ArrayList<Double> getMWs(Lane[] selectedlanes, double tolerance) {
        ArrayList<Double> result = new ArrayList<Double>();
        for (int i = 0; i < selectedlanes.length; i++) {
            double stds[] = selectedlanes[i].getStdpositionbands();
            for (int j = 0; j < stds.length; j++) {
                result = insertSortMWs(result, stds[j], tolerance);
            }
        }
        return result;
    }

    private static int[][] createOccurrenceMatrix(Lane[] selectedlanes, double tolerance, ArrayList<Double> mws) {

        int[][] matrix = new int[mws.size()][selectedlanes.length];
        for (int i = 0; i < selectedlanes.length; i++) {
            double[] stds = selectedlanes[i].getStdpositionbands();
            for (int j = 0; j < mws.size(); j++) {
                int k = 0;
                boolean b = true;
                while (b && k < stds.length) {
                    if ((stds[k] + tolerance > mws.get(j)) && (stds[k] - tolerance < mws.get(j))) {
                        matrix[j][i] = 1;
                        b = false;
                    } else {
                        k++;
                    }
                }
            }
        }
        return matrix;

    }

    public static void generateDendrograms(Comparison c, int output, ArrayList<String> fields, boolean mat, PleaseWait pw, boolean invert) throws IOException, Exception {

        Lane[] selectedlanes = new Lane[c.getLanes().size()];

        for (int i = 0; i < selectedlanes.length; i++) {
            LaneDB l = c.getLanes().get(i);
            selectedlanes[i] = LaneDBtoLaneIJ(l);
            selectedlanes[i].setName("");
            for (String field : fields) {
                String name = selectedlanes[i].getName();
                switch (field) {
                    case "Genus":
                        selectedlanes[i].setName(name + l.getGenus() + "\t\t");
                        break;
                    case "Species":
                        selectedlanes[i].setName(name + l.getSpecies() + "\t\t");
                        break;
                    case "Strain Number":
                        selectedlanes[i].setName(name + l.getStrainNumber() + "\t\t");
                        break;
                    case "Country":
                        selectedlanes[i].setName(name + l.getCountry() + "\t\t");
                        break;
                    case "MLST":
                        selectedlanes[i].setName(name + l.getMSLT() + "\t\t");
                        break;
                    case "isMarker":
                        selectedlanes[i].setName(name + l.isIsMarker() + "\t\t");
                        break;
                    case "Experiment":
                        selectedlanes[i].setName(name + l.getExperiment().getName() + "\t\t");
                        break;
                    case "Comments":
                        selectedlanes[i].setName(name + l.getComments() + "\t\t");
                        break;
                    default:
                        selectedlanes[i].setName(name + l.getLaneaddedinfo().get(field) + "\t\t");
                        break;
                }

            }
            selectedlanes[i].setName("Exp. " + selectedlanes[i].getName() + l.getId());
        }

        double[] stds = selectedlanes[0].getStdpositionbands();
        double tolerance_aux = stds[0] - stds[stds.length - 1];
        double tolerance = tolerance_aux * c.getTolerance() / 100;

        int similaritymethod = 0;

        int clustering_alg = 0;

        switch (c.getLinkage()) {
            case "Single linkage":
                clustering_alg = SINGLE;
                break;
            case "Complete linkage":
                clustering_alg = COMPLETE;
                break;
            case "UPGMA":
                clustering_alg = UPGMA;
                break;
            case "Mean linkage":
                clustering_alg = MEAN;
                break;
            case "UPGMC":
                clustering_alg = UPGMC;
                break;
            case "Ward":
                clustering_alg = WARD;
                break;
            default:
                break;
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 6; j++) {
                LaneComparison lc;
                switch (i) {
                    case 0:
                        lc = new Dice(tolerance);
                        similaritymethod = 0;
                        output = 0;
                        break;
                    case 1:
                        lc = new Jaccard(tolerance);
                        similaritymethod = 1;
                        output = 0;
                        break;
                    case 2:
                        lc = new Jeffrey(tolerance);
                        similaritymethod = 2;
                        output = 0;
                        break;
                    case 3:
                        lc = new Ochiai(tolerance);
                        similaritymethod = 3;
                        output = 0;
                        break;
                    case 4:
                        lc = new BandDifference(tolerance);
                        similaritymethod = 4;
                        output = 0;
                        break;
                    case 5:
                        lc = new PearsonCorrelation();
                        similaritymethod = 5;
                        output = 1;
                        break;
                    case 6:
                        lc = new CosineCorrelation();
                        similaritymethod = 6;
                        output = 1;
                        break;

                    default:
                        lc = new PearsonCorrelation();
                        similaritymethod = 5;
                        break;
                }
                double[][] similaritytable = lc.CompareLanes(selectedlanes);
                output = 3;
                WekaClustering wc = new WekaClustering(j, similaritytable,
                        selectedlanes, similaritymethod, invert, output, Color.BLUE, fields);
                wc.computeandvisualisecluster(pw);

                String image = "/home/joheras/Desktop/";
                File f = new File(image);
                if (!f.exists()) {
                    f.mkdir();
                }

                ImagePlus imp = IJ.getImage();
                IJ.saveAsTiff(imp, image + "dendrogram-" + i + "-" + j + ".tif");
                imp.close();
            }
        }

    }

}
