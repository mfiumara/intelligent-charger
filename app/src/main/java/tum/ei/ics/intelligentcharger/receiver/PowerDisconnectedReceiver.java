package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

/**
 * Created by mattia on 04.05.15.
 */
public class PowerDisconnectedReceiver extends BroadcastReceiver {

    public static final String TAG = "PowerDisconnectedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        String text = isCharging ? "Plugged" : "Unplugged";
  //      Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

    }
}