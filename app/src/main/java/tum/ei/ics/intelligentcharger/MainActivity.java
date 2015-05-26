package tum.ei.ics.intelligentcharger;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import tum.ei.ics.intelligentcharger.adapter.CycleAdapter;
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
        ListView lv = (ListView) findViewById(R.id.lvList);
        final CycleAdapter cycleAdapter = new CycleAdapter(this);
        List<Cycle> cycles = Cycle.listAll(Cycle.class);
        cycleAdapter.setData(cycles);
        lv.setAdapter(cycleAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: Set click method for cycle items so the user can erase useless cycles.
            }
        });
    }

    public void updateList(View view) { updateList(); } // I know it's ugly...

}
