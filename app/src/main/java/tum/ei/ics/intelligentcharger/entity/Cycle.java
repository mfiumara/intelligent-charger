package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;

/**
 * Created by mattia on 05.05.15.
 */
public class Cycle extends SugarRecord<Cycle> {

    String datetime;
    Float plugtime;
    Float unplugtime;
    Boolean charge; // Whether this is a charge cycle (true) or a discharge cycle (false)

    public Cycle() {}

    public Cycle(Event pluginEvent, Event plugoutEvent) {

    }
}
