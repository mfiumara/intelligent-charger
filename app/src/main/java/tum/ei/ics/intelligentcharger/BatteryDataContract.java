package tum.ei.ics.intelligentcharger;

import android.provider.BaseColumns;

/**
 * Created by mattia on 30.04.15.
 */
public final class BatteryDataContract {

    public BatteryDataContract() {}

    public static abstract class BatteryDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_DATETIME = "datetime";
        public static final String COLUMN_NAME_LEVEL = "level";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_PLUGGED = "plugged";
        public static final String COLUMN_NAME_VOLTAGE = "voltage";
        public static final String COLUMN_NAME_TEMPERATURE = "temperature";
    }

}
