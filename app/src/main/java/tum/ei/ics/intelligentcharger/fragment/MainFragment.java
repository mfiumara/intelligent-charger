package tum.ei.ics.intelligentcharger.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.Utility;

/**
 * Created by mattia on 01.06.15.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "Fragment";
    private static final int URL_LOADER = 0;

    private static View rootView;
    private static TextView charge_time;
    private static TextView unplug_time;
    private static TextView min_soc;
    private static TextView max_soc;
    private static TextView charging;
    private static TextView plugged;
    private static SeekBar minSoc;
    private static SeekBar maxSoc;
    private static Switch smartCharge;

    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefEdit;

    private static PointsGraphSeries<DataPoint> current;

    public static MainFragment newInstance(int sectionNumber) { return new MainFragment(); }

    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get template view
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get textviews and seekbars
        charge_time = (TextView) rootView.findViewById(R.id.charge_time);
        unplug_time = (TextView) rootView.findViewById(R.id.unplug_time);
        min_soc = (TextView) rootView.findViewById(R.id.min_soc_tv);
        max_soc = (TextView) rootView.findViewById(R.id.max_soc_tv);
        charging = (TextView) rootView.findViewById(R.id.charging);
        plugged = (TextView) rootView.findViewById(R.id.plugged);

        minSoc = (SeekBar) rootView.findViewById(R.id.min_soc_bar);
        maxSoc = (SeekBar) rootView.findViewById(R.id.max_soc_bar);

        smartCharge = (Switch) rootView.findViewById(R.id.smart_charge);

        // Open shared preference file
        prefs = this.getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        prefEdit = prefs.edit();

        // UI Onchange Listeners
        smartCharge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefEdit.putBoolean(getString(R.string.smart_charge), isChecked);
                prefEdit.apply();
            }
        });
        minSoc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int minimumSoc;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minimumSoc = progress;
                min_soc.setText(Integer.toString(minimumSoc) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.v(TAG, Integer.toString(minimumSoc));
                prefEdit.putInt(getString(R.string.min_soc), minimumSoc);
                prefEdit.apply();
                min_soc.setText(Integer.toString(minimumSoc) + "%");
            }
        });
        maxSoc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int maximumSoc;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maximumSoc = progress;
                max_soc.setText(Integer.toString(maximumSoc) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.v(TAG, Integer.toString(maximumSoc));
                prefEdit.putInt(getString(R.string.max_soc), maximumSoc);
                prefEdit.apply();
                max_soc.setText(Integer.toString(maximumSoc) + "%");
            }
        });

        updateData(getActivity());

        return rootView;
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
        // Populate textviews with relevant development data
        charge_time.setText((Utility.timeToString(prefs.getFloat(context.getString(R.string.charge_time), 0.0f))));
        unplug_time.setText((Utility.timeToString(prefs.getFloat(context.getString(R.string.unplug_time), 0.0f))));
        min_soc.setText((prefs.getInt(context.getString(R.string.min_soc), 0)) + "%");
        max_soc.setText((prefs.getInt(context.getString(R.string.max_soc), 0)) + "%");

        // Populate seekbar with relevant development data
        maxSoc.setProgress(prefs.getInt(context.getString(R.string.max_soc), 100));
        minSoc.setProgress(prefs.getInt(context.getString(R.string.min_soc), 100));

        // Set smart charging on or off
        smartCharge.setChecked(prefs.getBoolean(context.getString(R.string.smart_charge), true));

        // TODO: Set charging / discharging status
    }

}

