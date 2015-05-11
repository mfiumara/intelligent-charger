package tum.ei.ics.intelligentcharger.adapter;

import android.content.Context;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Event;

/**
 * Created by mattia on 05.05.15.
 */
public class EventAdapter extends BaseAdapter {

    private LayoutInflater myInflater;
    private List<Event> list;

    public EventAdapter(Context context) {
        myInflater = LayoutInflater.from(context);
    }

    public void setData(List<Event> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.item_event, parent, false);
            holder = new ViewHolder();
            holder.level = (TextView) convertView.findViewById(R.id.level);
            holder.datetime = (TextView) convertView.findViewById(R.id.datetime);
            holder.customStatus = (TextView) convertView.findViewById(R.id.custom_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.level.setText(Integer.toString(list.get(position).getLevel()));
        holder.datetime.setText(list.get(position).getDatetime());
        holder.customStatus.setText(list.get(position).getCustomStatus());
        return convertView;
    }

    static class ViewHolder {
        TextView level;
        TextView status;
        TextView datetime;
        TextView plugged;
        TextView customStatus;
    }
}
