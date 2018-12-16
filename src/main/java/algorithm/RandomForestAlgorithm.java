package main.java.algorithm;

import main.java.base.SparkBase;
import main.java.controller.MainController;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class RandomForestAlgorithm {

    public SparkBase sparkBase;

    public RandomForestAlgorithm(SparkBase sparkBase){
        this.sparkBase = sparkBase;
    }

    public void applyRandomForest(String svFilePath, MainController mainController, Integer categoryCount){
        Double accuracySum = new Double(0);
        Double precisionSum = new Double(0);
        Double recallSum = new Double(0);

        Dataset<Row> dataFrame              = sparkBase
                .getSpark()
                .read()
                .format("libsvm")
                .load(svFilePath);
        StringIndexerModel labelIndexer     = new StringIndexer()
                .setInputCol("label")
                .setOutputCol("indexedLabel")
                .fit(dataFrame);
        VectorIndexerModel featureIndexer   = new VectorIndexer()
                .setInputCol("features")
                .setOutputCol("indexedFeatures")
                .setMaxCategories(categoryCount)
                .fit(dataFrame);

        for(int i=0; i< mainController.getIterationCountValue(); i++){
            Dataset<Row>[] splits = dataFrame.randomSplit(new double[]
                    {mainController.getTrainingDataRate(), mainController.getTestDataRate()}, 1234L);
            Dataset<Row> train = splits[0];
            Dataset<Row> test = splits[1];

            RandomForestClassifier rf = new RandomForestClassifier()
                    .setLabelCol("indexedLabel")
                    .setFeaturesCol("indexedFeatures");

            IndexToString labelConverter = new IndexToString()
                    .setInputCol("prediction")
                    .setOutputCol("predictedLabel")
                    .setLabels(labelIndexer.labels());

            Pipeline pipeline = new Pipeline()
                    .setStages(new PipelineStage[] {labelIndexer, featureIndexer, rf, labelConverter});

            PipelineModel model = pipeline.fit(train);

            Dataset<Row> predictions = model.transform(test);
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setLabelCol("indexedLabel")
                    .setPredictionCol("prediction")
                    .setMetricName("accuracy");
            accuracySum += evaluator.evaluate(predictions);

            evaluator.setMetricName("weightedRecall");
            recallSum += (evaluator.evaluate(predictions));

            evaluator.setMetricName("weightedPrecision");
            precisionSum += (evaluator.evaluate(predictions));

            System.out.println("Iteration count: " + (i+1));
        }

        System.out.println("Done!\n");
        mainController.setAccuracy(accuracySum / mainController.getIterationCountValue());
        mainController.setPrecision(precisionSum / mainController.getIterationCountValue());
        mainController.setRecall(recallSum / mainController.getIterationCountValue());
    }

}
