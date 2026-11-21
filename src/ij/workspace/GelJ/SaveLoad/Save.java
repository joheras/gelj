/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.SaveLoad;

import db.Classes.Study;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.RoiEncoder;
import ij.io.SaveDialog;
import ij.workspace.GelJ.Band;
import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.SaveLoad.Lanewithoutimage.LanestoLaneswithoutimage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author joheras
 */
public class Save {
    
    private ImagePlus imp;
    private Lane[] finallanes;
    private Band[] bands;
    private boolean normalisation;
    private double initialoffset;
    private boolean invert;
    private String marker="";
    
     public Save(ImagePlus imp, Lane[] finallanes, Band[] bands, boolean normalisation,
            double initialoffset,boolean invert) {
        this.imp = imp;
        this.finallanes = finallanes;
        this.bands = bands;
        this.normalisation = normalisation;
        this.initialoffset = initialoffset;
        this.invert = invert;
        this.marker=marker;
    }

    public Save(ImagePlus imp, Lane[] finallanes, Band[] bands, boolean normalisation,
            double initialoffset,boolean invert,String marker) {
        this.imp = imp;
        this.finallanes = finallanes;
        this.bands = bands;
        this.normalisation = normalisation;
        this.initialoffset = initialoffset;
        this.invert = invert;
        this.marker=marker;
    }
    
    
    
    
    public void saveinfile () throws FileNotFoundException, IOException{
        
        SaveDialog sd = new SaveDialog("Save study...",
                imp.getTitle(), ".zip");
        String dir=sd.getDirectory();
        String name=sd.getFileName();
        if (dir == null || name == null)   return;
        
        FileSaver fs = new FileSaver(imp);
        fs.saveAsZip(dir+name);
        
        
        FileOutputStream saveFile=new FileOutputStream(dir+name.substring(0, name.length()-4)+
                ".gel");
        
        ObjectOutputStream save = new ObjectOutputStream(saveFile);
            //save.writeObject(imp);
            Lanewithoutimage[] ltlw = LanestoLaneswithoutimage(finallanes);
            save.writeObject(ltlw);
            // 
            save.writeObject(bands);
            save.writeObject(normalisation);
            save.writeObject(initialoffset);
             save.writeObject(invert);
            save.close();
        
        
        
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
                dir+name.substring(0, name.length()-4)+"_rois.zip"));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
        RoiEncoder re = new RoiEncoder(out);
        
        Roi[] rois = Lane.roisfromlanes(finallanes);
        for (Roi roi : rois) {
           zos.putNextEntry(new ZipEntry(roi.getName()+".roi"));
           re.write(roi);
           out.flush();
        }
        out.close();
        
        File zipfile = new File(dir+name);
       
        File[] files = {new File(dir+name.substring(0, name.length()-4)+".gel"),
        new File(dir+name.substring(0, name.length()-4)+"_rois.zip")};
        
        addFilesToZip(zipfile,files);
        
        files[0].delete();
        files[1].delete();
    }
    
    
    public void saveunfinishedexperiment (Study s, int id) throws FileNotFoundException, IOException{
        
        
        String dir=System.getProperty("user.dir") + "/GelJDBImages/" + s.getId() + "/" + s.getUser().getUsername() + "/unfinished/";
        String name=id + ".zip";
        
        FileSaver fs = new FileSaver(imp);
        fs.saveAsZip(dir+name);
        
        
        FileOutputStream saveFile=new FileOutputStream(dir+name.substring(0, name.length()-4)+
                ".gel");
        
        ObjectOutputStream save = new ObjectOutputStream(saveFile);
            //save.writeObject(imp);
            Lanewithoutimage[] ltlw = null;
            if(finallanes!=null){
             ltlw = LanestoLaneswithoutimage(finallanes);
            }
            save.writeObject(ltlw);
            // 
            save.writeObject(bands);
            save.writeObject(normalisation);
            save.writeObject(initialoffset);
             save.writeObject(invert);
             save.writeObject(marker);
            save.close();
        
        
        
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
                dir+name.substring(0, name.length()-4)+"_rois.zip"));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
        RoiEncoder re = new RoiEncoder(out);
        
         
        if(finallanes!=null){
        Roi[] rois = rois = Lane.roisfromlanes(finallanes);
        
        for (Roi roi : rois) {
           zos.putNextEntry(new ZipEntry(roi.getName()+".roi"));
           re.write(roi);
           out.flush();
        }
        out.close();
        
        }
        
        File zipfile = new File(dir+name);
       
        File[] files = {new File(dir+name.substring(0, name.length()-4)+".gel"),
        new File(dir+name.substring(0, name.length()-4)+"_rois.zip")};
        
        addFilesToZip(zipfile,files);
        
        files[0].delete();
        files[1].delete();
    }
    
    public static void addFilesToZip(File source, File[] files){
    try
    {

        File tmpZip = File.createTempFile(source.getName(), null);
        tmpZip.delete();
        if(!source.renameTo(tmpZip))
        {
            throw new Exception("Could not make temp file (" + source.getName() + ")");
        }
        byte[] buffer = new byte[1024];
        ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(source));

        for(int i = 0; i < files.length; i++)
        {
            InputStream in = new FileInputStream(files[i]);
            out.putNextEntry(new ZipEntry(files[i].getName()));
            for(int read = in.read(buffer); read > -1; read = in.read(buffer))
            {
                out.write(buffer, 0, read);
            }
            out.closeEntry();
            in.close();
        }

        for(ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry())
        {
            out.putNextEntry(ze);
            for(int read = zin.read(buffer); read > -1; read = zin.read(buffer))
            {
                out.write(buffer, 0, read);
            }
            out.closeEntry();
        }

        out.close();
        tmpZip.delete();
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }
}
    
    
    
 
}



   
