package main.java.util;

import java.util.ArrayList;

public class MathUtil {

    public double sigmoid(double value) {
        return 1.0 / (1.0 + Math.exp(-value));
    }

    public double findInformationGain(Integer x, Integer y){
        return (-(x.doubleValue()/(x.doubleValue()+y.doubleValue()))*log(2,(x.doubleValue()/(x.doubleValue()+y.doubleValue())))) +
                (-(y.doubleValue()/(x.doubleValue()+y.doubleValue()))*log(2,(y.doubleValue()/(x.doubleValue()+y.doubleValue()))));
    }

    public double log(double base, double value){
        return Math.log(value) / Math.log(base);
    }

    public double getStandardDeviation(ArrayList<Double> list, Double mean){
        double temp = 0;
        for(double value : list){
            temp += (value - mean) * (value - mean);
        }

        temp /= (list.size()-1);
        return Math.sqrt(temp);
    }

}
