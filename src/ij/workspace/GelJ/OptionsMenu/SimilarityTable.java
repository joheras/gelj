/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.OptionsMenu;

import ij.IJ;
import ij.measure.ResultsTable;

/**
 *
 * @author joheras
 */
public class SimilarityTable {

    
    private double[][] data;
    private int method;
    private String[] labels;
    
    public SimilarityTable(double[][] data, int method, String[] labels) {
        this.data = data;
        this.method = method;
        this.labels = labels;
        showTable();
 
    }

    private void showTable() {
        if (data==null||data.length==0){
            IJ.error("This option is only available if you have previously "
                    + "constructed a dendrogram.");
        }else{
            ResultsTable rt = new ResultsTable();
            for(int i = 0;i<labels.length;i++){
                    rt.setHeading(i, labels[i]);
                }
                
                
                
                for(int i = 0; i<data.length;i++){
                    rt.incrementCounter();
                    rt.addLabel(labels[i]);
                }
                
                for(int i = 0; i<data.length; i++){
                    for(int j = 0; j<data[i].length;j++){
                        rt.setValue(i, j, data[i][j]);
                    
                    }
                }
                
                String show = "Similarity Table. Method: ";
                
                switch (method) {
                case 0:
                    show = show + "DICE";
                    break;
                case 1:
                    show = show + "Jaccard";
                    break;
                case 2:
                   show = show + "Jeffrey";
                    break;
                case 3:
                    show = show + "Ochiai";
                    break;
                case 4:
                    show = show + "Band Difference";
                    break;
                case 5:
                    show = show + "Pearson Correlation";
                    break;
                case 6:
                    show = show + "Cosine Correlation";
                    break;
                case 7:
                    show = show + "Euclidean Distance";
                    break;
                case 8:
                    show = show + "Manhattan Distance";
                    break;
                default:
                    show = show + "Pearson Correlation";
                    break;
            } 
                
                
                
                rt.show(show);
        
        
        }
    
    }
    
    
    
    
    
    
    
    
}
