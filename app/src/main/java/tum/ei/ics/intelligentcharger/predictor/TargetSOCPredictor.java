package tum.ei.ics.intelligentcharger.predictor;

/**
 * Created by mattia on 08.06.15.
 */
public class TargetSOCPredictor {
    // TODO: Implement this estimator
    public TargetSOCPredictor() {
        Integer BATCH_SIZE = 10;
        float error = 0;
        float maxError = 0.2f;

        // Get last X results
   /* List<Cycle> cycles = Cycle.findWithQuery(Cycle.class, "Select top ? * from Cycle", Integer.toString(BATCH_SIZE));
    Cycle lastCycle = cycles.get(0);
    int[] deltaSOCArray = new int[cycles.size() - 1];
    for (int i = 1; i < cycles.size(); i++) {
        Cycle currentCycle = cycles.get(i);
        Integer deltaSOC = lastCycle.getPlugoutEvent().getLevel()
                        - currentCycle.getPluginEvent().getLevel();
        deltaSOCArray[i - 1] = deltaSOC;
        lastCycle = cycles.get(i);
        Log.v(TAG, cycles.get(i).getPluginEvent().getDatetime());
    }

    for (int i = 0; i < cycles.size() - 1; i++) {
        if (i > BATCH_SIZE) {
            Ints.max(deltaSOCArray);
        }
    }*/
    }

    public Integer predict() {
        return 100;
    }

}
