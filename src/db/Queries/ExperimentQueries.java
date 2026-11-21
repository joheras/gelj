package db.Queries;

import db.Classes.Experiment;
import static db.Queries.BackupQueries.addToZip;
import static db.Queries.BackupQueries.getAllFiles;
import static db.Queries.Connection.establishconnection;
import static db.Queries.MarkerQueries.getMarker;
import static db.Queries.StudyQueries.getStudy;
import static db.Queries.UserQueries.getUser;
import ij.io.SaveDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author joheras
 */
public class ExperimentQueries {

    public static void insertExperiment(Experiment s) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO Experiment (id, name, date, imageName, calibrationMethod, comments, fkStudy,fkMarker,fkUser)"
                    + "VALUES (?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, s.getId());
            ps.setString(2, s.getName());
            ps.setDate(3, s.getDate());
            ps.setString(4, s.getImagename());
            ps.setInt(5, s.getCalibrationMethod());
            ps.setString(6, s.getComments());
            ps.setInt(7, s.getStudy().getId());
            ps.setInt(8, s.getMarker().getId());
            ps.setInt(9, s.getUser().getId());
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

    public static ArrayList<Experiment> getExperiments(int studyid) {
        Connection con = null;
        Experiment s = null;
        ArrayList<Experiment> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Experiment INNER JOIN Study"
                    + " ON Experiment.fkStudy=Study.id"
                    + " WHERE Experiment.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new Experiment(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getInt(5),
                        res.getString(6), getStudy(studyid),
                        getMarker(res.getInt(8)),
                        getUser(res.getInt(9)));

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

    public static ArrayList<Experiment> getExperimentsofOtherStudies(int studyid) {
        Connection con = null;
        Experiment s = null;
        ArrayList<Experiment> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Experiment INNER JOIN Study"
                    + " ON Experiment.fkStudy=Study.id"
                    + " WHERE NOT Experiment.fkStudy=?");
            ps.setInt(1, studyid);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new Experiment(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getInt(5),
                        res.getString(6), getStudy(res.getInt(7)),
                        getMarker(res.getInt(8)),
                        getUser(res.getInt(9)));

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

    public static ArrayList<Experiment> getAllexperiments() {
        Connection con = null;
        Experiment s = null;
        ArrayList<Experiment> alu = new ArrayList<>();

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Experiment");
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new Experiment(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getInt(5),
                        res.getString(6), getStudy(res.getInt(7)),
                        getMarker(res.getInt(8)),
                        getUser(res.getInt(9)));

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

    public static boolean anyexperiment() {
        Connection con = null;
        boolean b = false;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Experiment");
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                b = true;
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
        return b;
    }

    public static int lastExperimentid() {
        Connection con = null;
        int n = 0;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM Experiment "
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

    public static void updateExperiment(int id, Experiment s) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("UPDATE Experiment "
                    + "SET name=?, date=?, imageName=?, calibrationMethod=?,comments=? "
                    + "WHERE id=?");

            ps.setString(1, s.getName());
            ps.setDate(2, s.getDate());
            ps.setString(3, s.getImagename());
            ps.setInt(4, s.getCalibrationMethod());
            ps.setString(5, s.getComments());
            ps.setInt(6, id);
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

    public static void updateExperimentnameorcomments(int id, String change, boolean nc) {
        Connection con = null;

        try {
            con = establishconnection();

            // if nc is true, we change the name; otherwise, we change the comments
            PreparedStatement ps = null;
            if (nc) {
                ps = con.prepareStatement("UPDATE Experiment "
                        + "SET name=? "
                        + "WHERE id=?");
            } else {
                ps = con.prepareStatement("UPDATE Experiment "
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

            PreparedStatement ps = con.prepareStatement("UPDATE Experiment "
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

    public static Experiment getExperiment(int id) {
        Connection con = null;
        Experiment s = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Experiment INNER JOIN Study"
                    + " ON Experiment.fkStudy=Study.id"
                    + " WHERE Experiment.id=?");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                s = new Experiment(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getInt(5),
                        res.getString(6),
                        getStudy(res.getInt(7)), getMarker(res.getInt(8)),
                        getUser(res.getInt(9)));
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

    public static boolean existsexperiment(String name, int studyid) {
        boolean res1 = false;
        Connection con = null;
        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Experiment INNER JOIN Study"
                    + " ON Experiment.fkStudy=Study.id"
                    + " WHERE Study.id=? AND Experiment.name=?");
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

    public static void deleteexperiment(int expid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM Experiment "
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

    public static void deletecomparisonsexperiment(int expid) {
        Connection con = null;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Comparison "
                    + "INNER JOIN ComparisonLane ON Comparison.id=ComparisonLane.fkComparison "
                    + "INNER JOIN Lane ON Lane.id=ComparisonLane.fkLane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "WHERE Experiment.id=?");
            ps.setInt(1, expid);
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                PreparedStatement ps1 = con.prepareStatement("DELETE FROM Comparison "
                        + "WHERE id=?");
                ps1.setInt(1, res.getInt(1));
                ps1.executeUpdate();
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

    }

    public static boolean existscomparisonsexperiment(int expid) {

        Connection con = null;
        boolean result = false;

        try {
            con = establishconnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM ComparisonLane "
                    + "INNER JOIN Lane ON Lane.id=ComparisonLane.fkLane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + "WHERE Experiment.id=?");
            ps.setInt(1, expid);
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

    public static ArrayList<Experiment> searchexperiments(int studyid, String name, String author, String marker, String fromdate, String todate, int n) {
        Connection con = null;
        Experiment s = null;
        ArrayList<Experiment> alu = new ArrayList<>();

        try {
            con = establishconnection();
            int m = n;
            //mysql
//            String query = "SELECT * FROM Experiment"
//                    + " INNER JOIN User ON Experiment.fkUser=User.id"
//                    + " INNER JOIN Study ON Experiment.fkStudy=Study.id"
//                    + " INNER JOIN Marker ON Experiment.fkMarker=Marker.id"
//                    + " WHERE Study.id='" + studyid +"' AND ";
            //javadb
            String query = "SELECT * FROM Experiment"
                    + " INNER JOIN User0 ON Experiment.fkUser=User0.id"
                    + " INNER JOIN Study ON Experiment.fkStudy=Study.id"
                    + " INNER JOIN Marker ON Experiment.fkMarker=Marker.id"
                    + " WHERE Study.id=" + studyid + " AND ";

            if (!name.equals("")) {
                query = query + " Experiment.name='" + name + "' ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!author.equals("")) {
                //mysql
//                query = query + " User.username='" + author + "' ";
                //javadb
                query = query + " User0.username='" + author + "' ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!marker.equals("")) {
                query = query + " Marker.name='" + marker + "' ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!fromdate.equals("")) {
                //mysql
//                query = query + " Experiment.date >='" + fromdate + "' ";
                //javadb
                query = query + " Experiment.date >= Date('" + fromdate + "') ";
                m--;
                if (m != 0) {
                    query = query + " AND ";
                }
            }
            if (!todate.equals("")) {
                //mysql
//                query = query + " Experiment.date <='" + todate.toString() + "' ";
                //javadb
                query = query + " Experiment.date <= Date('" + todate + "') ";
            }

            PreparedStatement ps = con.prepareStatement(query);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                s = new Experiment(res.getInt(1), res.getString(2),
                        res.getDate(3), res.getString(4),
                        res.getInt(5),
                        res.getString(6), getStudy(res.getInt(7)),
                        getMarker(res.getInt(8)),
                        getUser(res.getInt(9)));

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

    public static void exportexperiment(Experiment e) throws IOException {

        Connection con = null;

        try {
            con = establishconnection();
            File f0 = new File(System.getProperty("user.dir") + "/experiment.exp");
            f0.delete();
            PreparedStatement ps = con.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY "
                    + "('SELECT * FROM Experiment WHERE Experiment.id=" + e.getId() + "' , '"
                    + f0.getPath() + "',null,null,null)");
            ps.executeUpdate();
            ps.close();
            
            File f1 = new File(System.getProperty("user.dir") + "/experiment.lanes");
            f1.delete();
            PreparedStatement ps1 = con.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY "
                    + "('SELECT * FROM Lane INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + " WHERE Experiment.id=" + e.getId() + "' , '"
                    + f1.getPath() + "',null,null,null)");
            ps1.executeUpdate();
            ps1.close();
            
            
            File f2 = new File(System.getProperty("user.dir") + "/experiment.marker");
            f2.delete();
            PreparedStatement ps2 = con.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY "
                    + "('SELECT * FROM Marker INNER JOIN Experiment ON Marker.id=Experiment.fkMarker "
                    + " WHERE Experiment.id=" + e.getId() + "' , '"
                    + f2.getPath() + "',null,null,null)");
            ps2.executeUpdate();
            ps2.close();
            
            
            File f3 = new File(System.getProperty("user.dir") + "/experiment.user");
            f3.delete();
            PreparedStatement ps3 = con.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY "
                    + "('SELECT * FROM User0 INNER JOIN Lab ON User0.fkLab=Lab.id "
                    + "INNER JOIN Experiment ON Experiment.fkUser = User0.id "
                    + " WHERE Experiment.id=" + e.getId() + "' , '"
                    + f3.getPath() + "',null,null,null)");
            ps3.executeUpdate();
            ps3.close();
            
            
            File f4 = new File(System.getProperty("user.dir") + "/experiment.adds");
            f4.delete();
            PreparedStatement ps4 = con.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY "
                    + "('SELECT * FROM LaneAddedInfo "
                    + "INNER JOIN Lane ON Lane.id = LaneAddedInfo.fkLane "
                    + "INNER JOIN Experiment ON Lane.fkExperiment=Experiment.id "
                    + " WHERE Experiment.id=" + e.getId() + "' , '"
                    + f4.getPath() + "',null,null,null)");
            ps4.executeUpdate();
            ps4.close();
            
            
            
            List<File> fileList = new ArrayList<>();
            fileList.add(f0);
            fileList.add(f1);
            fileList.add(f2);
            fileList.add(f3);
            fileList.add(f4);
            
            File marker = new File(System.getProperty("user.dir") + "/standardfiles/" + e.getMarker().getName() + ".marker");
            fileList.add(marker);
            marker = new File(System.getProperty("user.dir") + "/standardfiles/info/" + e.getMarker().getName() + ".info");
            fileList.add(marker);
            marker = new File(System.getProperty("user.dir") + "/standardfiles/info/" + e.getMarker().getName() + ".pos");
            fileList.add(marker);
            zipexperiment(e,fileList);
            f0.delete();
            f1.delete();
            f2.delete();
            f3.delete();
            f4.delete();
            

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
    
    
    
    public static void zipexperiment(Experiment exp,List<File> fileList) throws IOException{
        
        
        File dbdirectoryToZip = new File(System.getProperty("user.dir") + "/GelJDBImages/" +
                exp.getStudy().getId()+"/" + exp.getUser().getUsername() + "/experiments/");
        getAllFilesExperiment(dbdirectoryToZip, fileList);
        writeZipFileExperiment(exp,fileList);
    }
    
    public static void getAllFilesExperiment(File dir, List<File> fileList) {
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                    fileList.add(file);
                    if (file.isDirectory()) {
                        System.out.println("directory:" + file.getCanonicalPath());
                        getAllFiles(file, fileList, false);
                    } else {
                        System.out.println("     file:" + file.getCanonicalPath());
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void writeZipFileExperiment(Experiment e,List<File> fileList) throws FileNotFoundException, IOException {

            SaveDialog sd = new SaveDialog("Export experiment...",
                    e.getName(), ".zip");
            String dir = sd.getDirectory();
            String name = sd.getFileName();
            if (dir == null || name == null) {
                return;
            }

            File directoryToZip = new File(System.getProperty("user.dir"));

            FileOutputStream fos = new FileOutputStream(dir + name);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : fileList) {
                if (!file.isDirectory()) { // we only zip files, not directories
                    addToZip(directoryToZip, file, zos);
                }
            }
            zos.close();
            fos.close();
    }
    

}
