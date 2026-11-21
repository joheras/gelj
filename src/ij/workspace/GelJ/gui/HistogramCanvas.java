/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.workspace.GelJ.BandManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 *
 * @author joheras
 */
public class HistogramCanvas extends ImageCanvas implements KeyListener {
    
    private BandManager bm;
    
    
        public HistogramCanvas(ImagePlus imp,BandManager bm) {
            super(imp);
            this.bm=bm;
            super.removeKeyListener(ij);
            super.addKeyListener(this);
        }


        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            bm.setVisible(false);
            bm.delete_temp_band();
            int y = (int)imp.getRoi().getFloatPolygon().ypoints[0];
            bm.add_temp_band(y);
            
            
            
            
            }
        
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
       int key = e.getKeyCode();
       
       BandManager bm=BandManager.getInstance();
	    if (bm == null) {
                return;
            }
       
       switch (key) {
           case KeyEvent.VK_A :
               bm.add_band();
               break;
           default:
               break;
       
       }
       
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }    
    
}
