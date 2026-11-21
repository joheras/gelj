package db.Queries;

import db.Classes.LaneDB;
import static db.Queries.Connection.establishconnection;
import static db.Queries.ExperimentQueries.getExperiment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author joheras
 */
public class LaneQueries {

    public static void insertLane(LaneDB l) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO Lane (id,genus,species,strainNumber,country,MSLT,isMarker,comments,fkExperiment)"
                    + "VALUES (?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, l.getId());
            ps.setString(2, l.getGenus());
            ps.setString(3, l.getSpecies());
            ps.setString(4, l.getStrainNumber());
            ps.setString(5, l.getCountry());
            ps.setString(6, l.getMSLT());
            ps.setBoolean(7, l.isIsMarker());
            ps.setString(8, l.getComments());
            ps.setInt(9, l.getExperiment().getId());
            ps.executeUpdate();
            ps.close();

            int lai = lastLaneAddedInfoid();
            Enumeration<String> keys = l.getLaneaddedinfo().keys();

            for (int i = 0; i < l.getLaneaddedinfo().size(); i++) {
                PreparedStatement ps1 = con.prepareStatement("INSERT INTO LaneAddedInfo (id,fieldName,fieldValue,fkLane)"
                        + "VALUES (?,?,?,?)");
                ps1.setInt(1, lai);
                String key = keys.nextElement();
                ps1.setString(2, key);
                ps1.setString(3, l.getLaneaddedinfo().get(key));
                ps1.setInt(4, l.getId());
                lai++;
                ps1.executeUpdate();
                ps1.close();
            }
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
    
   
    

    public static ArrayList<LaneDB> getLanes(int studyid) {
        Connection con = null;
        LaneDB u = null;
        ArrayList<LaneDB> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "INNER JOIN Study ON Experiment.fkStudy=Study.id "
                    + "WHERE Experiment.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                u = new LaneDB(res.getInt(1), res.getString(2), res.getString(3),
                        res.getString(4), res.getString(5), res.getString(6),
                        res.getBoolean(7),
                        res.getString(8), getExperiment(res.getInt(9)));

                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, u.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    u.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();
                alu.add(u);
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
    
    
    public static ArrayList<LaneDB> getUserLanes(int userid) {
        Connection con = null;
        LaneDB u = null;
        ArrayList<LaneDB> alu = new ArrayList<>();

        try {
            con = establishconnection();
            //mysql
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "INNER JOIN Study ON Experiment.fkStudy=Study.id "
                    + "WHERE Study.fkUser=?");
            
            ps.setInt(1, userid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                u = new LaneDB(res.getInt(1), res.getString(2), res.getString(3),
                        res.getString(4), res.getString(5), res.getString(6),
                        res.getBoolean(7),
                        res.getString(8), getExperiment(res.getInt(9)));

                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, u.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    u.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();
                alu.add(u);
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
    
    
    public static ArrayList<LaneDB> getAllLanes() {
        Connection con = null;
        LaneDB u = null;
        ArrayList<LaneDB> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lane ");
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                u = new LaneDB(res.getInt(1), res.getString(2), res.getString(3),
                        res.getString(4), res.getString(5), res.getString(6),
                        res.getBoolean(7),
                        res.getString(8), getExperiment(res.getInt(9)));
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, u.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    u.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();
                alu.add(u);
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
    
    
    public static ArrayList<LaneDB> getLanesComparison(int compid) {
        Connection con = null;
        LaneDB u = null;
        ArrayList<LaneDB> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lane "
                    + "INNER JOIN ComparisonLane ON Lane.id=ComparisonLane.fkLane "
                    + "WHERE ComparisonLane.fkComparison=?");
            ps.setInt(1, compid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                u = new LaneDB(res.getInt(1), res.getString(2), res.getString(3),
                        res.getString(4), res.getString(5), res.getString(6),
                        res.getBoolean(7),
                        res.getString(8), getExperiment(res.getInt(9)));

                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, u.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    u.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();
                alu.add(u);
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

    
    public static ArrayList<LaneDB> getlanesexperiment(int expid,boolean markers) {
        Connection con = null;
        LaneDB u = null;
        ArrayList<LaneDB> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps =null;
            if (markers) {
                ps = con.prepareStatement("SELECT * FROM Lane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "WHERE Experiment.id=?");
                ps.setInt(1, expid);
            }else{
                ps = con.prepareStatement("SELECT * FROM Lane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "WHERE Experiment.id=? AND Lane.isMarker=?");
                ps.setInt(1, expid);
                ps.setBoolean(2, false);
            }
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                u = new LaneDB(res.getInt(1), res.getString(2), res.getString(3),
                        res.getString(4), res.getString(5), res.getString(6),
                        res.getBoolean(7),
                        res.getString(8), getExperiment(res.getInt(9)));

                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, u.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    u.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();
                alu.add(u);
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
    
    public static ArrayList<LaneDB> getstandardsexperiment(int expid) {
        Connection con = null;
        LaneDB u = null;
        ArrayList<LaneDB> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps =null;

                ps = con.prepareStatement("SELECT * FROM Lane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "WHERE Experiment.id=? AND Lane.isMarker=?");
                ps.setInt(1, expid);
                ps.setBoolean(2, true);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                u = new LaneDB(res.getInt(1), res.getString(2), res.getString(3),
                        res.getString(4), res.getString(5), res.getString(6),
                        res.getBoolean(7),
                        res.getString(8), getExperiment(res.getInt(9)));

                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, u.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    u.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();
                alu.add(u);
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

    public static LaneDB getLane(int id) {
        Connection con = null;
        LaneDB u = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Lane WHERE id=?");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                u = new LaneDB(res.getInt("id"), res.getString("genus"), res.getString("species"),
                        res.getString("strainNumber"), res.getString("country"), res.getString("MSLT"),
                        res.getBoolean("isMarker"),
                        res.getString("comments"), getExperiment(res.getInt("fkExperiment")));

                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, u.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    u.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();

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
        return u;
    }

    public static int lastLaneid() {
        Connection con = null;
        int n = 0;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM Lane "
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

    public static void updateLane(LaneDB l) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("UPDATE Lane "
                    + "SET genus=?, species=?, strainNumber=?,country=?,MSLT=?,isMarker=?,comments=? "
                    + "WHERE id=?");
            ps.setString(1, l.getGenus());
            ps.setString(2, l.getSpecies());
            ps.setString(3, l.getStrainNumber());
            ps.setString(4, l.getCountry());
            ps.setString(5, l.getMSLT());
            ps.setBoolean(6, l.isIsMarker());
            ps.setString(7, l.getComments());
            ps.setInt(8, l.getId());
            ps.executeUpdate();
            ps.close();

            Enumeration<String> keys = l.getLaneaddedinfo().keys();

            for (int i = 0; i < l.getLaneaddedinfo().size(); i++) {
                PreparedStatement ps1 = con.prepareStatement("UPDATE LaneAddedInfo "
                        + "SET fieldValue=? "
                        + "WHERE fkLane=? AND fieldName=?");
                String key = keys.nextElement();
                ps1.setString(1, l.getLaneaddedinfo().get(key));
                ps1.setInt(2, l.getId());
                ps1.setString(3, key);
                ps1.executeUpdate();
                ps1.close();
            }

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

    public static void updateLane(int id, Object o, int column) {
        Connection con = null;
        try {
            con = establishconnection();
            PreparedStatement ps = null;
            switch (column) {
                case 0:
                    ps = con.prepareStatement("UPDATE Lane "
                            + "SET genus=? "
                            + "WHERE id=? ");
                    ps.setString(1, String.valueOf(o));
                    ps.setInt(2, id);
                    break;
                case 1:
                    ps = con.prepareStatement("UPDATE Lane "
                            + "SET species=? "
                            + "WHERE id=? ");
                    ps.setString(1, String.valueOf(o));
                    ps.setInt(2, id);
                    break;
                case 2:
                    ps = con.prepareStatement("UPDATE Lane "
                            + "SET strainNumber=? "
                            + "WHERE id=? ");
                    ps.setString(1, String.valueOf(o));
                    ps.setInt(2, id);
                    break;
                case 3:
                    ps = con.prepareStatement("UPDATE Lane "
                            + "SET country=? "
                            + "WHERE id=? ");
                    ps.setString(1, String.valueOf(o));
                    ps.setInt(2, id);
                    break;
                case 4:
                    ps = con.prepareStatement("UPDATE Lane "
                            + "SET MSLT=? "
                            + "WHERE id=? ");
                    ps.setString(1, String.valueOf(o));
                    ps.setInt(2, id);
                    break;
                case 5:
                    ps = con.prepareStatement("UPDATE Lane "
                            + "SET isMarker=? "
                            + "WHERE id=? ");
                    ps.setBoolean(1, Boolean.valueOf(String.valueOf(o)));
                    ps.setInt(2, id);
                    break;
                default:
                    ps = con.prepareStatement("UPDATE Lane "
                            + "SET comments=? "
                            + "WHERE id=? ");
                    ps.setString(1, String.valueOf(o));
                    ps.setInt(2, id);
                    break;

            }
            
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

    public static int lastLaneAddedInfoid() {
        Connection con = null;
        int n = 0;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM LaneAddedInfo "
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

    
    public static ArrayList<String> getAdditionalFields(int studyid) {
        Connection con = null;
        
        ArrayList<String> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT fieldName FROM LaneAddedInfo "
                    + "INNER JOIN Lane ON LaneAddedInfo.fkLane=Lane.id "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "INNER JOIN Study ON Experiment.fkStudy=Study.id "
                    + "WHERE Experiment.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                String u = res.getString(1);                
                alu.add(u);
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
    
    
    public static ArrayList<String> getAdditionalFieldsExp (int expid) {
        Connection con = null;
        
        ArrayList<String> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT fieldName FROM LaneAddedInfo "
                    + "INNER JOIN Lane ON LaneAddedInfo.fkLane=Lane.id "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "WHERE Experiment.id=?");
            ps.setInt(1, expid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                String u = res.getString(1);                
                alu.add(u);
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
    

    
    public static void insertLaneAddedInfo (int laneid,String fieldName, String fieldValue) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO LaneAddedInfo (id,fieldName,fieldValue,fkLane)"
                    + "VALUES (?,?,?,?)");
            ps.setInt(1, lastLaneAddedInfoid());
            ps.setString(2, fieldName);
            ps.setString(3, fieldValue);
            ps.setInt(4, laneid);
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
    
    
    public static boolean containadditionalfield (int laneid,String fieldName) {
        Connection con = null;
        boolean result =false;
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                    + "WHERE fkLane=? AND fieldName=?");
            ps.setInt(1, laneid);
            ps.setString(2, fieldName);
            ResultSet res = ps.executeQuery();
            result = res.next();
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
        
        return result;
    }
    
    
    
    
    public static void deletelane(int laneid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM Lane "
                    + "WHERE id=?");
            ps.setInt(1, laneid);
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
    
        public static void deletelanecomparison(int laneid,int compid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM ComparisonLane "
                    + "WHERE fkComparison=? AND fkLane=?");
            ps.setInt(1, compid);
            ps.setInt(2, laneid);
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
    
    
    
    
    public static void deletecomparisonslane(int laneid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM Comparison "
                    + "INNER JOIN ComparisonLane ON Comparison.id=ComparisonLane.fkComparison "
                    + "INNER JOIN Lane ON Lane.id=ComparisonLane.fkLane "
                    + "WHERE Lane.id=?");
            ps.setInt(1, laneid);
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
    
    
    public static boolean existscomparisonslane (int laneid){
        
        Connection con = null;
        boolean result = false;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM ComparisonLane "
                    + "INNER JOIN Lane ON Lane.id=ComparisonLane.fkLane "
                    + "WHERE Lane.id=?");
            ps.setInt(1, laneid);
            ResultSet res = ps.executeQuery();

            result = res.next();
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
        return result;
        
        
    
    }
    
    
    
    public static ArrayList<String> getGenus(int studyid) {
        Connection con = null;
        
        ArrayList<String> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT genus FROM Lane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "INNER JOIN Study ON Experiment.fkStudy=Study.id "
                    + "WHERE Experiment.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                String u = res.getString(1);                
                alu.add(u);
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
    
    
    public static ArrayList<String> getSpecies(int studyid) {
        Connection con = null;
        
        ArrayList<String> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT species FROM Lane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "INNER JOIN Study ON Experiment.fkStudy=Study.id "
                    + "WHERE Experiment.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                String u = res.getString(1);                
                alu.add(u);
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
    
    
    
    public static ArrayList<LaneDB> searchlanes(int studyid, String genus,String species,String strainnumber,String country,String MSLT,String experiment, String fromdate, String todate,int n) {
        Connection con = null;
        LaneDB l = null;
        ArrayList<LaneDB> alu = new ArrayList<>();

        try {
            con = establishconnection();
            int m = n;
            String query = "SELECT * FROM Lane"
                    + " INNER JOIN Experiment ON Experiment.id=Lane.fkExperiment"
                    + " INNER JOIN Study ON Experiment.fkStudy=Study.id"
                    + " WHERE Study.id=" + studyid +" AND ";
            
            if(!genus.equals("")){
                query = query + " Lane.genus='" + genus + "' ";
                m--;
                if(m!=0){
                    query = query + " AND ";
                }
            }
            if(!species.equals("")){
                query = query + " Lane.species='" + species + "' ";
                m--;
                if(m!=0){
                    query = query + " AND ";
                }
            }
            if(!strainnumber.equals("")){
                query = query + " Lane.strainNumber=" + strainnumber + " ";
                m--;
                if(m!=0){
                    query = query + " AND ";
                }
            }
            if(!country.equals("")){
                query = query + " Lane.country='" + country + "' ";
                m--;
                if(m!=0){
                    query = query + " AND ";
                }
            }
            if(!MSLT.equals("")){
                query = query + " Lane.MSLT='" + MSLT + "' ";
                m--;
                if(m!=0){
                    query = query + " AND ";
                }
            }
            if(!experiment.equals("")){
                query = query + " Experiment.name='" + experiment + "' ";
                m--;
                if(m!=0){
                    query = query + " AND ";
                }
            }
            
            
            if(!fromdate.equals("")){
                query = query + " Experiment.date >=Date('" + fromdate + "') ";
                m--;
                if(m!=0){
                    query = query + " AND ";
                }
            }
            if(!todate.equals("")){
                query = query + " Experiment.date <=Date('" + todate.toString() + "') ";
                
            }
            
            
            
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                l = new LaneDB(res.getInt(1), res.getString(2), res.getString(3),
                        res.getString(4), res.getString(5), res.getString(6),
                        res.getBoolean(7),
                        res.getString(8), getExperiment(res.getInt(9)));

                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM LaneAddedInfo "
                        + "WHERE fkLane=?");
                ps1.setInt(1, l.getId());
                ResultSet res1 = ps1.executeQuery();
                while (res1.next()) {
                    l.addnewinfo(res1.getString("fieldName"), res1.getString("fieldValue"));
                }
                res1.close();
                ps1.close();
                alu.add(l);
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
    
    
    
}
