package tum.ei.ics.intelligentcharger.receiver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.SwipeActivity;
import tum.ei.ics.intelligentcharger.bluetooth.BleService;

/**
 * Created by mattia on 02.06.15.
 */
public class StartChargeReceiver extends BroadcastReceiver {

    private static final String TAG = "StartChargeReceiver";

    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEdit;

    private BleService m_oBluetoothLeService;
    private ServiceConnection m_oServiceConnection;
    private BroadcastReceiver m_oGattUpdateReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_action_settings_input_hdmi)
                        .setContentTitle("Started charging")
                        .setContentText("");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, SwipeActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SwipeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Maybe use an ID in stead of 0 to build this notification
        mNotificationManager.notify(0, mBuilder.build());

        prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        prefEdit = prefs.edit();

        // Start alarm to record the charge curve every X minutes
        Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);
        prefEdit.putLong(context.getString(R.string.curve_id), ++curveID);

        // Setup the alarm to record the charge curve
        Intent i = new Intent(context, BatteryChangedReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Trigger the alarm for the amount of minutes defined
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), 1000 * 60 * Global.ALARM_FREQUENCY, pendingIntent);

        prefEdit.apply();

        // TODO: Connect to bluetooth and send start charge command, binding a service is not allowed within broadcast receiver
        startBleService(context, prefs.getString(Global.AUTOCONNECT_BLE_DEVICEADDRESS, ""), prefs.getString(Global.AUTOCONNECT_BLE_DEVICENAME,""));
    }
    public boolean startBleService(Context context, final String address, final String deviceName) {
        m_oServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                m_oBluetoothLeService = ((BleService.LocalBinder) service).getService();
                if(!m_oBluetoothLeService.initialize()) {
                    Log.e("BLE", "Unable to initialize Bluetooth");
                }
                Log.d(TAG, "connect to ble service");
                m_oBluetoothLeService.connect(address, deviceName);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                m_oBluetoothLeService = null;
            }
        };

        m_oGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if(BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    startBleService(context, address, deviceName);
                }
            }
        };

        Intent gattServiceIntent = new Intent(context, BleService.class);
        context.startService(gattServiceIntent);
//        context.bindService(gattServiceIntent, m_oServiceConnection, context.BIND_AUTO_CREATE);
//        context.registerReceiver(m_oGattUpdateReceiver, makeGattUpdateIntentFilter());

        return true;
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }


}
