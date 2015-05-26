package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Battery;
import tum.ei.ics.intelligentcharger.entity.ChargeCurve;

/**
 * Created by mattia on 30.04.15.
 */
public class BatteryChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "BatteryChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Battery battery = new Battery(context);

        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();

        Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
        ChargeCurve chargeCurve = ChargeCurve.findById(ChargeCurve.class, curveID);
/*

        // Only save event if charging
        if (isCharging) {
            Event event = new Event(currStatus, currPlugtype, currLevel, currVoltage,
                    currTemp, currCustomStatus, chargeCurve);
            event.save();
        } else {
//            context.stopService(intent);
        }
*/
    }
}
