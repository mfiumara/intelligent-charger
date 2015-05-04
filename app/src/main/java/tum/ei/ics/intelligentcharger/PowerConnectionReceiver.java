package tum.ei.ics.intelligentcharger;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by mattia on 30.04.15.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    public static final String TAG = "PowerConnectionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
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

        Toast.makeText(context, "Plugged: " + isCharging, Toast.LENGTH_SHORT).show();

        //TODO: Save battery information to SQLite database
        BatteryDataDbHelper mDbHelper = new BatteryDataDbHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Calendar datetime = Calendar.getInstance();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BatteryDataContract.BatteryDataEntry.COLUMN_NAME_DATETIME, datetime.toString());
        values.put(BatteryDataContract.BatteryDataEntry.COLUMN_NAME_LEVEL, level);
        values.put(BatteryDataContract.BatteryDataEntry.COLUMN_NAME_STATUS, status);
        values.put(BatteryDataContract.BatteryDataEntry.COLUMN_NAME_PLUGGED, chargePlug);
        values.put(BatteryDataContract.BatteryDataEntry.COLUMN_NAME_TEMPERATURE, temperature);
        values.put(BatteryDataContract.BatteryDataEntry.COLUMN_NAME_VOLTAGE, voltage);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(BatteryDataContract.BatteryDataEntry.TABLE_NAME, "null", values);

    }
}
