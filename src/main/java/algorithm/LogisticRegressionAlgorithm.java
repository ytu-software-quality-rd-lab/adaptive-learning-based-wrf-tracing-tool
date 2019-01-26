package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.controller.MainController;
import main.java.util.FileUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.Collection;

public class LogisticRegressionAlgorithm extends BaseAlgorithm{

    public SparkBase sparkBase;
    public String lrFamily;
    public FileUtil fileUtil;

    public LogisticRegressionAlgorithm(SparkBase sparkBase){
        this.sparkBase  = sparkBase;
        this.fileUtil   = new FileUtil();
    }

    public void applyLogisticRegression(String svFilePath, MainController mainController, Boolean isMultiClass, String fileName, Integer numOfFeatures){
        ArrayList<Double> accuracyList  = new ArrayList<>();
        ArrayList<Double> precisionList = new ArrayList<>();
        ArrayList<Double> recallList    = new ArrayList<>();

        if(isMultiClass){
            lrFamily = "multinomial";
        }else {
            lrFamily = "binomial";
        }

        for(int i=0; i<mainController.getIterationCountValue(); i++){
            Dataset<Row> trainingData = null;
            Dataset<Row> testData     = null;
            ArrayList<ArrayList<Dataset<Row>>> datasets = new ArrayList<>();

            Double accuracySumKFold  = new Double(0);
            Double precisionSumKFold = new Double(0);
            Double recallSumKFold    = new Double(0);

            int counter;
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

                LogisticRegression lr = new LogisticRegression()
                        .setMaxIter(10)
                        .setRegParam(0.3)
                        .setElasticNetParam(0.8)
                        .setFamily(lrFamily);

                LogisticRegressionModel lrModel = lr.fit(trainingData);
                Dataset<Row> predictions = lrModel.transform(testData);

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

    public void setLrFamily(String lrFamily) {
        this.lrFamily = lrFamily;
    }
}
