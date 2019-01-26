package main.java.util;

import main.java.base.SparkBase;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileUtil {

    public String getFullFilePath(String fileName, Integer iterationCount, Integer foldCount, String fileType){
        return System.getProperty("user.dir") + "/data/folds/" + fileName + "_" + iterationCount + "_" + fileType + "_iteration_" + foldCount + "_fold.txt";
    }

    public void writeToFile(Integer foldCount, Integer iterationCount, String fileName, ArrayList<String> lines, String fileType, Integer numOfFeatures){
        try{
            File file = new File(getFullFilePath(fileName, iterationCount, foldCount, fileType));
            if(!file.exists()){
                file.createNewFile();
            }

            PrintWriter writer = new PrintWriter(file.getPath());
            for(String line : lines){
                writer.write(line + "\n");
            }

            writer.write("0 " + numOfFeatures + ":1" + "\n");

            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Dataset<Row> getDataSet(SparkBase sparkBase, String filePath){
        Dataset<Row> data = sparkBase
                .getSpark()
                .read()
                .format("libsvm")
                .load(filePath);

        return data;
    }

}
