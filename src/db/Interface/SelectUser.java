/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db.Interface;

import db.Classes.User;
import static db.Queries.Connection.creategeljdb;
import static db.Queries.UserQueries.getUsers;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joheras
 */
public class SelectUser extends javax.swing.JFrame {

    /**
     * Creates new form SelectUser
     */
    static {
        try {
            // Mysql driver
//            Class.forName("com.mysql.jdbc.Driver");
            // JavaDB driver
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            creategeljdb();
            createstandardfolder();
        } catch (ClassNotFoundException e) {
            System.out.println("No puedo cargar el driver JDBC de la BD");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SelectUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SelectUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ArrayList<User> users = new ArrayList<>();

    public SelectUser() {
        initComponents();
        updateusers();
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void updateusers() {
        jComboBoxUser.removeAllItems();
        users = getUsers();

        if (users != null && users.size() != 0) {
            for (User user : users) {
                jComboBoxUser.addItem(user.getUsername() + " (" + user.getName()
                        + " " + user.getSurname() + ")");
            }
        } else {
            jComboBoxUser.addItem("");
        }
        jComboBoxUser.addItem("<New User>");
        jComboBoxUser.setSelectedIndex(0);

    }

    private static void createstandardfolder() throws FileNotFoundException, UnsupportedEncodingException {
        File templateFile = new File(System.getProperty("user.dir") + "/standardfiles");
        if (!templateFile.exists()) {
            templateFile.mkdir();
        }
        double[] lambdaladder = {727.5, 679, 630.5, 582.0, 533.5, 485.0, 436.5, 388.0, 339.5, 291, 242.5, 194.0, 145.5, 97.0, 48.5};
        String workingfile = System.getProperty("user.dir") + "/standardfiles/Lambda_ladder_PFG_Marker.marker";
        PrintWriter writer = new PrintWriter(workingfile, "UTF-8");
        for (int i = 0; i < lambdaladder.length; i++) {
            writer.println(lambdaladder[i]);
        }
        writer.close();

        double[] lowrange = {194, 145.5, 97, 48.5, 23.1, 9.42, 6.55, 4.36, 2.32, 2.03};
        String workingfile1 = System.getProperty("user.dir") + "/standardfiles/Low_Range_PFG_Marker.marker";
        PrintWriter writer1 = new PrintWriter(workingfile1, "UTF-8");
        for (int i = 0; i < lowrange.length; i++) {
            writer1.println(lowrange[i]);
        }
        writer1.close();

        double[] midrange = {242.5, 227.5, 209, 194, 179, 160.5, 145.5, 130.5, 112.0, 97, 82, 63.5, 48.5, 33.5, 15};
        String workingfile2 = System.getProperty("user.dir") + "/standardfiles/Midrange_PFG_Marker_I.marker";
        PrintWriter writer2 = new PrintWriter(workingfile2, "UTF-8");
        for (int i = 0; i < midrange.length; i++) {
            writer2.println(midrange[i]);
        }
        writer2.close();

        File templateFileinfo = new File(System.getProperty("user.dir") + "/standardfiles/info");
        if (!templateFileinfo.exists()) {
            templateFileinfo.mkdir();
        }

        double[] lambdaladderinfo = {52, 600, 9};
//        double[] lambdaladderinfo = {26, 300, 9};
        workingfile = System.getProperty("user.dir") + "/standardfiles/info/Lambda_ladder_PFG_Marker.info";
        writer = new PrintWriter(workingfile, "UTF-8");
        for (int i = 0; i < lambdaladderinfo.length; i++) {
            writer.println(lambdaladderinfo[i]);
        }
        writer.close();
        double[] lambdaladderpos = {4, 15, 25, 36, 48, 61, 76, 91, 108, 126, 145, 168, 192, 220, 249};
        workingfile = System.getProperty("user.dir") + "/standardfiles/info/Lambda_ladder_PFG_Marker.pos";
        writer = new PrintWriter(workingfile, "UTF-8");
        for (int i = 0; i < lambdaladderpos.length; i++) {
            writer.println(lambdaladderpos[i]*2);
        }
        writer.close();

        double[] lowrangeinfo = {42,600,12};
//        double[] lowrangeinfo = {21,300,12};
        workingfile1 = System.getProperty("user.dir") + "/standardfiles/info/Low_Range_PFG_Marker.info";
        writer1 = new PrintWriter(workingfile1, "UTF-8");
        for (int i = 0; i < lowrangeinfo.length; i++) {
            writer1.println(lowrangeinfo[i]);
        }
        writer1.close();
        double[] lowrangepos = {21,34,53,80,98,122,139,160,225,244};
        workingfile1 = System.getProperty("user.dir") + "/standardfiles/info/Low_Range_PFG_Marker.pos";
        writer1 = new PrintWriter(workingfile1, "UTF-8");
        for (int i = 0; i < lowrangepos.length; i++) {
            writer1.println(lowrangepos[i]*2);
        }
        writer1.close();

        
        double[] midrangeinfo = {56,640,9};
        workingfile2 = System.getProperty("user.dir") + "/standardfiles/info/Midrange_PFG_Marker_I.info";
        writer2 = new PrintWriter(workingfile2, "UTF-8");
        for (int i = 0; i < midrangeinfo.length; i++) {
            writer2.println(midrangeinfo[i]);
        }
        writer2.close();
        double[] midrangepos = {12,21,36,46,59,80,94,112,138,156,177,206,223,241,273};
        workingfile2 = System.getProperty("user.dir") + "/standardfiles/info/Midrange_PFG_Marker_I.pos";
        writer2 = new PrintWriter(workingfile2, "UTF-8");
        for (int i = 0; i < midrangepos.length; i++) {
            writer2.println(midrangepos[i]*2);
        }
        writer2.close();
        
        
        
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBoxUser = new javax.swing.JComboBox();
        jButtonOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Select User");

        jLabel1.setText("User:");

        jComboBoxUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUserActionPerformed(evt);
            }
        });

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxUser, 0, 326, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonOk)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOk)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed

        int id = jComboBoxUser.getSelectedIndex();
//        Studies s = new Studies(users.get(id).getId());
        User u = users.get(id);
//        s.setTitle("Studies of " + u.getUsername()+ " (" + u.getName() + " " + 
//                    u.getSurname() + ")");
//        ImageorDB idb  = new ImageorDB(u);

        Main m = new Main(id);

        dispose();
        m.setVisible(true);


    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jComboBoxUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUserActionPerformed
        String sel = (String) jComboBoxUser.getSelectedItem();
        int elem = jComboBoxUser.getSelectedIndex();
        jComboBoxUser.setSelectedIndex(0);
        jComboBoxUser.updateUI();

        if (sel.equals("<New User>")) {
            NewUser nu = new NewUser(this, true, this);
            nu.show();
        }else{
                jComboBoxUser.setSelectedIndex(elem);
        }


    }//GEN-LAST:event_jComboBoxUserActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ImageorDB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ImageorDB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ImageorDB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ImageorDB.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SelectUser().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOk;
    private javax.swing.JComboBox jComboBoxUser;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
