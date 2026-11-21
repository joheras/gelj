package db.Queries;

import db.Classes.Study;
import static db.Queries.Connection.establishconnection;
import static db.Queries.UserQueries.getUser;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class StudyQueries {

    public static void insertStudy(Study s) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO Study (id, name, date, path, comments, fkUser)"
                    + "VALUES (?,?,?,?,?,?)");
            ps.setInt(1, s.getId());
            ps.setString(2, s.getName());
            ps.setDate(3, s.getDate());
            ps.setString(4, s.getPath());
            ps.setString(5, s.getComments());
            ps.setInt(6, s.getUser().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    public static ArrayList<Study> getStudies(int userid) {
        Connection con = null;
        Study s = null;
        ArrayList<Study> alu = new ArrayList<>();

        try {
            con = establishconnection();
            //mysql
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM Study INNER JOIN User"
//                    + " ON Study.fkUser=User.id"
//                    + " WHERE Study.fkUser=?");
            //javadb
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Study INNER JOIN User0"
                    + " ON Study.fkUser=User0.id"
                    + " WHERE Study.fkUser=?");
            
            ps.setInt(1, userid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new Study(res.getInt(1),res.getString(2),
                    res.getDate(3),res.getString(4),
                    res.getString(5),
                    getUser(userid));
                
                alu.add(s);
            }
            res.close();
            ps.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return alu;
    }

    public static int lastStudyid() {
        Connection con = null;
        int n = 0;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM Study "
                    + "ORDER BY id DESC ");
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                n = res.getInt("id") + 1;
            }
            res.close();
            ps.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return n;

    }

    public static void updateStudy(int id, Study s) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("UPDATE Study "
                    + "SET name=?, date=?, path=?, comments=? "
                    + "WHERE id=?");
            
            ps.setString(1, s.getName());
            ps.setDate(2, s.getDate());
            ps.setString(3, s.getPath());
            ps.setString(4, s.getComments());
            ps.setInt(5, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    
        }
    
    
    public static void updateStudynameorcomments(int id, String change,boolean nc) {
        Connection con = null;

        try {
            con = establishconnection();
            
            // if nc is true, we change the name; otherwise, we change the comments
            PreparedStatement ps = null;
            if(nc){
                ps = con.prepareStatement("UPDATE Study "
                    + "SET name=? "
                    + "WHERE id=? ");
            }else{
                ps = con.prepareStatement("UPDATE Study "
                    + "SET comments=? "
                    + "WHERE id=? ");
            }
            ps.setString(1, change);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    
        }
    
    
    public static void updateDate(int id, Date date) {
        Connection con = null;

        try {
            con = establishconnection();
            
            // if nc is true, we change the name; otherwise, we change the comments
            PreparedStatement ps = con.prepareStatement("UPDATE Study "
                    + "SET date=? "
                    + "WHERE id=? ");
            ps.setDate(1, date);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    
        }
        

    public static Study getStudy(int id) {
        Connection con = null;
        Study s = null;

        try {
            con = establishconnection();
            //mysql
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM Study INNER JOIN User"
//                    + " ON Study.fkUser=User.id"
//                    + " WHERE Study.id=?");
            //javadb
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM Study INNER JOIN User0"
                    + " ON Study.fkUser=User0.id"
                    + " WHERE Study.id=?");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                 s = new Study(res.getInt(1),res.getString(2),
                    res.getDate(3),res.getString(4),
                    res.getString(5),
                    getUser(res.getInt(6)));
            }
            res.close();
            ps.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return s;
    }
    
    
    public static boolean existsstudy (String name,int userid){
        boolean res1 = false;
        Connection con = null;
        try {
            con = establishconnection();
            //mysql
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM Study INNER JOIN User"
//                    + " ON Study.fkUser=User.id"
//                    + " WHERE User.id=? AND Study.name=?");
            //javadb
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM Study INNER JOIN User0"
                    + " ON Study.fkUser=User0.id"
                    + " WHERE User.id=? AND Study.name=?");
            ps.setInt(1, userid);
            ps.setString(2, name);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
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
    
    
    
    public static void deletestudy(int studyid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM Study "
                    + "WHERE id=?");
            ps.setInt(1, studyid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    
        }
    

    
    
    


}
