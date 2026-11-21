package ij.workspace.GelJ.Assistant2;

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
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.process.ImageProcessor;
import ij.workspace.GelJ.Menu;
import ij.workspace.GelJ.SaveLoad.Save;
import static ij.workspace.GelJ.Standards.ComputeStandards.computestdlanes;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class ImageAssistant {

    public Menu m;
    public Main geljdb;
    public boolean darkbg = true;

    public ImageAssistant(Menu m, Main geljdb) {
        this.m = m;
        this.geljdb = geljdb;
    }

    public void runAssistant() {
        OpenDialog od = new OpenDialog("Load an image...");
        Opener o = new Opener();

        if (od.getPath() != null) {
            ImagePlus imp1 = o.openImage(od.getPath());
            imp1.show();
//            IJ.run(imp1, "Scale to Fit", "");
//
//            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//            if (imp1.getCanvas().getWidth() > screenSize.getWidth() * 0.7
//                    || imp1.getCanvas().getHeight() > screenSize.getHeight() * 0.7) {
//                imp1.getCanvas().setMagnification(.6);
//
//            }

            ImagePlus imp3 = scaledownuntilfits(imp1);
            String title = imp1.getTitle();
            imp1.changes = false;
            imp1.close();
            imp3.setTitle(title);
            imp3.show();
            CustomCanvas cc = new CustomCanvas(imp3);

            m.setWindowname(title);
            Step1Crop s1c = new Step1Crop(imp3, cc, this);
            IJ.run(imp3, "Scale to Fit", "");
        } else {
            geljdb.enableall();
        }

    }

    public void saveUnfinishedExperiment(Frame d, int step) throws IOException {
        if (step == 8) {
            IJ.getImage().changes = false;
            IJ.getImage().close();
        }

        ImagePlus imp1 = IJ.getImage();
        int id = lastUnfinishedExperimentid();
        Save s = new Save(imp1, m.finallanes, m.getBands(), m.normalisation,
                m.getInitialoffset(), !darkbg);
        Study study = getStudy(geljdb.studyid);
        s.saveunfinishedexperiment(study, id);

        int option = JOptionPane.showConfirmDialog(null, "The current analysis is unfinished, "
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
        geljdb.enableall();
        geljdb.toFront();

    }

    public void finishnormalisation() throws FileNotFoundException, UnsupportedEncodingException {
        computestdlanes(m.finallanes, m.getCalibrationmethod(), m.getInitialoffset());
        m.setStudyid(geljdb.studyid);
        m.setUser(geljdb.userid);

        Experiment e = m.storeexperiment();
        ImagePlus imp = IJ.getImage();
        imp.changes = false;
        imp.close();

        NewExperiment ne = new NewExperiment(geljdb, true, e);
        ne.setVisible(true);

        geljdb.updateexperiments(true, true);
        geljdb.enableall();

        geljdb.toFront();
    }

    public void recoverstep0(ImagePlus imp) {
        CustomCanvas cc = new CustomCanvas(imp);
        Step1Crop s1 = new Step1Crop(imp, cc, this);
    }

    public void recoverstep1(ImagePlus imp) {
        CustomCanvas cc = new CustomCanvas(imp);
        Step1Invert s1 = new Step1Invert(imp, cc, this);
    }

    public void recoverstep2(ImagePlus imp) {
        CustomCanvas cc = new CustomCanvas(imp);
        Step1AutoAdjust s1 = new Step1AutoAdjust(imp, cc, this);
    }

    public void recoverstep3(ImagePlus imp) {
        CustomCanvas cc = new CustomCanvas(imp);
        Step1OtherOperations s1 = new Step1OtherOperations(imp, cc, this);
    }

    public void recoverstep4(ImagePlus imp) {
        CustomCanvas cc = new CustomCanvas(imp);
        Step2DLBG s1 = new Step2DLBG(imp, cc, this);
    }

    public void recoverstep5(ImagePlus imp) {
        LaneManagerCanvas cc = new LaneManagerCanvas(imp);
        Step2Edit s1i = new Step2Edit(imp, cc, this, m.getinvertcolour());
        s1i.getLm().auto_detect();
    }

    public void recoverstep6(ImagePlus imp) {
        BandManagerCanvas cc = new BandManagerCanvas(imp);
        Step3 s1i = new Step3(imp, cc, this, m.getinvertcolour());
    }

    public void recoverstep7(ImagePlus imp) {

        CustomCanvas cc = new CustomCanvas(imp);
        Step4 s1i = new Step4(imp, cc, this, m.getinvertcolour());
    }

    public static ImagePlus scaledownuntilfits(ImagePlus imp) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        GraphicsDevice[] gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        
        
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        
        if(gd.length>1){
            width = width - gd[0].getDisplayMode().getWidth();
            height = gd[1].getDisplayMode().getHeight();
        }
        
        if(width==0){
             width = screenSize.getWidth();
        }
        
        
        
        double reduction_factor = 1;

        if (width / height > 1.5) {
            reduction_factor = .75;
        } else {
            reduction_factor = .6;
        }

        ImagePlus impd = imp.duplicate();
        impd.show();

        while (impd.getCanvas().getWidth() > width * reduction_factor
                || impd.getCanvas().getHeight() > height * reduction_factor) {

            ImageProcessor ip_big = impd.getProcessor();
            ip_big.setInterpolate(true);
            ImageProcessor ip_small = ip_big.resize((int) (impd.getWidth() * 0.9), (int) (impd.getHeight() * 0.9));
            ImagePlus small = new ImagePlus("small", ip_small);
            impd.close();
            impd = small;
            impd.show();
        }

        return impd;

    }

    public static ImagePlus scaleupuntilfits(ImagePlus imp) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ImagePlus impd = imp.duplicate();
        impd.show();

//        while (impd.getCanvas().getWidth() > screenSize.getWidth() * 0.75
//                || impd.getCanvas().getHeight() > screenSize.getHeight() * 0.75) {
//
//            ImageProcessor ip_big = impd.getProcessor();
//            ip_big.setInterpolate(true);
//            ImageProcessor ip_small = ip_big.resize((int) (impd.getWidth() * 0.9), (int) (impd.getHeight() * 0.9));
//            ImagePlus small = new ImagePlus("small", ip_small);
//            impd.close();
//            impd = small;
//            impd.show();
//        }
        while (impd.getCanvas().getWidth() < screenSize.getWidth() * 0.45
                && impd.getCanvas().getHeight() < screenSize.getHeight() * 0.45) {

            ImageProcessor ip_big = impd.getProcessor();
            ip_big.setInterpolate(true);
            ImageProcessor ip_small = ip_big.resize((int) (impd.getWidth() * 1.2), (int) (impd.getHeight() * 1.2));
            ImagePlus small = new ImagePlus("small", ip_small);
            impd.close();
            impd = small;
            small.close();
            impd.show();
        }

        return impd;

    }

}
