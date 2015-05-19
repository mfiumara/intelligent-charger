package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mattia on 04.05.15.
 */
public class Event extends SugarRecord<Event> {
    private Integer status;
    private Integer plugged;
    private Integer level;
    private Integer voltage;
    private Float temperature;
    private String datetime;
    private String customStatus;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

        String date = sdf.format(new Date());
        this.datetime = date;
    }
    public float getTime() {
        Date date = new Date();
        try {
            date = sdf.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return (float) (calendar.get(Calendar.HOUR_OF_DAY) +
                calendar.get(Calendar.MINUTE) /  60.0 + calendar.get(Calendar.SECOND) / 3600.0);

    }
    public Integer getStatus() { return status; }
    public Integer getLevel() { return level; }
    public Integer getPlugged() { return plugged; }
    public String getCustomStatus() { return customStatus; }
    public String getDatetime() { return datetime; }
    public Float getTemperature() { return temperature; }
    public Integer getVoltage() { return voltage; }

}
