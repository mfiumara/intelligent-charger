package tum.ei.ics.intelligentcharger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.BatteryManager;
import android.content.BroadcastReceiver;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

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
//        stopService(intent);

    }

    public void startReceiver(View view) {
        this.registerReceiver(batteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void stopReceiver(View view) {
        try {
            this.unregisterReceiver(batteryReceiver);
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), "Receiver already stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public void addStatus(View view) {
        // do stuff
        BatteryDataDbHelper mDbHelper = new BatteryDataDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + BatteryDataContract.BatteryDataEntry.TABLE_NAME;
        Cursor c = db.rawQuery(query, null);

        ListView lvItems = (ListView) findViewById(R.id.lvList);

        //SimpleCursorAdapter simpleAdapter = new SimpleCursorAdapter(

        BatteryDataAdapter batterydataAdapter = new BatteryDataAdapter(this, c);
        lvItems.setAdapter(batterydataAdapter);
//        batterydataAdapter.changeCursor(c);

    }
}
