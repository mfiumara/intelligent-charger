package tum.ei.ics.intelligentcharger.predictor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by mattia on 03.06.15.
 */
public class UnplugTimePredictor extends Predictor {
    private List<Cycle> cycles;
    private float shift;

    public UnplugTimePredictor(List<Cycle> cycles) {
        // Initialize class variables
        this.cycles = cycles;
        if (cycles.size() > Global.MINIMUM_SAMPLES) {
            // Fit predictor when minimum amount of charge cycles has been reached
            this.shift = calculateShift();
            fitPredictor();
        }
    }
    public double predict(ConnectionEvent event) {
        if (cycles.size() > Global.MINIMUM_SAMPLES) {
            // Create the vector to be predicted.
            Instance testInstance = new DenseInstance(2);
            testInstance.setDataset(trainingSet);
            testInstance.setValue(attributeList.get(0), (event.getTime() - shift) % 24);

            try { // Do the actual prediction and transform back to an actual time
                return (classifier.classifyInstance(testInstance) + shift) % 24;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
    public double predict(double time) {
        if (cycles.size() > Global.MINIMUM_SAMPLES) {
            // Create the vector to be predicted.
            Instance testInstance = new DenseInstance(2);
            testInstance.setDataset(trainingSet);
            testInstance.setValue(attributeList.get(0), (time - shift) % 24);

            try { // Do the actual prediction and transform back to an actual time
                return (classifier.classifyInstance(testInstance) + shift) % 24;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
    public float getShift() { return shift; }

    private float calculateShift() {
        // Get all plug events and sort them by plugtime
        int N = cycles.size();
        float[] plugEvents = new float[N];
        int i = 0;
        for (Cycle cycle : cycles) {
            plugEvents[i] = cycle.getPluginEvent().getTime();
            i++;
        }
        Arrays.sort(plugEvents);

        // Calculate the amount to shift the plugtimes with
        float shift, temp, maxval;
        shift = temp = maxval = 0.0f;
        for (i = 0; i < N - 1; i++) {
            temp = plugEvents[i + 1] - plugEvents[i];
            if (temp > maxval) {
                maxval = temp;
                shift = plugEvents[i] + maxval / 2;
            }
        }
        // Check the difference between the last and first event, max time could be in between days
        temp = plugEvents[0] + (24 - plugEvents[N - 1]);
        if (temp > maxval) {
            maxval = temp;
            shift = (plugEvents[N - 1] + maxval / 2) % 24;
        }
        return shift;
    }
    private void fitPredictor() {
        // Create weka attributes and the training set
        Attribute plugTimeAttribute= new Attribute("Plugtime");
        Attribute unplugTimeAttribute = new Attribute("UnplugTime");

        attributeList = new ArrayList<>();
        attributeList.add(plugTimeAttribute);
        attributeList.add(unplugTimeAttribute);

        trainingSet = new Instances("Rel", attributeList, cycles.size());
        trainingSet.setClassIndex(1);

        // Add all cycles to training set with shifted times
        float transform = 0.0f;
        for(Cycle cycle : cycles) {
            ConnectionEvent plugEvent = cycle.getPluginEvent();
            ConnectionEvent unplugEvent = cycle.getPlugoutEvent();

            float plugTime = plugEvent.getTime();
            float unplugTime = unplugEvent.getTime();

            float plugTimeShift = (plugTime - shift) % 24;
            transform = plugTimeShift > unplugTime - shift ? 24 : 0;
            float unplugTimeShift = unplugTime - shift + transform;

            // Create the weka instances
            Instance instance = new DenseInstance(2);
            instance.setValue((Attribute) attributeList.get(0), plugTimeShift);
            instance.setValue((Attribute) attributeList.get(1), unplugTimeShift);
            trainingSet.add(instance);
        }

        //TODO: Create better model and build the classifier
/*        Classifier decisionTree = (Classifier) new weka.classifiers.trees.REPTree();
        Bagging randomForest = new Bagging();
        randomForest.setClassifier(decisionTree);*/

        classifier = new LinearRegression();
        try {
            classifier.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
