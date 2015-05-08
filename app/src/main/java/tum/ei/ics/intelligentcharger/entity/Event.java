package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mattia on 04.05.15.
 */
public class Event extends SugarRecord<Event> {
    public Integer status;
    public Integer plugged;
    public Integer level;
    public Integer voltage;
    public Float temperature;
    public String datetime;
    public String customStatus;

//    public ChargeCurve chargeCurve;

    public Event() {}

    public Event(Integer status, Integer plugged, Integer level, Integer voltage,
                 Integer temperature, String customStatus) {
        this.status = status;
        this.plugged = plugged;
        this.level = level;
        this.voltage = voltage;
        this.temperature = temperature / 10.0f;
        this.customStatus = customStatus;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());
        this.datetime = date;
    }

}
