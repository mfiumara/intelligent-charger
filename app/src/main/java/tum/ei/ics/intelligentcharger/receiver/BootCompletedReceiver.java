package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import tum.ei.ics.intelligentcharger.service.BatteryService;

/**
 * Created by mattia on 05.05.15.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startIntent = new Intent(context, BatteryService.class);
        context.startService(startIntent);
        Toast.makeText(context, "Battery Logging Started", Toast.LENGTH_SHORT).show();
    }
}
