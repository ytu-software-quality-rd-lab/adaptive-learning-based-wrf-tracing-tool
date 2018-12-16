package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.controller.MainController;
import org.apache.spark.SparkConf;
import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class NaiveBayesAlgorithm {

    public SparkBase sparkBase;

    public NaiveBayesAlgorithm(SparkBase sparkBase){
        this.sparkBase = sparkBase;
    }

    public void applyNaiveBayes(String svFilePath, MainController mainController){
        Double accuracySum = new Double(0);
        Double precisionSum = new Double(0);
        Double recallSum = new Double(0);

        Dataset<Row> dataFrame =
                sparkBase.getSpark().read().format("libsvm").load(svFilePath);

        for(int i=0; i< mainController.getIterationCountValue(); i++){
            Dataset<Row>[] splits = dataFrame.randomSplit(new double[]
                    {mainController.getTrainingDataRate(), mainController.getTestDataRate()});
            Dataset<Row> train = splits[0];
            Dataset<Row> test = splits[1];

            NaiveBayes nb = new NaiveBayes();
            NaiveBayesModel model = nb.fit(train);

            Dataset<Row> predictions = model.transform(test);

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


}
