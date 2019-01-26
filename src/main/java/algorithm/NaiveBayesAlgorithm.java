package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.controller.MainController;
import main.java.util.FileUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;

public class NaiveBayesAlgorithm extends BaseAlgorithm {

    public SparkBase sparkBase;
    public FileUtil fileUtil;

    public NaiveBayesAlgorithm(SparkBase sparkBase){
        this.sparkBase = sparkBase;
        this.fileUtil  = new FileUtil();
    }

    public void applyNaiveBayes(String svFilePath, MainController mainController, String fileName, Integer numOfFeatures){
        ArrayList<Double> accuracyList  = new ArrayList<>();
        ArrayList<Double> precisionList = new ArrayList<>();
        ArrayList<Double> recallList    = new ArrayList<>();

        Dataset<Row> trainingData = null;
        Dataset<Row> testData     = null;
        ArrayList<ArrayList<Dataset<Row>>> datasets = new ArrayList<>();

        int counter;
        for(int i=0; i< mainController.getIterationCountValue(); i++){
            Double accuracySumKFold  = new Double(0);
            Double precisionSumKFold = new Double(0);
            Double recallSumKFold    = new Double(0);

            if(mainController.getTenFold().isSelected()){
                counter = 10;
                datasets = splitAccordingTo10FoldCrossValidation(svFilePath, i, fileName, sparkBase, numOfFeatures);
            }else {
                counter = 1;
                Dataset<Row>[] splits = fileUtil.getDataSet(sparkBase, svFilePath).randomSplit(new double[]{mainController.getTrainingDataRate(), mainController.getTestDataRate()});
                trainingData = splits[0];
                testData = splits[1];
            }

            for (int k=0; k<counter; k++){
                if(mainController.getTenFold().isSelected()){
                    testData = datasets.get(k).get(0);
                    trainingData = datasets.get(k).get(1);
                }

                NaiveBayes nb = new NaiveBayes();
                NaiveBayesModel model = nb.fit(trainingData);

                Dataset<Row> predictions = model.transform(testData);

                MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                        .setLabelCol("label")
                        .setPredictionCol("prediction")
                        .setMetricName("weightedPrecision");

                precisionSumKFold += (evaluator.evaluate(predictions));

                evaluator.setMetricName("weightedRecall");
                recallSumKFold += (evaluator.evaluate(predictions));

                evaluator.setMetricName("accuracy");
                accuracySumKFold += (evaluator.evaluate(predictions));
            }

            accuracySumKFold  /= counter;
            precisionSumKFold /= counter;
            recallSumKFold    /= counter;

            accuracyList.add(accuracySumKFold);
            precisionList.add(precisionSumKFold);
            recallList.add(recallSumKFold);
            System.out.println("Iteration count: " + (i+1));
        }

        setResults(mainController, accuracyList, precisionList, recallList);
    }


}
