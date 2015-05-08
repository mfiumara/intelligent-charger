package tum.ei.ics.intelligentcharger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import tum.ei.ics.intelligentcharger.adapter.EventAdapter;
import tum.ei.ics.intelligentcharger.entity.Event;
import tum.ei.ics.intelligentcharger.receiver.BatteryChangedReceiver;
import tum.ei.ics.intelligentcharger.service.BatteryService;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private static final String DEBUG_MESSAGES = "Debug";

    private static final BatteryChangedReceiver batteryReceiver = new BatteryChangedReceiver();
    private static Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lv = (ListView) findViewById(R.id.lvList);
        EventAdapter eventAdapter = new EventAdapter(this);
        List<Event> events = Event.listAll(Event.class);

        eventAdapter.setData(events);
        lv.setAdapter(eventAdapter);
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
        intent.setClass(this, BatteryService.class);
        startService(intent);
    }

    public void stopService(View view) {
        // Stop the service
        stopService(intent);
    }

    public void updateList(View view) {
        ListView lv = (ListView) findViewById(R.id.lvList);
        EventAdapter eventAdapter = new EventAdapter(this);

        List<Event> events = Event.listAll(Event.class);
        eventAdapter.setData(events);
        lv.setAdapter(eventAdapter);
    }

    public void processEvents(View view) {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public void exportCSV(View view) {

    }

}
