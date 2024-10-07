package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReasonAdapter extends BaseAdapter {
    ArrayList<ReasonItem> items  = new ArrayList<>();

    public void addItem(ReasonItem item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
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
        Context context = parent.getContext();
        ReasonItem gasStation = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.reason_item, parent, false);
        }

        convertView.setEnabled(false);

        TextView reasonTxt = convertView.findViewById(R.id.reasonTxt);
        TextView reasonIdTxt = convertView.findViewById(R.id.reasonIdTxt);
        TextView reasonUpTimeTxt = convertView.findViewById(R.id.reasonUpTimeTxt);

        reasonTxt.setText(gasStation.getReason());

        reasonIdTxt.setText(gasStation.getId());

        Timestamp timestamp = gasStation.getReasonUpTime();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
            String dateString = sdf.format(date);
            reasonUpTimeTxt.setText(dateString);
        }

        return convertView;
    }
}
