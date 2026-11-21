package db.Queries;

import db.Classes.Lab;
import db.Classes.User;
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
public class UserQueries {

    public static void insertUser(User u) {
        Connection con = null;

        try {
            con = establishconnection();
            // Mysql
//            PreparedStatement ps = con.prepareStatement("INSERT INTO User (id, userName, name, surname, fkLab)"
//                    + "VALUES (?,?,?,?,?)");

            // JavaDB
            PreparedStatement ps = con.prepareStatement("INSERT INTO User0 (id, userName, name, surname, fkLab)"
                    + "VALUES (?,?,?,?,?)");
            ps.setInt(1, u.getId());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getName());
            ps.setString(4, u.getSurname());
            ps.setInt(5, u.getLab().getId());
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

    public static ArrayList<User> getUsers() {
        Connection con = null;
        User u = null;
        ArrayList<User> alu = new ArrayList<>();

        try {
            con = establishconnection();

            //Mysql
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM User INNER JOIN Lab"
//                    + " ON User.fkLab=Lab.id");
            // JavaDB
            PreparedStatement ps = con.prepareStatement("SELECT * FROM User0 INNER JOIN Lab"
                    + " ON User0.fkLab=Lab.id");

            ResultSet res = ps.executeQuery();

            while (res.next()) {
                //mysql
//                u = new User(res.getInt("User.id"), res.getString("User.userName"),
//                        res.getString("User.name"), res.getString("User.surname"),
//                        new Lab(res.getInt("Lab.id"), res.getString("Lab.name"),
//                                res.getString("Lab.city"), res.getString("Lab.country")));
                //javadb
                u = new User(res.getInt(1), res.getString(2),
                        res.getString(3), res.getString(4),
                        new Lab(res.getInt(5), res.getString(7),
                                res.getString(8), res.getString(9)));

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

    public static int lastUserid() {
        Connection con = null;
        int n = 0;

        try {
            con = establishconnection();
            //Mysql
//            PreparedStatement ps = con.prepareStatement("SELECT id FROM User "
//                    + "ORDER BY id DESC "
//                    + "LIMIT 1");
            //JavaDB
            PreparedStatement ps = con.prepareStatement("SELECT id FROM User0 "
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

    public static void updateUser(int id, User u) {
        Connection con = null;

        try {
            con = establishconnection();
            //Mysql
//            PreparedStatement ps = con.prepareStatement("UPDATE User "
//                    + "SET userName=?, name=?, surname=?, lab=? "
//                    + "WHERE id=?");
            //JavaDB
            PreparedStatement ps = con.prepareStatement("UPDATE User0 "
                    + "SET userName=?, name=?, surname=?, lab=? "
                    + "WHERE id=?");
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getName());
            ps.setString(3, u.getSurname());
            ps.setInt(4, u.getLab().getId());
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

    public static User getUser(int id) {
        Connection con = null;
        User u = null;

        try {
            con = establishconnection();
            //mysql
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM User INNER JOIN Lab"
//                    + " ON User.fkLab=Lab.id"
//                    + " WHERE User.id=?");
            //javadb
            PreparedStatement ps = con.prepareStatement("SELECT * FROM User0 INNER JOIN Lab"
                    + " ON User0.fkLab=Lab.id"
                    + " WHERE User0.id=?");

            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                //mysql
//                u = new User(res.getInt("User.id"), res.getString("User.userName"),
//                        res.getString("User.name"), res.getString("User.surname"),
//                        new Lab(res.getInt("Lab.id"), res.getString("Lab.name"),
//                                res.getString("Lab.city"), res.getString("Lab.country")));
                //javadb
                u = new User(res.getInt(1), res.getString(2),
                        res.getString(3), res.getString(4),
                        new Lab(res.getInt(5), res.getString(7),
                                res.getString(8), res.getString(9)));
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

    public static boolean existssuser(String username) {
        boolean res1 = false;
        Connection con = null;
        try {
            con = establishconnection();
          //Mysql
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE userName=?");
            //javadb
            PreparedStatement ps = con.prepareStatement("SELECT * FROM User0 WHERE userName=?");

            ps.setString(1, username);
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

    public static User existssuser(String name, String surname) {
        User u = null;
        Connection con = null;
        try {
            con = establishconnection();
          //Mysql
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE userName=?");
            //javadb
            PreparedStatement ps = con.prepareStatement("SELECT * FROM User0 "
                    + "INNER JOIN Lab ON User0.fkLab=Lab.id " 
                    + "WHERE User0.name=? AND User0.surname=?");

            ps.setString(1, name);
            ps.setString(2, surname);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                //javadb
                u = new User(res.getInt(1), res.getString(2),
                        res.getString(3), res.getString(4),
                        new Lab(res.getInt(5), res.getString(7),
                                res.getString(8), res.getString(9)));
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

}
