package main.java.algorithm;

import main.java.base.SparkBase;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import scala.Tuple2;

import java.util.List;

public class SVMAlgorithm {

    public SparkBase sparkBase;

    public SVMAlgorithm(SparkBase sparkBase){
        this.sparkBase = sparkBase;
    }

    public void applySVMAlgorithm(String labeledFilePath, Integer testDataRate, Integer trainingDataRate) {
        JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sparkBase.getSc().sc(), labeledFilePath)
                .toJavaRDD();

        try {
            int nOfPrec =0;
            int nOfRec = 0;

            for(int j=0;j <1; j++){
                JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[]{trainingDataRate, testDataRate});
                JavaRDD<LabeledPoint> training = splits[0].cache();
                JavaRDD<LabeledPoint> test = splits[1].cache();
                // Run training algorithm to build the model.
                int numIterations = 100;
                final SVMModel svmModel = SVMWithSGD.train(training.rdd(), numIterations);

                // Clear the default threshold.
                svmModel.clearThreshold();

                // Compute raw scores on the test set.
                JavaRDD<Tuple2<Object, Object>> scoreAndLabels = test
                        .map(new Function<LabeledPoint, Tuple2<Object, Object>>() {
                            public Tuple2<Object, Object> call(LabeledPoint p) {
                                Double score = svmModel.predict(p.features());
                                return new Tuple2<Object, Object>(score, p.label());
                            }
                        });

                // Compute raw scores on the test set.
                JavaRDD<Double> svmResults = test
                        .map(new Function<LabeledPoint, Double>() {
                            public Double call(LabeledPoint p) {
                                Double score = svmModel.predict(p.features());
                                Double label = p.label();
                                Double prediction = 0.0;
                                if (score >= 0.0) {
                                    prediction = 1.0;
                                }
//                        Double result = 0.0;
//                        if (prediction.equals(label)) {
//                            result = 1.0;
//                        }
                                System.out.println(prediction + " " + label);
                                return score;
                            }
                        });

//            double[] weights = svmModel.weights().toArray();
//            for(int i=0; i<weights.length;i++){
//                System.out.println(weights[i]);
//            }
                List<Double> scores = svmResults.collect();

                String line;
                Double score;

//            for (int i = 0; i < scores.size(); i++) {
//                line = bufferedReader.readLine();
//                score = scores.get(i);
//                if (score >= 0.0) {
//                    bufferedWriter.write(line);
//                    bufferedWriter.newLine();
//                }
//            }

//            bufferedReader.close();
//            bufferedWriter.close();

//            Double totalCorrect = svmResults
//                    .reduce(new Function2<Double, Double, Double>() {
//                        public Double call(Double p, Double q) {
//                            return p + q;
//                        }
//                    });
                // Get evaluation metrics.
                BinaryClassificationMetrics metrics = new BinaryClassificationMetrics(
                        JavaRDD.toRDD(scoreAndLabels));

//            double auROC = metrics.areaUnderROC();
//            long count = data.count();
//
                JavaRDD<Tuple2<Object, Object>> precision = metrics.precisionByThreshold().toJavaRDD();
                JavaRDD<Tuple2<Object, Object>> recall = metrics.recallByThreshold().toJavaRDD();
                List<Tuple2<Object, Object>> precisionList = precision.collect();
                List<Tuple2<Object, Object>> recallList = recall.collect();


                System.out.println("Precision");
                for(int i=0;i< precisionList.size();i++){
                    System.out.println(precisionList.get(i)._1() + " " + precisionList.get(i)._2());
//                double threshold = (Double) precisionList.get(i)._1();
//                if( threshold == 0.0){
//                    System.out.println("Precision");
//                    System.out.println(precisionList.get(i)._2());
//                    nOfPrec++;
//
//                }

                }

                System.out.println("Recall");
                for(int i=0;i<recallList.size();i++){
                    System.out.println(recallList.get(i)._1() + " " + recallList.get(i)._2());
//                  if((Double) recallList.get(i)._1() == 0.0){
//                    System.out.println("Recall");
//                    System.out.println(recallList.get(i)._2());
//                    nOfRec++;
//
//                }
                }
                System.out.println("Count : " + test.count());
            }
            System.out.println("Prec : " + nOfPrec);
            System.out.println("Rec : " + nOfRec);

//            System.out.println(precision.collect().toString());
//            System.out.println(recall.collect().toString());
            //System.out.println("Accuracy SVM = " + totalCorrect / count + " Count: " + count);
            //System.out.println("Area under ROC SVM = " + auROC);
//            System.out.println("Precision by threshold: " + precision.toArray);
//            precision.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
