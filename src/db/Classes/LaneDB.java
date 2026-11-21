package db.Classes;

import static db.Interface.EditViewLaneDB.LaneDBtoLaneIJ;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.Lane.getImagesfromfinallanes;
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
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class LaneDB {

    private int id;
    private String genus;
    private String species;
    private String strainNumber;
    private String country;
    private String MSLT;
    private Boolean isMarker;
    private String comments;
    private Experiment experiment;
    private Hashtable<String, String> laneaddedinfo = new Hashtable<>();

    public LaneDB(int id, String genus, String species, String strainNumber, String country, String MSLT, Boolean isMarker, String comments, Experiment experiment) {
        this.id = id;
        this.genus = genus;
        this.species = species;
        this.strainNumber = strainNumber;
        this.country = country;
        this.MSLT = MSLT;
        this.isMarker = isMarker;
        this.comments = comments;
        this.experiment = experiment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getStrainNumber() {
        return strainNumber;
    }

    public void setStrainNumber(String strainNumber) {
        this.strainNumber = strainNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMSLT() {
        return MSLT;
    }

    public void setMSLT(String MSLT) {
        this.MSLT = MSLT;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Hashtable<String, String> getLaneaddedinfo() {
        return laneaddedinfo;
    }

    public void setLaneaddedinfo(Hashtable<String, String> laneaddedinfo) {
        this.laneaddedinfo = laneaddedinfo;
    }

    public void addnewinfo(String fieldname, String fieldvalue) {
        laneaddedinfo.put(fieldname, fieldvalue);
    }

    public Boolean isIsMarker() {
        return isMarker;
    }

    public void setIsMarker(Boolean isMarker) {
        this.isMarker = isMarker;
    }

    public static boolean allsamemarker(ArrayList<LaneDB> lanes) {
        boolean samemarker = true;

        if (lanes.isEmpty()) {
            return true;
        }

        int marker = lanes.get(0).getExperiment().getMarker().getId();

        for (LaneDB l : lanes) {
            if (marker != l.getExperiment().getMarker().getId()) {
                return false;
            }
        }
        return samemarker;

    }

    
    public static ArrayList<LaneDB> searchlanesstring (ArrayList<LaneDB> lanes, String s){
        ArrayList<LaneDB> res = new ArrayList<>();
        
        for(LaneDB l : lanes){
            if(l.searchinfo().contains(s.toUpperCase())){
                res.add(l);
            }
        
        }
        return res;
    }


    private String searchinfo() {
        String res = genus + species + strainNumber + country + MSLT + comments;
        
        Enumeration<String> keys = laneaddedinfo.keys();
        
        while(keys.hasMoreElements()){
            String k= keys.nextElement();
            res = res + laneaddedinfo.get(k);
        }
    
        return res.toUpperCase();
    }
    
    
    public static ArrayList<LaneDBwithsimilarity> computesimilarlanes (LaneDB lane, ArrayList<LaneDB> lanes, String simmethod, double tolerance, int similarity) throws IOException{
        ArrayList<LaneDBwithsimilarity> similarlanes = new ArrayList<>();
    
    
        Lane[] selectedlanes = new Lane[lanes.size()];

        for (int i = 0; i < selectedlanes.length; i++) {
            LaneDB l = lanes.get(i);
            selectedlanes[i] = LaneDBtoLaneIJ(l);
        }

        Lane mainlane = LaneDBtoLaneIJ(lane);
        

        double[] stds = selectedlanes[0].getStdpositionbands();
        double tolerance_aux = stds[0] - stds[stds.length - 1];
        double tol = tolerance_aux * tolerance / 100;

        LaneComparison lc;
        switch (simmethod) {
            case "Dice":
                lc = new Dice(tol);
                break;
            case "Jaccard":
                lc = new Jaccard(tol);
                break;
            case "Jeffrey's X":
                lc = new Jeffrey(tol);
               break;
            case "Ochiai":
                lc = new Ochiai(tol);
                break;
            case "Band difference":
                lc = new BandDifference(tol);
                break;
            case "Pearson correlation":
                lc = new PearsonCorrelation();
                break;
            case "Cosine coefficient":
                lc = new CosineCorrelation();
                break;
            case "Euclidean distance":
                lc = new EuclideanDistance();
                break;
            case "Manhattan distance":
                lc = new ManhattanDistance();
                break;
            default:
                lc = new PearsonCorrelation();
                break;
        }

        
        for(int i=0;i<selectedlanes.length;i++){
            double sim = lc.Compare2Lanes(mainlane, selectedlanes[i]);
            // The species must be the same
            if((1-sim)*100 >= similarity && lane.getId()!=lanes.get(i).getId() &&(lane.getSpecies().equals(lanes.get(i).getSpecies()))){
                similarlanes.add(new LaneDBwithsimilarity(lanes.get(i),(1-sim)*100));
            }
        }
        
        return similarlanes;
    }
    
    
    
    public static ImagePlus showlanes (LaneDB lane, ArrayList<LaneDBwithsimilarity> lanes) throws IOException{
        Lane mainlane = LaneDBtoLaneIJ(lane);
        Lane[] selectedlanes = new Lane[lanes.size()+1];
        int height = mainlane.getImp().getHeight();
        int width = mainlane.getImp().getWidth() + 50;
        selectedlanes[0]=mainlane;
        
        for (int i = 0; i < selectedlanes.length-1; i++) {
            LaneDB l = lanes.get(i).getLane();
            selectedlanes[i+1] = LaneDBtoLaneIJ(l);
            width = width + selectedlanes[i+1].getImp().getWidth() + 50;
            if(height < selectedlanes[i+1].getImp().getHeight()){
                height = selectedlanes[i+1].getImp().getHeight();
            }
        }
        
        
        ImagePlus impl = NewImage.createImage("Lanes", width+100,height+100, 0,24,0);
        
        
        impl.show();
        
        ImageProcessor ip1 = impl.getProcessor();
        ip1.setColor(Color.white);
        ip1.fill();
       
        ImagePlus[] images = getImagesfromfinallanes(selectedlanes,0,Color.BLUE,false);
        
        ip1.setColor(Color.black);
            
        
        for (int i = 0; i < selectedlanes.length;i++){
            ImagePlus imp2 = images[i].duplicate();
            ImageProcessor ip = imp2.getProcessor();
            ip1.insert(ip, (int)((i+0.5)*100 - (images[i].getWidth()/2)),0);
            ip1.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String info ="";
            if(i==0){
                info =  lane.getGenus()+"\n"+
                        lane.getSpecies()+"\n" +
                        lane.getStrainNumber() + "\n"+
                        lane.getExperiment().getName() + "\n" +
                        lane.getExperiment().getStudy().getName() + "\n" + 
                        "(selected lane)";
                
             }else{
            
                 info = lanes.get(i-1).getLane().getGenus()+"\n"+
                        lanes.get(i-1).getLane().getSpecies()+"\n" +
                        lanes.get(i-1).getLane().getStrainNumber() + "\n"+
                        lanes.get(i-1).getLane().getExperiment().getName() + "\n" +
                        lanes.get(i-1).getLane().getExperiment().getStudy().getName() + "\n" +
                        "Sim: " + (int)lanes.get(i-1).getSimilarity() + "%";
                
            
            }
            ip1.drawString(info,(int)((i+0.5)*100 - (images[i].getWidth()/2)),height+15);
            
            
        }
        
        
        
        impl.updateAndDraw();
        
        impl.getWindow().toFront();
        int n = JOptionPane.showConfirmDialog(null, "Do you want to save this comparison?", "Save", JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
            IJ.saveAsTiff(impl, null);
        }
               
        return impl;
        
    
    
    
    
    }
    
    
    
    
    
}
