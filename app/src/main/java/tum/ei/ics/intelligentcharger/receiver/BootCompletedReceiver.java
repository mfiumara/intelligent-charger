package tum.ei.ics.intelligentcharger.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Battery;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import tum.ei.ics.intelligentcharger.entity.CurveEvent;


/**
 * Created by mattia on 05.05.15.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get battery information
        Battery battery = new Battery(context);
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();

        // Delete start cycle if it exists
        Long startCycleID = prefs.getLong(context.getString(R.string.start_cycle_id), -1);
        if (startCycleID > 0) {
            ConnectionEvent event = ConnectionEvent.findById(ConnectionEvent.class, startCycleID);
            event.delete();
        }
        // Reset cycle information in shared preferences
        prefEdit.putLong(context.getString(R.string.start_cycle_id), -1);
        prefEdit.putLong(context.getString(R.string.end_cycle_id), -1);

        // Reset CurveEvents
        CurveEvent.deleteAll(CurveEvent.class);

        if (battery.isCharging()) {
            // Initialize new curveID
            Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
            prefEdit.putLong(context.getString(R.string.curve_id), ++curveID);

            // Start batterychanged alarm if charging to record new charge curve
            Intent i = new Intent(context, BatteryChangedReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000 * 60 * Global.ALARM_FREQUENCY, pendingIntent);
        }
        // Save new shared preferences
        prefEdit.apply();
    }
}
