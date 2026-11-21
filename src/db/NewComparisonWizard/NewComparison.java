package db.NewComparisonWizard;

import db.Classes.Comparison;
import db.Classes.Experiment;
import db.Classes.LaneDB;
import db.Classes.Study;
import static db.Queries.ComparisonQueries.lastComparisonid;
import static db.Queries.StudyQueries.getStudy;
import java.sql.Date;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class NewComparison {
    
    private int studyid;
    private String name;
    private String comments;
    private double tolerance = 1;
    private ArrayList<Experiment> nonSelectedExperiments= new ArrayList<>();
    private ArrayList<Experiment> SelectedExperiments= new ArrayList<>();
    private ArrayList<LaneDB> nonSelectedLanes = new ArrayList<>();
    private ArrayList<LaneDB> SelectedLanes= new ArrayList<>();
    private String linkage="UPGMA";
    private String similaritymethod="Dice";
    private ArrayList<String> nonSelectedFields;
    private ArrayList<String> SelectedFields;
    private boolean markers = false;
    private int id = -1;
    

    public NewComparison(int studyid) {
        this.studyid=studyid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ArrayList<Experiment> getNonSelectedExperiments() {
        return nonSelectedExperiments;
    }

    public void setNonSelectedExperiments(ArrayList<Experiment> nonSelectedExperiments) {
        this.nonSelectedExperiments = nonSelectedExperiments;
    }

    public ArrayList<Experiment> getSelectedExperiments() {
        return SelectedExperiments;
    }

    public void setSelectedExperiments(ArrayList<Experiment> SelectedExperiments) {
        this.SelectedExperiments = SelectedExperiments;
    }

    public ArrayList<LaneDB> getNonSelectedLanes() {
        return nonSelectedLanes;
    }

    public void setNonSelectedLanes(ArrayList<LaneDB> nonSelectedLanes) {
        this.nonSelectedLanes = nonSelectedLanes;
    }

    public ArrayList<LaneDB> getSelectedLanes() {
        return SelectedLanes;
    }

    public void setSelectedLanes(ArrayList<LaneDB> SelectedLanes) {
        this.SelectedLanes = SelectedLanes;
    }

    public String getLinkage() {
        return linkage;
    }

    public void setLinkage(String linkage) {
        this.linkage = linkage;
    }

    public String getSimilaritymethod() {
        return similaritymethod;
    }

    public void setSimilaritymethod(String similaritymethod) {
        this.similaritymethod = similaritymethod;
    }

    public ArrayList<String> getNonSelectedFields() {
        return nonSelectedFields;
    }

    public void setNonSelectedFields(ArrayList<String> nonSelectedFields) {
        this.nonSelectedFields = nonSelectedFields;
    }

    public ArrayList<String> getSelectedFields() {
        return SelectedFields;
    }

    public void setSelectedFields(ArrayList<String> SelectedFields) {
        this.SelectedFields = SelectedFields;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
    
    
    public Comparison toComparison(){
        if(id==-1){
            id = lastComparisonid();
        }
        Study s = getStudy(studyid);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date date = new Date(utilDate.getTime());

        Comparison c = new Comparison(id, name, date, similaritymethod, linkage, tolerance, comments, s);
        c.setLanes(SelectedLanes);
        return c;
    
    
    }

    public int getStudyid() {
        return studyid;
    }

    public void setStudyid(int studyid) {
        this.studyid = studyid;
    }

    public boolean isMarkers() {
        return markers;
    }

    public void setMarkers(boolean markers) {
        this.markers = markers;
    }
    
    
    

}
