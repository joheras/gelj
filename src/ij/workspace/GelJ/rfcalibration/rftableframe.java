/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.rfcalibration;

import ij.IJ;
import ij.plugin.frame.PlugInFrame;
import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author joheras
 */
public class rftableframe extends PlugInFrame {
    private JScrollPane jScrollPane1;
    private JTable jTable;
    private javax.swing.JButton jButtonAddrow;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDone;
    private javax.swing.JButton jButtonRefreshImage;
    private javax.swing.JButton jButtonDeleteRow;
    private rftable rft;
    private String[] headers;
    private ArrayList<String[]> bands;
    
    
    

    public rftableframe() {
        super("rf Calibration");
        showWindow();
    }

    public void showWindow() {
       jScrollPane1 = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable(){
            public boolean isCellEditable(int row,int column){  
                 if(column == 0) return false;//the 4th column is not editable  
                    return true;  
                }
        };
        jButtonAddrow = new javax.swing.JButton();
        jButtonRefreshImage = new javax.swing.JButton();
        jButtonDone = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonDeleteRow = new javax.swing.JButton();

        jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable);

        jButtonAddrow.setText("Add Row");
        jButtonAddrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddrowActionPerformed(evt);
            }
        });

        jButtonRefreshImage.setText("Refresh Image");
        jButtonRefreshImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshImageActionPerformed(evt);
            }
        });

        jButtonDone.setText("Ok");
        jButtonDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDoneActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonDeleteRow.setText("Delete Row");
        jButtonDeleteRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteRowActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 165, Short.MAX_VALUE)
                .addComponent(jButtonAddrow, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteRow, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRefreshImage, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDone, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonDone)
                    .addComponent(jButtonRefreshImage)
                    .addComponent(jButtonDeleteRow)
                    .addComponent(jButtonAddrow)))
        );
        setSize(700, 500);
        setResizable(true);
        
        show();
        
        
    }
    
    
    
    private void jButtonAddrowActionPerformed(java.awt.event.ActionEvent evt) { 
        int selectedRow = jTable.getSelectedRow();
        DefaultTableModel tm = (DefaultTableModel)jTable.getModel();
        
        if (selectedRow != -1){
            String[][] data = getTableData();
            String[][] newdata = addrflines(addrowatpositioni(data,selectedRow));
            changemodel(newdata,headers);
        } else{
            IJ.error("Select a row first");
        }
    }                                             

    private void jButtonRefreshImageActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        // TODO add your handling code here:
    }                                                   

    private void jButtonDoneActionPerformed(java.awt.event.ActionEvent evt) {   
        DefaultTableModel dtm = (DefaultTableModel) jTable.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        
        
        for (int c=1;c<nCol;c++){
            String[] col = getcolumndata(c);
            
            // Check for duplicates
            String duplicate = containsdupplicates(col);
            if (duplicate!=""){
                IJ.error("The element " + duplicate + " appears several times in Lane " + c);
                return;
            }
            
            // Check if elements are sorted
            String notsorted = notsorted(col);
            if (notsorted!=""){
                IJ.error("The elements in Lane " + c + " are not sorted ("+ notsorted + 
                        " is not in the correct position)");
                return;
            }
            
            // Check if all the bands have been selected
            String allselected = allselected(col,bands.get(c-1));
            if (allselected!=""){
                IJ.error(allselected + " has not been included in Lane " + c +
                        "(every band of a lane must appear in an rf-line)");
                return;
            }
        
        }
        
        rft.setFinalassignment(getTableDatawithoutfirstcolumn());
        close();
    }                                           

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {                                              
        close();
    }  
    
    private void jButtonDeleteRowActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        int selectedRow = jTable.getSelectedRow();
        DefaultTableModel tm = (DefaultTableModel)jTable.getModel();
        if (selectedRow != -1){
            String[] row = getrowdata(selectedRow);
            if (isemptyrow(row)){
                String[][] data = getTableData();
                String[][] newdata = removeElement(data,selectedRow);
                newdata = addrflines(newdata);
                changemodel(newdata,headers);
            }else{
                IJ.error("The selected row is not empty");
            } 
        } else{
            IJ.error("Select a non-empty row");
        }
        
    }   
    
    
    public void setrftable (rftable rft, ArrayList<String[]> bands){
        this.rft = rft;
        this.bands = bands;
    }
    
    public void changemodel (String[][] data, String[] names){
        headers = names;
        jTable.setModel(new javax.swing.table.DefaultTableModel(data,names));
        TableColumn column0 = jTable.getColumnModel().getColumn(0);
        
        
        for (int c=0; c<bands.size();c++){
            TableColumn column = jTable.getColumnModel().getColumn(c+1);
            JComboBox comboBox = new JComboBox();
            String[] col = new String[0];
            for (int r = 0; r<bands.get(c).length;r++){
                if (!ArrayUtils.contains(col,bands.get(c)[r])){
                    comboBox.addItem(bands.get(c)[r]);
                    col=ArrayUtils.add(col,bands.get(c)[r]);
                }
            }
            column.setCellEditor(new DefaultCellEditor(comboBox));
        }
           
    }
    
    

    
    
    private String[][] getTableData () {
        DefaultTableModel dtm = (DefaultTableModel) jTable.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        String[][] tableData = new String[nRow][nCol];
        for (int i = 0 ; i < nRow ; i++)
            for (int j = 0 ; j < nCol ; j++)
                tableData[i][j] = (String)dtm.getValueAt(i,j);
        return tableData;
    }
    
    
     private String[][] getTableDatawithoutfirstcolumn () {
        DefaultTableModel dtm = (DefaultTableModel) jTable.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        String[][] tableData = new String[nRow][nCol-1];
        for (int i = 0 ; i < nRow ; i++)
            for (int j = 1 ; j < nCol ; j++)
                tableData[i][j-1] = (String)dtm.getValueAt(i,j);
        return tableData;
    }
    
    
    private String[] getrowdata (int pos) {
        DefaultTableModel dtm = (DefaultTableModel) jTable.getModel();
        int nRow = pos, nCol = dtm.getColumnCount();
        String[] rowData = new String[nCol-1];
            for (int j = 1 ; j < nCol ; j++)
                rowData[j-1] = (String)dtm.getValueAt(nRow,j);
        return rowData;
       
    }
    
    
    private String[] getcolumndata (int pos) {
        DefaultTableModel dtm = (DefaultTableModel) jTable.getModel();
        int nRow = dtm.getRowCount(), nCol = pos;
        String[] colData = new String[nRow];
            for (int j = 0 ; j < nRow ; j++)
                colData[j] = (String)dtm.getValueAt(j,nCol);
        return colData;
       
    }
    
    
    private String[][] addrowatpositioni (String[][] data,int pos){
        String[][] res;
        String[] emptyrow = emptyStringArray(data[0].length);
        res = ArrayUtils.add(data,pos,emptyrow);
        return res;
    }
    
    
    private String[] emptyStringArray (int size){
        String[] res = new String[size];
        for (int i = 0; i<size;i++){
            res[i] = "";
        }
        return res;
    }
    
    
    private String[][] addrflines (String[][] data){
        String[][] res = data;
        
        for(int i=0;i<data.length;i++){
            res[i][0] =  "rf-line " + (i+1);
        }
        
        return res;
    }
    
    private boolean isemptyrow (String[] row){
        for(int i=0;i<row.length;i++){
            if(row[i]!=""){
                return false;
            }
        }
        return true;
    }

    private String[][] removeElement(String[][] data, int selectedRow) {
        
        String[][] res = new String[data.length-1][data[0].length];
        
        for(int i = 0;i<selectedRow;i++){
            res[i] = data[i];
        }
        for(int i = selectedRow+1;i<data.length;i++){
            res[i-1] = data[i];
        }
        return res;
    }

    private String containsdupplicates(String[] col) {
        for (int i = 0; i<col.length;i++){
            if(col[i]!=""){
                for(int j=i+1;j<col.length;j++){
                    if(col[i]==col[j]){
                        return col[i];
                    }
                }
              }
            }
        return "";
        
    }

    private String notsorted(String[] col) {
       for (int i = 0; i<col.length;i++){
            if(col[i]!=""){
                for(int j=i+1;j<col.length;j++){
                    if(col[j]!=""){
                        if(smallerBand(col[j],col[i])){
                             return col[i];
                        }
                    }
                }
              }
            } 
        return "";
    
    }

    private boolean smallerBand(String band1, String band2) {
        String b1 = band1.substring(5);
        String b2 = band2.substring(5);
        
        return (Integer.parseInt(b1)<Integer.parseInt(b2));
    
    
    }

    private String allselected(String[] col, String[] bands) {
        
        for(int i = 0;i<bands.length;i++){
            if(bands[i]!=""){
            if(!contains(col,bands[i])){
                return bands[i];
            }
         }
        }
        return "";
    }
    
    
    private boolean contains (String[] array, String elem){
        boolean res=false;
        for(int i = 0;i<array.length;i++){
            if(array[i]==elem){
                return true;
            }
        }
        return res;
    }
    
    
    
    
    
    
    
    
}
