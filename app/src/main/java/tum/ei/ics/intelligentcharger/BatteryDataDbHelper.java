package tum.ei.ics.intelligentcharger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mattia on 30.04.15.
 */
public class BatteryDataDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BatteryData.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BatteryDataContract.BatteryDataEntry.TABLE_NAME + " (" +
                    BatteryDataContract.BatteryDataEntry._ID + " INTEGER PRIMARY KEY," +
                    BatteryDataContract.BatteryDataEntry.COLUMN_NAME_DATETIME + TEXT_TYPE + COMMA_SEP +
                    BatteryDataContract.BatteryDataEntry.COLUMN_NAME_LEVEL + INT_TYPE + COMMA_SEP +
                    BatteryDataContract.BatteryDataEntry.COLUMN_NAME_STATUS + TEXT_TYPE + COMMA_SEP +
                    BatteryDataContract.BatteryDataEntry.COLUMN_NAME_PLUGGED + TEXT_TYPE + COMMA_SEP +
                    BatteryDataContract.BatteryDataEntry.COLUMN_NAME_VOLTAGE + INT_TYPE + COMMA_SEP +
                    BatteryDataContract.BatteryDataEntry.COLUMN_NAME_TEMPERATURE + INT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BatteryDataContract.BatteryDataEntry.TABLE_NAME;

    public BatteryDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
//        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
