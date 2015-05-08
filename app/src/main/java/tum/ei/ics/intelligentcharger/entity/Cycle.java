package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;

/**
 * Created by mattia on 05.05.15.
 */
public class Cycle extends SugarRecord<Cycle> {

    Event pluginEvent;
    Event plugoutEvent;

    public Cycle() {}

    public Cycle(Event pluginEvent, Event plugoutEvent) {
        this.pluginEvent = pluginEvent;
        this.plugoutEvent = plugoutEvent;
    }
}
