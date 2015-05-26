package tum.ei.ics.intelligentcharger.entity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import tum.ei.ics.intelligentcharger.R;

/**
 * Created by mattia on 26.05.15.
 */
public class Battery {
    private Integer status;
    private Integer level;
    private Integer voltage;
    private Integer temperature;
    private Integer plugged;

    private boolean isCharging;
    private boolean isFull;

    private String chargingStatus;

    public Battery() {}

    public Battery(Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, intentFilter);
        this.status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        this.level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,  -1);
        this.temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        this.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        this.plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        boolean usbCharge = plugged == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = plugged == BatteryManager.BATTERY_PLUGGED_AC;
        this.isFull = level == 100;

        this.isCharging =   status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL ||
                usbCharge || acCharge;

        // Check if the phone is either charging or discharging using all information available.
        // The app considers 100% SOC as charging, as this means it is not yet the end of
        // the charge cycle.
        this.chargingStatus = (isFull || isCharging)
                ? context.getString(R.string.charging) : context.getString(R.string.discharging);
    }

    public String getChargingStatus() {
        return chargingStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getVoltage() {
        return voltage;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public Integer getPlugged() {
        return plugged;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public boolean isFull() {
        return isFull;
    }


}
