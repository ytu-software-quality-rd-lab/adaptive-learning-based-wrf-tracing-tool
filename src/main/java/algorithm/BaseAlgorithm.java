package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.bean.Line;
import main.java.controller.MainController;
import main.java.util.FileUtil;
import main.java.util.MathUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

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

    static void setResults(MainController mainController, ArrayList<Double> accuracyList, ArrayList<Double> precisionList, ArrayList<Double> recallList) {
        MathUtil mathUtil = new MathUtil();

        mainController.setAccuracy(accuracyList.contains(Double.NaN) ? 1 : accuracyList.stream().mapToDouble(val -> val).average().orElse(0.0));
        mainController.setPrecision(precisionList.contains(Double.NaN) ? 1 : precisionList.stream().mapToDouble(val -> val).average().orElse(0.0));
        mainController.setRecall(recallList.contains(Double.NaN) ? 1 : recallList.stream().mapToDouble(val -> val).average().orElse(0.0));

        mainController.setSdAccuracy(accuracyList.contains(Double.NaN) ? 0 : (Double.isNaN(mathUtil.getStandardDeviation(accuracyList, mainController.getAccuracy())) ? 0.0 :
                mathUtil.getStandardDeviation(accuracyList, mainController.getAccuracy())));
        mainController.setSdPrecision(precisionList.contains(Double.NaN) ? 0 : (Double.isNaN(mathUtil.getStandardDeviation(precisionList, mainController.getPrecision())) ? 0.0 :
                mathUtil.getStandardDeviation(precisionList, mainController.getPrecision())));
        mainController.setSdRecall(recallList.contains(Double.NaN) ? 0 : (Double.isNaN(mathUtil.getStandardDeviation(recallList, mainController.getRecall())) ? 0.0 :
                mathUtil.getStandardDeviation(recallList, mainController.getRecall())));
    }

    public ArrayList<ArrayList<Line>> splitDateset(ArrayList<Line> dataset, Integer trainingRate, Integer testRate){
        ArrayList<ArrayList<Line>> splittedDataset  = new ArrayList<>();
        ArrayList<Line> trainingSet                 = new ArrayList<>();
        ArrayList<Line> testSet                     = new ArrayList<>();
        ArrayList<Integer> choosenLines             = new ArrayList<>();
        Random random                               = new Random();
        Integer trainingDatasetSize                 = ((Double) (dataset.size() * (trainingRate.doubleValue() / 100))).intValue();

        for(int i=0; i<trainingDatasetSize; i++){
            Integer linePointer = random.nextInt(dataset.size());
            while (choosenLines.contains(linePointer)){
                linePointer = random.nextInt(dataset.size());
            }

            trainingSet.add(dataset.get(linePointer));
            choosenLines.add(linePointer);
        }

        for(int i=0; i<dataset.size(); i++){
            if(!choosenLines.contains(i)){
                testSet.add(dataset.get(i));
            }
        }

        splittedDataset.add(trainingSet);
        splittedDataset.add(testSet);
        return splittedDataset;
    }

    public Integer getAttributeCount(ArrayList<Line> dataSet){
        Integer attributeCount = new Integer(0);
        for(Line line : dataSet){
            for(Integer attributePointer : line.getWordList()){
                if(attributeCount < attributePointer){
                    attributeCount = attributePointer;
                }
            }
        }

        return attributeCount;
    }

   /* public ArrayList<Dataset<Row>> splitAccordingTo10FoldCrossValidation(Dataset<Row> data, SparkSession sparkSession){
        ArrayList<Dataset<Row>> resultList = new ArrayList<>();
        HashMap<Integer, ArrayList<Row>> folds = new HashMap<>();
        for(int i=0; i<10; i++){
            folds.put(i, new ArrayList<Row>());
        }

        List<Row> allData = data.collectAsList();
        int foldSize = allData.size()/10;
        int randomLineCount;
        Random random = new Random();

        System.out.println("Splitting data into folds ...");
        for(int i=0; i<9; i++){
            System.out.println("Creating the " + (i+1) + ". fold ...");
            ArrayList<Row> choosenLines = new ArrayList<>();
            ArrayList<Integer> linePointers = new ArrayList<>();
            for(int j=0; j<foldSize; j++){
                randomLineCount = random.nextInt(allData.size());

                if(!linePointers.contains(randomLineCount)){
                    folds.get(i).add(allData.get(randomLineCount));
                    linePointers.add(randomLineCount);
                    choosenLines.add(allData.get(randomLineCount));
                }else {
                    j--;
                }
            }

            allData.remove(choosenLines);

            Dataset<Row> fold= sparkSession.createDataFrame(choosenLines, Row.class);
            resultList.add(fold);
        }

        System.out.println("Creating the 10. fold ...");
        Dataset<Row> fold= sparkSession.createDataFrame(allData, Row.class);
        resultList.add(fold);

        return resultList;
    }*/

    public ArrayList<ArrayList<Dataset<Row>>> splitAccordingTo10FoldCrossValidation(String filePath, Integer iterationCount, String fileName, SparkBase sparkBase, Integer numOfFeatures){
        ArrayList<ArrayList<Dataset<Row>>> dataSets = new ArrayList<>();
        for(int i=0; i<10; i++){
            dataSets.add(new ArrayList<Dataset<Row>>());
        }

        try{
            FileUtil fileUtil = new FileUtil();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            ArrayList<String> wholeFile = new ArrayList<>();
            ArrayList<String> testfiles = new ArrayList<>();
            ArrayList<String> trainingFiles = new ArrayList<>();

            HashMap<Integer, ArrayList<String>> folds = new HashMap<>();
            for(int i=0; i<10; i++){
                folds.put(i, new ArrayList<String>());
            }

            while ((line = bufferedReader.readLine()) != null){
                wholeFile.add(line);
            }

            ArrayList<String> copyWholeFile = new ArrayList<>();
            copyWholeFile.addAll(wholeFile);

            Random random = new Random();
            int randomLineCount;
            int foldSize = wholeFile.size()/10;

            for(int i=0; i<9; i++){
                ArrayList<String> choosenLines = new ArrayList<>();
                ArrayList<Integer> linePointers = new ArrayList<>();
                for(int j=0; j<foldSize; j++){
                    randomLineCount = random.nextInt(wholeFile.size());

                    if(!linePointers.contains(randomLineCount)){
                        folds.get(i).add(wholeFile.get(randomLineCount));
                        linePointers.add(randomLineCount);
                        choosenLines.add(wholeFile.get(randomLineCount));
                    }else {
                        j--;
                    }
                }

                fileUtil.writeToFile(i+1, iterationCount, fileName, choosenLines, "test", numOfFeatures);

                ArrayList<String> tempTraining = new ArrayList<>();
                tempTraining.addAll(copyWholeFile);

                int counter = 0;
                for(int k=0; k<linePointers.size(); k++){
                    tempTraining.remove(k-counter);
                    wholeFile.remove(k-counter);
                    counter++;
                }

                fileUtil.writeToFile(i+1, iterationCount, fileName, tempTraining, "training", numOfFeatures);

                testfiles.add(fileUtil.getFullFilePath(fileName, iterationCount, i+1, "test"));
                trainingFiles.add(fileUtil.getFullFilePath(fileName, iterationCount, i+1, "training"));
            }

            fileUtil.writeToFile(10, iterationCount, fileName, wholeFile, "test", numOfFeatures);

            ArrayList<String> tempTraining = new ArrayList<>();
            tempTraining.addAll(copyWholeFile);
            tempTraining.removeAll(wholeFile);
            fileUtil.writeToFile(10, iterationCount, fileName, tempTraining, "training", numOfFeatures);

            testfiles.add(fileUtil.getFullFilePath(fileName, iterationCount, 10, "test"));
            trainingFiles.add(fileUtil.getFullFilePath(fileName, iterationCount, 10, "training"));

            for(int i=0; i<dataSets.size(); i++){
                dataSets.get(i).add(fileUtil.getDataSet(sparkBase, testfiles.get(i)));
                dataSets.get(i).add(fileUtil.getDataSet(sparkBase, trainingFiles.get(i)));
            }

            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return dataSets;
    }

    public ArrayList<ArrayList<ArrayList<Line>>> splitAccordingTo10FoldCrossValidation(String filePath, Integer iterationCount, String fileName, Integer numOfFeatures){
        ArrayList<ArrayList<ArrayList<Line>>> dataSets = new ArrayList<>();
        for(int i=0; i<10; i++){
            dataSets.add(new ArrayList<ArrayList<Line>>());
        }

        try{
            FileUtil fileUtil = new FileUtil();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            ArrayList<String> wholeFile = new ArrayList<>();
            ArrayList<String> testfiles = new ArrayList<>();
            ArrayList<String> trainingFiles = new ArrayList<>();

            HashMap<Integer, ArrayList<String>> folds = new HashMap<>();
            for(int i=0; i<10; i++){
                folds.put(i, new ArrayList<String>());
            }

            while ((line = bufferedReader.readLine()) != null){
                wholeFile.add(line);
            }

            ArrayList<String> copyWholeFile = new ArrayList<>();
            copyWholeFile.addAll(wholeFile);

            Random random = new Random();
            int randomLineCount;
            int foldSize = wholeFile.size()/10;

            for(int i=0; i<9; i++){
                ArrayList<String> choosenLines = new ArrayList<>();
                ArrayList<Integer> linePointers = new ArrayList<>();
                for(int j=0; j<foldSize; j++){
                    randomLineCount = random.nextInt(wholeFile.size());

                    if(!linePointers.contains(randomLineCount)){
                        folds.get(i).add(wholeFile.get(randomLineCount));
                        linePointers.add(randomLineCount);
                        choosenLines.add(wholeFile.get(randomLineCount));
                    }else {
                        j--;
                    }
                }

                fileUtil.writeToFile(i+1, iterationCount, fileName, choosenLines, "test", numOfFeatures);

                ArrayList<String> tempTraining = new ArrayList<>();
                tempTraining.addAll(copyWholeFile);

                int counter = 0;
                for(int k=0; k<linePointers.size(); k++){
                    tempTraining.remove(k-counter);
                    wholeFile.remove(k-counter);
                    counter++;
                }

                fileUtil.writeToFile(i+1, iterationCount, fileName, tempTraining, "training", numOfFeatures);

                testfiles.add(fileUtil.getFullFilePath(fileName, iterationCount, i+1, "test"));
                trainingFiles.add(fileUtil.getFullFilePath(fileName, iterationCount, i+1, "training"));
            }

            fileUtil.writeToFile(10, iterationCount, fileName, wholeFile, "test", numOfFeatures);

            ArrayList<String> tempTraining = new ArrayList<>();
            tempTraining.addAll(copyWholeFile);
            tempTraining.removeAll(wholeFile);
            fileUtil.writeToFile(10, iterationCount, fileName, tempTraining, "training", numOfFeatures);

            testfiles.add(fileUtil.getFullFilePath(fileName, iterationCount, 10, "test"));
            trainingFiles.add(fileUtil.getFullFilePath(fileName, iterationCount, 10, "training"));

            for(int i=0; i<dataSets.size(); i++){
                dataSets.get(i).add(readSparseVector(testfiles.get(i)));
                dataSets.get(i).add(readSparseVector(trainingFiles.get(i)));
            }

            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return dataSets;
    }

    public ArrayList<ArrayList<Line>> splitDateSet(ArrayList<Line> data, Integer splitPoint){
        ArrayList<ArrayList<Line>> splittedDataSet = new ArrayList<>();

        int trainingSetSize = ((Double)(data.size() * (splitPoint.doubleValue() / 100))).intValue();

        Random random = new Random();
        ArrayList<Line> training = new ArrayList<>();
        ArrayList<Line> test     = new ArrayList<>();

        ArrayList<Integer> choosenPoints = new ArrayList<>();
        for(int i=0; i<trainingSetSize; i++){
            int randomPoint = random.nextInt(data.size());
            training.add(data.get(randomPoint));
            choosenPoints.add(randomPoint);
        }

        for(int i=0; i<data.size(); i++){
            if(!choosenPoints.contains(i)){
                test.add(data.get(i));
            }
        }

        splittedDataSet.add(training);
        splittedDataSet.add(test);

        return splittedDataSet;
    }

    public ArrayList<Integer> getDifferentClassLabels(ArrayList<Line> data){
        ArrayList<Integer> differentClassLabels = new ArrayList<>();

        for(Line line : data){
            if(!differentClassLabels.contains(line.getClassLabel())){
                differentClassLabels.add(line.getClassLabel());
            }
        }

        return differentClassLabels;
    }

}
