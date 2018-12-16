package main.java.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import main.java.base.SparkBase;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

public class SparseVectorProducerUtil {

    public SparkBase sparkBase;
    public Integer numOfVocab;

    public SparseVectorProducerUtil(SparkBase sparkBase){
        checkIfFoldersExist();
        this.numOfVocab = new Integer(0);
        this.sparkBase  = sparkBase;
    }

    public void checkIfFoldersExist(){
        String root = System.getProperty("user.dir") + "/data";
        File file = new File(root + "/sv");
        if(!file.exists()){
            file.mkdir();
        }
    }

    public String produceSparseVector(String logFilePath, String logFileName) {
        List<Row> data = addToVocabularyList(logFilePath);
        String sparseVectorFilePath = System.getProperty("user.dir") + "/data/sv/" +
                logFileName + "_sv_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());


        CountVectorizerModel cvModel;

        sparkBase.setDf(sparkBase.getSpark().createDataFrame(data, sparkBase.getSchema()));
        cvModel = new CountVectorizer()
                .setBinary(true)
                .setInputCol("text")
                .setOutputCol("feature")
                .setVocabSize(numOfVocab)
                .setMinDF(1)
                .fit(sparkBase.getDf());

        Dataset<Row> ds = cvModel.transform(sparkBase.getDf());
        List<Row> listr = ds.collectAsList();

        try {
            FileOutputStream outputStream = new FileOutputStream(sparseVectorFilePath);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            int[] indices;
            double[] values;

            for (int i = 0; i < listr.size(); i++) {

                SparseVector sv = listr.get(i).getAs(1);
                indices = sv.indices();
                values = sv.values();

                for (int j = 0; j < sv.indices().length; j++) {

                    bufferedWriter.append(" " + (indices[j] + 1) + ":" + (int) values[j]);
                }

                bufferedWriter.newLine();
            }

            bufferedWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sparseVectorFilePath;
    }

    public List<Row> addToVocabularyList(String logFilePath) {
        ArrayList<String> vocabList = new ArrayList<String>();
        List<Row> data              = new ArrayList<>();

        try {
            FileReader reader = new FileReader(logFilePath);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            String[] words;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim().replaceAll("\\s{2,}", " ");
                words = line.split(" ");

                for (int i = 0; i < words.length; i++) {
                    if (words[i].contains(":")) {
                        words[i] = words[i].replace(":", "");
                    }

                    if(!vocabList.contains(words[i])){
                        vocabList.add(words[i]);
                        numOfVocab++;
                    }
                }
                data.add(RowFactory.create((Object) words));
            }

            bufferedReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
