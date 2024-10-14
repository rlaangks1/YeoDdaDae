package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UsageHistoryAdapter extends BaseAdapter {
    private ArrayList<UsageHistoryItem> items  = new ArrayList<>();

    public void addItem(UsageHistoryItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void clearItem () {
        items.clear();
        notifyDataSetChanged();
    }

    public void onlyPark() {
        ArrayList<UsageHistoryItem> removeItems = new ArrayList<>();

        for (UsageHistoryItem item : items) {
            if (!item.getType().equals("일반주차장") && !item.getType().equals("공유주차장") && !item.getType().equals("제보주차장")) {
                removeItems.add(item);
            }
        }
        items.removeAll(removeItems);

        notifyDataSetChanged();
    }

    public void onlyGas() {
        ArrayList<UsageHistoryItem> removeItems = new ArrayList<>();

        for (UsageHistoryItem item : items) {
            if (!item.getType().equals("주유소")) {
                removeItems.add(item);
            }
        }
        items.removeAll(removeItems);

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
        UsageHistoryItem history = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_usage_history, parent, false);
        }

        TextView typeText = convertView.findViewById(R.id.typeText);
        TextView startText = convertView.findViewById(R.id.startText);
        TextView endText = convertView.findViewById(R.id.endText);
        TextView timeText = convertView.findViewById(R.id.timeText);

        typeText.setText(history.getType());

        TMapData tMapdata = new TMapData();
        tMapdata.reverseGeocoding(history.getStartLat(), history.getStartLon(), "A10", new TMapData.reverseGeocodingListenerCallback() {
            @Override
            public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                if (tMapAddressInfo != null) {
                    String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                    String address = adrresses[2];
                    startText.post(new Runnable() { // UI 업데이트를 메인 스레드에서 실행
                        @Override
                        public void run() {
                            startText.setText(address);
                        }
                    });
                }
            }
        });

        if (history.getPoiName() != null && !history.getPoiName().isEmpty()) {
            endText.setText(history.getPoiName());
        }
        else {
            tMapdata = new TMapData();
            tMapdata.reverseGeocoding(history.getEndLat(), history.getEndLon(), "A10", new TMapData.reverseGeocodingListenerCallback() {
                @Override
                public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                    if (tMapAddressInfo != null) {
                        String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                        String address = adrresses[2];
                        endText.setText(address);
                    }
                }
            });
        }

        Timestamp timestamp = history.getUpTime();
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
        String dateString = sdf.format(date);
        timeText.setText(dateString);

        return convertView;
    }
}
