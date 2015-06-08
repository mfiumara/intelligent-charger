package tum.ei.ics.intelligentcharger;

public class Utility {

    public static final String TAG = "Utility";

    public static String timeToString(double time) {
        int hours = (int) Math.floor(time) % 24;
        int minutes = (int) ((time - hours) * 60);
        String stringMinutes = minutes < 10 ? ("0" + Integer.toString(minutes)) : Integer.toString(minutes);
        return hours + ":" + stringMinutes;
    }
}
