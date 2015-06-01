package tum.ei.ics.intelligentcharger.fragment;

import android.graphics.Color;
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
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

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

        update(rootView);
        return rootView;
    }

    public void update(View view) {
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
                if (chargePoints.get(0).getPlugType() == BatteryManager.BATTERY_PLUGGED_AC) {
                    cyclePoints.setColor(Color.BLUE);
                } else {
                    cyclePoints.setColor(Color.GREEN);
                }
                cyclePoints.setThickness(8);
                graph.addSeries(cyclePoints);
                String plugged = chargePoints.get(0).getPlugType() == BatteryManager.BATTERY_PLUGGED_AC ? "AC" : "USB";
                Log.v(TAG, plugged);
            }

            // Format axes
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(-4);
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
        }
        double[] curvePrediction = plotChargePrediction(graph);
        graph.setTitle("Charge Time vs Battery Level");

    }
    public double[] plotChargePrediction(GraphView graph) {
        // Choose where to split our predictors
        double SPLIT = 75;
        // Create weka attributes
        Attribute LevelAttribute = new Attribute("Level");
        Attribute TimeAttribute= new Attribute("Time");

        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(LevelAttribute);
        attributeList.add(TimeAttribute);

        List<ChargePoint> chargePoints = ChargePoint.listAll(ChargePoint.class);

        Instances trainingSet = new Instances("Rel", attributeList, chargePoints.size());
        trainingSet.setClassIndex(1);

        // Add all chargepoints to training set
        for(ChargePoint chargePoint : chargePoints) {
            // TODO: Check for USB / AC charges and build two sepearte training sets
            // Create the weka instances
            Instance instance = new DenseInstance(2);
            instance.setValue((Attribute) attributeList.get(0), chargePoint.getLevel());
            instance.setValue((Attribute) attributeList.get(1), chargePoint.getTime());
            trainingSet.add(instance);
        }

        Classifier linearRegression = (Classifier) new LinearRegression();

        try {
            linearRegression.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the vector to be predicted.
        double[] predictionCurve = new double[101];

        for (int i = 0; i <= 100; i++) {
            Instance instance = new DenseInstance(2);
            instance.setDataset(trainingSet);
            instance.setValue((Attribute) attributeList.get(0), i);

            // Output the results
            try {
                // Do the actual prediction and transform back to an actual time
                double prediction = Math.abs(linearRegression.classifyInstance(instance));
                predictionCurve[i] = prediction;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DataPoint[] curveTime = new DataPoint[101];
        for (int i = 0 ; i <= 100; i++) {
            curveTime[i] = new DataPoint(-predictionCurve[i], i);
            Log.v(TAG, Integer.toString(i) + "%: " + String.valueOf(timeToString(predictionCurve[i])));
        }
        // Add them to the graph view
        LineGraphSeries<DataPoint> cyclePoints = new LineGraphSeries<DataPoint>(curveTime);
        cyclePoints.setColor(Color.GREEN);
        cyclePoints.setThickness(8);
        graph.addSeries(cyclePoints);

        return predictionCurve;
    }
    public String timeToString(double time) {
        int hours = (int) Math.floor(time) % 24;
        int minutes = (int) ((time - hours) * 60);
        String stringMinutes = minutes < 10 ? ("0" + Integer.toString(minutes)) : Integer.toString(minutes);
        return hours + ":" + stringMinutes;
    }
}
