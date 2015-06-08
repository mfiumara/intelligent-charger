package tum.ei.ics.intelligentcharger.adapter;

import android.content.Context;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tum.ei.ics.intelligentcharger.R;
import tum.ei.ics.intelligentcharger.entity.Cycle;

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
            holder.type1  = (TextView) convertView.findViewById(R.id.type1);
            holder.type2  = (TextView) convertView.findViewById(R.id.type2);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Integer level1 = list.get(position).getPluginEvent().getLevel();
        Integer level2 = list.get(position).getPlugoutEvent().getLevel();
        holder.level1.setText(Integer.toString(level1));
        holder.level2.setText(Integer.toString(level2));
        holder.datetime1.setText(list.get(position).getPluginEvent().getDatetime());
        holder.datetime2.setText(list.get(position).getPlugoutEvent().getDatetime());


        Integer plugged = list.get(position).getPluginEvent().getPlugged();
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
            holder.type1.setText("AC");
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
            holder.type1.setText("USB");
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            holder.type1.setText("Wireless");
        } else {
            holder.type1.setText("");
        }
        plugged = list.get(position).getPlugoutEvent().getPlugged();
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
            holder.type2.setText("AC");
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
            holder.type2.setText("USB");
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            holder.type2.setText("Wireless");
        } else {
            holder.type2.setText("");
        }

        return convertView;
    }

    static class ViewHolder {
        TextView level1;
        TextView level2;
        TextView datetime1;
        TextView datetime2;
        TextView type1;
        TextView type2;
    }
}