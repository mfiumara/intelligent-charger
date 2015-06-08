package tum.ei.ics.intelligentcharger.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.ChargePoint;
import tum.ei.ics.intelligentcharger.predictor.ChargeTimePredictor;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddExpression;

/**
 * Created by mattia on 01.06.15.
 */
public class ChargeCurveFragment extends Fragment {
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
    public static ChargeCurveFragment newInstance(int sectionNumber) {
        ChargeCurveFragment fragment = new ChargeCurveFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ChargeCurveFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState == null) { savedInstanceState = this.getArguments(); }

        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);

        // Use this to clean up charge curve of wrongly saved chargepoints
        // Should not be needed in final version
/*        List<ChargePoint> filter = ChargePoint.find(ChargePoint.class, "time < ?", "-15");
        for (ChargePoint point : filter) {
            point.delete();
        }*/

        plotChargePoints(rootView);
        return rootView;
    }

    public void plotChargePoints(View view) {
        // Get all unique charge curve IDs
        List<ChargePoint> curveIDs = ChargePoint.findWithQuery(ChargePoint.class, "Select Distinct curve_id from charge_point");
        Integer N = curveIDs.size();

        GraphView graph = (GraphView) view.findViewById(R.id.graph);

        if (N > 1) {
            for (ChargePoint curve : curveIDs) {
                // Get all charge cycles
                List<ChargePoint> chargePoints = ChargePoint.find(ChargePoint.class, "curve_id = ?", Long.toString(curve.getCurveID()));
                Integer M = chargePoints.size();
                DataPoint[] values = new DataPoint[M];
                for (int i = 0; i < M; i++) {
                    values[i] = new DataPoint(chargePoints.get(i).getTime(),
                            chargePoints.get(i).getLevel());
                }
                // Add them to the graph view
                LineGraphSeries<DataPoint> cyclePoints = new LineGraphSeries<DataPoint>(values);
                Paint paint = new Paint();
                if (chargePoints.get(0).getPlugType() == BatteryManager.BATTERY_PLUGGED_AC) {
                    paint.setColor(Color.BLUE);
                } else {
                    paint.setColor(Color.RED);
                }
                paint.setStyle(Paint.Style.STROKE); paint.setAlpha(50); paint.setStrokeWidth(5);
                cyclePoints.setCustomPaint(paint);
                graph.addSeries(cyclePoints);
            }

            // Format axes
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(-6);
            graph.getViewport().setMaxX(0);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(100);

            // Format labels
            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values
                        return super.formatLabel(-1 * value, isValueX) + ":00";
                    } else {
                        // show currency for y values
                        return super.formatLabel(value, isValueX) + "%";
                    }
                }
            });

            ChargeTimePredictor USBChargeTimePredictor =
                    new ChargeTimePredictor(ChargePoint.find(ChargePoint.class, "plug_type = ?",
                            Integer.toString(BatteryManager.BATTERY_PLUGGED_USB)));
            ChargeTimePredictor ACChargeTimePredictor =
                    new ChargeTimePredictor(ChargePoint.find(ChargePoint.class, "plug_type = ?",
                            Integer.toString(BatteryManager.BATTERY_PLUGGED_AC)));

            // Plot charge prediction
            DataPoint[] USBPoints = new DataPoint[101];
            DataPoint[] ACPoints = new DataPoint[101];
            for (int i = 0; i <= 100; i++) {
                USBPoints[i] = new DataPoint(-USBChargeTimePredictor.predict(i, 100), i);
                ACPoints[i] = new DataPoint(-ACChargeTimePredictor.predict(i, 100), i);
            }
            // Add them to the graph view
            LineGraphSeries<DataPoint> USBCurve = new LineGraphSeries<>(USBPoints);
            LineGraphSeries<DataPoint> ACCurve = new LineGraphSeries<>(ACPoints);
            ACCurve.setColor(Color.BLUE);
            ACCurve.setThickness(8);
            graph.addSeries(ACCurve);
            USBCurve.setColor(Color.RED);
            USBCurve.setThickness(8);
            graph.addSeries(USBCurve);


            graph.setTitle("Charge Time vs Battery Level");
        }
    }
}
