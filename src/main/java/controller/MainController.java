package main.java.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.java.algorithm.*;
import main.java.base.SparkBase;
import main.java.util.ClassLabelProducerUtil;
import main.java.util.MathUtil;
import main.java.util.PROVOGenerator;
import main.java.util.SparseVectorProducerUtil;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML Button chooseLogFileButton;
    @FXML Button runButton;

    @FXML ComboBox selectAlgorithmComboBox;

    @FXML ImageView ytuLogo;

    @FXML Spinner testRate;
    @FXML Spinner iterationCount;

    @FXML Label fileName;
    @FXML Label algorithmName;
    @FXML Label trainingDataRateLabel;
    @FXML Label testDataRateLabel;
    @FXML Label resultLabel;
    @FXML Label errorMessageLabel;
    @FXML Label iterationCountLabel;
    @FXML Label tenFoldString;
    @FXML Label iterationCountString;

    @FXML CheckBox tenFold;

    private String logFileName;
    private String logFilePath;
    private String fileWithBinaryLabelsPath;
    private String sparseVectorFilePath;
    private String fileWithMultiClassLabelsPath;
    private static final String filteredLogFilePath = System.getProperty("user.dir") + "/data/filtered_log_file";

    private Integer algorithmPointer;
    private Integer testDataRate;
    private Integer trainingDataRate;
    private Integer iterationCountValue;
    private Integer featureCount;

    private Double accuracy;
    private Double recall;
    private Double precision;

    private Double sdAccuracy;
    private Double sdRecall;
    private Double sdPrecision;

    private Boolean doesDataCreated;

    public ClassLabelProducerUtil classLabelProducerUtil;
    public SparseVectorProducerUtil sparseVectorProducerUtil;
    public SparkBase sparkBase;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        classLabelProducerUtil   = new ClassLabelProducerUtil();
        sparkBase                = new SparkBase();
        sparseVectorProducerUtil = new SparseVectorProducerUtil(sparkBase);
        testDataRate             = 20;
        trainingDataRate         = 80;
        iterationCountValue      = 100;
        doesDataCreated          = false;

        trainingDataRateLabel.setText(trainingDataRate.toString());
        testDataRateLabel.setText(testDataRate.toString());
        resultLabel.setText("Results: Nothing worked yet.");
        tenFoldString.setText("Inactive");

        ytuLogo.setImage(new Image(getClass().getResourceAsStream("../../resource/ytu.png")));
        testRate.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,99));
        testRate.getValueFactory().setValue(new Integer(20));
        testRate.setEditable(true);

        iterationCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,99999));
        iterationCount.getValueFactory().setValue(new Integer(100));
        iterationCountLabel.setText(new Integer(100).toString());
        iterationCount.setEditable(true);

        chooseLogFileButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                JFileChooser chooser = new JFileChooser();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    logFileName = chooser.getSelectedFile().getName();
                    logFilePath = chooser.getSelectedFile().getPath();

                    fileName.setText(logFileName);
                }

                doesDataCreated = false;
            }
        });

        selectAlgorithmComboBox.setVisibleRowCount(100);
        selectAlgorithmComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                algorithmPointer = selectAlgorithmComboBox.getSelectionModel().getSelectedIndex();
                algorithmName.setText(selectAlgorithmComboBox.getSelectionModel().getSelectedItem().toString());
            }
        });

        long time = System.currentTimeMillis();
        new PROVOGenerator().producePROVOFileFromFilteredWRFLogFile(System.getProperty("user.dir") + "/data/filtered_log_file");
        System.out.println("Total time to produce PROV-O file: " + (System.currentTimeMillis() - time));
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if(selectAlgorithmComboBox.getSelectionModel().getSelectedIndex() >= 0 && logFileName != null){
                    errorMessageLabel.setText("");
                    sparkBase.initializeResources();

                    if(!doesDataCreated){
                        System.out.println("Creating sparse vector ...");
                        sparseVectorFilePath         = sparseVectorProducerUtil.produceSparseVector(logFilePath, logFileName);
                        fileWithBinaryLabelsPath     = classLabelProducerUtil.
                                produceBinaryLabels(logFilePath, sparseVectorFilePath, filteredLogFilePath, logFileName);
                        fileWithMultiClassLabelsPath = classLabelProducerUtil.
                                produceMulticlassLabels(logFilePath, sparseVectorFilePath, filteredLogFilePath, logFileName);
                        featureCount                        = sparseVectorProducerUtil.numOfVocab;

                        System.out.println("Done!");
                        doesDataCreated = true;
                    }else {
                        System.out.println("Sparse vector was created before, skipping this part ...");
                    }

                    long start = System.currentTimeMillis();
                    if(algorithmPointer == 0){       // Logistic Regression With Binary Labels

                        LogisticRegressionAlgorithm logisticRegressionAlgorithm = new LogisticRegressionAlgorithm(sparkBase);
                        logisticRegressionAlgorithm.setLrFamily("binomial");
                        logisticRegressionAlgorithm.applyLogisticRegression(fileWithBinaryLabelsPath, getInstance(), false, logFileName, sparseVectorProducerUtil.numOfVocab);

                    }else if(algorithmPointer == 1){ // Logistic Regression With Multi Class Labels

                        LogisticRegressionAlgorithm logisticRegressionAlgorithm = new LogisticRegressionAlgorithm(sparkBase);
                        logisticRegressionAlgorithm.setLrFamily("multinomial");
                        logisticRegressionAlgorithm.applyLogisticRegression(fileWithMultiClassLabelsPath, getInstance(), true,  logFileName, sparseVectorProducerUtil.numOfVocab);

                    }else if(algorithmPointer == 2){ // Naive Bayes With Binary Labels

                        NaiveBayesAlgorithm naiveBayesAlgorithm = new NaiveBayesAlgorithm(sparkBase);
                        naiveBayesAlgorithm.applyNaiveBayes(fileWithBinaryLabelsPath, getInstance(), logFileName, sparseVectorProducerUtil.numOfVocab);

                    }else if(algorithmPointer == 3){ // Naive Bayes With Multi Class Labels

                        NaiveBayesAlgorithm naiveBayesAlgorithm = new NaiveBayesAlgorithm(sparkBase);
                        naiveBayesAlgorithm.applyNaiveBayes(fileWithMultiClassLabelsPath, getInstance(), logFileName, sparseVectorProducerUtil.numOfVocab);

                    }else if(algorithmPointer == 4){ // Random Forest With Binary Class Labels

                        RandomForestAlgorithm randomForestAlgorithm = new RandomForestAlgorithm(sparkBase);
                        randomForestAlgorithm.applyRandomForest(fileWithBinaryLabelsPath, getInstance(), 2, logFileName, sparseVectorProducerUtil.numOfVocab);
                        // 2 category for if row contains provenance info or not

                    }else if(algorithmPointer == 5){ // Random Forest With Multi Class Labels

                        RandomForestAlgorithm randomForestAlgorithm = new RandomForestAlgorithm(sparkBase);
                        randomForestAlgorithm.applyRandomForest(fileWithMultiClassLabelsPath, getInstance(), 4, logFileName, sparseVectorProducerUtil.numOfVocab);
                        // 4 category for communication, derivation, generation and usage

                    }else if(algorithmPointer == 6){ // Multilayer Perceptron Classifier With Binary Labels

                        MultilayerPerceptronClassifierAlgorithm perceptronClassifierAlgorithm = new MultilayerPerceptronClassifierAlgorithm(sparkBase);
                        perceptronClassifierAlgorithm.applyMultilayerPerceptronClassifier(fileWithBinaryLabelsPath, getInstance(), featureCount, 2, logFileName, sparseVectorProducerUtil.numOfVocab);
                        // 2 class for if row contains provenance info or not

                    }else if(algorithmPointer == 7){ // Multilayer Perceptron Classifier With Multi Class Labels

                        MultilayerPerceptronClassifierAlgorithm perceptronClassifierAlgorithm = new MultilayerPerceptronClassifierAlgorithm(sparkBase);
                        perceptronClassifierAlgorithm.applyMultilayerPerceptronClassifier(fileWithBinaryLabelsPath, getInstance(), featureCount, 4, logFileName, sparseVectorProducerUtil.numOfVocab);
                        // 4 class for communication, derivation, generation and usage

                    }

                    System.out.println("Time to apply the algorithm: " + (System.currentTimeMillis() - start));
                    String resultString = "Results:\n\nAlgorithm: " + selectAlgorithmComboBox.getSelectionModel().getSelectedItem().toString() +
                            "\nTest Method: " + (tenFold.isSelected() ? " 10-fold cross validations" : "Using test set") +
                            (tenFold.isSelected() ? "" : "\nTest Data Rate: " + getTestDataRate() + "\nTraining Data Rate: " + getTrainingDataRate()) +
                            "\nIteration Count: " + iterationCountValue +
                            "\nMean of Accuracy: %" +
                            ((Double) (getAccuracy() * 100)).toString().substring(0, ((Double) (getAccuracy() * 100)).toString().length() > 6 ? 6 : ((Double) (getAccuracy() * 100)).toString().length()) +
                            "\nMean of Precision: %" +
                            ((Double) (getPrecision() * 100)).toString().substring(0, ((Double) (getPrecision() * 100)).toString().length() > 6 ? 6 : ((Double) (getPrecision() * 100)).toString().length()) +
                            "\nMean of Recall: %" +
                            ((Double) (getRecall() * 100)).toString().substring(0, ((Double) (getRecall() * 100)).toString().length() > 6 ? 6 : ((Double) (getRecall() * 100)).toString().length()) +
                            "\nStandard Deviation for Accuracy: " +
                            ((Double) (getSdAccuracy() * 100)).toString().substring(0, ((Double) (getSdAccuracy() * 100)).toString().length() > 6 ? 6 : ((Double) (getSdAccuracy() * 100)).toString().length()) +
                            "\nStandard Deviation for Precision: " +
                            ((Double) (getSdPrecision() * 100)).toString().substring(0, ((Double) (getSdPrecision() * 100)).toString().length() > 6 ? 6 : ((Double) (getSdPrecision() * 100)).toString().length()) +
                            "\nStandard Deviation for Recall: " +
                            ((Double) (getSdRecall() * 100)).toString().substring(0, ((Double) (getSdRecall() * 100)).toString().length() > 6 ? 6 : ((Double) (getSdRecall() * 100)).toString().length());
                    resultLabel.setText(resultString);
                    System.out.println(resultString);

                    sparkBase.cleanResources();
                }else {
                    errorMessageLabel.setText("Warning: Please choose a log file and an algorithm.");
                }

            }
        });

        testRate.getValueFactory().valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                setTestDataRate(Integer.parseInt(t1.toString()));;
                setTrainingDataRate(Math.abs(100 - Integer.parseInt(t1.toString())));

                trainingDataRateLabel.setText(getTrainingDataRate().toString());
                testDataRateLabel.setText(getTestDataRate().toString());
            }
        });

        iterationCount.getValueFactory().valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                setIterationCountValue(Integer.parseInt(t1.toString()));
                iterationCountLabel.setText(t1.toString());
            }
        });

        tenFold.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(tenFold.isSelected()){
                    testRate.setEditable(false);
                    tenFoldString.setText("Active");
                }else {
                    testRate.setEditable(true);
                    tenFoldString.setText("Inactive");
                }
            }
        });

    }

    public Integer getTestDataRate() {
        return testDataRate;
    }

    public void setTestDataRate(Integer testDataRate) {
        this.testDataRate = testDataRate;
    }

    public Integer getTrainingDataRate() {
        return trainingDataRate;
    }

    public void setTrainingDataRate(Integer trainingDataRate) {
        this.trainingDataRate = trainingDataRate;
    }

    public MainController getInstance(){
        return this;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Integer getIterationCountValue() {
        return iterationCountValue;
    }

    public void setIterationCountValue(Integer iterationCountValue) {
        this.iterationCountValue = iterationCountValue;
    }

    public String getFileWithBinaryLabelsPath() {
        return fileWithBinaryLabelsPath;
    }

    public void setFileWithBinaryLabelsPath(String fileWithBinaryLabelsPath) {
        this.fileWithBinaryLabelsPath = fileWithBinaryLabelsPath;
    }

    public String getSparseVectorFilePath() {
        return sparseVectorFilePath;
    }

    public void setSparseVectorFilePath(String sparseVectorFilePath) {
        this.sparseVectorFilePath = sparseVectorFilePath;
    }

    public String getFileWithMultiClassLabelsPath() {
        return fileWithMultiClassLabelsPath;
    }

    public void setFileWithMultiClassLabelsPath(String fileWithMultiClassLabelsPath) {
        this.fileWithMultiClassLabelsPath = fileWithMultiClassLabelsPath;
    }

    public CheckBox getTenFold() {
        return tenFold;
    }

    public void setTenFold(CheckBox tenFold) {
        this.tenFold = tenFold;
    }

    public Double getSdAccuracy() {
        return sdAccuracy;
    }

    public void setSdAccuracy(Double sdAccuracy) {
        this.sdAccuracy = sdAccuracy;
    }

    public Double getSdRecall() {
        return sdRecall;
    }

    public void setSdRecall(Double sdRecall) {
        this.sdRecall = sdRecall;
    }

    public Double getSdPrecision() {
        return sdPrecision;
    }

    public void setSdPrecision(Double sdPrecision) {
        this.sdPrecision = sdPrecision;
    }
}
