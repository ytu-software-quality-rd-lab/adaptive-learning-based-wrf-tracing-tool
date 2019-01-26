package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.controller.MainController;

import main.java.util.FileUtil;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;

public class MultilayerPerceptronClassifierAlgorithm extends BaseAlgorithm {

    public SparkBase sparkBase;
    public FileUtil fileUtil;

    public MultilayerPerceptronClassifierAlgorithm(SparkBase sparkBase){
        this.sparkBase = sparkBase;
        this.fileUtil  = new FileUtil();
    }

    public void applyMultilayerPerceptronClassifier(String svFilePath, MainController mainController, Integer featureCount, Integer classCount, String fileName, Integer numOfFeatures){
        ArrayList<Double> accuracyList  = new ArrayList<>();
        ArrayList<Double> precisionList = new ArrayList<>();
        ArrayList<Double> recallList    = new ArrayList<>();
        int[] layers = new int[] {featureCount, 5, 4, classCount};

        Dataset<Row> trainingData = null;
        Dataset<Row> testData     = null;
        ArrayList<ArrayList<Dataset<Row>>> datasets = new ArrayList<>();

        int counter;
        for(int i=0; i<mainController.getIterationCountValue(); i++){
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

                MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                        .setLayers(layers)
                        .setBlockSize(128)
                        .setSeed(1234L)
                        .setMaxIter(20)
                        .setTol(0.0001);

                MultilayerPerceptronClassificationModel model = trainer.fit(trainingData);

                Dataset<Row> result = model.transform(testData);
                Dataset<Row> predictions = result.select("prediction", "label");
                MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
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
