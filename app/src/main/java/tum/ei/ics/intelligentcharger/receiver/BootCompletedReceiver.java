package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;

/**
 * Created by mattia on 05.05.15.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Register plugging and unplugging events after reboot.
        PowerConnectionReceiver powerConnectionReceiver = new PowerConnectionReceiver();
        // Register one broadcast receiver for both plug-in and plug-out events
        IntentFilter powerConnected = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        IntentFilter powerDisconnected = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        context.registerReceiver(powerConnectionReceiver, powerConnected);
        context.registerReceiver(powerConnectionReceiver, powerDisconnected);

        // Get current battery info
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        intent = context.registerReceiver(null, intentFilter);
        int currStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int currLevel  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,  -1);
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
        String currCustomStatus = (isFull || isCharging)
                ? context.getString(R.string.charging) : context.getString(R.string.discharging);

        // Reset charge events in the case that the phone was (dis)connected while it was turned off
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();
        Long shutDownID = prefs.getLong(context.getString(R.string.shutdown_cycle_id), -1);
        if (shutDownID > 0) {
            ConnectionEvent shutDownEvent = ConnectionEvent.findById(ConnectionEvent.class, shutDownID);
            if (!shutDownEvent.getCustomStatus().equals(currCustomStatus)) {
                // Reset charge cycles in case events are different
                prefEdit.putLong(context.getString(R.string.start_cycle_id), -1);
                prefEdit.putLong(context.getString(R.string.end_cycle_id), -1);
                prefEdit.putLong(context.getString(R.string.shutdown_cycle_id), -1);
            }
        }
    }
}
