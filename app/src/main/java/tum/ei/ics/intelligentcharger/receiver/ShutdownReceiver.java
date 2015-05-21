package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.ChargeCurve;
import tum.ei.ics.intelligentcharger.entity.Event;

/**
 * Created by mattia on 18.05.15.
 */
public class ShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Save charge event
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

        Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
        ChargeCurve chargeCurve = ChargeCurve.findById(ChargeCurve.class, curveID);
        Event currEvent = new Event(currStatus, currPlugtype, currLevel, currVoltage,
                currTemp, currCustomStatus, chargeCurve);
        // Save event to database
        currEvent.save();
        prefEdit.putLong(context.getString(R.string.shutdown_cycle_id), currEvent.getId());
    }

}
