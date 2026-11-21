/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db.PopulationGenetic;

import db.Classes.Comparison;
import db.Classes.LaneDB;
import static db.Interface.EditViewLaneDB.LaneDBtoLaneIJ;
import ij.io.DirectoryChooser;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.text.TextWindow;
import ij.workspace.GelJ.Lane;
import ij.workspace.GelJ.Utils.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 *
 * @author jonathan
 */
public class xmlCreator {

    private Comparison c;
    private ArrayList<String> fields;
    private ArrayList<Double> mws = new ArrayList<Double>();
    // The hashtable representes Population -> Individuals (Individual name, occurrence matrix)
    private Hashtable<String, ArrayList<Pair<String, ArrayList<Boolean>>>> hashtable = new Hashtable<String, ArrayList<Pair<String, ArrayList<Boolean>>>>();
    private ArrayList<String> xslts;

    public xmlCreator(Comparison c, ArrayList<String> fields, ArrayList<String> xslts) throws Exception {
        this.c = c;
        this.fields = fields;
        this.xslts = xslts;
        generateOcurrenceMatrix();
        generateDocument();
        convertXML();

    }

    public void convertXML() throws IOException, TransformerConfigurationException, TransformerException {
        DirectoryChooser dc = new DirectoryChooser("Select a directory to save the exported documents");

        String dir = dc.getDirectory();

        if (dir == null) {
            return;
        }
        File xmlFile = new File(System.getProperty("user.dir") + "/temp.xml");
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xmlSource = new StreamSource(xmlFile);
        
        for (String xsltName : xslts) {
            File xsltFile = new File(System.getProperty("user.dir") + "/xslts/" + xsltName + ".xsl");
            Source xslt = new StreamSource(xsltFile);
            Transformer transformer = factory.newTransformer(xslt);
            transformer.transform(xmlSource, new StreamResult(new File(dir + c.getName() + "_" + xsltName +  ".txt")));
            
            
            File instructions = new File(System.getProperty("user.dir") + "/xslts/instructions/" + xsltName + ".txt");
            File instructionsCopy = new File(dir + "instructions" + "_" + xsltName +  ".txt");
            if(instructions.exists()){
            Files.copy(instructions.toPath(),instructionsCopy.toPath(), REPLACE_EXISTING);
            }
        }

        File f = new File(dir + "/gelj.xml");
        Files.copy(xmlFile.toPath(), f.toPath(), REPLACE_EXISTING);
        
        File fgeljXMLsource = new File(System.getProperty("user.dir") + "/xslts/html/gelj.xsl");
        File fgeljXMLtarget = new File(dir + "/gelj.xsl");
        Files.copy(fgeljXMLsource.toPath(), fgeljXMLtarget.toPath(), REPLACE_EXISTING);

        c.getName();

    }

    public void generateDocument() throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("study");
        doc.appendChild(rootElement);
        
        doc.setXmlStandalone(true);
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"gelj.xsl\"");
        doc.insertBefore(pi, rootElement);
        
        // name elements
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(c.getName()));
        rootElement.appendChild(name);

        /// Compute number of populations
        Element npopulations = doc.createElement("n_populations");
        npopulations.appendChild(doc.createTextNode("" + hashtable.size()));
        rootElement.appendChild(npopulations);

        Element nindividuals = doc.createElement("n_individuals");
        nindividuals.appendChild(doc.createTextNode("" + c.getLanes().size()));
        rootElement.appendChild(nindividuals);

        Element nloci = doc.createElement("n_loci");
        nloci.appendChild(doc.createTextNode("" + mws.size()));
        rootElement.appendChild(nloci);

        Element loci = doc.createElement("loci");
        for (int i = 0; i < mws.size(); i++) {
            Element locus = doc.createElement("locus");
            locus.appendChild(doc.createTextNode("Locus" + (i+1) + "(" + (Math.round(mws.get(i) * 100.0) / 100.0)  + ")"));
            loci.appendChild(locus);
        }
        rootElement.appendChild(loci);

        Element populations = doc.createElement("populations");
        Enumeration<String> keys = hashtable.keys();

        int popid = 0;
        int indid = 0;

        while (keys.hasMoreElements()) {
            popid++;
            String pop = keys.nextElement();
            Element population = doc.createElement("population");
            Element popname = doc.createElement("name");
            popname.appendChild(doc.createTextNode(pop));
            population.appendChild(popname);
            Element popidxml = doc.createElement("ID");
            popidxml.appendChild(doc.createTextNode("" + popid));
            population.appendChild(popidxml);

            ArrayList<Pair<String, ArrayList<Boolean>>> individuals = hashtable.get(pop);

            Element popnindividuals = doc.createElement("n_individuals");
            popnindividuals.appendChild(doc.createTextNode("" + individuals.size()));
            population.appendChild(popnindividuals);

            Element individualsXML = doc.createElement("individuals");
            for (int i = 0; i < individuals.size(); i++) {
                indid++;

                Element individualXML = doc.createElement("individual");
                Element individualName = doc.createElement("name");
                individualName.appendChild(doc.createTextNode(individuals.get(i).getKey()));
                individualXML.appendChild(individualName);

                ArrayList<Boolean> locus = individuals.get(i).getValue();
                for (Boolean b : locus) {
                    Element locusXML = doc.createElement("locus");
                    locusXML.appendChild(doc.createTextNode("" + b));
                    individualXML.appendChild(locusXML);
                }

                Element individualID = doc.createElement("ID");
                individualID.appendChild(doc.createTextNode("" + indid));
                individualXML.appendChild(individualID);

                individualsXML.appendChild(individualXML);
            }

            population.appendChild(individualsXML);
            populations.appendChild(population);

        }

        rootElement.appendChild(populations);

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(System.getProperty("user.dir") + "/temp.xml"));
        transformer.transform(source, result);

        System.out.println("File saved!");

    }

    private void generateOcurrenceMatrix() throws IOException, Exception {

        Lane[] selectedlanes = new Lane[c.getLanes().size()];

        for (int i = 0; i < selectedlanes.length; i++) {
            LaneDB l = c.getLanes().get(i);
            selectedlanes[i] = LaneDBtoLaneIJ(l);
            selectedlanes[i].setName("");
            for (String field : fields) {
                String name = selectedlanes[i].getName();
                switch (field) {
                    case "Genus":
                        selectedlanes[i].setName(name + l.getGenus() + " ");
                        break;
                    case "Species":
                        selectedlanes[i].setName(name + l.getSpecies() + " ");
                        break;
                    case "Strain Number":
                        selectedlanes[i].setName(name + l.getStrainNumber() + " ");
                        break;
                    case "Country":
                        selectedlanes[i].setName(name + l.getCountry() + " ");
                        break;
                    case "MLST":
                        selectedlanes[i].setName(name + l.getMSLT() + " ");
                        break;
                    case "isMarker":
                        selectedlanes[i].setName(name + l.isIsMarker() + " ");
                        break;
                    case "Experiment":
                        selectedlanes[i].setName(name + l.getExperiment().getName() + " ");
                        break;
                    case "Comments":
                        selectedlanes[i].setName(name + l.getComments() + " ");
                        break;
                    default:
                        selectedlanes[i].setName(name + l.getLaneaddedinfo().get(field) + " ");
                        break;
                }

            }
            selectedlanes[i].setName(selectedlanes[i].getName());
        }

        double[] stds = selectedlanes[0].getStdpositionbands();
        double tolerance_aux = stds[0] - stds[stds.length - 1];
        double tolerance = tolerance_aux * c.getTolerance() / 100;

        getMWs(selectedlanes, tolerance);
        createOccurrenceMatrix(selectedlanes, tolerance, mws);

//        ArrayList list = new ArrayList();
//        String headings = "mw \t";
//        for (int i = 0; i < selectedlanes.length - 1; i++) {
//            String name = selectedlanes[i].getName().replaceAll("\t", " ");
//            headings = headings + name.substring(4, name.lastIndexOf(" ")) + "\t";
//        }
//        String name = selectedlanes[selectedlanes.length - 1].getName().replaceAll("\t", " ");
////        headings = headings + name.substring(4, name.lastIndexOf(" "));
//
//        for (int i = 0; i < mws.size(); i++) {
//            name = "" + mws.get(i);
//            String row = name + "\t";
//            for (int j = 0; j < selectedlanes.length - 1; j++) {
//                row = row + (1 - similaritytable[i][j]) + "\t";
//            }
//            row = row + similaritytable[i][selectedlanes.length - 1];
//            list.add(row);
//        }
//        TextWindow textWindow = new TextWindow("Occurrence Matrix", headings, list, 600, 400);
//        textWindow.setVisible(true);
    }

    private void createOccurrenceMatrix(Lane[] selectedlanes, double tolerance, ArrayList<Double> mws) {

        int[][] matrix = new int[mws.size()][selectedlanes.length];
        for (int i = 0; i < selectedlanes.length; i++) {

            String popname = c.getLanes().get(i).getLaneaddedinfo().get("Population");
            if (popname == null) {
                popname = "defpop";
            }
            String indname = selectedlanes[i].getName();
            double[] stds = selectedlanes[i].getStdpositionbands();
            ArrayList<Boolean> occurrences = new ArrayList<Boolean>();
            for (int j = 0; j < mws.size(); j++) {
                int k = 0;
                boolean b = true;
                while (b && k < stds.length) {
                    if ((stds[k] + tolerance > mws.get(j)) && (stds[k] - tolerance < mws.get(j))) {
                        occurrences.add(true);
                        b = false;
                    } else {
                        k++;
                    }
                }
                if (b) {
                    occurrences.add(false);
                }
            }
            Pair p = new Pair(indname, occurrences);
            ArrayList<Pair<String, ArrayList<Boolean>>> res = hashtable.get(popname);
            if (res == null) {
                ArrayList<Pair<String, ArrayList<Boolean>>> temp = new ArrayList<Pair<String, ArrayList<Boolean>>>();
                temp.add(p);
                hashtable.put(popname, temp);
            } else {
                res.add(p);
            }
        }

    }

    private void getMWs(Lane[] selectedlanes, double tolerance) {

        for (int i = 0; i < selectedlanes.length; i++) {
            double stds[] = selectedlanes[i].getStdpositionbands();
            for (int j = 0; j < stds.length; j++) {
                mws = insertSortMWs(mws, stds[j], tolerance);
            }
        }

    }

    private static ArrayList<Double> insertSortMWs(ArrayList<Double> mws, double mw, double tolerance) {
        ArrayList<Double> res = new ArrayList<Double>();
        if (mws.size() == 0) {
            res.add(mw);
            return res;
        }

        int i = 0;
        boolean b = true;

        while (b && (i < mws.size())) {
            if (mws.get(i) + tolerance < mw) {
                b = false;
                res.add(mw);
            } else {
                if (mws.get(i) - tolerance < mw) {
                    b = false;
                } else {
                    res.add(mws.get(i));
                    i++;
                }
            }
        }
        if (b == true) {
            res.add(mw);
        } else {

            for (int j = i; j < mws.size(); j++) {
                res.add(mws.get(j));
            }
        }
        return res;

    }

}

