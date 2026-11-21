package db.Classes;

import java.sql.Date;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class Experiment {
    
    private int id;
    private String name;
    private Date date;
    private String imagename;
    private String comments;
    private Study study;
    private User user;
    private Marker marker;
    private int calibrationMethod;

    public Experiment(int id, String name, Date date, String imagename, int calibrationMethod, String comments, Study study, Marker marker,User user) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.imagename = imagename;
        this.calibrationMethod = calibrationMethod;
        this.comments = comments;
        this.study = study;
        this.user = user;
        this.marker = marker;
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

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    
    @Override
    public String toString() {
        return name;
    }
    
    
    public String printInfo() {
        String res = "<html>Experiment " + name + ":<br>";
        res = res + "   author: " + user.getName() + " " + user.getSurname() + "<br>";
        res = res + "   date: " + date + "<br>";
        res = res + "   image: " + imagename + "<br>";
        if(comments!=null){
        res = res + "   Comments: " + comments + "</html>";
        }
        return res;
    }

    public int getCalibrationMethod() {
        return calibrationMethod;
    }

    public void setCalibrationMethod(int calibrationMethod) {
        this.calibrationMethod = calibrationMethod;
    }
    
    
    
    

    public static boolean allwithsamemarker (ArrayList<Experiment> exps){
       
        for(int i=0;i<exps.size();i++){
            if(exps.get(i).getMarker().getId()!=exps.get(0).getMarker().getId()){
                return false;
            }
        }
        
        return true;
        
        
    
    
    
    }
    
    
    
    
    
    
    

}
