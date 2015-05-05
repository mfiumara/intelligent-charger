package tum.ei.ics.intelligentcharger;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by mattia on 30.04.15.
 */
public class BatteryDataAdapter extends CursorAdapter {
    public BatteryDataAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }
    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvStatus = (TextView) view.findViewById(R.id.tvStatus);
        TextView tvLevel = (TextView) view.findViewById(R.id.tvLevel);
        // Extract properties from cursor
        String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
        int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
        // Populate fields with extracted properties
        tvStatus.setText(status);
        tvLevel.setText(String.valueOf(level));
    }
}
