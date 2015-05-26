package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mattia on 05.05.15.
 */
public class Cycle extends SugarRecord<Cycle> {

    ConnectionEvent pluginEvent;
    ConnectionEvent plugoutEvent;

    public Cycle() {}

    public Cycle(ConnectionEvent pluginEvent, ConnectionEvent plugoutEvent) {
        this.pluginEvent = pluginEvent;
        this.plugoutEvent = plugoutEvent;
    }

    public Float getDuration() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date pluginDate = sdf.parse(pluginEvent.getDatetime());
        Date plugoutDate = sdf.parse(plugoutEvent.getDatetime());
//        DateTime pluginDateTime = new DateTime(pluginDate);
//        DateTime plugoutDateTime = new DateTime(plugoutDate);


//        Float duration = sdf.format(pluginEvent.datetime);

        return 1.0f;
    }

    public ConnectionEvent getPluginEvent() { return pluginEvent; }
    public ConnectionEvent getPlugoutEvent() { return plugoutEvent; }
}
