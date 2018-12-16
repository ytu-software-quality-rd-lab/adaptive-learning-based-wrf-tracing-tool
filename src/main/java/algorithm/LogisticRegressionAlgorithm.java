package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.controller.MainController;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.Collection;

public class LogisticRegressionAlgorithm {

    public SparkBase sparkBase;
    public String lrFamily;

    public LogisticRegressionAlgorithm(SparkBase sparkBase){
        this.sparkBase  = sparkBase;
        lrFamily        = "binomial";
    }

    public void applyLogisticRegression(String svFilePath, MainController mainController){
        Double accuracySum = new Double(0);
        Double precisionSum = new Double(0);
        Double recallSum = new Double(0);

        Dataset<Row> dataFrame =
                sparkBase.getSpark().read().format("libsvm").load(svFilePath);

        for(int i=0; i<mainController.getIterationCountValue(); i++){
            Dataset<Row>[] splits = dataFrame.randomSplit(new double[]
                    {mainController.getTrainingDataRate(), mainController.getTestDataRate()}, 1234L);
            Dataset<Row> training = splits[0];
            Dataset<Row> test = splits[1];
            LogisticRegression lr = new LogisticRegression()
                    .setMaxIter(10)
                    .setRegParam(0.3)
                    .setElasticNetParam(0.8)
                    .setFamily(lrFamily);

            LogisticRegressionModel lrModel = lr.fit(training);
            Dataset<Row> predictions = lrModel.transform(test);

            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setLabelCol("label")
                    .setPredictionCol("prediction")
                    .setMetricName("weightedPrecision");

            precisionSum += (evaluator.evaluate(predictions));

            evaluator.setMetricName("weightedRecall");
            recallSum += (evaluator.evaluate(predictions));

            evaluator.setMetricName("accuracy");
            accuracySum += (evaluator.evaluate(predictions));

            System.out.println("Iteration count: " + (i+1));
        }

        System.out.println("Done!\n");
        mainController.setAccuracy(accuracySum / mainController.getIterationCountValue());
        mainController.setPrecision(precisionSum / mainController.getIterationCountValue());
        mainController.setRecall(recallSum / mainController.getIterationCountValue());
    }

    public void setLrFamily(String lrFamily) {
        this.lrFamily = lrFamily;
    }
}
