package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import tum.ei.ics.intelligentcharger.entity.Event;

/**
 * Created by mattia on 30.04.15.
 */
public class BatteryChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "BatteryChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,  -1);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        // Check if charging, if so, save events to database to use for charge curves

    }
}
