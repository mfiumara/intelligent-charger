package tum.ei.ics.intelligentcharger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import tum.ei.ics.intelligentcharger.adapter.EventAdapter;
import tum.ei.ics.intelligentcharger.entity.Event;
import tum.ei.ics.intelligentcharger.receiver.BatteryChangedReceiver;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    private static final BatteryChangedReceiver batteryReceiver = new BatteryChangedReceiver();
    private static Intent intent = new Intent();

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lv = (ListView) findViewById(R.id.lvList);
        EventAdapter eventAdapter = new EventAdapter(this);
        List<Event> events = Event.listAll(Event.class);

        eventAdapter.setData(events);
        lv.setAdapter(eventAdapter);

        SharedPreferences prefs = this.getSharedPreferences(
                this.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String customStatus = prefs.getString(this.getString(R.string.custom_status), "");
        TextView tv = (TextView) findViewById(R.id.tv_status);
        tv.setText(customStatus);
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

    public void clearEvents(View view) {
        Event.deleteAll(Event.class);
        this.updateList(view);

        SharedPreferences prefs = this.getSharedPreferences(
                this.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();
        prefEdit.putString(this.getString(R.string.custom_status), "");
        TextView tv = (TextView) findViewById(R.id.tv_status);
        tv.setText("NA");
    }

    public void updateList(View view) {
        ListView lv = (ListView) findViewById(R.id.lvList);
        EventAdapter eventAdapter = new EventAdapter(this);
        List<Event> events = Event.listAll(Event.class);
        eventAdapter.setData(events);
        lv.setAdapter(eventAdapter);

        SharedPreferences prefs = this.getSharedPreferences(
                this.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String customStatus = prefs.getString(this.getString(R.string.custom_status), "");
        TextView tv = (TextView) findViewById(R.id.tv_status);
        tv.setText(customStatus);
    }

}
