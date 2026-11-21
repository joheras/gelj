/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author joheras
 */
public class MyWindow extends ImageWindow implements ActionListener{
    
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

    public MyWindow(ImagePlus imp, ImageCanvas ic) {
        super(imp);
        removeAll();
        canvas1 = ic;
        initComponents();
        ic.requestFocus();
        
    }
    
    
    private void initComponents() {
        
        
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
        setLayout(new java.awt.GridLayout(1, 3));
        add(canvas1);

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

        pack();
    }// </editor-fold>                        

    
    private void exitForm(java.awt.event.WindowEvent evt) {                          
        System.exit(0);
    }   

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
