package main.java.util;

import main.java.AdaptiveWRFTracingTool.bean.Keyword;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClassLabelProducerUtil {

    public static final String keywordFilePath = System.getProperty("user.dir") + "/data/keywords.xml";
    public ArrayList<main.java.AdaptiveWRFTracingTool.bean.Keyword> keywordList;

    public ClassLabelProducerUtil(){
        keywordList = new ArrayList<>();
        checkIfFoldersExist();
        readKeywordList();
    }

    public void checkIfFoldersExist(){
        String root = System.getProperty("user.dir") + "/data";
        File file = new File(root + "/bl");
        if(!file.exists()){
            file.mkdir();
        }

        file = new File(root + "/mcl");
        if(!file.exists()){
            file.mkdir();
        }
    }

    public String produceBinaryLabels(String logFilePath, String featureFilePath, String filteredFilePath, String logFileName) {
        String labeledFilePath = System.getProperty("user.dir") + "/data/bl/" +
                logFileName + "_bl_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        try {
            FileReader reader = new FileReader(logFilePath);
            BufferedReader bufferedReader = new BufferedReader(reader);

            FileReader reader2 = new FileReader(featureFilePath);
            BufferedReader bufferedReader2 = new BufferedReader(reader2);

            FileOutputStream outputStream = new FileOutputStream(labeledFilePath);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            FileOutputStream outputStream2 = new FileOutputStream(filteredFilePath);
            OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(outputStream2);
            BufferedWriter bufferedWriter2 = new BufferedWriter(outputStreamWriter2);

            String line;
            String line2;

            boolean found;
            int i;
            int label;

            while ((line = bufferedReader.readLine()) != null) {
                found = false;
                i = 0;
                label = 0;

                while (!found && i < keywordList.size()) {
                    if (line.contains(keywordList.get(i).getName())) {
                        found = true;
                    } else {
                        i++;
                    }

                }

                line2 = bufferedReader2.readLine();

                if (found) {
                    label = 1;
                    bufferedWriter2.write(line);
                    bufferedWriter2.newLine();
                }

                bufferedWriter.write(label + line2);
                bufferedWriter.newLine();
            }

            bufferedReader.close();
            bufferedReader2.close();
            bufferedWriter.close();
            bufferedWriter2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return labeledFilePath;
    }

    public void readKeywordList() {

        try {
            File fXmlFile = new File(keywordFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("element");

            String name;
            String parentname;
            String type;
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                //System.out.println("\nCurrent Element :" + nNode.getNodeName() +" | " +  nNode.getNodeType());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    //System.out.println("Staff id : " + eElement.getAttribute("id"));
                    name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    parentname = eElement.getElementsByTagName("parentname").item(0).getTextContent();
                    type = eElement.getElementsByTagName("type").item(0).getTextContent();
                    keywordList.add(new Keyword(name, parentname, type));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String produceMulticlassLabels(String logFilePath, String featureFilePath, String filteredFilePath, String logFileName) {
        String outputFilePath = System.getProperty("user.dir") + "/data/mcl/" +
                logFileName + "_mcl_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        try {
            FileReader reader = new FileReader(logFilePath);
            BufferedReader bufferedReader = new BufferedReader(reader);

            FileReader reader2 = new FileReader(featureFilePath);
            BufferedReader bufferedReader2 = new BufferedReader(reader2);

            FileOutputStream outputStream = new FileOutputStream(outputFilePath);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            FileOutputStream outputStream2 = new FileOutputStream(filteredFilePath);
            OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(outputStream2);
            BufferedWriter bufferedWriter2 = new BufferedWriter(outputStreamWriter2);

            String line;
            String line2;
            String words[];
            String element1;
            String element2;
            boolean found, found2;
            int j, k = 0;
            int label = 0;

            while ((line = bufferedReader.readLine()) != null) {
                found = false;
                j = 0;
                label = 0;
                while (!found && j < keywordList.size()) {
                    String keyword = keywordList.get(j).getName();

                    if (line.contains(keyword)) {
                        found = true;
                        words = line.split(" ");
                        element1 = null;
                        element2 = null;
                        int index1 = 0, index2 = 0;
                        for (int i = 0; i < words.length; i++) {
                            if (words[i].contains(":")) {
                                words[i] = words[i].replace(":", "");
                            }
                            k = 0;
                            found2 = false;
                            while (!found2 && k < keywordList.size()) {
                                if (words[i].equals(keywordList.get(k).getName())) {
                                    found2 = true;
                                    if (element1 == null) {
                                        element1 = words[i];
                                        index1 = k;
                                    } else {
                                        element2 = words[i];
                                        index2 = k;
                                    }
                                }
                                k++;
                            }
                        }

                        String TextRawElement = new String();

                        if (element1 != null && element2 != null) {
                            if (keywordList.get(index2).getParentname().equals(element1)) {
                                if (keywordList.get(index1).getType().equals("activity") && keywordList.get(index2).getType().equals("activity")) {
                                    TextRawElement = "Communication " + element1 + " " + element2;
                                    label = 1;
                                } else if (keywordList.get(index1).getType().equals("entity") && keywordList.get(index2).getType().equals("entity")) {
                                    TextRawElement = "Derivation " + element1 + " " + element2;
                                    label = 2;
                                } else if (keywordList.get(index1).getType().equals("entity") && keywordList.get(index2).getType().equals("activity")) {
                                    TextRawElement = "Generation " + element1 + " " + element2;
                                    label = 4;
                                } else {
                                    TextRawElement = "Usage " + element1 + " " + element2;
                                    label = 3;
                                }

                                bufferedWriter2.write(TextRawElement);
                                bufferedWriter2.newLine();
                                //}
                            }else if (keywordList.get(index1).getParentname().equals(element2)) {
                                if (keywordList.get(index1).getType().equals("activity") && keywordList.get(index2).getType().equals("activity")) {
                                    TextRawElement = "Communication " + element1 + " " + element2;
                                    label = 1;
                                } else if (keywordList.get(index1).getType().equals("entity") && keywordList.get(index2).getType().equals("entity")) {
                                    TextRawElement = "Derivation " + element1 + " " + element2;
                                    label = 2;
                                } else if (keywordList.get(index1).getType().equals("entity") && keywordList.get(index2).getType().equals("activity")) {
                                    TextRawElement = "Usage " + element1 + " " + element2;
                                    label = 3;
                                } else {
                                    TextRawElement = "Generation " + element1 + " " + element2;
                                    label = 4;
                                }

                                bufferedWriter2.write(TextRawElement);
                                bufferedWriter2.newLine();

                            } else {
                                label = getLabel(bufferedWriter2, element2, label, index2);
                            }
                        } else if (element1 != null) {
                            label = getLabel(bufferedWriter2, element1, label, index1);
                        }
                    } else {
                        j++;
                    }
                }
                line2 = bufferedReader2.readLine();
                bufferedWriter.write(label + line2);
                bufferedWriter.newLine();
            }

            bufferedReader.close();
            bufferedReader2.close();
            bufferedWriter.close();
            bufferedWriter2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputFilePath;
    }

    private int getLabel(BufferedWriter bufferedWriter2, String element2, int label, int index2) throws IOException {
        String TextRawElement;
        if (!keywordList.get(index2).getParentname().equals("null")) {
            int i = 0;
            boolean flag = false;
            int parentIndex = -1;
            while (i < keywordList.size() && !flag) {
                if (keywordList.get(i).getName().equals(keywordList.get(index2).getParentname())) {
                    flag = true;
                    parentIndex = i;

                }
                i++;
            }
            if (flag) {
                if (keywordList.get(index2).getType().equals("activity") && keywordList.get(parentIndex).getType().equals("activity")) {
                    TextRawElement = "Communication " + keywordList.get(index2).getParentname() + " " + element2;
                    label = 1;
                } else if (keywordList.get(index2).getType().equals("entity") && keywordList.get(parentIndex).getType().equals("entity")) {
                    TextRawElement = "Derivation " + keywordList.get(index2).getParentname() + " " + element2;
                    label = 2;
                } else if (keywordList.get(index2).getType().equals("entity") && keywordList.get(parentIndex).getType().equals("activity")) {
                    TextRawElement = "Usage " + keywordList.get(index2).getParentname() + " " + element2;
                    label = 3;
                } else {
                    TextRawElement = "Generation " + keywordList.get(index2).getParentname() + " " + element2;
                    label = 4;
                }

                bufferedWriter2.write(TextRawElement);
                bufferedWriter2.newLine();

            }
        }
        return label;
    }

}
