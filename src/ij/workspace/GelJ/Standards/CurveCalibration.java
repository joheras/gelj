/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ.Standards;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.measure.CurveFitter;
import static ij.plugin.frame.Fitter.plot;
import ij.workspace.GelJ.Assistant.Assistant;
import ij.workspace.GelJ.Assistant2.ImageAssistant;
import ij.workspace.GelJ.Assistant2.Step4;
import ij.workspace.GelJ.Assistant2.Step4new;
import ij.workspace.GelJ.Assistant3.step4_2;
import ij.workspace.GelJ.Assistant4.Image_Assistant;
import ij.workspace.GelJ.Assistant4.Step3_3;
import ij.workspace.GelJ.Menu;
import java.awt.Button;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joheras
 */
public class CurveCalibration {

    private double[] x, y;
    private CurveFitter cf;
    private int method;
    private Menu m;
    private Assistant a = null;
    private ImageAssistant ia = null;
    private Step4 s4 = null;
    private Step4new s4n = null;
    private Step3_3 s41 = null;
    private step4_2 s42 = null;
    private Image_Assistant ia1 = null;

    public CurveCalibration(double[] x, double[] y, Menu m) {
        this.x = x;
        this.y = y;
        this.m = m;

        cf = new CurveFitter(x, y);

        this.cf.doFit(12);
        plot(this.cf);

        ImagePlus imp = WindowManager.getCurrentImage();
        ImageCanvas cc = new ImageCanvas(imp);
        CurveCalibrationWindow cc1 = new CurveCalibrationWindow(imp, cc, x, y, cf, 0, m);
        cc1.requestFocus();
        cc1.setAlwaysOnTop(true);

    }

    public CurveCalibration(double[] x, double[] y, Menu m, Assistant a) {
        this.x = x;
        this.y = y;
        this.m = m;
        this.a = a;

        cf = new CurveFitter(x, y);

        this.cf.doFit(12);
        plot(this.cf);

        ImagePlus imp = WindowManager.getCurrentImage();
        ImageCanvas cc = new ImageCanvas(imp);
        CurveCalibrationWindow cc1 = new CurveCalibrationWindow(imp, cc, x, y, cf, 0, m);

        cc1.requestFocus();

    }

    public CurveCalibration(double[] x, double[] y, Menu m, ImageAssistant a, Step4 s4) {
        this.x = x;
        this.y = y;
        this.m = m;
        this.ia = a;
        this.s4 = s4;

        cf = new CurveFitter(x, y);

        this.cf.doFit(12);
        plot(this.cf);

        ImagePlus imp = WindowManager.getCurrentImage();
        ImageCanvas cc = new ImageCanvas(imp);
        CurveCalibrationWindow cc1 = new CurveCalibrationWindow(imp, cc, x, y, cf, 0, m);

        cc1.requestFocus();

    }

    public CurveCalibration(double[] x, double[] y, Menu m, ImageAssistant a, Step4new s4new) {
        this.x = x;
        this.y = y;
        this.m = m;
        this.ia = a;
        this.s4n = s4new;

        cf = new CurveFitter(x, y);

        this.cf.doFit(12);
        plot(this.cf);

        ImagePlus imp = WindowManager.getCurrentImage();
        ImageCanvas cc = new ImageCanvas(imp);
        CurveCalibrationWindow cc1 = new CurveCalibrationWindow(imp, cc, x, y, cf, 0, m);

        cc1.requestFocus();
        cc1.setAlwaysOnTop(true);

    }

    public CurveCalibration(double[] x, double[] y, Menu m, Image_Assistant a, Step3_3 s41) {
        this.x = x;
        this.y = y;
        this.m = m;
        this.ia1 = a;
        this.s41 = s41;

        cf = new CurveFitter(x, y);

        this.cf.doFit(12);
        plot(this.cf);

        ImagePlus imp = WindowManager.getCurrentImage();
        ImageCanvas cc = new ImageCanvas(imp);
        CurveCalibrationWindow cc1 = new CurveCalibrationWindow(imp, cc, x, y, cf, 0, m);

        cc1.requestFocus();
        cc1.setAlwaysOnTop(true);
    }

    public CurveCalibration(double[] x, double[] y, Menu m, Image_Assistant a, step4_2 s4new) {
        this.x = x;
        this.y = y;
        this.m = m;
        this.ia1 = a;
        this.s42 = s4new;

        cf = new CurveFitter(x, y);

        this.cf.doFit(12);
        plot(this.cf);

        ImagePlus imp = WindowManager.getCurrentImage();
        ImageCanvas cc = new ImageCanvas(imp);
        CurveCalibrationWindow cc1 = new CurveCalibrationWindow(imp, cc, x, y, cf, 0, m);

        cc1.requestFocus();
        cc1.setAlwaysOnTop(true);
    }

    class CurveCalibrationWindow extends ImageWindow implements ActionListener {

        private Button button1, button2, button3;
        private Choice choice;
        private double[] x;
        private double[] y;
        private CurveFitter cf;
        private int method;
        private Menu m;

        private final String[] fitList = {"Gaussian", "y = a+b*ln(x-c)", "Rodbard", "Log",
            "Straight Line", "2nd Degree Polynomial",
            "3rd Degree Polynomial", "4th Degree Polynomial", "Exponential", "Power",
            "Gamma Variate", "Rodbard (NIH Image)",
            "Exponential with Offset", "Exponential Recovery",
            "Inverse Rodbard", "Exponential (linear regression)", "Power (linear regression)",
            "5th Degree Polynomial", "6th Degree Polynomial", "7th Degree Polynomial", "8th Degree Polynomial",
            "Gaussian (no offset)", "Exponential Recovery (no offset)",
            "Chapman-Richards"};

        CurveCalibrationWindow(ImagePlus imp, ImageCanvas ic, double[] x, double[] y,
                CurveFitter cf, int method, Menu m) {

            super(imp, ic);
            setTitle("Curve calibration");
            setLayout(new FlowLayout());
            setResizable(true);
            this.x = x;
            this.y = y;
            this.cf = cf;
            this.method = method;
            this.m = m;
            addPanel();
            this.toFront();
        }

        void addPanel() {
            Panel panel = new Panel();
            panel.setLayout(new GridLayout(5, 1));
            choice = new Choice();

            for (int i = 0; i < fitList.length; i++) {
                choice.add(fitList[i]);
            }
            choice.select(method);
            Label l = new Label("Select regression method:");
            panel.add(l);
            panel.add(choice);

            button1 = new Button("Recompute");
            button1.addActionListener(this);
            panel.add(button1);
            Panel panel2 = new Panel();
            panel2.setLayout(new GridLayout(1, 2));
            button2 = new Button("Done");
            button2.addActionListener(this);
            panel2.add(button2);
            button3 = new Button("Cancel");
            button3.addActionListener(this);
            panel2.add(button3);
            panel.add(panel2);
            add(panel);
            pack();
        }

        public void actionPerformed(ActionEvent e) {
            Object b = e.getSource();
            if (b == button1) {
                int i = choice.getSelectedIndex();
                this.close();
                cf = new CurveFitter(x, y);

                switch (i) {
                    case 0:
                        cf.doFit(12);
                        break;

                    case 1:
                        cf.doFit(9);
                        break;

                    case 2:
                        cf.doFit(7);
                        break;

                    case 3:
                        cf.doFit(6);
                        break;

                    case 4:
                        cf.doFit(0);
                        break;

                    case 5:
                        cf.doFit(1);
                        break;

                    case 6:
                        cf.doFit(2);
                        break;

                    case 7:
                        cf.doFit(3);
                        break;

                    case 8:
                        cf.doFit(4);
                        break;

                    case 9:
                        cf.doFit(5);
                        break;

                    case 10:
                        cf.doFit(8);
                        break;

                    case 11:
                        cf.doFit(10);
                        break;

                    case 12:
                        cf.doFit(11);
                        break;

                    default:
                        cf.doFit(i);
                        break;
                }

                plot(cf);

                ImagePlus imp = WindowManager.getCurrentImage();
                ImageCanvas cc = new ImageCanvas(imp);
                CurveCalibrationWindow ccw = new CurveCalibrationWindow(imp, cc, x, y, cf, i, m);
                ccw.setAlwaysOnTop(true);
                ccw.toFront();
            } else if (b == button2) {
                switch (method) {
                    case 0:
                        m.setCalibrationmethod(12);
                        break;

                    case 1:
                        m.setCalibrationmethod(9);
                        break;

                    case 2:
                        m.setCalibrationmethod(7);
                        break;

                    case 3:
                        m.setCalibrationmethod(6);
                        break;

                    case 4:
                        m.setCalibrationmethod(0);
                        break;

                    case 5:
                        m.setCalibrationmethod(1);
                        break;

                    case 6:
                        m.setCalibrationmethod(2);
                        break;

                    case 7:
                        m.setCalibrationmethod(3);
                        break;

                    case 8:
                        m.setCalibrationmethod(4);
                        break;

                    case 9:
                        m.setCalibrationmethod(5);
                        break;

                    case 10:
                        m.setCalibrationmethod(8);
                        break;

                    case 11:
                        m.setCalibrationmethod(10);
                        break;

                    case 12:
                        m.setCalibrationmethod(11);
                        break;

                    default:
                        m.setCalibrationmethod(method);
                        break;
                }
                try {
                    m.savestandardadditionalinfo();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(CurveCalibration.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(CurveCalibration.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.close();
//                 IJ.error("The normalisation process has finished");
//                 
                if (a != null) {
                    try {
                         // Old version
//                         a.dendrogramgeneration();

                        a.finishnormalisation();

                    } catch (Exception ex) {
                        Logger.getLogger(CurveCalibration.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (ia != null || ia1 != null) {
                    try {
                        // Old version
//                         a.dendrogramgeneration();
                        if (s4 != null) {
                            s4.dispose();
                        } else if (s4n != null) {
                            s4n.dispose();
                        } else if (s41 != null) {
                            s41.dispose();
                        } else if (s42 != null) {
                            s42.dispose();
                        }
//                         ia.finishnormalisation();
                        ia1.finishnormalisation();

                    } catch (Exception ex) {
                        Logger.getLogger(CurveCalibration.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } else if (b == button3) {
                try {
                    if (a != null) {
                        a.saveUnfinishedExperiment(this, 8);
                    }
                    if (ia != null) {
                        ia.saveUnfinishedExperiment(this, 8);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CurveCalibration.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

    }

}
