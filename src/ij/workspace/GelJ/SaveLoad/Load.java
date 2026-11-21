/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ij.workspace.GelJ.SaveLoad;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.io.RoiDecoder;
import ij.workspace.GelJ.Band;
import static ij.workspace.GelJ.Lane.roisfromlanes;
import ij.workspace.GelJ.Menu;
import static ij.workspace.GelJ.SaveLoad.Lanewithoutimage.LaneswitoutimagetoLanes;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;



/**
 *
 * @author joheras
 */
public class Load {
    
    private Menu m;

    public Load(Menu m) {
        this.m = m;
    }
    
    
    
    public void loadimageorstudy ()  throws FileNotFoundException, IOException, ClassNotFoundException{
        
        GenericDialog gd = new GenericDialog("Load image or study...");
        ImageorStudy ios = new ImageorStudy();
        gd.add(ios);
        gd.addMessage("");
        gd.showDialog();
        
        if (gd.wasCanceled()){
            return;
        }
        
        if (ios.isnewimageselected()){
            OpenDialog od = new OpenDialog("Load an image...");
            Opener o = new Opener();
            //IJ.error(od.getPath());
            ImagePlus imp1 = o.openImage(od.getPath());
            imp1.show();
            //IJ.error("1");
        }else{
            OpenDialog od = new OpenDialog("Load a study...");
            String path = od.getPath();
            loadzipfile(path);
            }
    }
    
    
    public void loadzipfile (String path ) throws FileNotFoundException, IOException, ClassNotFoundException{
        ImagePlus imp = null;
        if(!path.endsWith(".zip")){
                IJ.error("You must select a zip file.");
            }else{
                ZipFile zipfile = new ZipFile(path);
                ZipInputStream zis = new ZipInputStream(new FileInputStream(path));
                ZipEntry entry_gel = zis.getNextEntry();
                ZipEntry entry_rois = zis.getNextEntry();
		ZipEntry entry_image = zis.getNextEntry();
                
                if (entry_gel==null) {
                    zis.close();
                    IJ.error("Empty file");
		    return;
		}
                String name_gel = entry_gel.getName();
                if (!name_gel.endsWith(".gel")) {
                    zis.close();
                    IJ.error("This zip file was not created with GelJ.");
		    return;
		}
                String name_image = entry_image.getName();
                if (name_image.endsWith(".tif")) {
                    Opener o = new Opener();
		    imp = o.openTiff(zis, name_image);
                    InputStream fs = zipfile.getInputStream(entry_gel);
                    m.setWindowname(imp.getTitle());
                    opengel(imp,fs,entry_rois,zipfile);
                    imp.show();
                    imp.setRoi(new Rectangle(0,0,0,0));
		}else{
                   zis.close();
                   IJ.error("This zip file was not created with GelJ.");
		   return;     
                
                }
                
                zis.close();
                
                
            
            }
    
    
    }
    
    
    
  private void opengel (ImagePlus imp, InputStream is, ZipEntry roizip,
          ZipFile zipfile) 
          throws IOException, ClassNotFoundException{
    
      
    Lanewithoutimage[] finallanes = null;
    Band[] bands = null;
    boolean normalisation = false;
    double initialoffset = 0;
    boolean invert = false;
                
    InputStream saveFile = is;
    ObjectInputStream save = new ObjectInputStream(saveFile);
    
    finallanes = (Lanewithoutimage[])save.readObject();
    
    /*
    
    for (int i = 0;i<rois.length;i++){
        rois[i] = (Roi)save.readObject();
    }*/
    
    if(finallanes!=null){
    Roi[] rois = new Roi[finallanes.length];
    int i=0;
    
    ByteArrayOutputStream out;
    ZipInputStream in = new ZipInputStream(zipfile.getInputStream(roizip));
    ZipEntry entry = in.getNextEntry();
    int len;
    byte[] buf = new byte[1024];
    while (entry != null) {
        String name = entry.getName();
        if (name.endsWith(".roi")) {
            out = new ByteArrayOutputStream();
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            byte[] bytes = out.toByteArray();
            RoiDecoder rd = new RoiDecoder(bytes, name);
            rois[i] = rd.getRoi();
            i++;
            entry = in.getNextEntry();
            }
    }
    
    
    
    bands = (Band[])save.readObject();
    normalisation = (Boolean)save.readObject();
    initialoffset = (Double)save.readObject();  
    invert = (Boolean)save.readObject();
    String marker = (String)save.readObject();
    
    m.setBands(bands);
    m.setNormalisation(normalisation);
    m.setInitialoffset(initialoffset);
    m.finallanes = LaneswitoutimagetoLanes(finallanes,imp,rois); ///
    m.setLanes(roisfromlanes(m.finallanes));
    m.setInvertcolour(invert);
    m.setMarker(marker);
    
    if (finallanes!=null && finallanes.length!=0){
        m.makebuttonsavailable();
    }
    
    if (bands!=null && bands.length!=0){
        m.makebuttonstdavailable();
    }
    
    
    save.close();
    
    }
      
  
  }  
    
    
    
    
    
    
    
}
