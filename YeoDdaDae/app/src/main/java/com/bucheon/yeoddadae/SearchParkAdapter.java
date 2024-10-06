package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
                convertView = inflater.inflate(R.layout.search_history_item, parent, false);
            }

            TextView historyName = convertView.findViewById(R.id.historyName);
            TextView historyTime = convertView.findViewById(R.id.historyTime);
            ImageButton historyDeleteBtn = convertView.findViewById(R.id.historyDeleteBtn);

            historyName.setText(historyItem.getKeyword());

            Timestamp timestamp = historyItem.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd", Locale.KOREA);
                String dateString = sdf.format(date);
                historyTime.setText(dateString);
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
            convertView = inflater.inflate(R.layout.search_park_item, parent, false);
        }

        // 파인드 뷰
        TextView parkOrder = convertView.findViewById(R.id.parkOrder);
        TextView parkName = convertView.findViewById(R.id.parkName);
        TextView parkType = convertView.findViewById(R.id.parkType);
        TextView parkDistance = convertView.findViewById(R.id.parkDistance);
        TextView parkPrice = convertView.findViewById(R.id.parkPrice);
        TextView parkConditionAndDiscount = convertView.findViewById(R.id.parkConditionAndDiscount);
        TextView parkPhone = convertView.findViewById(R.id.parkPhone);

        // 뷰 내용
        switch (park.getType()) {
            case 1 :
                parkType.setText("일반주차장");
                parkPrice.setVisibility(View.VISIBLE);
                parkConditionAndDiscount.setVisibility(View.GONE);
                break;
            case 2 :
                parkType.setText("공영주차장");
                parkPrice.setVisibility(View.VISIBLE);
                parkConditionAndDiscount.setVisibility(View.GONE);
                break;
            case 3 :
                parkType.setText("공유주차장");
                parkPrice.setVisibility(View.VISIBLE);
                parkConditionAndDiscount.setVisibility(View.GONE);
                break;
            case 4 :
                parkType.setText("주소");
                parkPrice.setVisibility(View.VISIBLE);
                parkConditionAndDiscount.setVisibility(View.GONE);
                break;
            case 5 :
                parkType.setText("장소");
                parkPrice.setVisibility(View.VISIBLE);
                parkConditionAndDiscount.setVisibility(View.GONE);
                break;
            case 6 :
                parkType.setText("제보주차장");
                parkPrice.setVisibility(View.GONE);
                parkConditionAndDiscount.setVisibility(View.VISIBLE);
                break;
            default :
                parkType.setText("뭐냐고");
        }

        parkOrder.setText(Integer.toString(parkPosition + 1));
        parkName.setText(park.getName());

        // 숫자 포맷 지정 (세 번째 자리에서 반올림)
        DecimalFormat formatter = new DecimalFormat("#.##");
        double number = Double.parseDouble(park.getRadius());
        String formattedDistanceString = formatter.format(number);
        parkDistance.setText(formattedDistanceString + "km");

        String parkPriceValue = park.getParkPrice();
        if (parkPriceValue != null && !parkPriceValue.equals("null")) {
            if (Integer.parseInt(parkPriceValue) == 0) {
                parkPrice.setText("무료");
            } else {
                formatter = new DecimalFormat("#,###");
                number = Double.parseDouble(parkPriceValue);
                String formattedPriceString = formatter.format(number);
                parkPrice.setText("시간 당 " + formattedPriceString + "원");
            }
        } else {
            parkPrice.setText("");
        }

        String conditionAndDscountString = "";
        if (park.getType() == 6 && park.getCondition() != null && park.getDiscount() != null && park.getCondition().size() == park.getDiscount().size()) {
            formatter = new DecimalFormat("#,###");

            for (int i = 0; i < park.getCondition().size(); i++) {
                conditionAndDscountString += park.getCondition().get(i);

                if (park.getDiscount().get(i) == 0) {
                    conditionAndDscountString += "/무료";
                }
                else {
                    long discount = park.getDiscount().get(i);
                    String formattedDiscount= formatter.format(discount);
                    conditionAndDscountString += "/" + formattedDiscount + "원 할인";
                }
                if (i != park.getCondition().size() - 1) {
                    conditionAndDscountString += "\n";
                }
            }
        }
        parkConditionAndDiscount.setText(conditionAndDscountString);

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

