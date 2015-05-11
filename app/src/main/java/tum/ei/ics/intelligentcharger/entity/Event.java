package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mattia on 04.05.15.
 */
public class Event extends SugarRecord<Event> {
    Integer status;
    Integer plugged;
    Integer level;
    Integer voltage;
    Float temperature;
    String datetime;
    String customStatus;

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

    public Integer getStatus() { return status; }
    public Integer getLevel() { return level; }
    public Integer getPlugged() { return plugged; }
    public String getCustomStatus() { return customStatus; }
    public String getDatetime() { return datetime; }
    public Float getTemperature() { return temperature; }
    public Integer getVoltage() { return voltage; }
}
