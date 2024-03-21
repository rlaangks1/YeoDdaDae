package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GasStationAdapter extends BaseAdapter {
    ArrayList<GasStationItem> items = new ArrayList<GasStationItem>();
    Context context;

    public void addItem(GasStationItem item) {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        GasStationItem gasStation = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gas_station_item, parent, false);
        }

        TextView gasStationName = convertView.findViewById(R.id.gasStationName);
        TextView gasStationDistance = convertView.findViewById(R.id.gasStationDistance);
        TextView gasStationOilPrice = convertView.findViewById(R.id.gasStationOilPrice);
        TextView gasStationAddition = convertView.findViewById(R.id.gasStationAddition);
        TextView gasStationStarRate = convertView.findViewById(R.id.gasStationStarRate);
        TextView gasStationPhone = convertView.findViewById(R.id.gasStationPhone);

        gasStationName.setText(gasStation.getName());
        gasStationDistance.setText(gasStation.getDistance());
        gasStationOilPrice.setText(gasStation.getGasolinePrice() + gasStation.getDieselPrice());
        gasStationAddition.setText("세차(수정)");
        gasStationStarRate.setText(Integer.toString(gasStation.getStarRate()));
        gasStationPhone.setText(gasStation.getPhone());

        return convertView;
    }
}
