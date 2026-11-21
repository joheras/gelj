package ij.workspace.GelJ.Assistant3;

import db.Classes.Experiment;
import db.Classes.Study;
import db.Classes.UnfinishedExperiment;
import db.Interface.Main;
import db.Interface.NewExperiment;
import static db.Queries.StudyQueries.getStudy;
import static db.Queries.UnfinishedExperimentsQueries.insertUnfinishedExperiment;
import static db.Queries.UnfinishedExperimentsQueries.lastUnfinishedExperimentid;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.workspace.GelJ.BandManager;
import ij.workspace.GelJ.Menu;
import ij.workspace.GelJ.NormaliseManager;
import ij.workspace.GelJ.SaveLoad.Save;
import static ij.workspace.GelJ.Standards.ComputeStandards.computestdlanes;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class Image_Assistant {

    public Menu m;
    public Main geljdb;
    public boolean darkbg = true;

    public Image_Assistant(Menu m, Main geljdb) {
        this.m = m;
        this.geljdb = geljdb;
    }

    public void runAssistant() {
        OpenDialog od = new OpenDialog("Load an image...");
        Opener o = new Opener();

        if (od.getPath() != null) {
            ImagePlus imp1 = o.openImage(od.getPath());
            if (imp1 == null) {
                JOptionPane.showMessageDialog(m, "Select a valid image file", "Error", JOptionPane.ERROR_MESSAGE);
                geljdb.enableall();
                return;
            }
            imp1.show();
            IJ.run(imp1, "Scale to Fit", "");
            imp1.getWindow().setLocked(true);

            m.setWindowname(imp1.getTitle());

            step1_1 s = new step1_1(imp1, this);

            imp1.getWindow().toFront();

        } else {
            geljdb.enableall();
        }

    }

    public void saveUnfinishedExperiment(JDialog d, int step) throws IOException {

        if (IJ.getImage() != null) {

            if (step == 8) {
                IJ.getImage().changes = false;
                IJ.getImage().close();
            }

            ImagePlus imp1 = IJ.getImage();

            int id = lastUnfinishedExperimentid();
            Save s = new Save(imp1, m.finallanes, m.getBands(), m.normalisation,
                    m.getInitialoffset(), darkbg);
            Study study = getStudy(geljdb.studyid);
            s.saveunfinishedexperiment(study, id);

            int option = JOptionPane.showConfirmDialog(d, "The current analysis is unfinished, "
                    + "do you want to save it for further use?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);

            if (option == JOptionPane.YES_OPTION) {
                String name = JOptionPane.showInputDialog("Introduce a name");

                UnfinishedExperiment ue = new UnfinishedExperiment(id, name, step, study);
                insertUnfinishedExperiment(ue);

            }
            if (step == 8) {
                IJ.getImage().changes = false;
                IJ.getImage().close();
            }
            d.dispose();
            imp1.getWindow().close();
            geljdb.enableall();
            geljdb.toFront();

        } else {
            d.dispose();
            geljdb.enableall();
            geljdb.toFront();
        }

    }

    public void saveUnfinishedExperiment(JFrame d, int step) throws IOException {
        if (WindowManager.getCurrentImage() != null) {

            if (step == 8) {
                IJ.getImage().changes = false;
                IJ.getImage().close();
            }

            ImagePlus imp1 = IJ.getImage();
            imp1.getWindow().setLocked(false);
            imp1.getWindow().toBack();

            int id = lastUnfinishedExperimentid();
            Save s = new Save(imp1, m.finallanes, m.getBands(), m.normalisation,
                    m.getInitialoffset(), !darkbg);
            Study study = getStudy(geljdb.studyid);
            s.saveunfinishedexperiment(study, id);

            int option = JOptionPane.showConfirmDialog(d, "The current analysis is unfinished, "
                    + "do you want to save it for further use?\n "
                    + "(Unfinished experiments can be opened from the menu Tools > Experiments > Unfinished Experiments)", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);

            if (option == JOptionPane.YES_OPTION) {
                String name = JOptionPane.showInputDialog("Introduce a name");

                UnfinishedExperiment ue = new UnfinishedExperiment(id, name, step, study);
                insertUnfinishedExperiment(ue);

            }
            if (step == 8) {
                IJ.getImage().changes = false;
                IJ.getImage().close();
            }
            d.dispose();
            imp1.getWindow().close();
            geljdb.activateemptytoolbar();
            geljdb.enableall();
            geljdb.toFront();
        } else {
            d.dispose();
            geljdb.activateemptytoolbar();
            geljdb.enableall();
            geljdb.toFront();
        }

    }

    public void finishnormalisation() throws FileNotFoundException, UnsupportedEncodingException, IOException {
//        computestdlanes(m.finallanes, m.getCalibrationmethod(), m.getInitialoffset());
        
        computestdlanes(m);
        m.setStudyid(geljdb.studyid);
        m.setUser(geljdb.userid);

        NormaliseManager nm = new NormaliseManager(true);
        nm.delete_without_asking(true);
        nm.dispose();

        Experiment e = m.storeexperiment();
        ImagePlus imp = IJ.getImage();
        imp.getWindow().setLocked(false);
        imp.changes = false;
        imp.close();

        NewExperiment ne = new NewExperiment(geljdb, true, e);
        ne.setVisible(true);

        geljdb.updateexperiments(true, true);
        geljdb.enableall();

        geljdb.toFront();
    }

    public void recoverstep0(ImagePlus imp) {
        imp.getWindow().setLocked(true);
        step1_1 s = new step1_1(imp, this);
    }

    public void recoverstep1(ImagePlus imp) {
        imp.getWindow().setLocked(true);
        step1_2 s = new step1_2(imp, this);
    }

    public void recoverstep2(ImagePlus imp) {
        imp.getWindow().setLocked(true);
        step1_3 s = new step1_3(imp, this);
    }

    public void recoverstep3(ImagePlus imp) {
        imp.getWindow().setLocked(true);
        Step1_4 s = new Step1_4(imp, this);
    }

    public void recoverstep4(ImagePlus imp) {
        imp.getWindow().setLocked(true);
        step2_1 s = new step2_1(imp, this);
    }

    public void recoverstep5(ImagePlus imp) {
        ImageWindow iw = imp.getWindow();
        LaneManagerCanvas cc = new LaneManagerCanvas(imp);
        ImageWindow iw2 = new ImageWindow(imp, cc);
        imp.changes = false;
        iw.dispose();
        iw2.setLocked(true);
        step2_2 s = new step2_2(imp, this, true, null);
        s.getLm().auto_detect();
    }

    public void recoverstep6(ImagePlus imp) {
        ImageWindow iw = imp.getWindow();
        BandManagerCanvas cc = new BandManagerCanvas(imp);
        ImageWindow iw2 = new ImageWindow(imp, cc);
        iw2.setLocked(true);
        iw.dispose();
        step3_1 s = new step3_1(imp, this, !m.getinvertcolour());
    }

    public void recoverstep6b(ImagePlus imp) {
        ImageWindow iw = imp.getWindow();
        BandManagerCanvas cc = new BandManagerCanvas(imp);
        ImageWindow iw2 = new ImageWindow(imp, cc);
        iw.dispose();
        iw2.setLocked(true);
        step3_3 s = new step3_3(imp, this, !m.getinvertcolour(), null);
        BandManager bm = s.getBm();
        bm.delete_without_asking(false);

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
                        if (m.finallanes[i].isIsstandard()) {
                            if (m.getColour() == Color.RED) {
                                r1.setFillColor(Color.BLUE);
                            } else {
                                r1.setFillColor(Color.RED);
                            }

                        } else {
                            r1.setFillColor(m.getColour());
                        }
                        bm.add(imp, r1, 0, i);
                        bm.showAll(0);
                    }
                }
            }
        }

        bm.requestFocus();

    }

    public void recoverstep7(ImagePlus imp) {
        imp.getWindow().setLocked(true);
        step4_1 s = new step4_1(imp, this, !m.getinvertcolour());
    }

}
