package tum.ei.ics.intelligentcharger.predictor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.entity.ChargePoint;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.AddExpression;

/**
 * Created by mattia on 03.06.15.
 */
public class ChargeTimePredictor extends Predictor {
    private List<ChargePoint> chargePoints;
    private double SPLIT = 75;

    public ChargeTimePredictor(List<ChargePoint> chargePoints) {
        // Initialize class variables
        this.chargePoints = chargePoints;

        // Fit predictor when at least one charge curve has been recorded
        if (chargePoints.size() > Global.MINIMUM_SAMPLES) {
            fitPredictor();
        }
    }

    private void fitPredictor() {
        // Create weka attributes
        attributeList = new ArrayList<>();
        attributeList.add(new Attribute("Level"));
        attributeList.add(new Attribute("Time"));

        trainingSet = new Instances("ChargePoint", attributeList, chargePoints.size());
        trainingSet.setClassIndex(1);

        // Add all chargepoints to training set
        for(ChargePoint chargePoint : chargePoints) {
            // Create the weka instances
            Instance instance = new DenseInstance(2);
            instance.setValue((Attribute) attributeList.get(0), chargePoint.getLevel());
            instance.setValue((Attribute) attributeList.get(1), chargePoint.getTime());
            trainingSet.add(instance);
        }
        // TODO: Split the classifier into two parts.
        // Second Order Polynomial Transform
        AddExpression addExpression = new AddExpression();
        addExpression.setExpression("a1^2");

        // Base Classifier
        Classifier linearRegression = new LinearRegression();

        // Meta Classifier
        FilteredClassifier filteredClassifier = new FilteredClassifier();
        filteredClassifier.setFilter(addExpression);
        filteredClassifier.setClassifier(linearRegression);

        try {
            filteredClassifier.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        classifier = filteredClassifier;
    }

    public double predict(double startSOC, double endSOC) {
        if (chargePoints.size() > Global.MINIMUM_SAMPLES) {
            if (startSOC < endSOC) {
                // Create the vector to be predicted.
                Instance startInstance = new DenseInstance(2);
                startInstance.setDataset(trainingSet);
                Instance endInstance = new DenseInstance(2);
                endInstance.setDataset(trainingSet);

                startInstance.setValue(attributeList.get(0), startSOC);
                endInstance.setValue(attributeList.get(0), endSOC);

                try { // Do the actual prediction
                    double startTime = classifier.classifyInstance(startInstance);
                    double endTime = classifier.classifyInstance(endInstance);
                    return (endTime - startTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }
}
