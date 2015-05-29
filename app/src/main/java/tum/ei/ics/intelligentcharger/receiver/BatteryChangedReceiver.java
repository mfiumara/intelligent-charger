package tum.ei.ics.intelligentcharger.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Battery;
import tum.ei.ics.intelligentcharger.entity.ChargePoint;
import tum.ei.ics.intelligentcharger.entity.CurveEvent;

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
        Long curveID = prefs.getLong(context.getString(R.string.curve_id), -1);

        CurveEvent event = new CurveEvent (battery.getStatus(), battery.getPlugged(),
                battery.getLevel(), battery.getVoltage(),
                battery.getTemperature(), battery.getChargingStatus(),
                curveID);
        event.save();

        // Only save event if it is a full cycle
        if (battery.isFull()){
//        if (battery.getLevel() == 14){
            // Save this charge cycle into the curve map
            List<CurveEvent> events = CurveEvent.listAll(CurveEvent.class);
            CurveEvent lastEvent = event;
            float time = 0.0f;
            for (CurveEvent curveEvent : events) {
                time = curveEvent.getTime() < lastEvent.getTime() ?
                        - (lastEvent.getTime() - curveEvent.getTime()) :
                        curveEvent.getTime() - 24 - lastEvent.getTime();
                ChargePoint chargePoint = new ChargePoint(curveEvent.getPlugged(), time,
                        curveEvent.getLevel(), curveEvent.getVoltage(), curveID);
                chargePoint.save();
            }
            // Make sure the last point is 100% at time 0 to ensure alignment
            ChargePoint chargePoint = new ChargePoint(event.getPlugged(), 0.0f, 100, event.getVoltage(), curveID);
            // Clear CurveEvent Database
            CurveEvent.deleteAll(CurveEvent.class);

            // Stop recording the charge curve
            Intent i = new Intent(context, BatteryChangedReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
    }
}

