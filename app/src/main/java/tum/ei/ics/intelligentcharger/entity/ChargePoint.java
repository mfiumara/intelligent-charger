package tum.ei.ics.intelligentcharger.entity;

import com.orm.SugarRecord;

/**
 * Created by mattia on 28.05.15.
 */
public class ChargePoint extends SugarRecord<ChargePoint> {
    Integer plugType;
    Integer level;
    Integer voltage;
    float time;
    long curveID;

    public ChargePoint() {}

    public ChargePoint(Integer plugType, float time, Integer level, Integer voltage, long curveID) {
        this.plugType = plugType;
        this.time = time;
        this.level = level;
        this.voltage = voltage;
        this.curveID = curveID;
    }
    public Integer getPlugType() {
        return plugType;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getVoltage() {
        return voltage;
    }

    public float getTime() {
        return time;
    }

    public long getCurveID() {
        return curveID;
    }

}
