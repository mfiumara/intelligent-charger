package tum.ei.ics.intelligentcharger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import tum.ei.ics.intelligentcharger.entity.Event;

/**
 * Created by mattia on 11.05.15.
 */
public class CycleAdapter extends BaseAdapter {

    private LayoutInflater myInflater;
    private List<Cycle> list;

    public CycleAdapter(Context context) {
        myInflater = LayoutInflater.from(context);
    }

    public void setData(List<Cycle> list) {
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
            convertView = myInflater.inflate(R.layout.item_cycle, parent, false);
            holder = new ViewHolder();


            holder.level1 = (TextView) convertView.findViewById(R.id.level1);
            holder.level2 = (TextView) convertView.findViewById(R.id.level2);
            holder.datetime1 = (TextView) convertView.findViewById(R.id.datetime1);
            holder.datetime2 = (TextView) convertView.findViewById(R.id.datetime2);
            holder.type  = (TextView) convertView.findViewById(R.id.type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.level1.setText(Integer.toString(list.get(position).getPluginEvent().getLevel()));
        holder.level2.setText(Integer.toString(list.get(position).getPlugoutEvent().getLevel()));
        holder.datetime1.setText(list.get(position).getPluginEvent().getDatetime());
        holder.datetime2.setText(list.get(position).getPlugoutEvent().getDatetime());

        // TODO: Convert integer code to usb / ac / unknown string
        //holder.type.setText(list.get(position).getPluginEvent().getPlugged());

        return convertView;
    }

    static class ViewHolder {
        TextView level1;
        TextView level2;
        TextView datetime1;
        TextView datetime2;
        TextView type;
    }
}