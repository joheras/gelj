/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ;

import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.workspace.GelJ.Assistant.Assistant;


/**
 *
 * @author joheras
 */
public class GelJ implements PlugIn {
    
    private ImagePlus imp;
    
    public void run(String args) {
        
        //imp = IJ.getImage();
        
       /* MenuGelJ panelOptions = new MenuGelJ();
        GenericDialog gdG = new GenericDialog("GelJ Menu");
        gdG.add(panelOptions);
        //gdG.addMessage("");
        gdG.hideCancelButton();
        gdG.showDialog();   */
        Menu m = new Menu();
        m.setVisible(false);
        Assistant a = new Assistant(m);
        a.initAssistant();
        
        
    }

  
}
