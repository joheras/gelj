package db.Queries;

import db.Classes.Marker;
import static db.Queries.Connection.establishconnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class MarkerQueries {

    
    public static void insertMarker (Marker l){
        Connection con = null;
        
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO Marker (id,name,file)"
                    + "VALUES (?,?,?)");
            ps.setInt(1, l.getId());
            ps.setString(2,l.getName());
            ps.setString(3,l.getFile());
            ps.executeUpdate();
            ps.close(); 
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
    
    }
    
    
    public static ArrayList<Marker> getMarkers (){
        Connection con = null;
        Marker u = null;
        ArrayList<Marker> alu = new ArrayList<>();
        
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Marker");
            ResultSet res = ps.executeQuery();
            
            while (res.next()){
                u = new Marker(res.getInt("id"), res.getString("name"), res.getString("file"));
                alu.add(u);
            }
            res.close();
            ps.close(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
        return alu;
    }
    
    
    public static Marker getMarker (int id){
        Connection con = null;
        Marker u = null;
       
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Marker WHERE id=?");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            
            if (res.next()){
                u = new Marker(res.getInt("id"), res.getString("name"), res.getString("file"));
            }
            res.close();
            ps.close(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
        return u;
    }
    
    
    public static Marker getMarker (String marker){
        Connection con = null;
        Marker u = null;
       
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Marker WHERE name=?");
            ps.setString(1, marker);
            ResultSet res = ps.executeQuery();
            
            if (res.next()){
                u = new Marker(res.getInt("id"), res.getString("name"), res.getString("file"));
            }
            res.close();
            ps.close(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
        return u;
    }
    
    
    public static int lastMarkerid (){
        Connection con = null;
        int n = 0;
        
        
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM Marker "
                    + "ORDER BY id DESC ");
            ResultSet res = ps.executeQuery();
            
            if (res.next()){
                n = res.getInt("id") + 1;
            }
            res.close();
            ps.close(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
        
        return n;
    
    }
    
    
    public static void updateMarker (int id, Marker l){
        Connection con = null;
        
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("UPDATE Marker "
                    + "SET name=?, file=?"
                    + "WHERE id=?");
            ps.setString(1,l.getName());
            ps.setString(2,l.getFile());
            ps.setInt(3, id);
            ps.executeUpdate();
            ps.close(); 
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
    
    }
    
    
     public static boolean existsmarker (String name){
        boolean res1 = false;
        Connection con = null;
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Marker WHERE name=?");
            ps.setString(1, name);
            ResultSet res = ps.executeQuery();
            
            if (res.next()){
                res1=true;
            }
            res.close();
            ps.close(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
    
    
        return res1;
    }
    
     
     public static int existsmarker_id (String name){
        int res1 = -1;
        Connection con = null;
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Marker WHERE name=?");
            ps.setString(1, name);
            ResultSet res = ps.executeQuery();
            
            if (res.next()){
                res1=res.getInt(1);
            }
            res.close();
            ps.close(); 
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally{
        
           if (con != null){
               try {
                   con.close();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
        }
    
    
        return res1;
    }
    
    
    
    
    
}
