package tum.ei.ics.intelligentcharger.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.SwipeActivity;

/**
 * Created by mattia on 02.06.15.
 */
public class StartChargeReceiver extends BroadcastReceiver {

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
    }



}
