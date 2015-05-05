package tum.ei.ics.intelligentcharger.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
            holder.status = (TextView) convertView.findViewById(R.id.status);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.level.setText(Integer.toString(list.get(position).level));
        holder.status.setText(Integer.toString(list.get(position).status));
        holder.datetime.setText(list.get(position).datetime);

        return convertView;
    }

    static class ViewHolder {
        TextView level;
        TextView status;
        TextView datetime;
        TextView plugged;
    }
}
