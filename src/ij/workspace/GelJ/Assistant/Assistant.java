package ij.workspace.GelJ.Assistant;

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
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.workspace.GelJ.Assistant2.PleaseWait;
import ij.workspace.GelJ.Band;
import ij.workspace.GelJ.BandManager;
import ij.workspace.GelJ.Dendogram;
import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.Lane.getstandardlanes;
import static ij.workspace.GelJ.Lane.indexesstandardlanes;
import static ij.workspace.GelJ.Lane.lanesnames;
import static ij.workspace.GelJ.Lane.numberofbandslanes;
import static ij.workspace.GelJ.Lane.whatarestandardlanes;
import ij.workspace.GelJ.LaneCompare.BandDifference;
import ij.workspace.GelJ.LaneCompare.CosineCorrelation;
import ij.workspace.GelJ.LaneCompare.Dice;
import ij.workspace.GelJ.LaneCompare.EuclideanDistance;
import ij.workspace.GelJ.LaneCompare.Jaccard;
import ij.workspace.GelJ.LaneCompare.Jeffrey;
import ij.workspace.GelJ.LaneCompare.LaneComparison;
import ij.workspace.GelJ.LaneCompare.ManhattanDistance;
import ij.workspace.GelJ.LaneCompare.Ochiai;
import ij.workspace.GelJ.LaneCompare.PearsonCorrelation;
import ij.workspace.GelJ.LaneManager;
import ij.workspace.GelJ.Menu;
import ij.workspace.GelJ.NormaliseManager;
import ij.workspace.GelJ.SaveLoad.Save;
import static ij.workspace.GelJ.Standards.ComputeStandards.computestdlanes;
import ij.workspace.GelJ.Standards.CurveCalibration;
import ij.workspace.GelJ.Standards.ListofValues;
import static ij.workspace.GelJ.Standards.StandardUtils.createStandard;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardfromfile;
import static ij.workspace.GelJ.Standards.StandardUtils.readstandardsavailable;
import static ij.workspace.GelJ.Standards.StandardUtils.rectangleintersectionroi;
import ij.workspace.GelJ.Standards.StandardsPanel;
import static ij.workspace.GelJ.Utils.Utilities.allequal;
import static ij.workspace.GelJ.Utils.Utilities.intarraytodoublearray;
import static ij.workspace.GelJ.Utils.Utilities.selectlanes;
import static ij.workspace.GelJ.Utils.Utilities.selectstandards;
import static ij.workspace.GelJ.Utils.Utilities.trueinbooleanarray;
import ij.workspace.GelJ.WekaClustering;
import static ij.workspace.GelJ.WekaClustering.COMPLETE;
import static ij.workspace.GelJ.WekaClustering.MEAN;
import static ij.workspace.GelJ.WekaClustering.SINGLE;
import static ij.workspace.GelJ.WekaClustering.UPGMA;
import static ij.workspace.GelJ.WekaClustering.UPGMC;
import static ij.workspace.GelJ.WekaClustering.WARD;
import ij.workspace.GelJ.gui.BandManagerCanvas;
import ij.workspace.GelJ.gui.LaneManagerCanvas;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.ConfirmationCallback;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class Assistant {

    public Menu m;
    private boolean lightbg = false;
    private LaneManager lm;
    private BandManager bm;
    private boolean referencelanes;
    private ImagePlus initialimage;
    public Main geljdb;

    public Assistant(Menu m, Main geljdb) {
        this.m = m;
        this.geljdb = geljdb;
    }

    public Assistant(Menu m) {
        this.m = m;
    }

    public void initAssistant() {

        GenericDialog gd = new GenericDialog("GelJ Assistant");
        gd.addMessage("Do you want to use the assistant?");

        gd.hideCancelButton();
        gd.enableYesNoCancel();
        gd.addMessage("");
        // gd.addHelp("");
        // gd.setHelpLabel("Back");

        gd.showDialog();

        if (gd.wasOKed()) {
            IJ.getInstance().setVisible(false);
            gd.dispose();
            m.setFirst_time(false);
            openimageassitant();
        } else {
            IJ.getInstance().setVisible(false);
            m.setVisible(true);
        }
    }

    public void openimageassitant() {
        OpenDialog od = new OpenDialog("Load an image...");
        Opener o = new Opener();

        if (od.getPath() != null) {
            ImagePlus imp1 = o.openImage(od.getPath());
            m.setWindowname(imp1.getTitle());
            initialimage = imp1.duplicate();
            imp1.show();
            cropimage();
        } else {
            // Old version
//            m.setVisible(true);
            // New version
            geljdb.enableall();
        }
    }

    public void cropimage() {
        m.enableall();
        CropImage ci = new CropImage(this);
        ci.show();
    }

    public void autoadjustbc() {
        /*  GenericDialog gd = new GenericDialog("Step 1 of 5. Image Edition");
         gd.enableYesNoCancel();
         gd.addMessage("Do you want to auto-adjust the brightness/contrast of the image?");
         gd.addMessage("");
         gd.showDialog();

         if (gd.wasCanceled()) {
         m.setVisible(true);
         } else if (gd.wasOKed()) {
         ImagePlus imp = IJ.getImage();
         IJ.run(imp, "Enhance Contrast", "saturated=0.35");
         invertcolour();
         } else {
         invertcolour();
         }*/
        AutoAdjustImage ai = new AutoAdjustImage(this);
        ai.show();

    }

    public void invertcolour() {
        InvertImage ii = new InvertImage(this);
        ii.show();

//        GenericDialog gd = new GenericDialog("Step 1 of 5. Image Edition");
//        gd.enableYesNoCancel();
//        gd.addMessage("Do you want to invert the colours of the image?");
//        gd.addMessage("");
//        gd.showDialog();
//
//        if (gd.wasCanceled()) {
//            m.setVisible(true);
//        } else if (gd.wasOKed()) {
//            ImagePlus imp = IJ.getImage();
//            IJ.run(imp, "Invert", "");
//            m.changeinvertcolour();
//            preprocessimage();
//        } else {
//            preprocessimage();
//        }
    }

    public void preprocessimage() {
//        GenericDialog gd = new GenericDialog("Step 1 of 5. Image Edition");
//        gd.enableYesNoCancel();
//        gd.addMessage("Do you want to perform other edition operations?");
//        gd.addMessage("(E.g. remove background, filter, rotate, ...)");
//        gd.addMessage("");
//        gd.showDialog();
//
//        if (gd.wasCanceled()) {
//            m.setVisible(true);
//        } else if (gd.wasOKed()) {
//            PreProcessingAssistant p = new PreProcessingAssistant(this);
//            p.show();
//        } else {
//            lightdarkbackground();
//        }
        EditImage ei = new EditImage(this);
        ei.show();

    }

    public void lightdarkbackground() throws IOException {

        int option = JOptionPane.showConfirmDialog(geljdb, "Is the background of the image dark?", "Step 2 of 5. Lane Detection", ConfirmationCallback.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);

        if (option == JOptionPane.YES_OPTION) {
            lightbg = false;
            autolanedetection();
            return;
        }

        if (option == JOptionPane.NO_OPTION) {
            lightbg = true;
            autolanedetection();
            return;
        }

        
        saveUnfinishedExperiment(m, 4);
        geljdb.enableall();
        ImagePlus imp = IJ.getImage();
        imp.changes = false;
        imp.close();

        // Old version
//        GenericDialog gd = new GenericDialog("Step 2 of 5. Lane Detection") ;
//        gd.enableYesNoCancel("Dark", "Light");
//        gd.hideCancelButton();
//        gd.addMessage("Is the background of the image dark or light?");
//        gd.addMessage("");
//        gd.showDialog();
//        m.setChanges(true);
//        
//        if (gd.wasOKed()) {
//            lightbg = false;
//            autolanedetection();
//        } 
//        
//        if(gd.wasCanceled()){
//            lightbg = true;
//            autolanedetection();
//        }
    }

    public void autolanedetection() {

        lm = new LaneManager(m, this);

        ImagePlus imp = WindowManager.getCurrentImage();
        LaneManagerCanvas cc = new LaneManagerCanvas(imp);
        new ImageWindow(imp, cc);
        cc.requestFocus();
        lm.setbgcheckboxstate(!lightbg);
        lm.auto_detect();
        lm.hide();

        LaneEdition le = new LaneEdition(this, lm);
        le.show();

    }

    public void laneedition() {

        lm = new LaneManager(m, this);

        ImagePlus imp = WindowManager.getCurrentImage();
        LaneManagerCanvas cc = new LaneManagerCanvas(imp);
        new ImageWindow(imp, cc);
        cc.requestFocus();
        lm.hide();

        LaneEdition le = new LaneEdition(this, lm);
        le.show();

    }

    public void addreferencelanes() throws Exception {

        Lane[] stds = getstandardlanes(m.finallanes);

        if ((stds == null) || (stds.length == 0) || (stds.length == 1)) {
            // Old version
//            ReferenceLanes rl = new ReferenceLanes(this);
//            rl.show();

            boolean[] selectstandards = selectstandards(lanesnames(m.finallanes),
                    whatarestandardlanes(m.finallanes));
            if (selectstandards.length < 2) {
                IJ.error("Select at least 2 lanes.");
                try {
                    addreferencelanes();
                } catch (Exception ex) {
                    Logger.getLogger(ReferenceLanes.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                for (int i = 0; i < selectstandards.length; i++) {
                    m.finallanes[i].setIsstandard(selectstandards[i]);
                }
                autobanddetection();
            }

        } else {
            autobanddetection();
        }

    }

    public void autobanddetection() {

        IJ.setTool("point");
        bm = new BandManager(m);
        ImagePlus imp = WindowManager.getCurrentImage();
        BandManagerCanvas cc = new BandManagerCanvas(imp);
        ImageWindow imageWindow = new ImageWindow(imp, cc);
        IJ.setTool("point");
        bm.setAssistant(this);
        bm.hide();
        bm.auto_detect();
        bandedition();


//        GenericDialog gd = new GenericDialog("Step 3 of 5. Band Detection");
//        gd.enableYesNoCancel();
//        gd.addMessage("Do you want to edit the selected bands?");
//        gd.addMessage("");
//        gd.showDialog();
//        lm.setAssistant(this);
//
//        if (gd.wasCanceled()) {
//            m.setVisible(true);
//        } else if (gd.wasOKed()) {
//            bm.setVisible(true);
//
//        } else {
//            bm.close();
//        }
    }
    
    public void bandedition (){
        
        if(bm==null){
                    bm = new BandManager(m);
                    bm.setAssistant(this);
        bm.hide();
        }

        
        
        BandEdition be = new BandEdition(this, bm);
        be.show();
    }

    public void normalise() throws UnsupportedEncodingException, Exception {

//        GenericDialog gd = new GenericDialog("Step 4 of 5. Normalisation");
//        gd.addMessage("Do you want to normalise the gel?");
//        gd.addMessage("Without normalising, you can only cluster lanes using curve-based methods.");
//        gd.enableYesNoCancel();
//        gd.showDialog();
//        // Old version
//        Normalise no = new Normalise(this);
//        no.show();
        normalisegel();

    }

    public void normalisegel() throws FileNotFoundException, UnsupportedEncodingException, Exception {
        Lane[] stdlanes = getstandardlanes(m.finallanes);

        int[] indexesstd = numberofbandslanes(stdlanes);

        if (!allequal(indexesstd)) {
            GenericDialog gdG = new GenericDialog("No Standards");
            gdG.addMessage("The number of bands in the reference lanes is different, "
                    + "all the reference lanes must have the same number of bands. ");

            for (int i = 0; i < stdlanes.length; i++) {
                gdG.addMessage(stdlanes[i].getName()
                        + " contains " + stdlanes[i].getPositionbands().length + ".");

            }

            // Old version
//            gdG.addMessage("Would you like to modify the number of bands?");
//
//            gdG.showDialog();
//            if (gdG.wasOKed()) {
            gdG.hideCancelButton();
            gdG.showDialog();
            if (gdG.wasOKed()) {
            bm = new BandManager(m);
            bm.setAssistant(this);
            ImagePlus imp = WindowManager.getCurrentImage();
            BandManagerCanvas cc = new BandManagerCanvas(imp);
            new ImageWindow(imp, cc);
            cc.requestFocus();

            for (Band band : m.getBands()) {
                bm.add(imp, band.getRoi(), 0, band.getLane());
                bm.showAll(0);
            }
            return;
            }

            // Old version
//            if (gdG.wasCanceled()) {
//                m.setVisible(true);
//            }
        }

        // We show the dialog asking the user whether she wants to use a 
        // previously defined standard or create a new one. 
        
        GenericDialog gdG = new GenericDialog("Standards");
        StandardsPanel sp = new StandardsPanel();
        ArrayList<String> availablestd = readstandardsavailable();

        if (availablestd == null || 0 == availablestd.size()) {
            sp.disableavailablestd();
        }
        sp.setlistofstandards(availablestd);
        gdG.add(sp);
        gdG.addMessage("");
        gdG.enableYesNoCancel("Ok", "Back");
        gdG.showDialog();

        if (gdG.wasCanceled()) {
            gdG.dispose();
            saveUnfinishedExperiment(m, 7);
            return;
        }
        
        if (!gdG.wasOKed()){
            gdG.dispose();
            bandedition();
            bm.showAll(0);
            return;
        }

        int[] indexes = indexesstandardlanes(m.finallanes);

        // If she chooses to create a new standard, she has to introduce a name,
        // and the molecular wheights for the bands of the standard.
        if (sp.isnewstandardselected()) {

            /////////////////////////////////////////////////////////////
            ImagePlus imp = WindowManager.getCurrentImage();
            NormaliseManager rm = new NormaliseManager();

            int[] positionbands = m.finallanes[indexes[0]].getPositionbands();
            if (positionbands != null) {
                for (int b : positionbands) {
                    Roi r0 = m.finallanes[indexes[0]].getRoi();
                    int r0x = r0.getBounds().x;
                    int r0y = r0.getBounds().y;
                    int w = r0.getBounds().width;
                    Rectangle r = new Rectangle(r0x, r0y + b, w, 2);
                    Roi r1 = rectangleintersectionroi(r0, r);
                    rm.add(imp, r1, 0);
                    rm.showAll(0);
                }
            }
            /////////////////////////////////////////////////////////////////// 

            double[] res = createStandard(m.finallanes[indexes[0]].getPositionbands().length);
            rm.close();
            if (res == null) {
                normalise();
            } else {
                for (int i = 0; i < indexes.length; i++) {
                    m.finallanes[indexes[i]].setStdpositionbands(res);
                }

            }
        } else {
            // If she chooses an available standard, we check that the standard contains
            // as much molecular weights as bands in the image. In such a case, we assign
            // the values; otherwise, we ask the user to remove/add the molecular weights that 
            // do not appear in the image. 
            String file = sp.nameoptionmenu();
            m.setMarker(file);
            double[] stdvalues = readstandardfromfile(file);
            if (stdvalues.length == m.finallanes[indexes[0]].getPositionbands().length) {
                for (int i = 0; i < indexes.length; i++) {
                    m.finallanes[indexes[i]].setStdpositionbands(stdvalues);
                }
            } else {
                IJ.error("The standard contains " + stdvalues.length + " elements,"
                        + " but the reference lane contains "
                        + m.finallanes[indexes[0]].getPositionbands().length
                        + " bands. Please, add or remove the necessary values.");

                /////////////////////////////////////////////////////////////
                ImagePlus imp = WindowManager.getCurrentImage();
                NormaliseManager rm = new NormaliseManager();

                int[] positionbands = m.finallanes[indexes[0]].getPositionbands();
                if (positionbands != null) {
                    for (int b : positionbands) {
                        Roi r0 = m.finallanes[indexes[0]].getRoi();
                        int r0x = r0.getBounds().x;
                        int r0y = r0.getBounds().y;
                        int w = r0.getBounds().width;
                        Rectangle r = new Rectangle(r0x, r0y + b, w, 2);
                        Roi r1 = rectangleintersectionroi(r0, r);
                        rm.add(imp, r1, 0);
                        rm.showAll(0);
                    }
                }
                /////////////////////////////////////////////////////////////////// 

                GenericDialog gdG2 = new GenericDialog("List of values");
                ListofValues lp = new ListofValues();
                lp.init();
                lp.addelements(stdvalues);
                gdG2.add(lp);
                gdG2.addMessage("");
                gdG2.showDialog();

                while (stdvalues.length != m.finallanes[indexes[0]].getPositionbands().length) {

                    if (gdG2.wasCanceled()) {
                        gdG2.dispose();
                        normalise();
                        return;
                    }
                    if (gdG2.wasOKed()) {
                        stdvalues = lp.getelements();
                        if (stdvalues.length != m.finallanes[indexes[0]].getPositionbands().length) {
                            IJ.error("The standard contains " + stdvalues.length + " elements,"
                                    + " but the reference lane contains "
                                    + m.finallanes[indexes[0]].getPositionbands().length
                                    + " bands.");

                            gdG2.dispose();
                            gdG2 = new GenericDialog("List of values");
                            lp = new ListofValues();
                            lp.init();
                            lp.addelements(stdvalues);
                            gdG2.add(lp);
                            gdG2.addMessage("");
                            gdG2.showDialog();
                        }

                    }

                }
                m.setInitialoffset(m.finallanes[indexes[0]].getPositionbands()[0]);
                double tempoffset = 0;
                for (int i = 0; i < indexes.length; i++) {
                    m.finallanes[indexes[i]].setStdpositionbands(stdvalues);
                    tempoffset = m.getInitialoffset()
                            - m.finallanes[indexes[i]].getPositionbands()[0];
                    m.finallanes[indexes[i]].setOffset(tempoffset);
                }

            }

        }

        // Finally, we ask the user to select the calibration method. 
        CurveCalibration cc = new CurveCalibration(
                intarraytodoublearray(m.finallanes[indexes[0]].getPositionbands()),
                m.finallanes[indexes[0]].getStdpositionbands(),
                m, this);

        m.normalisation = true;

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

        
        geljdb.updateexperiments(true,true);
        geljdb.enableall();

    }

    public void dendrogramgeneration() throws Exception {
        ImagePlus imp = IJ.getImage();
        Dendogram panelOptions = new Dendogram(imp);

        double tolerance_aux = 100;

        if (!m.normalisation) {

        } else {
            panelOptions.activatebandbased();
            computestdlanes(m.finallanes, m.getCalibrationmethod(), m.getInitialoffset());
            int[] indexes = indexesstandardlanes(m.finallanes);
            double[] stds = m.finallanes[indexes[0]].getStdpositionbands();
            tolerance_aux = stds[0] - stds[stds.length - 1];

        }

        boolean[] temp = selectlanes(lanesnames(m.finallanes));
        m.setSelectedlanesforcomparison(temp);
        if (temp == null || temp.length == 0) {
            return;
        }

        m.selectedlanes = new Lane[trueinbooleanarray(temp)];
        int j = 0;

        for (int i = 0; i < temp.length; i++) {
            if (temp[i]) {
                m.selectedlanes[j] = m.finallanes[i];
                j++;
            }

        }

        GenericDialog gdG = new GenericDialog("Step 5 of 5. Cluster Analysis");
        gdG.add(panelOptions);
        gdG.addMessage("");
        gdG.showDialog();
        if (gdG.wasCanceled()) {
            m.setVisible(true);
            return;
        }
        m.similaritymethod = panelOptions.getSimilarity();
        int clustering_alg = panelOptions.getClustering();
        m.setTol(panelOptions.getTolerance());
        double tolerance = tolerance_aux * panelOptions.getTolerance() / 100;
        int output = panelOptions.getOutput();

        LaneComparison lc;
        switch (m.similaritymethod) {
            case 0:
                lc = new Dice(tolerance);
                m.setSimmethod("Dice");
                break;
            case 1:
                lc = new Jaccard(tolerance);
                m.setSimmethod("Jaccard");
                break;
            case 2:
                lc = new Jeffrey(tolerance);
                m.setSimmethod("Jeffrey's X");
                break;
            case 3:
                lc = new Ochiai(tolerance);
                m.setSimmethod("Ochiai");
                break;
            case 4:
                lc = new BandDifference(tolerance);
                m.setSimmethod("Band difference");
                break;
            case 5:
                lc = new PearsonCorrelation();
                m.setSimmethod("Pearson correlation");
                break;
            case 6:
                lc = new CosineCorrelation();
                m.setSimmethod("Cosine coefficient");
                break;
            case 7:
                lc = new EuclideanDistance();
                m.setSimmethod("Euclidean distance");
                break;
            case 8:
                lc = new ManhattanDistance();
                m.setSimmethod("Manhattan distance");
                break;
            default:
                lc = new PearsonCorrelation();
                m.setSimmethod("Pearson correlation");
                break;
        }

        switch (clustering_alg) {
            case SINGLE:
                m.setLinkage("Single linkage");
                break;
            case COMPLETE:
                m.setLinkage("Complete linkage");
                break;
            case UPGMA:
                m.setLinkage("UPGMA");
                break;
            case MEAN:
                m.setLinkage("Mean linkage");
                break;
            case UPGMC:
                m.setLinkage("UPGMC");
                break;
            case WARD:
                m.setLinkage("Ward");
                break;
            default:
                break;
        }

        m.similaritytable = lc.CompareLanes(m.selectedlanes);
        PleaseWait pw = new PleaseWait((JDialog)null, false);
        pw.setVisible(true);
        pw.toFront();
        WekaClustering wc = new WekaClustering(clustering_alg, m.similaritytable,
                m.selectedlanes, m.similaritymethod, m.getinvertcolour(), output, m,null);
        wc.computeandvisualisecluster(pw);
        m.changefirstcomparison();
        m.createnewstudy();
        m.setVisible(true);
        m.enableall();

    }

    public ImagePlus getInitialimage() {
        return initialimage;
    }

    public void saveUnfinishedExperiment(Frame d, int step) throws IOException {

        int option = JOptionPane.showConfirmDialog(m, "The current analysis is unfinished, "
                + "do you want to save it for further use?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);

        if (option == JOptionPane.YES_OPTION) {
            String name = JOptionPane.showInputDialog("Introduce a name");
            int id = lastUnfinishedExperimentid();
            d.dispose();
            if(step==8){
                IJ.getImage().changes=false;        
                IJ.getImage().close();
            }
           
            Save s = new Save(IJ.getImage(), m.finallanes, m.getBands(), m.normalisation,
                        m.getInitialoffset(), m.getinvertcolour());
            Study study = getStudy(geljdb.studyid);
            s.saveunfinishedexperiment(study, id);
            UnfinishedExperiment ue = new UnfinishedExperiment(id, name, step, study);
            insertUnfinishedExperiment(ue);
            
        }
        
       
        IJ.getImage().changes=false;        
        IJ.getImage().close();
        geljdb.enableall();
        
    }

}
