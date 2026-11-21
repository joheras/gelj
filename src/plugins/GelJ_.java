/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.workspace.GelJ.*;
/**
 *
 * @author joheras
 */
public class GelJ_ implements PlugIn {
    
    ImagePlus imp;
    

    @Override
    public void run(String arg) {
       GelJ aux = new GelJ ();
        
       aux.run(arg);
       //IJ.run("Action Bar","/ij/workspace/GelJ/GelJ.txt");
       
       IJ.getInstance().setVisible(false);
       
      
       
    }
    
}
