package tum.ei.ics.intelligentcharger.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.Utility;

/**
 * Created by mattia on 01.06.15.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "Fragment";
    private static final int URL_LOADER = 0;

    public static MainFragment newInstance(int sectionNumber) { return new MainFragment(); }

    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get template view
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get textviews
        TextView charge_time = (TextView) rootView.findViewById(R.id.charge_time);
        TextView unplug_time = (TextView) rootView.findViewById(R.id.unplug_time);
        TextView min_soc = (TextView) rootView.findViewById(R.id.min_soc);
        TextView max_soc = (TextView) rootView.findViewById(R.id.max_soc);

        TextView charging = (TextView) rootView.findViewById(R.id.charging);
        TextView plugged = (TextView) rootView.findViewById(R.id.plugged);

        // Open shared preference file
        SharedPreferences prefs = this.getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Populate textviews with relevant development data
        charge_time.setText((Utility.timeToString(prefs.getFloat(getString(R.string.charge_time), 0.0f))));
        unplug_time.setText((Utility.timeToString(prefs.getFloat(getString(R.string.unplug_time), 0.0f))));
        min_soc.setText((prefs.getInt(getString(R.string.min_soc), 0)) + "%");
        max_soc.setText((prefs.getInt(getString(R.string.max_soc), 0)) + "%");

        return rootView;
    }
}
