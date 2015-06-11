package tum.ei.ics.intelligentcharger.receiver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.SwipeActivity;

/**
 * Created by mattia on 02.06.15.
 */
public class StartChargeReceiver extends BroadcastReceiver {
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEdit;
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

        // TODO: Connect to bluetooth and send start charge command
    }


}
