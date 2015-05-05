package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;

/**
 * Created by mattia on 04.05.15.
 */
public class Event extends SugarRecord<Event> {
    Integer status;
    Integer plugged;
    Integer level;
    Integer voltage;
    Float temperature;

    public Event() {}

    public Event(Integer status, Integer plugged, Integer level, Integer voltage, Float temperature) {
        this.status = status;
        this.plugged = plugged;
        this.level = level;
        this.voltage = voltage;
        this.temperature = temperature;
    }

}
