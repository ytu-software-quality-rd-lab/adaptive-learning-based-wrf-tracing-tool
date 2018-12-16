package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.controller.MainController;

import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class MultilayerPerceptronClassifierAlgorithm {

    public SparkBase sparkBase;

    public MultilayerPerceptronClassifierAlgorithm(SparkBase sparkBase){
        this.sparkBase = sparkBase;
    }

    public void applyMultilayerPerceptronClassifier(String svFilePath, MainController mainController, Integer featureCount, Integer classCount){
        Double accuracySum = new Double(0);
        Double precisionSum = new Double(0);
        Double recallSum = new Double(0);
        int[] layers = new int[] {featureCount, 5, 4, classCount};

        Dataset<Row> dataFrame =
                sparkBase.getSpark().read().format("libsvm").load(svFilePath);

        Dataset<Row>[] splits = dataFrame.randomSplit(new double[]
                {mainController.getTrainingDataRate(), mainController.getTestDataRate()}, 1234L);
        Dataset<Row> train = splits[0];
        Dataset<Row> test = splits[1];

        MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setBlockSize(128)
                .setSeed(1234L)
                .setMaxIter(100);

        MultilayerPerceptronClassificationModel model = trainer.fit(train);

        Dataset<Row> result = model.transform(test);
        Dataset<Row> predictions = result.select("prediction", "label");
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setMetricName("weightedPrecision");

        precisionSum += (evaluator.evaluate(predictions));

        evaluator.setMetricName("weightedRecall");
        recallSum += (evaluator.evaluate(predictions));

        evaluator.setMetricName("accuracy");
        accuracySum += (evaluator.evaluate(predictions));

        System.out.println("Iteration count: " + (1));

        System.out.println("Done!\n");
        mainController.setAccuracy(accuracySum / mainController.getIterationCountValue());
        mainController.setPrecision(precisionSum / mainController.getIterationCountValue());
        mainController.setRecall(recallSum / mainController.getIterationCountValue());
    }

}
