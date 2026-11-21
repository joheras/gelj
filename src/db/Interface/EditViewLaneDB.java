package db.Interface;

import db.Classes.LaneDB;
import static db.Queries.LaneQueries.getstandardsexperiment;
import ij.IJ;
import ij.ImageJ;
import static ij.ImageJ.NO_SHOW;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.Opener;
import ij.io.RoiDecoder;
import ij.workspace.GelJ.Assistant3.EditOneLane;
import ij.workspace.GelJ.BandManager;
import ij.workspace.GelJ.Lane;
import ij.workspace.GelJ.Menu;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joheras
 */
public class EditViewLaneDB {

    private LaneDB l;
    private boolean edit;
    private Main gelj;

    public EditViewLaneDB(LaneDB l, boolean edit, Main gelj) throws IOException {
        this.l = l;
        this.edit = edit;
        this.gelj = gelj;
        Menu m = new Menu();

        String windowname = IJ.getImage().getTitle();
        m.setWindowname(windowname);
        Lane[] lanes = new Lane[1];
        lanes[0] = LaneDBtoLaneIJ(l);
        m.setStudyid(l.getExperiment().getStudy().getId());
        m.setExperimentid(l.getExperiment().getId());
        m.setUsername(l.getExperiment().getUser().getUsername());
        m.setCalibrationmethod(l.getExperiment().getCalibrationMethod());
        m.setMarker(l.getExperiment().getMarker().getName());
        m.finallanes = lanes;
        Roi[] rois = new Roi[1];
        rois[0] = lanes[0].getRoi();
        m.setLanes(rois);

        if (IJ.getInstance() == null) {
            new ImageJ(NO_SHOW);
        }
        BandManager bm = null;

        if (edit) {
            Lane[] markers = getmarkerlanesexperiment(l.getExperiment().getId());
            bm = new BandManager(m, true);
            ImagePlus imp = WindowManager.getCurrentImage();
            ImageWindow iw = imp.getWindow();
            BandManagerCanvas cc = new BandManagerCanvas(imp);
            ImageWindow iw2 = new ImageWindow(imp, cc);
            iw2.toFront();
            iw.dispose();
            bm.delete_without_asking(true);
            if (m.finallanes != null) {
                for (int i = 0; i < m.finallanes.length; i++) {
                    int[] positionbands = m.finallanes[i].getPositionbands();
                    if (positionbands != null) {
                        for (int b : positionbands) {
                            Roi r0 = m.finallanes[i].getRoi();
                            int r0x = r0.getBounds().x;
                            int r0y = r0.getBounds().y;
                            int w = r0.getBounds().width;
                            Rectangle r = new Rectangle(r0x, r0y + b, w, 2);
                            Roi r1 = rectangleintersectionroi(r0, r);
                            r1.setFillColor(m.getColour());
                            bm.add(imp, r1, 0, i);
                            bm.showAll(0);
                        }
                    }
                }
            }
            bm.setLaneid(l.getId());
            bm.setMarkersedit(markers);
            bm.setVisible(false);

            EditOneLane be = new EditOneLane(imp, bm, gelj);
        } else {
            bm = m.bandmanagerfunctionality();
            bm.setVisible(true);
            ImagePlus imp = IJ.getImage();
//            imp.show();
            ImagePlus imp2 = imp.flatten();
            imp.close();
            imp2.show();
            imp2.getWindow().setAlwaysOnTop(true);
            imp2.setTitle("Selected lane");
            bm.setVisible(false);
            m.dispose();
        }

    }

    public static Lane LaneDBtoLaneIJ(LaneDB l) throws IOException {

        String folderlanepath = System.getProperty("user.dir") + "/GelJDBImages/" + l.getExperiment().getStudy().getId() + "/" + l.getExperiment().getUser().getUsername() + "/experiments/" + l.getExperiment().getId() + "/lanes/" + l.getId() + "/";
//        IJ.open(folderlanepath + l.getId() + ".tiff");
        Opener o = new Opener();
        ImagePlus imp = o.openImage(folderlanepath + l.getId() + ".tiff");
//        ImagePlus imp = IJ.getImage();
        Roi roi = RoiDecoder.open(folderlanepath + l.getId() + ".roi");
        double[] profile = readdoublearrayfile(folderlanepath + l.getId() + ".curve");
        int[] positionbands = readintarrayfile(folderlanepath + l.getId() + ".bands");
        int[] positionbands2 = readintarrayfile(folderlanepath + l.getId() + ".bands2");
        double[] stdpositionbands = readdoublearrayfile(folderlanepath + l.getId() + ".mw");

        Lane lane = new Lane(imp, l.getGenus(), roi, profile, positionbands, stdpositionbands);
        lane.setIsstandard(l.isIsMarker());
        lane.setPositionbands2(positionbands2);
        imp.changes = false;
        imp.close();
        return lane;
    }

    public static int[] readintarrayfile(String path) throws IOException {

        String workingfile = path;

        Path p = Paths.get(workingfile);
        Charset c = Charset.defaultCharset();

        List<String> lines = Files.readAllLines(p, c);

        int[] res = new int[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            res[i] = Integer.parseInt(lines.get(i));
        }

        return res;
    }

    public static double[] readdoublearrayfile(String path) throws IOException {

        String workingfile = path;

        Path p = Paths.get(workingfile);
        Charset c = Charset.defaultCharset();

        List<String> lines = Files.readAllLines(p, c);
        
        if (lines.size()==0){
            double[] res = new double[1];
            res[0] = -1;
            return res;
        }

        double[] res = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            res[i] = Double.parseDouble(lines.get(i));
        }

        return res;
    }

    private Lane[] getmarkerlanesexperiment(int id) throws IOException {
        ArrayList<LaneDB> lanes = getstandardsexperiment(id);
        Lane[] markers = new Lane[lanes.size()];

        for (int i = 0; i < lanes.size(); i++) {
            markers[i] = LaneDBtoLaneIJ(lanes.get(i));
        }

        return markers;

    }

}
