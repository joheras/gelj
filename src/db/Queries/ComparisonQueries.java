package db.Queries;

import db.Classes.Comparison;
import db.Classes.LaneDB;
import static db.Queries.Connection.establishconnection;
import static db.Queries.LaneQueries.getLane;
import static db.Queries.StudyQueries.getStudy;
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
public class ComparisonQueries {

    public static void insertComparison(Comparison s) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO Comparison (id, name, date, similarityMethod, linkage, tolerance,comments,fkStudy)"
                    + "VALUES (?,?,?,?,?,?,?,?)");
            ps.setInt(1, s.getId());
            ps.setString(2, s.getName());
            ps.setDate(3, s.getDate());
            ps.setString(4, s.getSimilarityMethod());
            ps.setString(5, s.getLinkage());
            ps.setDouble(6, s.getTolerance());
            ps.setString(7, s.getComments());
            ps.setInt(8, s.getStudy().getId());
            ps.executeUpdate();
            ps.close();

            ArrayList<LaneDB> lanes = s.getLanes();

            for (LaneDB l : lanes) {
                PreparedStatement ps1 = con.prepareStatement("INSERT INTO ComparisonLane (fkComparison,fkLane)"
                        + "VALUES (?,?)");
                ps1.setInt(1, s.getId());
                ps1.setInt(2, l.getId());
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

    public static void insertComparisonLane(int compid, ArrayList<LaneDB> lanes) {
        Connection con = null;

        try {
            con = establishconnection();

            for (LaneDB l : lanes) {
                PreparedStatement ps1 = con.prepareStatement("INSERT INTO ComparisonLane (fkComparison,fkLane)"
                        + "VALUES (?,?)");
                ps1.setInt(1, compid);
                ps1.setInt(2, l.getId());
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
    
    public static void insertComparisonLane(int compid, int laneid) {
        Connection con = null;

        try {
            con = establishconnection();


                PreparedStatement ps1 = con.prepareStatement("INSERT INTO ComparisonLane (fkComparison,fkLane)"
                        + "VALUES (?,?)");
                ps1.setInt(1, compid);
                ps1.setInt(2, laneid);
                ps1.executeUpdate();
                ps1.close();

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


    public static ArrayList<Comparison> getComparisons(int studyid) {
        Connection con = null;
        Comparison s = null;
        ArrayList<Comparison> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Comparison INNER JOIN Study"
                    + " ON Comparison.fkStudy=Study.id"
                    + " WHERE Comparison.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new Comparison(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getString(5),
                        res.getInt(6),
                        res.getString(7), getStudy(studyid));
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM ComparisonLane"
                        + " WHERE fkComparison=?");
                ps1.setInt(1, res.getInt(1));
                ResultSet res1 = ps1.executeQuery();

                while (res1.next()) {
                    s.addLane(getLane(res1.getInt("fkLane")));
                }
                res1.close();
                ps1.close();

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

    public static int lastComparisonid() {
        Connection con = null;
        int n = 0;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM Comparison "
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

    public static void updateComparison(Comparison s) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("UPDATE Comparison "
                    + "SET name=?, date=?, similarityMethod=?, linkage=?, tolerance=?,comments=? "
                    + "WHERE id=?");

            ps.setString(1, s.getName());
            ps.setDate(2, s.getDate());
            ps.setString(3, s.getSimilarityMethod());
            ps.setString(4, s.getLinkage());
            ps.setDouble(5, s.getTolerance());
            ps.setString(6, s.getComments());
            ps.setInt(7, s.getId());
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

    public static void updateComparisonnameorcomments(int id, String change, boolean nc) {
        Connection con = null;

        try {
            con = establishconnection();

            // if nc is true, we change the name; otherwise, we change the comments
            PreparedStatement ps = null;
            if (nc) {
                ps = con.prepareStatement("UPDATE Comparison "
                        + "SET name=? "
                        + "WHERE id=?");
            } else {
                ps = con.prepareStatement("UPDATE Comparison "
                        + "SET comments=? "
                        + "WHERE id=?");
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

            PreparedStatement ps = con.prepareStatement("UPDATE Comparison "
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

    public static Comparison getComparison(int id) {
        Connection con = null;
        Comparison s = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Comparison INNER JOIN Study"
                    + " ON Comparison.fkStudy=Study.id"
                    + " WHERE Comparison.id=?");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                s = new Comparison(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getString(5),
                        res.getInt(6),
                        res.getString(7),
                        getStudy(res.getInt(8)));
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM ComparisonLane"
                        + " WHERE fkComparison=?");
                ps1.setInt(1, id);
                ResultSet res1 = ps1.executeQuery();

                while (res1.next()) {
                    s.addLane(getLane(res1.getInt("fkLane")));
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
        return s;
    }

    public static boolean existscomparison(String name, int studyid) {
        boolean res1 = false;
        Connection con = null;
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Comparison INNER JOIN Study"
                    + " ON Comparison.fkStudy=Study.id"
                    + " WHERE Study.id=? AND Comparison.name=?");
            ps.setInt(1, studyid);
            ps.setString(2, name);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                res1 = true;
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
        return res1;
    }

    public static void deletecomparison(int compid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM Comparison "
                    + "WHERE id=?");
            ps.setInt(1, compid);
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

    public static ArrayList<Comparison> searchcomparisons(int studyid, String name, String similarity, String linkage, String tolerance, String fromdate, String todate, int n) {
        Connection con = null;
        Comparison s = null;
        ArrayList<Comparison> alu = new ArrayList<>();

        try {
            con = establishconnection();
            int m = n;
            String query = "SELECT * FROM Comparison"
                    + " INNER JOIN Study ON Comparison.fkStudy=Study.id"
                    + " WHERE Study.id=" + studyid + " AND ";

            if (!name.equals("")) {
                query = query + " Comparison.name='" + name + "' ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!similarity.equals(" ")) {
                query = query + " Comparison.similarityMethod='" + similarity + "' ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!linkage.equals(" ")) {
                query = query + " Comparison.linkage='" + linkage + "' ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!tolerance.equals("")) {
                query = query + " Comparison.tolerance=" + tolerance + " ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!fromdate.equals("")) {
                query = query + " Comparison.date >=Date('" + fromdate + "') ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!todate.equals("")) {
                query = query + " Comparison.date <=Date('" + todate.toString() + "') ";

            }

            PreparedStatement ps = con.prepareStatement(query);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new Comparison(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getString(5),
                        res.getInt(6),
                        res.getString(7), getStudy(studyid));
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM ComparisonLane"
                        + " WHERE fkComparison=?");
                ps1.setInt(1, studyid);
                ResultSet res1 = ps1.executeQuery();

                while (res1.next()) {
                    s.addLane(getLane(res1.getInt("fkLane")));
                }
                res1.close();
                ps1.close();

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

}
