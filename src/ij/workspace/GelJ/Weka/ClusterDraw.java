/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.workspace.GelJ.Weka;

import ij.IJ;
import ij.ImageJ;
import static ij.ImageJ.NO_SHOW;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.TextRoi;
import ij.process.ImageProcessor;
import static ij.process.ImageProcessor.BICUBIC;
import ij.workspace.GelJ.Assistant2.PleaseWait;
import ij.workspace.GelJ.Lane;
import static ij.workspace.GelJ.Lane.getImagesfromfinallanes;
import static ij.workspace.GelJ.Lane.getemptyimages;
import static ij.workspace.GelJ.Lane.getonlyImagesfromfinallanes;
import static ij.workspace.GelJ.Lane.getonlybandsfromfinallanes;
import ij.workspace.GelJ.Weka.HierarchyVisualizer.Node;
import java.awt.Color;
import java.awt.Font;
import static java.lang.Math.max;
import java.util.ArrayList;

/**
 *
 * @author joheras
 */
public class ClusterDraw {
    
    String graph;
    Lane[] lanes;
    int method;
    double scaleX = 100;
    double scaleY = 100;
    boolean invert;
    int output;
    Color c;
    ArrayList<String> fields;
    
    public ClusterDraw(String graph, Lane[] lanes, int method, boolean invert, int output, Color c, ArrayList<String> fields) {
        this.graph = graph;
        this.lanes = lanes;
        this.method = method;
        this.invert = invert;
        this.output = output;
        this.c = c;
        this.fields = fields;
    }
    
    public void showDiagram(PleaseWait pw) {
        
        if (IJ.getInstance() == null) {
            new ImageJ(NO_SHOW);
        }
        
        HierarchyVisualizer a = new HierarchyVisualizer(graph);
        Node tree = a.m_tree;
        ImagePlus[] images;
        
        switch (output) {
            case 0:
                images = getImagesfromfinallanes(lanes, method, c, invert);
                break;
            case 1:
                images = getonlyImagesfromfinallanes(lanes);
                break;
            case 2:
                images = getonlybandsfromfinallanes(lanes, invert);
                break;
            case 3:
                images = getemptyimages(lanes);
                break;
            default:
                images = getImagesfromfinallanes(lanes, method, c, invert);
                break;
        }
        
        double md = maxDepth(tree);
        
        int maxheight = images[0].getHeight();
//        for (ImagePlus i : images) {
//            if (maxheight < i.getHeight()) {
//                maxheight = i.getHeight();
//            }
//        }
        
        double scalefactor = 1;
        
        if (images[0].getHeight() < 600 && output!=3) {
            scalefactor = 600 / (double) images[0].getHeight();
        }
        
        int height = (int) (maxDepth(tree) * (int) scaleX * 10 + (maxheight * scalefactor) + (fields.size() * 200) + 100);
        int width = (int) (images[0].getWidth() * scalefactor * lanes.length + 75);
        
        ImagePlus impl = NewImage.createImage("Dendrogram", width, height, 0, 24, 0);
        ImageProcessor ip1 = impl.getProcessor();
        ip1.setColor(Color.white);
        ip1.fill();
        ip1.setColor(Color.black);
        ip1.setLineWidth(3);
        
        ip1.drawLine(width - 60, 0, width - 60, (int) (md * scaleX * 10));
        for (int i = 1; i < 11; i++) {
            ip1.setColor(new Color(240, 240, 240));
            ip1.drawLine(width - 30, i * ((int) (maxDepth(tree) * scaleX * 10)) / 10, 0, i * ((int) (maxDepth(tree) * scaleX * 10)) / 10);
            ip1.setColor(Color.black);
            ip1.setFont(new Font("SansSerif", Font.PLAIN, 15));
            ip1.drawString("" + i * 10, width - 58, i * ((int) (maxDepth(tree) * scaleX * 10)) / 10);
            ip1.drawLine(width - 59, i * ((int) (maxDepth(tree) * scaleX * 10)) / 10, width - 65, i * ((int) (maxDepth(tree) * scaleX * 10)) / 10);
            
            int pos1 = (i - 1) * ((int) (maxDepth(tree) * scaleX * 10)) / 10;
            int pos2 = i * ((int) (maxDepth(tree) * scaleX * 10)) / 10;
        }
        
        for (int j = 0; j < 100; j++) {
            ip1.drawLine(width - 59, j * ((int) (maxDepth(tree) * scaleX * 10)) / 100, width - 61, j * ((int) (maxDepth(tree) * scaleX * 10)) / 100);
        }
        
        scaleX = images[0].getWidth() * scalefactor;
        
        ip1.setColor(Color.black);
        
        drawdendrogram(ip1, tree);
        
        for (int i = 0; i < lanes.length; i++) {
            ImagePlus imp2 = images[i].duplicate();
            ImageProcessor ip = imp2.getProcessor();
            ip.setInterpolationMethod(BICUBIC);
            ip = ip.resize((int) (ip.getWidth() * scalefactor), (int) (ip.getHeight() * scalefactor), true);
            imp2.setProcessor(ip);
            imp2.updateAndDraw();
            ip = imp2.getProcessor();
            ip1.insert(ip, (int) (i * scaleX), (int) (maxDepth(tree) * 100 * 10 + 10));
//            ip1.setFont(new Font("SansSerif", Font.PLAIN, 100));
//            String name = lanes[i].getName();
//            int w = (int) ((i + 0.5) * scaleX - (images[i].getWidth() / 2));
//            int h = height - 175;
//            
//            TextRoi tr = new TextRoi(name.substring(0, name.lastIndexOf("\n")), w, h, new Font("SansSerif", Font.PLAIN, 100));
////            tr.setAngle(-90);
//            impl.setRoi(tr);
//            IJ.run(impl, "Add Selection...", "");

//            ip1.drawString(name.substring(0, name.lastIndexOf("\n")), (int) ((i + 0.5) * scaleX - (images[i].getWidth() / 2)), height - 175);
        }
        
        ip1.setLineWidth(1);
        IJ.run(impl, "Rotate 90 Degrees Left", "");
        
        ip1 = impl.getProcessor();
        
        for (int i = 0; i < fields.size(); i++) {
            TextRoi tr = new TextRoi(fields.get(i), height - 75 - (fields.size() - i) * 200, 30, new Font("SansSerif", Font.BOLD, 20));
            ip1.draw(tr);
        }
        
        for (int i = 0; i < lanes.length; i++) {
            
            String name = lanes[i].getName();
            String[] parts = name.substring(0, name.lastIndexOf("\t\t")).split("\t\t");
            // works//name.substring(0, name.lastIndexOf("\t\t")).replace("\t\t", "      ") 
            
            for (int j = 0; j < parts.length; j++) {
                TextRoi tr;
                if (j == 0) {
                    tr = new TextRoi(parts[j].substring(4), height - 75 - (fields.size() - j) * 200, width - ((i + 1) * scaleX) + (images[i].getWidth() / 2), new Font("SansSerif", Font.PLAIN, 20));
                    
                } else {
                    tr = new TextRoi(parts[j], height - 75 - (fields.size() - j) * 200, width - ((i + 1) * scaleX) + (images[i].getWidth() / 2), new Font("SansSerif", Font.PLAIN, 20));
                }
                ip1.draw(tr);
            }
            
        }
        
        impl.updateAndDraw();
        impl.show();
        impl.getWindow().toFront();
        
        impl.changes = true;
        
        pw.dispose();
//        int n = JOptionPane.showConfirmDialog(null, "Do you want to save the dendrogram?", "Save", JOptionPane.YES_NO_OPTION);
//
//        if (n == JOptionPane.YES_OPTION) {
//            IJ.saveAsTiff(impl, null);
//        }

    }
    
    void drawdendrogram(ImageProcessor ip, Node tree) {
        
        if (tree.isLeaf()) {
            ip.drawLine((int) (tree.m_fPosX * scaleX), (int) (tree.m_fPosY * scaleY * 10), (int) (tree.m_fPosX * scaleX), (int) ((tree.m_fPosY - tree.m_fLength) * scaleY * 10));
        } else {
            for (int i = 0; i < tree.m_children.length; i++) {
                drawdendrogram(ip, tree.m_children[i]);
                ip.drawLine((int) (tree.m_fPosX * scaleX), (int) (tree.m_fPosY * scaleY * 10), (int) (tree.m_fPosX * scaleX), (int) ((tree.m_fPosY - tree.m_fLength) * scaleY * 10));
                double fPosX1 = tree.m_children[0].m_fPosX;
                double fPosX2 = tree.m_children[tree.m_children.length - 1].m_fPosX;
                ip.drawLine((int) (fPosX1 * scaleX), (int) (tree.m_fPosY * scaleY * 10), (int) (fPosX2 * scaleX), (int) (tree.m_fPosY * scaleY * 10));
                
            }
            
        }
        
    }
    
    double maxDepth(Node node) {
        double res = 0;
        if (node.isLeaf()) {
            res = node.m_fPosY;
        } else {
            res = maxDepthNodes(node.m_children);
        }
        return res;
    }
    
    double maxDepthNodes(Node[] nodes) {
        double res = 0;
        
        for (Node n : nodes) {
            if (n.isLeaf()) {
                res = max(res, n.m_fPosY);
            } else {
                res = max(res, maxDepth(n));
            }
        }
        
        return res;
    }
    
    double maxWidth(Node node) {
        double res = 0;
        if (node.isLeaf()) {
            res = node.m_fPosX;
        } else {
            res = maxWidthNodes(node.m_children);
        }
        return res;
    }
    
    double maxWidthNodes(Node[] nodes) {
        double res = 0;
        
        for (Node n : nodes) {
            if (n.isLeaf()) {
                res = max(res, n.m_fPosX);
            } else {
                res = max(res, maxWidth(n));
            }
        }
        return res;
        
    }
    
}
