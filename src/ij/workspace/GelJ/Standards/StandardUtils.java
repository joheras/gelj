/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ.Standards;

import db.Classes.Marker;
import static db.Queries.MarkerQueries.getMarkers;
import static db.Queries.MarkerQueries.insertMarker;
import static db.Queries.MarkerQueries.lastMarkerid;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author joheras
 */
public class StandardUtils {

    public static double[] readstandardfromfile(String filename) throws IOException {

        String workingdir = System.getProperty("user.dir") + "/standardfiles/";

        String workingfile = workingdir + filename + ".marker";

        Path p = Paths.get(workingfile);
        Charset c = Charset.defaultCharset();

        List<String> lines = Files.readAllLines(p, c);

        double[] res = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            res[i] = Double.parseDouble(lines.get(i));
        }

        return res;
    }

    public static int readexperimentconfig() throws IOException {

        String workingfile = System.getProperty("user.dir") + "/experiment.config";

        Path p = Paths.get(workingfile);

        File f = p.toFile();

        if (f.exists()) {
            Charset c = Charset.defaultCharset();

            List<String> lines = Files.readAllLines(p, c);

            int res = Integer.parseInt(lines.get(0));
            return res;
        }
        return -1;
    }

    public static String[] readcomparisonconfig() throws IOException {

        String workingfile = System.getProperty("user.dir") + "/comparison.config";

        Path p = Paths.get(workingfile);
        if (p.toFile().exists()) {
            Charset c = Charset.defaultCharset();

            List<String> lines = Files.readAllLines(p, c);

            String[] res = new String[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                res[i] = lines.get(i);
            }

            return res;
        }
        return null;
    }

    public static double[] readstandardinfofromfile(String filename) throws IOException {

        String workingdir = System.getProperty("user.dir") + "/standardfiles/info/";

        String workingfile = workingdir + filename + ".info";

        Path p = Paths.get(workingfile);
        Charset c = Charset.defaultCharset();

        List<String> lines = Files.readAllLines(p, c);

        double[] res = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            res[i] = Double.parseDouble(lines.get(i));
        }

        return res;
    }

    public static double[] readstandardposfromfile(String filename) throws IOException {

        String workingdir = System.getProperty("user.dir") + "/standardfiles/info/";

        String workingfile = workingdir + filename + ".pos";

        Path p = Paths.get(workingfile);
        Charset c = Charset.defaultCharset();

        List<String> lines = Files.readAllLines(p, c);

        double[] res = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            res[i] = Double.parseDouble(lines.get(i));
        }

        return res;
    }

    public static void deletestandard(String filename) throws IOException {

        String workingdir = System.getProperty("user.dir") + "/standardfiles/";
        String sourcefile = workingdir + filename + ".marker";

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy-hh:mm:ss");
        String date = sdf.format(new Date());
        String targetfile = workingdir + "/deletedstandards/" + filename + "-" + date + ".marker";
        Files.copy(Paths.get(sourcefile), Paths.get(targetfile), REPLACE_EXISTING);
        Files.delete(Paths.get(sourcefile));

    }

    public static void recoverstandard(String filename) throws IOException {

        String workingdir = System.getProperty("user.dir") + "/standardfiles/";
        String targetfile = workingdir + filename + ".marker";
        String sourcefile = workingdir + "/deletedstandards/" + filename + ".marker";
        Files.copy(Paths.get(sourcefile), Paths.get(targetfile), REPLACE_EXISTING);
        Files.delete(Paths.get(sourcefile));

    }

    public static ArrayList<String> readstandardsavailable() {
//        String workingdir = System.getProperty("user.dir") + "/standardfiles/";
//    
//        File f2 = new File(System.getProperty("user.dir") + "/standardfiles");
//        File f3 = new File(System.getProperty("user.dir") + "/standardfiles/deletedstandards");
//        
//        if (! f2.exists()){
//            f2.mkdirs();
//            f3.mkdir();
//            return null;
//        }
//        
//        
//        
//        ArrayList<String> res = new ArrayList<String>();
//        File directory = new File(workingdir);
//        String[] files = directory.list();
//        int nFiles = files.length;
//        if (nFiles != 0) {
//            for (int i = 0; i < nFiles; i++) {
//                if (files[i].contains(".marker")) //encuentra los archivos .tif y almacena su ruta de acceso
//                {
//                    String name = files[i].substring(0, files[i].indexOf('.'));
//                    res.add(name);
//                }
//            }
//        }
//    
//        return res;

        ArrayList<Marker> markers = getMarkers();

        ArrayList<String> res = new ArrayList<>();

        for (Marker m : markers) {
            res.add(m.getName());
        }

        return res;
    }

    public static ArrayList<String> readstandardsremoved() {
        String workingdir = System.getProperty("user.dir") + "/standardfiles/deletedstandards/";

        ArrayList<String> res = new ArrayList<String>();
        File directory = new File(workingdir);
        String[] files = directory.list();
        int nFiles = files.length;
        if (nFiles != 0) {
            for (int i = 0; i < nFiles; i++) {
                if (files[i].contains(".marker")) //encuentra los archivos .tif y almacena su ruta de acceso
                {
                    String name = files[i].substring(0, files[i].indexOf('.'));
                    res.add(name);
                }
            }
        }

        return res;
    }

    public static double[] createStandard(int n) throws FileNotFoundException, UnsupportedEncodingException {
        double[] temp = new double[n];
        return createStandard(temp, "");
    }

    public static double[] createStandard(double[] array, String name) throws FileNotFoundException, UnsupportedEncodingException {

        int n = array.length;
        double[] res = new double[n];
        String newname = name;
        GenericDialog gdG = new GenericDialog("New standard");
        gdG.addStringField("Name of the standard", name);
        for (int i = 0; i < n; i++) {
            gdG.addNumericField("Value of band " + (i + 1) + ":", array[i], 2);
        }
        gdG.addMessage("");
        gdG.showDialog();
        if (gdG.wasCanceled()) {
            gdG.dispose();
            return null;
        }

        if (gdG.wasOKed()) {
            newname = gdG.getNextString();
            ArrayList<String> names = readstandardsavailable();
            double temp;
            int fail = -1;
            for (int i = 0; i < n; i++) {
                temp = gdG.getNextNumber();
                if ((temp == Double.NaN) || (temp < 0)) {
                    fail = i;
                    res[i] = 0;
                }
                res[i] = (int) temp;
            }
            if (names.contains(newname)) {
                IJ.error("There is already a standard called " + newname + ".");
                gdG.dispose();
                return createStandard(res, newname);
            }
            if (newname.equals("")) {
                IJ.error("The name of the standard should not be empty.");
                gdG.dispose();
                return createStandard(res, newname);
            }
            if (fail != -1) {
                IJ.error("Band " + fail + " should be a positive number.");
                gdG.dispose();
                return createStandard(res, newname);
            }
            if (!(sorted(res))) {
                IJ.error("The values of the bands are not sorted.");
                gdG.dispose();
                return createStandard(res, newname);
            }
        }

        save_standard(newname, res);
        return res;

    }

    public static void save_experimentconfig(int threshold) throws FileNotFoundException, UnsupportedEncodingException {
        String workingfile = System.getProperty("user.dir") + "/experiment.config";

        PrintWriter writer = new PrintWriter(workingfile, "UTF-8");
        writer.println(threshold);
        writer.close();

    }

    public static void save_comparisonconfig(String similaritymethod,
            String linkage, double tolerance, Object[] selectedfields) throws FileNotFoundException, UnsupportedEncodingException {
        String workingfile = System.getProperty("user.dir") + "/comparison.config";

        PrintWriter writer = new PrintWriter(workingfile, "UTF-8");
        writer.println(similaritymethod);
        writer.println(linkage);
        writer.println(tolerance);
        for(Object o : selectedfields){
            writer.println((String) o);
        }
        writer.close();


    }

    public static void save_standard(String name, double[] std) throws FileNotFoundException, UnsupportedEncodingException {
        String workingdir = System.getProperty("user.dir") + "/standardfiles/";

        String workingfile = workingdir + name + ".marker";
        PrintWriter writer = new PrintWriter(workingfile, "UTF-8");
        for (int i = 0; i < std.length; i++) {
            writer.println(std[i]);
        }
        writer.close();

        Marker m = new Marker(lastMarkerid(), name, workingfile);
        insertMarker(m);

    }

    /* Show Standard Dialog
    
     GenericDialog gdG = new GenericDialog("Choose a Standard");
     gdG.addChoice("Standards:", list, list[0]);
     gdG.addMessage("");
     gdG.showDialog();
    
     */
    private static boolean sorted(double[] res) {

        for (int i = 0; i < res.length - 1; i++) {
            if (res[i] < res[i + 1]) {
                return false;
            }
        }

        return true;
    }

    public static Roi rectangleintersectionroi(Roi roi, Rectangle r) {
        Rectangle rect;
        Boolean inside = false;
        double x0 = r.getX();
        double y0 = r.getY();
        double x1, y1;
        double x = r.getX();
        double y = r.getY();

        for (int i = 1; i < r.width; i++) {
            if (!inside) {
                if (roi.getPolygon().contains(x + i, y)) {
                    x0 = x + i;
                    y0 = y;
                    inside = true;
                }
            } else {
                if (!roi.getPolygon().contains(x + i, y)) {
                    x1 = x + i;
                    y1 = y;
                    rect = new Rectangle((int) x0, (int) y0, (int) (x1 - x0), 2);
                    Roi r1 = new Roi(rect);
                    r1.setFillColor(Color.blue);
                    return r1;
                }
            }
        }
        rect = new Rectangle((int) x0, (int) y0, r.width, 2);
        Roi r1 = new Roi(rect);
        r1.setFillColor(Color.blue);
        return r1;

    }

}
