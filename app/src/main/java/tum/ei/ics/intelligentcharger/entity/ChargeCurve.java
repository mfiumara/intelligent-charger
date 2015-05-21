package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

/**
 * Created by mattia on 06.05.15.
 */
public class ChargeCurve extends SugarRecord<ChargeCurve> {
    Integer plugType;

    public ChargeCurve() {}
    public ChargeCurve(Integer plugType) {
        this.plugType = plugType;
    }

    public List<Event> getEvents() {
        return Event.find(Event.class, "charge_curve = ? order by ? desc", this.getId().toString(), "level");
    }
}
