package tum.ei.ics.intelligentcharger.fragment;

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
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Cycle;

/**
 * Created by mattia on 01.06.15.
 */
public class CycleFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "Fragment";
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CycleFragment newInstance(int sectionNumber) {
        CycleFragment fragment = new CycleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public CycleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState == null) { savedInstanceState = this.getArguments(); }

        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        update(rootView);
        return rootView;
    }

    public void update(View view) {
        // Get all charge cycles
        List<Cycle> cycles = Cycle.listAll(Cycle.class);
        Integer N = cycles.size();
        DataPoint[] values = new DataPoint[N];
        for (int i = 0; i < N; i++) {
            values[i] = new DataPoint(cycles.get(i).getPluginEvent().getTime(),
                    cycles.get(i).getPlugoutEvent().getTime());
        }
        // Add them to the graph view
        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        PointsGraphSeries<DataPoint> cyclePoints = new PointsGraphSeries<DataPoint>(values);
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

        // TODO: Plot the unplug predictor
    }
}
