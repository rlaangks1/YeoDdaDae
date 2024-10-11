package com.bucheon.yeoddadae;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class SearchParkAdapter extends BaseAdapter {
    Activity activity;
    LayoutInflater inflater;

    ArrayList<SearchHistoryItem> historyItems = new ArrayList<>();
    ArrayList<ParkItem> parkItems = new ArrayList<ParkItem>();
    OnHistoryDeleteListener onHistoryDeleteListener;

    public SearchParkAdapter (Activity activity) {
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addPark(ParkItem item) {
        if (activity instanceof AddReportDiscountParkActivity && item.getType() == 4) {
            return;
        }
        parkItems.add(item);
    }

    public void clearPark() {
        parkItems.clear();
        notifyDataSetChanged();
    }

    public void addHistory(SearchHistoryItem item) {
        historyItems.add(item);
    }

    public void deleteHistory(int i) {
        historyItems.remove(i);
        notifyDataSetChanged();
    }

    public void clearHistory() {
        historyItems.clear();
        notifyDataSetChanged();
    }

    public ParkItem findItem(String parkItemName) {
        for (ParkItem item : parkItems) {
            if (item.getName().equals(parkItemName)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void duplicationRemove() {
        if (parkItems != null && !parkItems.isEmpty()) {
            HashSet<ParkItem> uniqueItemsSet = new HashSet<>(parkItems);
            parkItems.clear();
            parkItems.addAll(uniqueItemsSet);
        }
        notifyDataSetChanged();
    }

    public void setOnHistoryDeleteListener(OnHistoryDeleteListener listener) {
        this.onHistoryDeleteListener = listener;
    }

    @Override
    public int getCount() {
        return historyItems.size() + parkItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < historyItems.size()) {
            return historyItems.get(position);
        } else {
            return parkItems.get(position - historyItems.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (position < historyItems.size()) { // HistoryItem을 먼저 처리
            SearchHistoryItem historyItem = historyItems.get(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_search_history, parent, false);
            }

            TextView historyName = convertView.findViewById(R.id.historyName);
            TextView historyTime = convertView.findViewById(R.id.historyTime);
            ImageButton historyDeleteBtn = convertView.findViewById(R.id.historyDeleteBtn);

            historyName.setText(historyItem.getKeyword());

            Timestamp timestamp = historyItem.getUpTime();
            if (timestamp != null) {
                Timestamp now = Timestamp.now();

                Date date = timestamp.toDate();
                Date currentDate = now.toDate();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yy.MM.dd", Locale.KOREA);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);

                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(date);

                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(currentDate);

                if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)) {
                    String timeString = timeFormat.format(date);
                    historyTime.setText(timeString);
                } else {
                    String dateString = dateFormat.format(date);
                    historyTime.setText(dateString);
                }
            }

            historyDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onHistoryDeleteListener != null) {
                        onHistoryDeleteListener.onHistoryDelete(historyItem, position);
                    }
                }
            });

            return convertView;
        }

        // ParkItem을 처리
        int parkPosition = position - historyItems.size();
        ParkItem park = parkItems.get(parkPosition);

        if (convertView == null || convertView.getTag() instanceof SearchHistoryItem) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_search_park, parent, false);
        }

        // 파인드 뷰
        TextView parkOrder = convertView.findViewById(R.id.parkOrder);
        TextView parkName = convertView.findViewById(R.id.parkName);
        TextView parkType = convertView.findViewById(R.id.parkType);
        TextView parkAddressTxt = convertView.findViewById(R.id.parkAddressTxt);
        TextView parkDistance = convertView.findViewById(R.id.parkDistance);
        TextView parkPhone = convertView.findViewById(R.id.parkPhone);

        // 뷰 내용
        parkOrder.setText(Integer.toString(parkPosition + 1));

        parkName.setText(park.getName());

        switch (park.getType()) {
            case 1 :
                parkType.setText("일반주차장");
                break;
            case 2 :
                parkType.setText("공영주차장");
                break;
            case 4 :
                parkType.setText("주소");
                break;
            case 5 :
                parkType.setText("장소");
                break;
            default :
                parkType.setText("뭐냐고");
        }

        double number = Double.parseDouble(park.getRadius());
        DecimalFormat formatter;
        String formattedDistanceString;
        if (number < 1) {
            number *= 1000;
            formatter = new DecimalFormat("#,###");
            formattedDistanceString = formatter.format(number) + "m";
        }
        else {
            formatter = new DecimalFormat("#.##");
            formattedDistanceString = formatter.format(number) + "km";
        }
        parkDistance.setText(formattedDistanceString);

        TMapData tMapdata = new TMapData();
        tMapdata.reverseGeocoding(park.getLat(), park.getLon(), "A10", new TMapData.reverseGeocodingListenerCallback() {
            @Override
            public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                if (tMapAddressInfo != null) {
                    String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                    String address = adrresses[2];
                    parkAddressTxt.post(new Runnable() { // UI 업데이트를 메인 스레드에서 실행
                        @Override
                        public void run() {
                            parkAddressTxt.setText(address);
                        }
                    });
                }
            }
        });

        String originalPhoneString = park.getPhone();
        String newPhoneString = "";
        if (originalPhoneString != null) {
            if (originalPhoneString.length() == 8) {
                newPhoneString = originalPhoneString.substring(0, 4) + "-" + originalPhoneString.substring(4);
            }
            else if (originalPhoneString.length() == 10) {
                newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 6) + "-" + originalPhoneString.substring(6);
            }
            else if (originalPhoneString.length() == 11) {
                newPhoneString = originalPhoneString.substring(0, 3) + "-" + originalPhoneString.substring(3, 7) + "-" + originalPhoneString.substring(7);
            }
            else {
                newPhoneString = originalPhoneString;
            }
        }
        parkPhone.setText(newPhoneString);

        return convertView;
    }
}

