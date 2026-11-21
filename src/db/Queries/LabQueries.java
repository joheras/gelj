package db.Queries;

import db.Classes.Lab;
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
public class LabQueries {

    
    public static void insertLab (Lab l){
        Connection con = null;
        
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO Lab (id, name, city, country)"
                    + "VALUES (?,?,?,?)");
            ps.setInt(1, l.getId());
            ps.setString(2,l.getName());
            ps.setString(3,l.getCity());
            ps.setString(4,l.getCountry());
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
    
    
    public static ArrayList<Lab> getLabs (){
        Connection con = null;
        Lab u = null;
        ArrayList<Lab> alu = new ArrayList<>();
        
        try {
            con = establishconnection();;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lab");
            ResultSet res = ps.executeQuery();
            
            while (res.next()){
                u = new Lab(res.getInt("id"), res.getString("name"), res.getString("city"),res.getString("country"));
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
    
    
    public static Lab getLab (int id){
        Connection con = null;
        Lab u = null;
       
        try {
            con = establishconnection();;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lab WHERE id=?");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            
            if (res.next()){
                u = new Lab(res.getInt("id"), res.getString("name"), res.getString("city"), res.getString("country"));
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
    
    
    public static int lastLabid (){
        Connection con = null;
        int n = 0;
        
        
        try {
            con = establishconnection();;
            PreparedStatement ps = con.prepareStatement("SELECT id FROM Lab "
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
    
    
    public static void updateLab (int id, Lab l){
        Connection con = null;
        
        try {
            con = establishconnection();;
            PreparedStatement ps = con.prepareStatement("UPDATE Lab "
                    + "SET name=?, city=?, country=? "
                    + "WHERE id=?");
            ps.setString(1,l.getName());
            ps.setString(2,l.getCity());
            ps.setString(3,l.getCountry());
            ps.setInt(4, id);
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
    
    
     public static boolean existslab (String name,String city, String country){
        boolean res1 = false;
        Connection con = null;
        try {
            con = establishconnection();;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lab WHERE name=? AND city=? AND country=?");
            ps.setString(1, name);
            ps.setString(2, city);
            ps.setString(3, country);
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
    
     
     
      public static int existslab_id (String name,String city, String country){
        int res1 = -1;
        Connection con = null;
        try {
            con = establishconnection();;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lab WHERE name=? AND city=? AND country=?");
            ps.setString(1, name);
            ps.setString(2, city);
            ps.setString(3, country);
            ResultSet res = ps.executeQuery();
            
            if (res.next()){
                res1=res.getInt("id");
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
