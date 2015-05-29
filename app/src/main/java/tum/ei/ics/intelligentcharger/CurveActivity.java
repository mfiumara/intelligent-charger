package tum.ei.ics.intelligentcharger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.List;

import tum.ei.ics.intelligentcharger.entity.ChargePoint;
import tum.ei.ics.intelligentcharger.entity.Cycle;


/**
 * Created by mattia on 27.05.15.
 */
public class CurveActivity extends ActionBarActivity {

    private static final String TAG = "GraphActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        this.updateGraph();
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

    public void updateGraph() {
        // Get all charge cycles
        List<ChargePoint> cycles = ChargePoint.listAll(ChargePoint.class);
        Integer N = cycles.size();
        if (N > 1) {
            DataPoint[] values = new DataPoint[N];
            for (int i = 0; i < N; i++) {
                values[i] = new DataPoint(cycles.get(i).getTime(),
                        cycles.get(i).getLevel());
            }
            // Add them to the graph view
            GraphView graph = (GraphView) findViewById(R.id.graph);
            PointsGraphSeries<DataPoint> cyclePoints = new PointsGraphSeries<DataPoint>(values);
            graph.addSeries(cyclePoints);
            // Format axes
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinX(-5);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxX(0);
            graph.getViewport().setMaxY(100);
            // Format the points
            cyclePoints.setCustomShape(new PointsGraphSeries.CustomShape() {
                @Override
                public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                    paint.setStrokeWidth(10);
                    canvas.drawPoint(x, y, paint);
                }
            });
            if (cycles.get(0).getPlugType() == BatteryManager.BATTERY_PLUGGED_AC) {
                cyclePoints.setColor(Color.RED);
            } else {
                cyclePoints.setColor(Color.GREEN);
            }

        }
    }

    public void updateGraph(View view) { updateGraph(); } // I know it's ugly...

}
