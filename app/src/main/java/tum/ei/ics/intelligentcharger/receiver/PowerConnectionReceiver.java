package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Battery;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import tum.ei.ics.intelligentcharger.service.BatteryService;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by mattia on 07.05.15.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    public static final String TAG = "PowerConnectionReceiver";
    public static Integer MIN_SAMPLES = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Open shared preference file
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();

        // Get battery information
        Battery battery = new Battery(context);

        // Get ID's of important events
        Long startCycleID = prefs.getLong(context.getString(R.string.start_cycle_id), -1);
        Long endCycleID = prefs.getLong(context.getString(R.string.end_cycle_id), -1);

        // Save event to database
        ConnectionEvent currEvent = new ConnectionEvent(
                battery.getStatus(), battery.getPlugged(),
                battery.getLevel(), battery.getVoltage(),
                battery.getTemperature(), battery.getChargingStatus());
        currEvent.save();

        // Check battery state to determine type of event.
        if (battery.isCharging()) {
            if (!battery.isFull()) {    // Charging and not full: plug-in event
                // Check if there is a cycle to save
                if ((startCycleID > 0) && (endCycleID > 0)) {
                    // Yes, we remembered the events to save, so now we save it to the database
                    ConnectionEvent startEvent = ConnectionEvent.findById(ConnectionEvent.class, startCycleID);
                    ConnectionEvent endEvent = ConnectionEvent.findById(ConnectionEvent.class, endCycleID);
                    Cycle cycle = new Cycle(startEvent, endEvent);
                    saveCycle(context, cycle);
                }
                // Charging and not full: plug-in event so save this event as the start of a cycle
                prefEdit.putLong(context.getString(R.string.start_cycle_id), currEvent.getId());

                // Do the predictions!
                // Create training set from list of Cycles
                List<Cycle> cycles = Cycle.listAll(Cycle.class);
                Integer N = cycles.size();
                if ( N < MIN_SAMPLES ) {
                    prefEdit.apply();
                    return;
                }
                updateShift(context, cycles);   // Update shifted times
                fitUnplugPredictor(context, currEvent, cycles);   // Fit the predictor using shifted times
                fitChargePredictor(context);

                //TODO: Start batterychanged service to record the charge curve
                Intent i = new Intent(context, BatteryService.class);
                context.startService(i);
            } else {
                //TODO: Stop batterychanged service to stop recording the charge curve receiver
                Intent i = new Intent(context, BatteryService.class);
                context.stopService(i);
            }
        } else {
            if (battery.isFull()) {
                // Not charging but full: either disconnected charger or repetitive cycle
                // Save this as temporary end cycle but do not save cycle to database yet
                prefEdit.putLong(context.getString(R.string.end_cycle_id), currEvent.getId());
                //TODO: Stop batterychanged broadcast receiver and save curve
            } else {
                // Not charging and not full: Disconnected charger
                // Save cycle to database using start_cycle_id and current id
                if (startCycleID > 0) {
                    ConnectionEvent startEvent = ConnectionEvent.findById(ConnectionEvent.class, startCycleID);
                    Cycle cycle = new Cycle(startEvent, currEvent);
                    saveCycle(context, cycle);
                }
                // Reset saved events
                prefEdit.putLong(context.getString(R.string.start_cycle_id), -1);
                prefEdit.putLong(context.getString(R.string.end_cycle_id), -1);
                // TODO: Reset charge curve

            }
        }
        // Save data to shared preference file
        prefEdit.apply();
    }

    public void saveCycle(Context context, Cycle cycle) {
        // Save cycle to database
        // TODO: If cycle duration is less then X minutes / SOC, do not save to database
        // For now just check if the SOC is correct, and not the same
        if (cycle.getPluginEvent().getLevel() < cycle.getPlugoutEvent().getLevel()) {
            cycle.save();
            // Notify user of saved charge cycle.
            Toast.makeText(context, "Charge cycle saved", Toast.LENGTH_SHORT).show();
        }
        // Reset saved events
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();
        prefEdit.putLong(context.getString(R.string.start_cycle_id), -1);
        prefEdit.putLong(context.getString(R.string.end_cycle_id), -1);
    }

    public void fitUnplugPredictor(Context context, ConnectionEvent event, List<Cycle> cycles) {
        // Create weka attributes
        Attribute plugTimeAttribute= new Attribute("Plugtime");
        Attribute unplugTimeAttribute = new Attribute("UnplugTime");

        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(plugTimeAttribute);
        attributeList.add(unplugTimeAttribute);

        Instances trainingSet = new Instances("Rel", attributeList, cycles.size());
        trainingSet.setClassIndex(1);

        // Get amount to shift times with
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        float shift = prefs.getFloat(context.getString(R.string.shift), 0.0f);
        float transform = 0.0f;
        // Add all cycles to training set
        for(Cycle cycle : cycles) {
            ConnectionEvent plugEvent = cycle.getPluginEvent();
            ConnectionEvent unplugEvent = cycle.getPlugoutEvent();

            float plugTime = plugEvent.getTime();
            float unplugTime = unplugEvent.getTime();

            float plugTimeShift = (plugTime - shift) % 24;
            transform = plugTimeShift > unplugTime - shift ? 24 - shift : - shift;
            float unplugTimeShift = unplugTime + transform;

            // Create the weka instances
            Instance instance = new DenseInstance(2);
            instance.setValue((Attribute) attributeList.get(0), plugTimeShift);
            instance.setValue((Attribute) attributeList.get(1), unplugTimeShift);
            trainingSet.add(instance);
        }

        //TODO: Create better model and build the classifier
        Classifier linearRegression = (Classifier) new LinearRegression();
        Classifier decisionTree = (Classifier) new weka.classifiers.trees.REPTree();

//        weka.classifiers.meta.LogitBoost boostingTree = new weka.classifiers.meta.LogitBoost();
//        boostingTree.setClassifier(decisionTree);
//        boostingTree.setNumIterations(5);

        //Classifier adaBoost = (Classifier) new
        try {
            linearRegression.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the vector to be predicted.
        Instance testInstance = new DenseInstance(2);
        testInstance.setDataset(trainingSet);
        testInstance.setValue((Attribute) attributeList.get(0), event.getTime());

        // Output the results
        try {
            // Do the actual prediction and transform back to an actual time
            double prediction = Math.abs(linearRegression.classifyInstance(testInstance) - transform);
            // Convert to hours and minutes
            int hours = (int) Math.floor(prediction) % 24;
            int minutes = (int) ((prediction - hours) * 60);
            Toast.makeText(context, "Unplug prediction: " + hours + ":" + minutes, Toast.LENGTH_LONG).show();
            Log.v(TAG, "Unplug prediction: " + hours + ":" + minutes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void fitChargePredictor(Context context) {}
    public void updateShift(Context context, List<Cycle> cycles) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();

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
        Log.v(TAG, "Amount of shift: " + shift);
        prefEdit.putFloat(context.getString(R.string.shift), shift);

    }

}
