package main.java.base;

import java.util.ArrayList;

public class Line {

    public ArrayList<Integer> wordList;
    public Integer classLabel;

    public Line(ArrayList<Integer> wordList, Integer classLabel) {
        this.wordList = wordList;
        this.classLabel = classLabel;
    }

    public ArrayList<Integer> getWordList() {
        return wordList;
    }

    public void setWordList(ArrayList<Integer> wordList) {
        this.wordList = wordList;
    }

    public Integer getClassLabel() {
        return classLabel;
    }

    public void setClassLabel(Integer classLabel) {
        this.classLabel = classLabel;
    }
}
