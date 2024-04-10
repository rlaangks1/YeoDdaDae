package com.bucheon.yeoddadae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchParkAdapter extends BaseAdapter {
    ArrayList<ParkItem> items = new ArrayList<ParkItem>();

    public void addItem(ParkItem item) {
        items.add(item);
    }

    public ParkItem findItem(String parkItemName) {
        for (ParkItem item : items) {
            if (item.getName().equals(parkItemName)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void sortByDistance () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<ParkItem>() {
                @Override
                public int compare(ParkItem o1, ParkItem o2) {
                    // Compare by star rate in descending order
                    return Double.compare(Double.parseDouble(o1.getRadius()), Double.parseDouble(o2.getRadius()));
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    public void sortByRate () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<ParkItem>() {
                @Override
                public int compare(ParkItem o1, ParkItem o2) {
                    // Compare by star rate in descending order
                    return Integer.compare(o2.getStarRate(), o1.getStarRate());
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
    }

    public void sortByParkPrice () {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<ParkItem>() {
                @Override
                public int compare(ParkItem o1, ParkItem o2) {
                    // Compare by gasoline price in ascending order
                    return Long.compare(Long.parseLong(o1.getParkPrice()), Long.parseLong(o2.getParkPrice()));
                }
            });
            notifyDataSetChanged(); // Notify adapter that dataset has changed
        }
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
        ParkItem park = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.park_item, parent, false);
        }

        // 파인드 뷰
        TextView parkOrder = convertView.findViewById(R.id.parkOrder);
        TextView parkName = convertView.findViewById(R.id.parkName);
        TextView parkType = convertView.findViewById(R.id.parkType);
        TextView parkDistance = convertView.findViewById(R.id.parkDistance);
        TextView parkPrice = convertView.findViewById(R.id.parkPrice);
        TextView parkAddition = convertView.findViewById(R.id.parkAddition);
        TextView parkStarRate = convertView.findViewById(R.id.parkStarRate);
        TextView parkPhone = convertView.findViewById(R.id.parkPhone);

        // 뷰 내용
        parkDistance.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) parkType.getLayoutParams();
        params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        params.rightToLeft = ConstraintLayout.LayoutParams.UNSET;
        parkType.setLayoutParams(params);

        parkOrder.setText (Integer.toString(position + 1));

        switch (park.getType()) {
            case 0 :
                parkType.setText("???");
                break;
            case 1 :
                parkType.setText("일반주차장");
                break;
            case 2 :
                parkType.setText("공영주차장");
                break;
            case 3 :
                parkType.setText("공유주차장");
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

        parkName.setText(park.getName());

        String parkPriceValue = park.getParkPrice();
        if (parkPriceValue != null && !parkPriceValue.equals("null")) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            Double number = Double.parseDouble(parkPriceValue);
            String formattedPriceString = formatter.format(number);
            parkPrice.setText("시간 당 " + formattedPriceString + "원");
        }
        else {
            parkPrice.setText("");
        }

        parkAddition.setText(park.getAddition());

        parkStarRate.setText(Integer.toString(park.getStarRate()));

        String originalPhoneString = park.getPhone();
        String newPhoneString = "";
        if (originalPhoneString != null) {
            if (originalPhoneString.length() == 10) {
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
