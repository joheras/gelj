/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.rfcalibration;

import ij.ImagePlus;
import ij.workspace.GelJ.Menu;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class rftable {
    
    private String[][] data;
    private int rows, columns;
    private rftableframe rfpanel;
    private String[][] datawithrflines;
    private String[] lanes;
    private String[][] finalassignment;
    private Menu menu;
    private ImagePlus imp;
    

    public rftable(String[][] data,Menu menu,ImagePlus imp) {
        this.data = data;
        rows = this.data.length;
        columns = this.data[0].length;
        this.menu = menu;
        this.imp = imp;
        showDialog();
    }
    
    
    public void showDialog() {
       
       datawithrflines = new String[rows][columns+1];
       lanes = new String[columns+1];
       
       for(int i = 0;i<rows;i++){
           for(int j = 0; j<=columns;j++){
               if(j==0){
                   datawithrflines[i][j] = "rf-line " + (i+1);
               }else{
                   datawithrflines[i][j] = data[i][j-1];
               }
           }
       }
       
       for(int j = 0; j<=columns;j++){
               if(j==0){
                   lanes[j]="";
               }else{
                   lanes[j] = "Lane" + j;
               }
        }
       
        //rfpanel.changemodel(data, lanes);
        
       rfpanel = new rftableframe();  
       ArrayList<String[]> bands = new ArrayList<String[]>();
       for(int i=0;i<data[0].length;i++){
           bands.add(getcolumn(data, i));
       }
       
       rfpanel.setrftable(this,bands);
       rfpanel.changemodel(datawithrflines, lanes);
       
      
    }
    
   
    
    
    
    private String[] getcolumn (String[][] data, int c){
        ArrayList<String> res = new ArrayList<String>();
        for(int i=0;i<data.length;i++){
            if(data[i][c]!=""){
                res.add(data[i][c]);
            }
        }
        res.add("");
        String[] res1 = new String[res.size()];
        for(int i = 0; i< res1.length;i++){
            res1[i] = res.get(i);
        }
        return res1;
    }

    public void setFinalassignment(String[][] finalassignment) {
        this.finalassignment = finalassignment;
    }

    public String[][] getFinalassignment() {
        return finalassignment;
    }
    
    
    
    
    
    
}
