package main.java.algorithm;

import main.java.base.Line;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class BaseAlgorithm {

    public ArrayList<Line> readSparseVector(String sparseVectorPath){
        ArrayList<Line> sparseVector    = new ArrayList<>();
        FileReader fileReader           = null;
        BufferedReader bufferedReader   = null;

        try{
            fileReader          = new FileReader(sparseVectorPath);
            bufferedReader      = new BufferedReader(fileReader);

            String currentLine  = null;
            while ((currentLine = bufferedReader.readLine()) != null){
                String words[]              = currentLine.split(" ");
                ArrayList<Integer> wordList = new ArrayList<>();
                Integer classLabel          = Integer.parseInt(words[0]);

                for(int i=1; i<words.length; i++){
                    wordList.add(Integer.parseInt(words[i].split(":")[0]));
                }

                sparseVector.add(new Line(wordList, classLabel));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return sparseVector;
    }

}
