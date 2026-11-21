/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ;

import ij.plugin.frame.PlugInFrame;
import javax.swing.JLabel;

/**
 *
 * @author joheras
 */
public class WaitMessage  extends PlugInFrame {
    private JLabel jLabel1;

    public WaitMessage(String title) {
        super(title);
        showWindow();
    }
    
    void showWindow(){
        jLabel1 = new javax.swing.JLabel();

        jLabel1.setText("Please, wait...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        setSize(200, 41);
        setResizable(false);
        
        show();
        
    }
    
    @Override
    public void close(){
        super.close();
    }
    
}
