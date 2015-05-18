package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.widget.Toast;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import tum.ei.ics.intelligentcharger.entity.Event;

/**
 * Created by mattia on 07.05.15.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    public static final String TAG = "PowerConnectedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Open shared preference file
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();

        // Get current battery info
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        intent = context.registerReceiver(null, intentFilter);
        int currStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int currLevel  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,  -1);
        int currTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int currVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        int currPlugtype = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = currPlugtype == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = currPlugtype == BatteryManager.BATTERY_PLUGGED_AC;
        boolean isCharging =
                currStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                currStatus == BatteryManager.BATTERY_STATUS_FULL ||
                usbCharge ||
                acCharge;
        boolean isFull = currLevel == 100;

        // Check if the phone is either charging or discharging using all information available.
        // The app considers 100% SOC as charging, as this means it is not yet the end of
        // the charge cycle.
        String currCustomStatus = (isFull || isCharging)
                ? context.getString(R.string.charging) : context.getString(R.string.discharging);
        // Create current event
        Event currEvent = new Event(currStatus, currPlugtype, currLevel, currVoltage,
                currTemp, currCustomStatus);
        // Save event to database
        currEvent.save();

        // Get ID's of important events
        Long startCycleID = prefs.getLong(context.getString(R.string.start_cycle_id), -1);
        Long endCycleID = prefs.getLong(context.getString(R.string.end_cycle_id), -1);

        // Check battery state to determine type of event.
        if (isCharging) {
            if (!isFull) {    // Charging and not full: plug-in event
                // Check if there is a cycle to save
                if ((startCycleID > 0) && (endCycleID > 0)) {
                    // Yes, we remembered the events to save, so now we save it to the database
                    Event startEvent = Event.findById(Event.class, startCycleID);
                    Event endEvent = Event.findById(Event.class, endCycleID);
                    Cycle cycle = new Cycle(startEvent, endEvent);
                    saveCycle(context, cycle);
                }
                // Charging and not full: plug-in event so save this event as the start of a cycle
                prefEdit.putLong(context.getString(R.string.start_cycle_id), currEvent.getId());

                // Do the predictions!
                fitPredictor(currEvent);
                String unplugDatetime = predictUnplugTime(currEvent);
                String chargeDatetime = predictChargeTime(currEvent);

                //TODO: Start batterychanged broadcast receiver to record the charge curve

            }
        } else {
            //TODO: Stop batterychanged broadcast receiver, we do not need to monitor the charge curve anymore
            if (isFull) {
                // Not charging but full: either disconnected charger or repetitive cycle
                // Save this as temporary end cycle but do not save cycle to database yet
                prefEdit.putLong(context.getString(R.string.end_cycle_id), currEvent.getId());
            } else {
                // Not charging and not full: Disconnected charger
                // Save cycle to database using start_cycle_id and current id
                if (startCycleID > 0) {
                    Event startEvent = Event.findById(Event.class, startCycleID);
                    Cycle cycle = new Cycle(startEvent, currEvent);
                    saveCycle(context, cycle);
                }
                // Reset saved events
                prefEdit.putLong(context.getString(R.string.start_cycle_id), -1);
                prefEdit.putLong(context.getString(R.string.end_cycle_id), -1);
            }
        }
        // Save data to shared preference file
        prefEdit.apply();
    }

    public String predictUnplugTime(Event event) {
        return "Foo";
    }

    public String predictChargeTime(Event event) {
        return "Bar";
    }

    public void saveCycle(Context context, Cycle cycle) {
        // Save cycle to database
        // TODO: If cycle duration is less then X minutes, do not save to database
        cycle.save();

        // Notify user of saved charge cycole.
        Toast.makeText(context, "Charge cycle saved", Toast.LENGTH_SHORT).show();

        // Reset saved events
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();
        prefEdit.putLong(context.getString(R.string.start_cycle_id), -1);
        prefEdit.putLong(context.getString(R.string.end_cycle_id), -1);
    }

    public void fitPredictor(Event event) {
        ;
    }

}
