package tum.ei.ics.intelligentcharger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import tum.ei.ics.intelligentcharger.service.BatteryService;

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

        //TODO: Check if charger was connected while the phone was off, register the event if so.

        // If poweroff event ~~~

    }
}
