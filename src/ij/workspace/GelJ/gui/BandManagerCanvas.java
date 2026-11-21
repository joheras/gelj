/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.gui;

import ij.IJ;
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
public class BandManagerCanvas extends ImageCanvas implements KeyListener {
    
        public BandManagerCanvas(ImagePlus imp) {
            super(imp);
            super.removeKeyListener(ij);
            super.addKeyListener(this);
        }


        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            BandManager bm=BandManager.getInstance();
	    if (bm == null) {
                return;
            }
            bm.delete_temp_band();
            int x = (int)imp.getRoi().getFloatPolygon().xpoints[0];
            int y = (int)imp.getRoi().getFloatPolygon().ypoints[0];
            
            if(bm.ishistogramcheckboxselected()){
                bm.showprofile(x,y);
            }
            
            int index = bm.getRoiIndexPos(x, y);
            imp.getWindow().toFront();
            //int index = bm.getRoiIndexPos(offScreenX(e.getX()), offScreenX(e.getY()));
            if (index < 0) {
                return ;
            }
            bm.select(imp, index);
            
            
            
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
           case KeyEvent.VK_DELETE :
               bm.delete(false);
               break;
           case KeyEvent.VK_P :
               IJ.setTool("point");
               break;     
           case KeyEvent.VK_Z :
               if (e.isControlDown() && !e.isShiftDown()){
                  bm.undo();
               }else if (e.isControlDown() && e.isShiftDown()){
                  bm.redo();
               }else{
                   IJ.setTool("zoom");
               }
               break;
           case KeyEvent.VK_H :
               IJ.setTool("hand");
               break;
           case KeyEvent.VK_U :
               bm.undo();
               break; 
           case KeyEvent.VK_R :
               bm.redo();
               break;
           case KeyEvent.VK_C :
               bm.pick_colour();
               break;
           default:
               break;
       
       }
       
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }    
    
}
