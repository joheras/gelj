package plugins;

import ij.plugin.*;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import java.awt.event.*;

/**
    Adds a panel containing "Invert" and "Flip"  buttons to the right side of the current
    image or stack. Creates a blank 400x400 byte image if no image is open.
*/
public class Side_Panel implements PlugIn {

    static final int WIDTH = 400;
    static final int HEIGHT = 400;

    public void run(String arg) {
        ImagePlus imp = WindowManager.getCurrentImage();
        ImageProcessor ip;
        if (imp==null) {
            ip = new ByteProcessor(WIDTH, HEIGHT);
            ip.setColor(Color.white);
            ip.fill();
            imp = new ImagePlus("Side Panel Demo", ip);
        }
        if (imp.isHyperStack()) {
            IJ.error("Side Panel ", "This plugin does not work with hyperstacks.");
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);
        
        
        CustomWindow cw = new CustomWindow(imp, cc);
        cc.requestFocus();
        
    }


    class CustomCanvas extends ImageCanvas {
    
        CustomCanvas(ImagePlus imp) {
            super(imp);
            
        }

        public void paint(Graphics g) {
            super.paint(g);
        }
    
        /*public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            int newx = offScreenX(e.getX());
            int newy = offScreenY(e.getY());
            
            Rectangle r = new Rectangle(newx-10, 0, 20, imp.getHeight());
            imp.setRoi(r);
            
            
            ImagePlus imp2 = imp.duplicate();
            IJ.run(imp2, "Rotate 90 Degrees Left", "");
            IJ.run(imp2, "Select All", "");
            ProfilePlot pp = new ProfilePlot(imp2);
            double[] profile = pp.getProfile();
            
            int[] xValues = new int[profile.length];
            int[] yValues = new int[profile.length];
            for(int i=0;i<profile.length;i++){
            xValues[i]=i;
            yValues[i]= (int)profile[i];
            }   
            
            
                   
            
            ImagePlus imp4 = NewImage.createImage("Histogram", yValues.length,
                    255, 0, 8, 0);
            
            ImageProcessor ip = imp4.getProcessor();
            
            Polygon p = new Polygon(xValues, yValues, xValues.length);
            ip.drawPolygon(p);
            imp4.updateAndDraw();
            IJ.run(imp4,"Rotate 90 Degrees Right", "");
            IJ.run(imp4,"Flip Horizontally","");
            imp4.show();
            
            // Plot plot = new Plot("Histogram", "", "", xValues, profile);
            
            //ImagePlus imp3 = plot.getImagePlus();
            
            
            
            
            
        }*/

        /** Overrides handlePopupMenu() in ImageCanvas to suppress the right-click popup menu. */
       //protected void handlePopupMenu(MouseEvent e) {
       //}
    
    } // CustomCanvas inner class
    
    
    class CustomWindow extends ImageWindow implements ActionListener {
    
    private java.awt.Button button1;
    private java.awt.Button button10;
    private java.awt.Button button2;
    private java.awt.Button button3;
    private java.awt.Button button4;
    private java.awt.Button button5;
    private java.awt.Button button6;
    private java.awt.Button button7;
    private java.awt.Button button8;
    private java.awt.Button button9;
    private java.awt.Canvas canvas1;
    private java.awt.MenuBar menuBar1;
    private java.awt.Menu menuConfiguration;
    private java.awt.Menu menuFile;
    private java.awt.Menu menuHelp;
    private java.awt.MenuItem menuItemAbout;
    private java.awt.MenuItem menuItemExit;
    private java.awt.MenuItem menuItemHelpContents;
    private java.awt.MenuItem menuItemLoadImage;
    private java.awt.MenuItem menuItemLoadStudy;
    private java.awt.MenuItem menuItemNewProtocol;
    private java.awt.MenuItem menuItemSave;
    private java.awt.MenuItem menuItemSeeProtocols;
    private java.awt.Panel panel1;
    private java.awt.Panel panel2;
       
        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            removeAll();
            addPanel(ic);
            
        }
    
        void addPanel(ImageCanvas canvas1) {
        
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
            
        panel1 = new java.awt.Panel();
        button1 = new java.awt.Button();
        button2 = new java.awt.Button();
        button3 = new java.awt.Button();
        button4 = new java.awt.Button();
        button5 = new java.awt.Button();
        panel2 = new java.awt.Panel();
        button6 = new java.awt.Button();
        button7 = new java.awt.Button();
        button8 = new java.awt.Button();
        button9 = new java.awt.Button();
        button10 = new java.awt.Button();
        
        menuBar1 = new java.awt.MenuBar();
        menuFile = new java.awt.Menu();
        menuItemLoadImage = new java.awt.MenuItem();
        menuItemLoadStudy = new java.awt.MenuItem();
        menuItemSave = new java.awt.MenuItem();
        menuItemExit = new java.awt.MenuItem();
        menuConfiguration = new java.awt.Menu();
        menuItemNewProtocol = new java.awt.MenuItem();
        menuItemSeeProtocols = new java.awt.MenuItem();
        menuHelp = new java.awt.Menu();
        menuItemHelpContents = new java.awt.MenuItem();
        menuItemAbout = new java.awt.MenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        panel1.setLayout(new java.awt.GridLayout(0, 5));

        button1.setLabel("button1");
        panel1.add(button1);

        button2.setLabel("button2");
        panel1.add(button2);

        button3.setLabel("button3");
        panel1.add(button3);

        button4.setLabel("button4");
        panel1.add(button4);

        button5.setLabel("button5");
        panel1.add(button5);

        panel2.setLayout(new java.awt.GridLayout(5, 0));

        button6.setLabel("button6");
        panel2.add(button6);

        button7.setLabel("button7");
        panel2.add(button7);

        button8.setLabel("button8");
        panel2.add(button8);

        button9.setLabel("button9");
        panel2.add(button9);

        button10.setLabel("button10");
        panel2.add(button10);

        menuFile.setLabel("File");

        menuItemLoadImage.setLabel("Load Study");
        menuFile.add(menuItemLoadImage);

        menuItemLoadStudy.setLabel("Load Study");
        menuFile.add(menuItemLoadStudy);

        menuItemSave.setEnabled(false);
        menuItemSave.setLabel("Save");
        menuFile.add(menuItemSave);
        menuFile.addSeparator();
        menuItemExit.setLabel("Exit");
        menuFile.add(menuItemExit);

        menuBar1.add(menuFile);

        menuConfiguration.setLabel("Configuration");

        menuItemNewProtocol.setLabel("New Protocol");
        menuConfiguration.add(menuItemNewProtocol);

        menuItemSeeProtocols.setLabel("See Protocols");
        menuConfiguration.add(menuItemSeeProtocols);

        menuBar1.add(menuConfiguration);

        menuHelp.setLabel("Help");

        menuItemHelpContents.setLabel("Help Contents");
        menuHelp.add(menuItemHelpContents);

        menuItemAbout.setLabel("About");
        menuHelp.add(menuItemAbout);

        menuBar1.add(menuHelp);

        setMenuBar(menuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 112, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(canvas1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
            /*Panel panel = new Panel();
            panel.setLayout(new GridLayout(1, 1));
            ImagePlus imp4 = NewImage.createImage("Histogram", 255,
                    imp.getHeight(), 0, 8, 0);
            ImageCanvas ic2 = new ImageCanvas(imp4);
            
   
            panel.add(ic2);
            add(panel);
            pack();*/
        }
        
        private void exitForm(java.awt.event.WindowEvent evt) {                          
        super.close();
    }
      
        public void actionPerformed(ActionEvent e) {
        }
        
    } // CustomWindow inner class

   class CustomStackWindow extends StackWindow implements ActionListener {
    
        private Button button1, button2;
       
        CustomStackWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            setLayout(new FlowLayout());
            remove(sliceSelector);
            addPanel();
        }
    
        void addPanel() {
            Panel panel = new Panel();
            panel.setLayout(new GridLayout(4, 1));
            button1 = new Button("Invert Pixels");
            button1.addActionListener(this);
            panel.add(button1);
            button2 = new Button("Flip Vertically");
            button2.addActionListener(this);
            panel.add(button2);
            panel.add(new Label(""));
            panel.add(sliceSelector);
            add(panel);
            pack();
        }
      
        public void actionPerformed(ActionEvent e) {
            Object b = e.getSource();
            if (b==button1) {
                imp.getProcessor().invert();
                imp.updateAndDraw();
            } else {
                imp.getProcessor().flipVertical();
                imp.updateAndDraw();
            }
            ImageCanvas ic = imp.getCanvas();
            if (ic!=null)
                ic.requestFocus();
        }
        
    } // CustomStackWindow inner class

} // Side_Panel class
