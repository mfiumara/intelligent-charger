package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import tum.ei.ics.intelligentcharger.R;
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
        boolean isCharging = currStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                currStatus == BatteryManager.BATTERY_STATUS_FULL;
        boolean isFull = currLevel == 100;
        int currPlugtype = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = currPlugtype == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = currPlugtype == BatteryManager.BATTERY_PLUGGED_AC;
        String customStatus = (currLevel == 100 || isCharging) ? "Charging" : "Discharging";

/*
        // Get last event battery info
        int prevStatus = prefs.getInt(context.getString(R.string.status), -1);
        int prevLevel = prefs.getInt(context.getString(R.string.level), -1);
        int prevTemp = prefs.getInt(context.getString(R.string.temperature), -1);
        int prevVoltage = prefs.getInt(context.getString(R.string.voltage), -1);
        int prevPlugtype = prefs.getInt(context.getString(R.string.plugtype), -1);


        // Determine whether or not to save a cycle
        if (currLevel != 100 && isCharging) {
            // Plug-in event
            Event pluginEvent = new Event(currStatus, currPlugtype, currLevel, currVoltage, currTemp, customStatus);
        }


        if (prevLevel != -1) {
            // No previous cycle yet.
        } else {

        }
*/
        // Save event to SQL database and update last event battery info
        Event event = new Event(currStatus, currPlugtype, currLevel, currVoltage, currTemp, customStatus);
        event.save();

        prefEdit.putInt(context.getString(R.string.status), currStatus);
        prefEdit.putInt(context.getString(R.string.level), currLevel);
        prefEdit.putInt(context.getString(R.string.temperature), currTemp);
        prefEdit.putInt(context.getString(R.string.voltage), currVoltage);
        prefEdit.putInt(context.getString(R.string.plugtype), currPlugtype);
        prefEdit.putBoolean(context.getString(R.string.is_charging), isCharging);
        prefEdit.putBoolean(context.getString(R.string.is_full), isFull);
        prefEdit.putString(context.getString(R.string.custom_status), customStatus);
        prefEdit.commit();
    }
}
