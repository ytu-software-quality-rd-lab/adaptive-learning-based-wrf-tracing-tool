package main.java.base;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.*;

public class SparkBase {

    public StructType schema;
    public Dataset<Row> df;
    public JavaSparkContext sc;

    public SparkConf conf;

    private transient SparkSession spark;

    public void initializeResources(){
        try{
            conf = new SparkConf().setAppName("Linear Classifiers Examples")
                    .setMaster("local");

            sc   = new JavaSparkContext(conf);
            sc.setLogLevel("ERROR");
            spark = SparkSession
                    .builder()
                    .appName("Sparse Vector Producer")
                    .getOrCreate();

            schema = new StructType(new StructField[]{
                    new StructField("text", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
            });
        }catch (Exception e){
            cleanResources();
            initializeResources();
            e.printStackTrace();
        }
    }

    public void cleanResources(){
        sc.cancelAllJobs();
        sc.stop();
        spark.stop();
    }

    public StructType getSchema() {
        return schema;
    }

    public void setSchema(StructType schema) {
        this.schema = schema;
    }

    public Dataset<Row> getDf() {
        return df;
    }

    public void setDf(Dataset<Row> df) {
        this.df = df;
    }

    public JavaSparkContext getSc() {
        return sc;
    }

    public void setSc(JavaSparkContext sc) {
        this.sc = sc;
    }

    public SparkConf getConf() {
        return conf;
    }

    public void setConf(SparkConf conf) {
        this.conf = conf;
    }

    public SparkSession getSpark() {
        return spark;
    }

    public void setSpark(SparkSession spark) {
        this.spark = spark;
    }
}
