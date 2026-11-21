package db.Queries;

import ij.io.OpenDialog;
import ij.io.SaveDialog;
import static ij.workspace.GelJ.Utils.Utilities.deleteDir;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

/**
 *
 * @author joheras
 */
public class BackupQueries {

    public static void createbackup() {

        List<File> fileList = new ArrayList<File>();

        //images
        File dbdirectoryToZip = new File(System.getProperty("user.dir"));
        getAllFiles(dbdirectoryToZip, fileList, true);
        
        //images
//        File imagesdirectoryToZip = new File(System.getProperty("user.dir") + "/GelJDBImages/");
//        getAllFiles(imagesdirectoryToZip, fileList);
//        //standards
//        File standarddirectoryToZip = new File(System.getProperty("user.dir") + "/standardfiles/");
//        getAllFiles(standarddirectoryToZip, fileList);
//        
        
        writeZipFile(fileList);

    }

    public static void restorebackup() throws IOException {
        OpenDialog od = new OpenDialog("Restore a backup...");
        String path = od.getPath();
        if (!path.endsWith(".zip")) {
            JOptionPane.showMessageDialog(null, "You must select a zip file", "Restore backup", JOptionPane.ERROR_MESSAGE);
        } else {

            try{
                DriverManager.getConnection("jdbc:derby:;shutdown=true;user=test;password=test");
            }catch(SQLException e){
            
            }
            
            unzipFunction(path);
            File fdbs = new File(System.getProperty("user.dir") + "/temp-geljdb/geljdb");
            File fdbt = new File(System.getProperty("user.dir") + "/geljdb");
            fdbt.renameTo(new File(System.getProperty("user.dir") + "/geljdb-1"));
            Files.move(fdbs.toPath(), fdbt.toPath(), REPLACE_EXISTING);
            File fims = new File(System.getProperty("user.dir") + "/temp-geljdb/GelJDBImages");
            File fimt = new File(System.getProperty("user.dir") + "/GelJDBImages");
            fimt.renameTo(new File(System.getProperty("user.dir") + "/GelJDBImages-1"));
            Files.move(fims.toPath(), fimt.toPath(), REPLACE_EXISTING);
            File fstandards = new File(System.getProperty("user.dir") + "/temp-geljdb/standardfiles");
            File fstandardt = new File(System.getProperty("user.dir") + "/standardfiles");
            fstandardt.renameTo(new File(System.getProperty("user.dir") + "/standardfiles-1"));
            Files.move(fstandards.toPath(), fstandardt.toPath(), REPLACE_EXISTING);

            
            deleteDir(new File(System.getProperty("user.dir") + "/temp-geljdb"));
            deleteDir(new File(System.getProperty("user.dir") + "/geljdb-1"));
            deleteDir(new File(System.getProperty("user.dir") + "/GelJDBImages-1"));
            deleteDir(new File(System.getProperty("user.dir") + "/standardfiles-1"));
            
            JOptionPane.showMessageDialog(null, "You need to restart GelJDB to finish the recovery.");
//            restartApplication();
        }
    }

/// Zipping and unzipping
    public static void getAllFiles(File dir, List<File> fileList, boolean first) {
        try {
            File[] files = dir.listFiles();
            File file1 = new File(System.getProperty("user.dir") + "/GelJDBImages");
            File file2 = new File(System.getProperty("user.dir") + "/geljdb");
            File file3 = new File(System.getProperty("user.dir") + "/standardfiles");
            for (File file : files) {
                if (!first) {
                    fileList.add(file);
                    if (file.isDirectory()) {
                        System.out.println("directory:" + file.getCanonicalPath());
                        getAllFiles(file, fileList, false);
                    } else {
                        System.out.println("     file:" + file.getCanonicalPath());
                    }
                } else {
                    if (file.isDirectory()
                            && (file.getPath().equals(file1.getPath())
                            || file.getPath().equals(file2.getPath())
                            || file.getPath().equals(file3.getPath()))) {
                        fileList.add(file);
                        getAllFiles(file, fileList, false);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeZipFile(List<File> fileList) {

        try {
            java.text.SimpleDateFormat todaysDate
                    = new java.text.SimpleDateFormat("yyyy-MM-dd");

            SaveDialog sd = new SaveDialog("Save backup...",
                    "geljdb-" + todaysDate.format((java.util.Calendar.getInstance()).getTime()), ".zip");
            String dir = sd.getDirectory();
            String name = sd.getFileName();
            if (dir == null || name == null) {
                return;
            }

            File directoryToZip = new File(System.getProperty("user.dir"));

            FileOutputStream fos = new FileOutputStream(dir + name);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : fileList) {
                if (!file.isDirectory()) { // we only zip files, not directories
                    addToZip(directoryToZip, file, zos);
                }
            }
            zos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
            IOException {

        FileInputStream fis = new FileInputStream(file);

        // we want the zipEntry's path to be a relative path that is relative
        // to the directory being zipped, so chop off the rest of the path
        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                file.getCanonicalPath().length());
        System.out.println("Writing '" + zipFilePath + "' to zip file");
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    public static void unzipFunction(String filename) {
        File srcFile = new File(filename);

        // create a directory with the same name to which the contents will be extracted
        String zipPath = System.getProperty("user.dir") + "/temp-geljdb";
        File temp = new File(zipPath);
        temp.mkdir();

        ZipFile zipFile = null;

        try {

            zipFile = new ZipFile(srcFile);
            // get an enumeration of the ZIP file entries
            Enumeration<? extends ZipEntry> e = zipFile.entries();

            while (e.hasMoreElements()) {

                ZipEntry entry = e.nextElement();

                File destinationPath = new File(zipPath, entry.getName());

                //create parent directories
                destinationPath.getParentFile().mkdirs();

                // if the entry is a file extract it
                if (entry.isDirectory()) {
                    continue;
                } else {

                    System.out.println("Extracting file: " + destinationPath);

                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

                    int b;
                    byte buffer[] = new byte[1024];

                    FileOutputStream fos = new FileOutputStream(destinationPath);

                    BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }

                    bos.close();
                    bis.close();

                }

            }

        } catch (IOException ioe) {
            System.out.println("Error opening zip file" + ioe);
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing zip file" + ioe);
            }
        }

    }

}
