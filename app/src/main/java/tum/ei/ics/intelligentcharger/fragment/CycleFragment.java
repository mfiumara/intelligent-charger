package tum.ei.ics.intelligentcharger.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.Utility;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import tum.ei.ics.intelligentcharger.predictor.UnplugTimePredictor;

/**
 * Created by mattia on 01.06.15.
 */
public class CycleFragment extends Fragment {

    private static View rootView;
    private static GraphView graph;
    private static PointsGraphSeries<DataPoint> cyclePoints;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "Fragment";

    public static CycleFragment newInstance(int sectionNumber) {
        CycleFragment fragment = new CycleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public CycleFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState == null) { savedInstanceState = this.getArguments(); }

        rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        updateView();
        return rootView;
    }

    public void updateView() {
        // Get all charge cycles
        List<Cycle> cycles = Cycle.listAll(Cycle.class);
        Integer N = cycles.size();
        DataPoint[] values = new DataPoint[N];
        for (int i = 0; i < N; i++) {
            values[i] = new DataPoint(cycles.get(i).getPluginEvent().getTime(),
                    cycles.get(i).getPlugoutEvent().getTime());
        }
        // Add them to the graph view
        graph = (GraphView) rootView.findViewById(R.id.graph);
        cyclePoints = new PointsGraphSeries<DataPoint>(values);
        graph.addSeries(cyclePoints);
        // Format axes
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxX(24);
        graph.getViewport().setMaxY(24);

        // Format the points
//        cyclePoints.setShape(PointsGraphSeries.Shape.POINT);
        cyclePoints.setCustomShape(new PointsGraphSeries.CustomShape() {
            @Override
            public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                paint.setStrokeWidth(10);
                canvas.drawPoint(x, y, paint);
            }
        });
        cyclePoints.setColor(Color.BLUE);
        // custom label formatter to show hourly format
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                return super.formatLabel(value, isValueX) + ":00";
            }
        });
        graph.setTitle("Plug-in Time vs Plug-out Time");

        // Plot predictor
        UnplugTimePredictor unplugTimePredictor = new UnplugTimePredictor(Cycle.listAll(Cycle.class));
        float shift = unplugTimePredictor.getShift();

        DataPoint[] shiftPoints = new DataPoint[25];
        DataPoint[] unplugTimes = new DataPoint[25];
        for (int i = 0; i <= 24; i++) {
            double unplugTime = unplugTimePredictor.predict(i);
            unplugTimes[i] = new DataPoint(i, unplugTime);
            shiftPoints[i] = new DataPoint(shift, i);
        }
        LineGraphSeries<DataPoint> unplugCurve = new LineGraphSeries<>(unplugTimes);
        unplugCurve.setColor(Color.BLUE);
        unplugCurve.setThickness(8);
        graph.addSeries(unplugCurve);

        // Plot shifted time
/*        LineGraphSeries<DataPoint> shiftLine = new LineGraphSeries<>(shiftPoints);
        shiftLine.setColor(Color.GREEN);
        shiftLine.setThickness(8);
        graph.addSeries(shiftLine);*/
    }
    public static class updateView extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (rootView != null) {
                updateData(context);
            }
        }
    }

    public static void updateData(Context context) {
        // TODO: Debug this
        // Populate graph
        List<Cycle> cycles = Cycle.listAll(Cycle.class);
        Integer N = cycles.size();
        DataPoint[] values = new DataPoint[N];
        for (int i = 0; i < N; i++) {
            values[i] = new DataPoint(cycles.get(i).getPluginEvent().getTime(),
                    cycles.get(i).getPlugoutEvent().getTime());
        }
        cyclePoints.resetData(values);
    }
}
