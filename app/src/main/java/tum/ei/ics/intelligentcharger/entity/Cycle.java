package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public Float getDuration() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date pluginDate = sdf.parse(pluginEvent.datetime);
        Date plugoutDate = sdf.parse(plugoutEvent.datetime);
//        DateTime pluginDateTime = new DateTime(pluginDate);
//        DateTime plugoutDateTime = new DateTime(plugoutDate);


//        Float duration = sdf.format(pluginEvent.datetime);

        return 1.0f;
    }
}
