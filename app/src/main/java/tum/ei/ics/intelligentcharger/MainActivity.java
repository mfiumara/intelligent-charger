package tum.ei.ics.intelligentcharger;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

import tum.ei.ics.intelligentcharger.adapter.CurveEventAdapter;
import tum.ei.ics.intelligentcharger.adapter.CycleAdapter;
import tum.ei.ics.intelligentcharger.adapter.EventAdapter;
import tum.ei.ics.intelligentcharger.entity.CurveEvent;
import tum.ei.ics.intelligentcharger.entity.Cycle;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.updateList();
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

    public void updateList() {
        // Use for showing Cycle events in list form
        ListView lv = (ListView) findViewById(R.id.lvList);
        final CycleAdapter cycleAdapter = new CycleAdapter(this);
        List<Cycle> cycles = Cycle.listAll(Cycle.class);
        cycleAdapter.setData(cycles);
        lv.setAdapter(cycleAdapter);

        // Use for showing curve events in list form
/*        ListView lv = (ListView) findViewById(R.id.lvList);
        final CurveEventAdapter curveEventAdapter = new CurveEventAdapter(this);
        List<CurveEvent> events = CurveEvent.listAll(CurveEvent.class);
        curveEventAdapter.setData(events);
        lv.setAdapter(curveEventAdapter);*/

        // Use for showing chargepoints in list form
/*        ListView lv = (ListView) findViewById(R.id.lvList);
        final CurveEventAdapter curveEventAdapter = new CurveEventAdapter(this);
        List<CurveEvent> events = CurveEvent.listAll(CurveEvent.class);
        curveEventAdapter.setData(events);
        lv.setAdapter(curveEventAdapter);*/
    }

    public void updateList(View view) { updateList(); } // I know it's ugly...

}
