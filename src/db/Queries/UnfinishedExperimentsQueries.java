package db.Queries;

import db.Classes.UnfinishedExperiment;
import static db.Queries.Connection.establishconnection;
import static db.Queries.StudyQueries.getStudy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class UnfinishedExperimentsQueries {
    
    
    public static void insertUnfinishedExperiment(UnfinishedExperiment s) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO UnfinishedExperiment (id, name, step, path, fkStudy)"
                    + "VALUES (?,?,?,?,?)");
            ps.setInt(1, s.getId());
            ps.setString(2, s.getName());
            ps.setInt(3, s.getStep());
            ps.setString(4, "");
            ps.setInt(5, s.getStudy().getId());
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
    
    
    public static ArrayList<UnfinishedExperiment> getUnfinishedExperiments(int studyid) {
        Connection con = null;
        UnfinishedExperiment s = null;
        ArrayList<UnfinishedExperiment> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM UnfinishedExperiment INNER JOIN Study"
                    + " ON UnfinishedExperiment.fkStudy=Study.id"
                    + " WHERE UnfinishedExperiment.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new UnfinishedExperiment(res.getInt(1),res.getString(2),
                        res.getInt(3),
                    getStudy(studyid));
                
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
    
    public static int lastUnfinishedExperimentid() {
        Connection con = null;
        int n = 0;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM UnfinishedExperiment "
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
    
    public static void deleteunfinishedexperiment(int expid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM UnfinishedExperiment "
                    + "WHERE id=?");
            ps.setInt(1, expid);
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
