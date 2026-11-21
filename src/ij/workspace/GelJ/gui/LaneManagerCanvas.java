/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.workspace.GelJ.LaneManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 *
 * @author joheras
 */
public class LaneManagerCanvas extends ImageCanvas implements KeyListener {
    
        public LaneManagerCanvas(ImagePlus imp) {
            super(imp);
            super.removeKeyListener(ij);
            super.addKeyListener(this);
        }


        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            LaneManager lm=LaneManager.getInstance();
	    if (lm == null) {
                return;
            }
            int index = lm.getRoiIndexPos(offScreenX(e.getX()), offScreenX(e.getY()));
            if (index < 0) {
                return ;
            }
            lm.select(imp, index);
            
            
            
            }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
       int key = e.getKeyCode();
       
       LaneManager lm=LaneManager.getInstance();
	    if (lm == null) {
                return;
            }
       
       if (!e.isControlDown()){     
            switch (key) {
                case KeyEvent.VK_A :
                    lm.addRoi(false);
                    break;
                case KeyEvent.VK_DELETE :
                    lm.delete(false);
                    break;  
                case KeyEvent.VK_F :
                    lm.fix_standard();
                    break;    
                default:
                    break;
        }}else{
            switch (key) {
                case KeyEvent.VK_Z :
                    IJ.run(imp, "Undo", "");
                    break;    
                }        
       }
       
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
 
    
}
