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
import android.widget.SeekBar;
import android.widget.TextView;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.Utility;

/**
 * Created by mattia on 01.06.15.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "Fragment";
    private static final int URL_LOADER = 0;

    TextView charge_time;
    TextView unplug_time;
    TextView min_soc;
    TextView max_soc;
    TextView charging;
    TextView plugged;
    SeekBar minSoc;
    SeekBar maxSoc;

    SharedPreferences prefs;

    public static MainFragment newInstance(int sectionNumber) { return new MainFragment(); }

    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get template view
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get textviews and seekbars
        charge_time = (TextView) rootView.findViewById(R.id.charge_time);
        unplug_time = (TextView) rootView.findViewById(R.id.unplug_time);
        min_soc = (TextView) rootView.findViewById(R.id.min_soc_tv);
        max_soc = (TextView) rootView.findViewById(R.id.max_soc_tv);
        charging = (TextView) rootView.findViewById(R.id.charging);
        plugged = (TextView) rootView.findViewById(R.id.plugged);

        minSoc = (SeekBar) rootView.findViewById(R.id.min_soc_bar);
        maxSoc = (SeekBar) rootView.findViewById(R.id.max_soc_bar);

        // Open shared preference file
        prefs = this.getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Populate textviews with relevant development data
        charge_time.setText((Utility.timeToString(prefs.getFloat(getString(R.string.charge_time), 0.0f))));
        unplug_time.setText((Utility.timeToString(prefs.getFloat(getString(R.string.unplug_time), 0.0f))));
        min_soc.setText((prefs.getInt(getString(R.string.min_soc), 0)) + "%");
        max_soc.setText((prefs.getInt(getString(R.string.max_soc), 0)) + "%");

        // Populate seekbar with relevant development data
        maxSoc.setProgress(prefs.getInt(getString(R.string.max_soc), 100));
        minSoc.setProgress(prefs.getInt(getString(R.string.min_soc), 100));

        return rootView;
    }

    public static class updateView extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: Update all fields, do this for all fragments!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! YOU KNOW I AM DONE WITH PROGRAMMING FOR TODAY
        }
    }
}

