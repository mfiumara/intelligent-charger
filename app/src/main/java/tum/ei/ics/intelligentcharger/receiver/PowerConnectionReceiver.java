package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.adapter.EventAdapter;
import tum.ei.ics.intelligentcharger.entity.Event;

/**
 * Created by mattia on 07.05.15.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    public static final String TAG = "PowerConnectedReceiver";

    Bundle bundle = new Bundle();

    @Override
    public void onReceive(Context context, Intent intent) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        intent = context.registerReceiver(null, ifilter);

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,  -1);
        int scale  = intent.getIntExtra(BatteryManager.EXTRA_SCALE,  -1);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        Event event = new Event(status, chargePlug, level, voltage, temperature);
        event.save();
    }
}
