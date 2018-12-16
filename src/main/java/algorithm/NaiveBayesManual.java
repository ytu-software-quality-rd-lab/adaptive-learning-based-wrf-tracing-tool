package main.java.algorithm;

import main.java.base.Line;
import main.java.controller.MainController;
import main.java.util.MathUtil;

import java.util.*;

public class NaiveBayesManual extends BaseAlgorithm{

    MathUtil mathUtil;

    public NaiveBayesManual(){
        mathUtil = new MathUtil();
    }

    // This is a naive bayes classifier implementation for continious attributes
    public void applyNaiveBayesAlgorithmForBinaryClassLabels(MainController controller, String filePath){
        Double accuracy  = new Double(0);
        Double precision = new Double(0);
        Double recall    = new Double(0);

        ArrayList<Line> dataSet = readSparseVector(filePath);
        HashMap<Integer, Double> classLabelRates = findClassLabelRates(dataSet);

        for(int i=0; i<controller.getIterationCountValue(); i++){
            Integer tp = new Integer(0);
            Integer fp = new Integer(0);
            Integer tn = new Integer(0);
            Integer fn = new Integer(0);

            ArrayList<ArrayList<Line>> splittedDataset          = splitDateset(dataSet, controller.getTrainingDataRate(), controller.getTestDataRate());
            ArrayList<Line> training                            = splittedDataset.get(0);
            ArrayList<Line> test                                = splittedDataset.get(1);
            ArrayList<ArrayList<Integer>> classConfusionMatrix  = new ArrayList<>();
            for(int k=0; k<classLabelRates.size(); k++){
                classConfusionMatrix.add(k, new ArrayList<Integer>());
                for(int m=0; m<classLabelRates.size(); m++){
                    classConfusionMatrix.get(k).add(new Integer(0));
                }
            }

            for(Line line : test){
                Double maxProbability       = new Double(0);
                Double currentProbability   = new Double(1);

                Set<Integer> classes    = classLabelRates.keySet();
                Integer predictedClass  = -1;
                for(Integer classValue : classes){
                    for(int l=0; l<line.getWordList().size(); l++){
                        currentProbability *= findAttributeProbability(training, line.getWordList().get(l), classValue);
                    }

                    currentProbability *= classLabelRates.get(classValue);
                    if(currentProbability >= maxProbability){
                        maxProbability = currentProbability;
                        predictedClass = classValue;
                    }
                }

                classConfusionMatrix.get(findIndex(line.getClassLabel(), classes)).set(findIndex(predictedClass, classes),
                        (classConfusionMatrix.get(findIndex(line.getClassLabel(), classes)).get(findIndex(predictedClass, classes)) + 1));
            }

            for(int k=0; k<classConfusionMatrix.size(); k++){
                tp = classConfusionMatrix.get(k).get(k);
                fn = sumAll(classConfusionMatrix.get(k)) - classConfusionMatrix.get(k).get(k);
                tn = test.size() - sumAll(classConfusionMatrix.get(k));
                fp = getColumnSum(classConfusionMatrix, k) - classConfusionMatrix.get(k).get(k);

                accuracy    += (((tp.doubleValue() + tn.doubleValue()) /
                        (tp.doubleValue() + fp.doubleValue() + fn.doubleValue() + tn.doubleValue()))
                        * (sumAll(classConfusionMatrix.get(k))) / test.size());
                precision   += ((tp.doubleValue()/((tp.doubleValue() + fp.doubleValue()) == 0 ? 1 : tp.doubleValue() + fp.doubleValue()))
                        * (sumAll(classConfusionMatrix.get(k))) / test.size());
                recall      += ((tp.doubleValue()/((tp.doubleValue() + fn.doubleValue()) == 0 ? 1 : (tp.doubleValue() + fn.doubleValue()))
                        * (sumAll(classConfusionMatrix.get(k))) / test.size()));
            }
        }

        accuracy  /= controller.getIterationCountValue();
        precision /= controller.getIterationCountValue();
        recall    /= controller.getIterationCountValue();

        controller.setAccuracy(accuracy);
        controller.setPrecision(precision);
        controller.setRecall(recall);
    }

    public Integer findIndex(Integer classValue, Set<Integer> classList){
        Integer index = new Integer(0);
        for(Integer temp : classList){
            if(classValue == temp){
                break;
            }

            index++;
        }

        return index;
    }

    public Integer getColumnSum(ArrayList<ArrayList<Integer>> matrix, Integer columnPointer){
        Integer result = new Integer(0);
        for(ArrayList<Integer> column :matrix){
            result += column.get(columnPointer);
        }

        return result;
    }

    public Integer sumAll(ArrayList<Integer> list){
        Integer result = new Integer(0);
        for(Integer temp : list){
            result += temp;
        }

        return result;
    }

    public Double findAttributeProbability(ArrayList<Line> training, Integer attributeValue, Integer classLabel){
        Integer totalCount   = new Integer(0);
        Integer similarCount = new Integer(0);

        for(Line line : training){
            if(line.getClassLabel() == classLabel){
                totalCount++;
                if(line.getWordList().contains(attributeValue)){
                    similarCount++;
                }
            }
        }

        return (similarCount.doubleValue() / totalCount.doubleValue());
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

    public HashMap<Integer, Double> findClassLabelRates(ArrayList<Line> dataset){
        HashMap<Integer, Double> classLabelRates = new HashMap<>();
        ArrayList<Integer> differentClassLabels  = findDifferentClassLabels(dataset);
        for(Integer classLabel : differentClassLabels){
            classLabelRates.put(classLabel, new Double(0));
        }

        for (Line line : dataset){
            classLabelRates.put(line.getClassLabel(), ((classLabelRates.get(line.getClassLabel()) + new Double(1)) / dataset.size()));
        }

        return classLabelRates;
    }

    public ArrayList<Integer> findDifferentClassLabels(ArrayList<Line> dataset){
        ArrayList<Integer> differentClassLabels = new ArrayList<>();

        for(Line line : dataset){
            if(!differentClassLabels.contains(line.getClassLabel())){
                differentClassLabels.add(line.getClassLabel());
            }
        }

        return differentClassLabels;
    }
}
