package tum.ei.ics.intelligentcharger;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import tum.ei.ics.intelligentcharger.receiver.BatteryChangedReceiver;
import tum.ei.ics.intelligentcharger.service.BatteryService;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private static final BatteryChangedReceiver batteryReceiver = new BatteryChangedReceiver();
    private static Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startService(View view) {
        // Start the service
        intent = new Intent(this, BatteryService.class);
        startService(intent);
    }

    public void stopService(View view) {
        // Stop the service
        stopService(intent);
    }

    public void startReceiver(View view) {
        this.registerReceiver(batteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Toast.makeText(getApplicationContext(), "Receiver started", Toast.LENGTH_SHORT).show();
    }

    public void stopReceiver(View view) {
        try {
            this.unregisterReceiver(batteryReceiver);
            Toast.makeText(getApplicationContext(), "Receiver stopped", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), "Receiver already stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateList(View view) {
        // Open Database
        BatteryDataDbHelper mDbHelper = new BatteryDataDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + BatteryDataContract.BatteryDataEntry.TABLE_NAME + " ORDER BY " + BatteryDataContract.BatteryDataEntry.COLUMN_NAME_DATETIME + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        String[] fromColumns = {BatteryDataContract.BatteryDataEntry.COLUMN_NAME_STATUS,
                BatteryDataContract.BatteryDataEntry.COLUMN_NAME_LEVEL};
        int[] toViews = {R.id.tvLevel, R.id.tvStatus};

        // Different adapters
        BatteryDataAdapter batterydataAdapter = new BatteryDataAdapter(this, cursor);

        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex("name")));
            cursor.moveToNext();
        }


        ListView lv = (ListView) findViewById(R.id.lvList);
        lv.setAdapter(batterydataAdapter);

/*
        // do stuff
        BatteryDataDbHelper mDbHelper = new BatteryDataDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + BatteryDataContract.BatteryDataEntry.TABLE_NAME;
        Cursor c = db.rawQuery(query, null);

        ListView lvItems = (ListView) findViewById(R.id.lvList);



        BatteryDataAdapter batterydataAdapter = new BatteryDataAdapter(this, c);
        lvItems.setAdapter(batterydataAdapter);
//        batterydataAdapter.changeCursor(c);
*/
        cursor.close();
    }
}
