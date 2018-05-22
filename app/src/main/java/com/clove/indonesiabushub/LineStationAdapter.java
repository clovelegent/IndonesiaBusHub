package com.clove.indonesiabushub;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clove.indonesiabushub.utils.Utils;

import java.util.List;

class LineStationAdapter extends BaseAdapter {

    private Context context;
    private List<BusStation> mStations;

    public LineStationAdapter(Context context, List mStations) {
        this.context = context;
        this.mStations = mStations;
    }

    @Override
    public int getCount() {
        return mStations.size();
    }

    @Override
    public Object getItem(int position) {
        return mStations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.stations_list_item, null);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            viewHolder.station = (TextView) convertView.findViewById(R.id.station);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.station.setText(Utils.getStationName(mStations.get(position).getName()));
        if (mStations.get(position).isArrived()){
            viewHolder.status.setText("arrived");
            viewHolder.status.setTextColor(Color.RED);
            viewHolder.station.setTextColor(Color.RED);
        }else{
            viewHolder.status.setText("not arrived");
            viewHolder.status.setTextColor(Color.WHITE);
            viewHolder.station.setTextColor(Color.WHITE);
        }

        return convertView;
    }

    class ViewHolder {
        public TextView station;
        public TextView status;
    }
}

