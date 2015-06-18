package tum.ei.ics.intelligentcharger.receiver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.SwipeActivity;
import tum.ei.ics.intelligentcharger.fragment.CycleFragment;
import tum.ei.ics.intelligentcharger.fragment.MainFragment;
import tum.ei.ics.intelligentcharger.predictor.ChargeTimePredictor;
import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.Utility;
import tum.ei.ics.intelligentcharger.entity.Battery;
import tum.ei.ics.intelligentcharger.entity.ChargePoint;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import tum.ei.ics.intelligentcharger.entity.CurveEvent;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import tum.ei.ics.intelligentcharger.predictor.TargetSOCPredictor;
import tum.ei.ics.intelligentcharger.predictor.UnplugTimePredictor;

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

        // Check if user has turned on smart charging, otherwise do nothing and charge regularly
        if (prefs.getBoolean(context.getString(R.string.smart_charge), true)) {
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

            // If plug-in event: Start bluetooth service

            // Initialize bluetooth connection

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

                    // TODO: Remove the start of charge curve recording here when the actual charger is implemented
                    // Start alarm to record the charge curve every X minutes
                    Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
                    prefEdit.putLong(context.getString(R.string.curve_id), ++curveID);

                    // Setup the alarm to record the charge curve
                    Intent i = new Intent(context, BatteryChangedReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    // Trigger the alarm for the amount of minutes defined
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime(), 1000 * 60 * Global.ALARM_FREQUENCY, pendingIntent);

                    // Initialize predictor classes using Object lists from sqlite database
                    UnplugTimePredictor unplugTimePredictor =
                            new UnplugTimePredictor(Cycle.listAll(Cycle.class));
                    ChargeTimePredictor chargeTimePredictor = // Only use chargepoints belonging to USB / AC cycles
                            new ChargeTimePredictor(ChargePoint.find(ChargePoint.class, "plug_type = ?",
                                    currEvent.getPlugged().toString()));

                    // Get target SOC, either from user input or from target SOC Predictor
                    Integer maxSOC = 100;
                    if (prefs.getBoolean(context.getString(R.string.enable_soc), true)) {
                        maxSOC = prefs.getInt(context.getString(R.string.max_soc), 100);
                    } else {
                        TargetSOCPredictor targetSOCPredictor =
                                new TargetSOCPredictor(context, Cycle.listAll(Cycle.class), Global.HISTORY_SIZE);
                        maxSOC = targetSOCPredictor.predict();
                    }
                    // Check if the targetSOC is higher then the current SOC, otherwise don't charge
                    if (maxSOC > currEvent.getLevel()) {
                        // Predict unplugtime and chargetime now
                        double unplugPrediction = unplugTimePredictor.predict(currEvent);
                        double chargeTimePrediction = chargeTimePredictor.predict(currEvent.getLevel(), maxSOC);
                        prefEdit.putInt(context.getString(R.string.min_soc), Global.MIN_SOC);
                        prefEdit.putInt(context.getString(R.string.max_soc), maxSOC);
                        prefEdit.putFloat(context.getString(R.string.unplug_time), (float) unplugPrediction);
                        prefEdit.putFloat(context.getString(R.string.charge_time), (float) chargeTimePrediction);

                        double chargeStart = unplugPrediction < chargeTimePrediction ? unplugPrediction - chargeTimePrediction + 24
                                : unplugPrediction - chargeTimePrediction;

                        // Prepare start of charging broadcast
                        i = new Intent(context, StartChargeReceiver.class);
                        if (unplugPrediction > 0 && chargeTimePrediction > 0) {
                            // We have enough data to have done a successfully prediction
                            int hours = (int) Math.floor(chargeStart) % 24;
                            int minutes = (int) ((chargeStart - hours) * 60);

                            // Get current time of day
                            Calendar calendar = Calendar.getInstance();
                            double currentTime = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0;

                            Log.v(TAG, "Unplug time prediction: " + unplugPrediction);
                            Log.v(TAG, "Charge time prediction: " + chargeTimePrediction);
                            Log.v(TAG, "Current time          : " + currentTime);

                            if (unplugPrediction < currentTime) { // Add one day to the calendar, as prediction is earlier than the current time
                                calendar.setTimeInMillis(calendar.getTimeInMillis() + 1000 * 60 * 60 * 24);
                            } else if (chargeStart < currentTime) {
                                // Start charging immediately
                                context.sendBroadcast(i);
                            } else {
                                // Create pending intent to fire the start of charging at the predicted time
                                pendingIntent = PendingIntent.getBroadcast(context, 1, i, 0);
                                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                setNotification(context, chargeStart);

                                calendar.set(Calendar.HOUR_OF_DAY, hours);
                                calendar.set(Calendar.MINUTE, minutes);
                                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            }
                        } else {
                            // Not enough data, or unusable prediction, start charging immediately
                            context.sendBroadcast(i);
                        }
                        Toast.makeText(context, "Unplug prediction: " + Utility.timeToString(unplugPrediction) + "\n" +
                                "charge time prediction: " + Utility.timeToString(chargeTimePrediction), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Target SOC is lower then the current SOC, no charge needed!", Toast.LENGTH_SHORT).show();
                    }
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

            // Reload main fragment with new data
            Intent update = new Intent(context, MainFragment.updateView.class);
            context.sendBroadcast(update);
        }
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

        // Repopulate cycle graph (if needed)
        Intent i = new Intent(context, CycleFragment.updateView.class);
        context.sendBroadcast(i);
    }
    public void setNotification(Context context, double time) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_action_settings_input_hdmi)
                .setContentTitle("Charging postponed")
                .setContentText("Charging will start at " + Utility.timeToString(time));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, SwipeActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SwipeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Maybe use an ID in stead of 0 to build this notification
        mNotificationManager.notify(0, mBuilder.build());
    }
}
