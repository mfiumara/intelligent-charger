package tum.ei.ics.intelligentcharger.predictor;

import java.util.ArrayList;
import java.util.List;

import tum.ei.ics.intelligentcharger.entity.ChargePoint;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Created by mattia on 03.06.15.
 */
public class Predictor {
    protected Classifier classifier;
    protected Instances trainingSet;
    protected ArrayList<Attribute> attributeList;

    public final static String TAG = "Predictor";

    public Predictor() {}

}
