package tum.ei.ics.intelligentcharger.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.predictor.ChargeTimePredictor;
import tum.ei.ics.intelligentcharger.predictor.Predictor;
import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.Utility;
import tum.ei.ics.intelligentcharger.entity.Battery;
import tum.ei.ics.intelligentcharger.entity.ChargePoint;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import tum.ei.ics.intelligentcharger.entity.CurveEvent;
import tum.ei.ics.intelligentcharger.entity.Cycle;

import tum.ei.ics.intelligentcharger.predictor.TargetSOCPredictor;
import tum.ei.ics.intelligentcharger.predictor.UnplugTimePredictor;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by mattia on 07.05.15.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    public static final String TAG = "PowerConnectionReceiver";

    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEdit;

    public void onReceive(Context context, Intent intent) {
        // Open shared preference file
        prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        prefEdit = prefs.edit();

        // Get battery information
        Battery battery = new Battery(context);

        // Get ID's of important events
        Long startCycleID = prefs.getLong(context.getString(R.string.start_cycle_id), -1);
        Long endCycleID = prefs.getLong(context.getString(R.string.end_cycle_id), -1);

        // Save current event to database
        ConnectionEvent currEvent = new ConnectionEvent(
                battery.getStatus(), battery.getPlugged(),
                battery.getLevel(), battery.getVoltage(),
                battery.getTemperature(), battery.getChargingStatus());
        currEvent.save();

        // TODO: Decide whether we are dealing with an actual plug event or dealing with a hypothetical plug event
        // Check battery state to determine type of event.
        if (battery.isCharging()) {
            if (!battery.isFull()) {    // Charging and not full: plug-in event
                if ((startCycleID > 0) && (endCycleID > 0)) { // Yes, we remembered a full charge event, so now we save it to the database
                    ConnectionEvent startEvent = ConnectionEvent.findById(ConnectionEvent.class, startCycleID);
                    ConnectionEvent endEvent = ConnectionEvent.findById(ConnectionEvent.class, endCycleID);
                    saveCycle(context, new Cycle(startEvent, endEvent));
                }
                // Save this plug-in event as the start of a cycle
                prefEdit.putLong(context.getString(R.string.start_cycle_id), currEvent.getId());

                // Start alarm to record the charge curve every X minutes
                Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
                prefEdit.putLong(context.getString(R.string.curve_id), ++curveID);

                // Setup the alarm to record the charge curve
                Intent i = new Intent(context, BatteryChangedReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                // Trigger the alarm for the amount of minutes defined
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000 * 60 * Global.ALARM_FREQUENCY, pendingIntent);

                // Initialize predictor classes using Object lists from sqlite database
                UnplugTimePredictor unplugTimePredictor =
                        new UnplugTimePredictor(Cycle.listAll(Cycle.class));
                ChargeTimePredictor chargeTimePredictor = // Only use chargepoints belonging to USB / AC cycles
                        new ChargeTimePredictor(ChargePoint.find(ChargePoint.class, "plug_type = ?",
                                currEvent.getPlugged().toString()));
                TargetSOCPredictor targetSOCPredictor =
                        new TargetSOCPredictor();

                // Calculate charge starting point and put it in a Calendar to set the alarm time
                double unplugTime = unplugTimePredictor.predict(currEvent);
                double chargeTime = chargeTimePredictor.predict(currEvent.getLevel(),
                        targetSOCPredictor.predict());
                double chargeStart = unplugTime < chargeTime ? unplugTime - chargeTime + 24
                        : unplugTime - chargeTime;

                // Setup the alarm
                i = new Intent(context, StartChargeReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(context, 1, i, 0);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int hours = (int) Math.floor(chargeStart) % 24;
                int minutes = (int) ((chargeStart - hours) * 60);
                calendar.set(Calendar.HOUR_OF_DAY, hours);
                calendar.set(Calendar.MINUTE, minutes);
                // TODO: Set day of charging
                Toast.makeText(context, "Charging starts at: " + Utility.timeToString(chargeStart),
                        Toast.LENGTH_SHORT).show();
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } else {
            Intent i = new Intent(context, BatteryChangedReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (battery.isFull()) {
                // Not charging but full: either disconnected charger or repetitive cycle
                // Save this as temporary end cycle but do not save cycle to database yet
                prefEdit.putLong(context.getString(R.string.end_cycle_id), currEvent.getId());
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

                // Reset charge curve and don't save it as battery is not fully charged
                CurveEvent.deleteAll(CurveEvent.class);
                // Stop recording the charge curve here
                alarmManager.cancel(pendingIntent);
            }
        }
        // Save data to shared preference file
        prefEdit.apply();
    }

    public void saveCycle(Context context, Cycle cycle) {
        // Save cycle to database only if SOC levels differ
        if (cycle.getPluginEvent().getLevel() < cycle.getPlugoutEvent().getLevel()) {
            cycle.save();
            // Notify user of saved charge cycle.
            Toast.makeText(context, "Charge cycle saved", Toast.LENGTH_SHORT).show();
        }
        // Reset saved events
        prefEdit.putLong(context.getString(R.string.start_cycle_id), -1);
        prefEdit.putLong(context.getString(R.string.end_cycle_id), -1);
        prefEdit.apply();
    }
}
