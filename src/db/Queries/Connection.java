package db.Queries;

import db.Classes.Marker;
import static db.Queries.MarkerQueries.insertMarker;
import static db.Queries.MarkerQueries.lastMarkerid;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author joheras
 */
public class Connection {

    public static java.sql.Connection establishconnection() throws SQLException {
//      Mysql connection
//        return DriverManager.getConnection("jdbc:mysql://localhost/GelJDB", "test", "test");
//      JavaDB connection    
        return DriverManager.getConnection("jdbc:derby:geljdb;create=true;user=test;password=test");

    }

    public static java.sql.Connection establishconnectionrestore(String restore) throws SQLException {
//      Mysql connection
//        return DriverManager.getConnection("jdbc:mysql://localhost/GelJDB", "test", "test");
//      JavaDB connection    
        String nsURL1 = "jdbc:derby:;shutdown=true;user=test;password=test";
        String nsURL2 = "jdbc:derby:geljdb;restoreFrom="+ restore+";user=test;password=test";

        try{
            DriverManager.getConnection(nsURL1);
        }catch(java.sql.SQLException e){
            
        }
        
        return DriverManager.getConnection(nsURL2);

    }

    public static void creategeljdb() {

        try {
            // Creation of database in JavaDB
            java.sql.Connection dbConnection = establishconnection();
            String sqlcreation = "CREATE  TABLE  Lab ( id INT NOT NULL , name VARCHAR(100) , city VARCHAR(100) , country VARCHAR(100) , PRIMARY KEY (id) )";
            Statement statement = dbConnection.createStatement();
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  User0 ( id INT NOT NULL , userName VARCHAR(100) , name VARCHAR(50) , surname VARCHAR(50) , fkLab INT , PRIMARY KEY (id) , CONSTRAINT fk_User_Lab1   FOREIGN KEY (fkLab )   REFERENCES Lab (id )   ON DELETE NO ACTION   ON UPDATE NO ACTION)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE INDEX fk_User_Lab1 ON User0 (fkLab ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  Study ( id INT NOT NULL , name VARCHAR(45) , date DATE , path VARCHAR(500) , comments VARCHAR(500) , fkUser INT , PRIMARY KEY (id) , CONSTRAINT fk_Study_User1   FOREIGN KEY (fkUser )   REFERENCES User0 (id )   ON DELETE NO ACTION   ON UPDATE NO ACTION)";
            statement.execute(sqlcreation);
            sqlcreation = "CREATE  INDEX fk_Study_User1 ON Study (fkUser ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  Marker ( id INT NOT NULL , name VARCHAR(100) , file VARCHAR(500) , PRIMARY KEY (id) )";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  Experiment ( id INT NOT NULL , name VARCHAR(45) , date DATE , imageName VARCHAR(100) , calibrationMethod INT , comments VARCHAR(500) , fkStudy INT CONSTRAINT fk_Experiment_Study   REFERENCES Study    ON DELETE CASCADE   ON UPDATE RESTRICT, fkMarker INT  CONSTRAINT fk_Experiment_Marker1   REFERENCES Marker (id )   ON DELETE NO ACTION   ON UPDATE NO ACTION, fkUser INT CONSTRAINT fk_Experiment_User1   REFERENCES User0    ON DELETE NO ACTION   ON UPDATE NO ACTION, PRIMARY KEY (id))";
            statement.execute(sqlcreation);
            sqlcreation = "CREATE INDEX fk_Experiment_Study on Experiment (fkUser ASC)";
            statement.execute(sqlcreation);
            sqlcreation = "CREATE INDEX fk_Experiment_Marker1 on Experiment (fkMarker ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  Comparison ( id INT NOT NULL , name VARCHAR(45) , date DATE , similarityMethod VARCHAR(45) , linkage VARCHAR(45) , tolerance DOUBLE , comments VARCHAR(500) , fkStudy INT , PRIMARY KEY (id) , CONSTRAINT fk_comparison_Study1   FOREIGN KEY (fkStudy )   REFERENCES Study (id )   ON DELETE CASCADE   ON UPDATE RESTRICT)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  INDEX fk_comparison_Study1 on Comparison (fkStudy ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  Lane ( id INT NOT NULL , genus VARCHAR(100) , species VARCHAR(100) , strainNumber VARCHAR(100) , country VARCHAR(100) , MSLT VARCHAR(100) , isMarker BOOLEAN , comments VARCHAR(500) , fkExperiment INT , PRIMARY KEY (id) , CONSTRAINT fk_Lane_Experiment1   FOREIGN KEY (fkExperiment )   REFERENCES Experiment (id )   ON DELETE CASCADE   ON UPDATE RESTRICT)";
            statement.execute(sqlcreation);
            sqlcreation = "Create  INDEX fk_Lane_Experiment1 on Lane (fkExperiment ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  LaneAddedInfo ( id INT NOT NULL , fieldName VARCHAR(100) , fieldValue VARCHAR(100) , fkLane INT , PRIMARY KEY (id) , CONSTRAINT fk_LaneAddedInfo_Lane1   FOREIGN KEY (fkLane )   REFERENCES Lane (id )   ON DELETE CASCADE   ON UPDATE RESTRICT)";
            statement.execute(sqlcreation);
            sqlcreation = "CREATE  INDEX fk_LaneAddedInfo_Lane1 on LaneAddedInfo (fkLane ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  ComparisonLane ( fkComparison INT NOT NULL , fkLane INT NOT NULL , PRIMARY KEY (fkComparison, fkLane) ,CONSTRAINT fk_comparison_has_Lane_comparison1   FOREIGN KEY (fkComparison )   REFERENCES Comparison (id )   ON DELETE CASCADE   ON UPDATE RESTRICT, CONSTRAINT fk_comparison_has_Lane_Lane1   FOREIGN KEY (fkLane )   REFERENCES Lane (id )   ON DELETE CASCADE   ON UPDATE RESTRICT)";
            statement.execute(sqlcreation);
            sqlcreation = "CREATE INDEX fk_comparison_has_Lane_Lane1 on ComparisonLane (fkLane ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE INDEX fk_comparison_has_Lane_comparison1 on ComparisonLane (fkComparison ASC)";
            statement.execute(sqlcreation);

            sqlcreation = "CREATE  TABLE  UnfinishedExperiment ( id INT NOT NULL , name VARCHAR(45) , step INT , path VARCHAR(100) , fkStudy INT , PRIMARY KEY (id) , CONSTRAINT fkStudy   FOREIGN KEY (fkStudy )   REFERENCES Study (id )   ON DELETE CASCADE   ON UPDATE RESTRICT)";
            statement.execute(sqlcreation);
            sqlcreation = "CREATE  INDEX fkStudy on UnfinishedExperiment (fkStudy ASC)";
            statement.execute(sqlcreation);

            if (lastMarkerid() == 0) {
                String workingdir = System.getProperty("user.dir") + "/standardfiles/";
                insertMarker(new Marker(0, "Lambda_ladder_PFG_Marker", workingdir + "Lambda_ladder_PFG_Marker" + ".marker"));
                insertMarker(new Marker(1, "Midrange_PFG_Marker_I", workingdir + "Midrange_PFG_Marker_I" + ".marker"));
                insertMarker(new Marker(2, "Low_Range_PFG_Marker", workingdir + "Low_Range_PFG_Marker" + ".marker"));
            }

            dbConnection.close();
        } catch (SQLException ex) {
            if (ex.getSQLState().equalsIgnoreCase("X0Y32")) {
            }
        }

    }

}
