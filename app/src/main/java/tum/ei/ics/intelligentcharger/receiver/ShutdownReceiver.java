package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mattia on 18.05.15.
 */
public class ShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /*// Save charge event
        // Open shared preference file
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();

        // Get current battery info
        Battery battery = new Battery(context);

        // Check if the phone is either charging or discharging using all information available.
        // The app considers 100% SOC as charging, as this means it is not yet the end of
        // the charge cycle.
        String currCustomStatus = (battery.isFull() || battery.isCharging())
                ? context.getString(R.string.charging) : context.getString(R.string.discharging);

        // Create current event
        Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
        ConnectionEvent currEvent = new ConnectionEvent(battery.getStatus(),
                battery.getPlugged(), battery.getLevel(), battery.getVoltage(),
                battery.getTemperature(), battery.getChargingStatus());
        // Save event to database
        currEvent.save();
        prefEdit.putLong(context.getString(R.string.shutdown_cycle_id), currEvent.getId());*/
    }

}
