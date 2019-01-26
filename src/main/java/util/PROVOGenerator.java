package main.java.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class PROVOGenerator {

    private ArrayList<String> activityNodes;
    private ArrayList<String> agentNodes;
    private ArrayList<String> entityNodes;

    public PROVOGenerator(){
        activityNodes = new ArrayList<>();
        agentNodes    = new ArrayList<>();
        entityNodes   = new ArrayList<>();
    }

    public void producePROVOFileFromFilteredWRFLogFile(String filePath){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            String newDocument     = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<prov:document xmlns:prov=\"http://www.w3.org/ns/prov#\" xmlns:ns2=\"http://openprovenance.org/prov/extension#\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:pronaliz=\"http://www.pronaliz.yildiz.edu.tr\">\n";

            while ((line = reader.readLine()) != null){
                String words[] = line.split(" ");
                if(words[0].equals("Communication")){
                    newDocument += createWasInformedBy(words[1], words[2]);
                    activityNodes.add(words[1]);
                    activityNodes.add(words[2]);
                }else if(words[0].equals("Usage")){
                    newDocument += createUsed(words[1], words[2]);
                    entityNodes.add(words[2]);
                    activityNodes.add(words[1]);
                }else if(words[0].equals("Derivation")){
                    newDocument += createWasDerivedFrom(words[1], words[2]);
                    entityNodes.add(words[1]);
                    entityNodes.add(words[2]);
                }else if(words[0].equals("Generation")){
                    newDocument += createWasGeneratedBy(words[1], words[2]);
                    entityNodes.add(words[1]);
                    activityNodes.add(words[2]);
                }
            }

            newDocument += "</prov:document>";

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + "_prov_version.xml"));
            writer.write(newDocument);

            writer.close();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String createWasInformedBy(String informed, String informant){

        return (!activityNodes.contains(informed) ? "<prov:activity prov:id=\"" + informed + "\"/>\n" : "") +
                (!activityNodes.contains(informant) ? "<prov:activity prov:id=\"" + informant + "\"/>\n" : "") +
               "<prov:wasInformedBy>\n" +
                   "\t<prov:informed prov:ref=\"" + informed +  "\"/>\n" +
                   "\t<prov:informant prov:ref=\"" + informant + "\"/>\n" +
               "</prov:wasInformedBy>\n";

    }

    public String createWasDerivedFrom(String generatedEntity, String usedEntity){

        return (!entityNodes.contains(generatedEntity) ? "<prov:entity prov:id=\"" + generatedEntity + "\"/>\n" : "") +
                (!entityNodes.contains(usedEntity) ? "<prov:entity prov:id=\"" + usedEntity + "\"/>\n" : "") +
                "<prov:wasDerivedFrom>\n" +
                "\t<prov:generatedEntity prov:ref=\"" + generatedEntity +  "\"/>\n" +
                "\t<prov:usedEntity prov:ref=\"" + usedEntity + "\"/>\n" +
                "</prov:wasDerivedFrom>\n";
    }

    public String createWasGeneratedBy(String entity, String activity){
        return (!entityNodes.contains(entity) ? "<prov:entity prov:id=\"" + entity + "\"/>\n" : "") +
                (!entityNodes.contains(activity) ? "<prov:activity prov:id=\"" + activity + "\"/>\n" : "") +
                "<prov:wasGeneratedBy>\n" +
                "\t<prov:entity prov:ref=\"" + entity +  "\"/>\n" +
                "\t<prov:activity prov:ref=\"" + activity + "\"/>\n" +
                "</prov:wasGeneratedBy>\n";
    }

    public String createUsed(String activity, String entity){

        return (!entityNodes.contains(entity) ? "<prov:entity prov:id=\"" + entity + "\"/>\n" : "") +
                (!entityNodes.contains(activity) ? "<prov:activity prov:id=\"" + activity + "\"/>\n" : "") +
                "<prov:used>\n" +
                "\t<prov:activity prov:ref=\"" + activity + "\"/>\n" +
                "\t<prov:entity prov:ref=\"" + entity +  "\"/>\n" +
                "</prov:used>\n";
    }

}
