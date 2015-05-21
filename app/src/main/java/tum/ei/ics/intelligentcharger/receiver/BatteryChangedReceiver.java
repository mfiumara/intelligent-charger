package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.ChargeCurve;
import tum.ei.ics.intelligentcharger.entity.Event;

/**
 * Created by mattia on 30.04.15.
 */
public class BatteryChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "BatteryChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
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

        String currCustomStatus = (isFull || isCharging)
                ? context.getString(R.string.charging) : context.getString(R.string.discharging);
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();
        Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
        ChargeCurve chargeCurve = ChargeCurve.findById(ChargeCurve.class, curveID);

        // Only save event if charging
        if (isCharging) {
            Event event = new Event(currStatus, currPlugtype, currLevel, currVoltage,
                    currTemp, currCustomStatus, chargeCurve);
            event.save();
        } else {
//            context.stopService(intent);
        }
    }
}
