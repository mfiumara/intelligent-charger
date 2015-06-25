package tum.ei.ics.intelligentcharger.predictor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Cycle;

/**
 * Created by mattia on 08.06.15.
 */
public class TargetSOCPredictor {
    public List<Cycle> inputCycles = new ArrayList<Cycle>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEdit;

    private Integer maxError;
    private Integer maxSOC;

    public TargetSOCPredictor(Context context, List<Cycle> cycles, Integer batchSize) {
        prefs = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        prefEdit = prefs.edit();

        Integer N = cycles.size();
        maxSOC = prefs.getInt(context.getString(R.string.max_soc), 100);
        maxError = prefs.getInt(context.getString(R.string.max_error), Global.ERROR_MARGIN);
        Integer error = 0;

        if (N > batchSize) {
            for (int i = N - 1; i > N - 1 - batchSize; i--) { inputCycles.add(cycles.get(i)); }
            Integer M = inputCycles.size();
            Cycle previousCycle = inputCycles.get(0);
            int[] deltaSOCArray = new int[M - 1];
            for (int i =1; i < M; i++) {
                Cycle currentCycle = inputCycles.get(i);
                int deltaSOC = currentCycle.getPlugoutEvent().getLevel() - previousCycle.getPluginEvent().getLevel();
                deltaSOCArray[i - 1] = deltaSOC;
                previousCycle = inputCycles.get(i);
            }

            // Calculate maximum error in last batchSize number of cycles and correct with maximum error last recorded
            //error = Ints.max(deltaSOCArray) - maxSOC;
            maxSOC = Ints.max(deltaSOCArray) + maxError;
            // Clip maxSOC between 0 and 100%
            if (maxSOC >= 100) {
                maxSOC = 100;
            } else if (maxSOC <= 0) {
                maxSOC = 0;
            }
            //maxError = error > 0 ? error : maxError;

            prefEdit.putInt(context.getString(R.string.max_soc), maxSOC);
            prefEdit.putInt(context.getString(R.string.max_error), maxError);
        }
        prefEdit.apply();
    }

    public Integer predict() {
        return maxSOC;
    }

}
