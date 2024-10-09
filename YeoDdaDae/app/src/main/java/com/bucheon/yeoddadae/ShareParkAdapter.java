package com.bucheon.yeoddadae;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ShareParkAdapter extends BaseAdapter {
    ArrayList<ShareParkItem> firstItems = new ArrayList<>();
    ArrayList<ShareParkItem> secondItems = new ArrayList<>();
    ArrayList<ShareParkItem> savedFirstItems = new ArrayList<>();
    ArrayList<ShareParkItem> savedSecondItems = new ArrayList<>();
    Activity activity;
    LayoutInflater inflater;
    boolean isItemsSaved = false;

    public ShareParkAdapter (Activity activity) {
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(ShareParkItem item) {
        if (activity instanceof MainActivity) {
            if (item.getStatus().equals("승인됨") || item.getStatus().equals("공유 중") || item.getStatus().equals("정산 대기 중") || item.getStatus().equals("승인 대기 중")) {
                firstItems.add(item);
            }
            else {
                secondItems.add(item);
            }
        }
        else if (activity instanceof ApproveShareParkActivity) {
            firstItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        firstItems.clear();
        secondItems.clear();
        savedFirstItems.clear();
        savedSecondItems.clear();
        isItemsSaved = false;
        notifyDataSetChanged();
    }

    public ShareParkItem findItem(String documentId) {
        for (ShareParkItem item : firstItems) {
            if (item.getDocumentId().equals(documentId)) {
                return item;
            }
        }
        for (ShareParkItem item : secondItems) {
            if (item.getDocumentId().equals(documentId)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public int searchSharePark(String keyword) {
        if (!isItemsSaved) {
            savedFirstItems = new ArrayList<>(firstItems);
            savedSecondItems = new ArrayList<>(secondItems);
            isItemsSaved = true;
        }

        ArrayList<ShareParkItem> removeItems = new ArrayList<>();
        for (ShareParkItem item : firstItems) {
            if (!item.getDocumentId().contains(keyword) && !item.getStatus().contains(keyword) && (item.getAddress() == null || !item.getAddress().contains(keyword))) {
                removeItems.add(item);
            }
        }
        firstItems.removeAll(removeItems);

        removeItems.clear();
        for (ShareParkItem item : secondItems) {
            if (!item.getDocumentId().contains(keyword) && !item.getStatus().contains(keyword) && (item.getAddress() == null || !item.getAddress().contains(keyword))) {
                removeItems.add(item);
            }
        }
        secondItems.removeAll(removeItems);

        notifyDataSetChanged();

        return firstItems.size() + secondItems.size();
    }

    public void loadSavedItems() {
        if (isItemsSaved) {
            firstItems = new ArrayList<>(savedFirstItems); // savedItems의 복사본으로 items 복원
            savedFirstItems.clear();
            secondItems = new ArrayList<>(savedSecondItems); // savedItems의 복사본으로 items 복원
            savedSecondItems.clear();
            isItemsSaved = false;

            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        if (activity instanceof MainActivity) {
            int count = firstItems.size() + secondItems.size();
            if (!firstItems.isEmpty()) {
                count += 1; // For enable_item.xml
            }
            if (!secondItems.isEmpty()) {
                count += 1; // For disable_item.xml
            }
            return count;
        }
        else if (activity instanceof ApproveShareParkActivity) {
            return firstItems.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (activity instanceof MainActivity) {
            if (position == 0 && !firstItems.isEmpty()) {
                return null; // This is the enable_item, we return null since it doesn't have an item.
            }

            // Adjust for the disable item
            else if (!secondItems.isEmpty() && (!firstItems.isEmpty() && position == firstItems.size() + 1 ||  (firstItems.isEmpty() && position == 0))) {
                return null; // This is the disable_item, we return null since it doesn't have an item.
            }
            else {
                ShareParkItem sharePark;

                if (firstItems.isEmpty()) {
                    sharePark = secondItems.get(position - 1);
                }
                else if (secondItems.isEmpty()) {
                    sharePark = firstItems.get(position - 1);
                }
                else {
                    if (position > 0 && position < firstItems.size() + 1) {
                        sharePark = firstItems.get(position - 1); // Account for the enable_item.xml
                    }
                    else {
                        sharePark = secondItems.get(position - firstItems.size() - 2); // Account for both enable_item.xml and disable_item.xml
                    }
                }

                return  sharePark;
            }
        }
        else if (activity instanceof ApproveShareParkActivity) {
            return firstItems.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (activity instanceof MainActivity) {
            if (position == 0 && !firstItems.isEmpty()) {
                return -1; // ID for the enable item
            }
            else if (!secondItems.isEmpty() && (!firstItems.isEmpty() && position == firstItems.size() + 1 ||  (firstItems.isEmpty() && position == 0))) {
                return -2; // ID for the disable item
            }
            else {
                if (firstItems.isEmpty()) {
                    return secondItems.get(position - 1).getDocumentId().hashCode();
                }
                else if (secondItems.isEmpty()) {
                    return firstItems.get(position - 1).getDocumentId().hashCode();
                }
                else {
                    if (position > 0 && position < firstItems.size() + 1) {
                        return firstItems.get(position - 1).getDocumentId().hashCode(); // Account for the enable_item.xml
                    }
                    else {
                        return secondItems.get(position - firstItems.size() - 2).getDocumentId().hashCode(); // Account for both enable_item.xml and disable_item.xml
                    }
                }
            }
        }
        else if (activity instanceof ApproveShareParkActivity) {
            return firstItems.get(position).getDocumentId().hashCode();
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (activity instanceof MainActivity) {
            if (!firstItems.isEmpty() && position == 0) { // enable_item.xml
                convertView = inflater.inflate(R.layout.enable_item, parent, false);
                convertView.setEnabled(false);
                return convertView;
            }
            else if (!secondItems.isEmpty() && (!firstItems.isEmpty() && position == firstItems.size() + 1 ||  (firstItems.isEmpty() && position == 0))) { // disable_item.xml
                convertView = inflater.inflate(R.layout.disable_item, parent, false);
                convertView.setEnabled(false);
                return convertView;
            }
            else {
                convertView = inflater.inflate(R.layout.item_share_park, parent, false);

                ShareParkItem sharePark;
                if (firstItems.isEmpty()) {
                    sharePark = secondItems.get(position - 1);
                }
                else if (secondItems.isEmpty()) {
                    sharePark = firstItems.get(position - 1);
                }
                else {
                    if (position > 0 && position < firstItems.size() + 1) {
                        sharePark = firstItems.get(position - 1); // Account for the enable_item.xml
                    }
                    else {
                        sharePark = secondItems.get(position - firstItems.size() - 2); // Account for both enable_item.xml and disable_item.xml
                    }
                }

                TextView shareParkLocationTxt = convertView.findViewById(R.id.shareParkLocationTxt);
                TextView shareParkShareTimeTxt = convertView.findViewById(R.id.shareParkShareTimeTxt);
                TextView shareParkIsCancelledTxt = convertView.findViewById(R.id.shareParkIsCancelledTxt);
                TextView shareParkPriceTxt = convertView.findViewById(R.id.shareParkPriceTxt);
                TextView shareParkUpTimeTxt = convertView.findViewById(R.id.shareParkUpTimeTxt);

                TMapData tMapdata = new TMapData();
                tMapdata.reverseGeocoding(sharePark.getLat(), sharePark.getLon(), "A10", new TMapData.reverseGeocodingListenerCallback() {
                    @Override
                    public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                        if (tMapAddressInfo != null) {
                            String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                            String address = adrresses[2] + " / " + sharePark.getParkDetailAddress();

                            sharePark.setAddress(address);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    shareParkLocationTxt.setText(address);
                                }
                            });
                        }
                    }
                });

                HashMap<String, ArrayList<String>> hm = sharePark.getTime();
                ArrayList<String> keys = new ArrayList<>(hm.keySet());
                Collections.sort(keys);

                String reservationTimeString = "";
                for (String key : keys) {
                    int year = Integer.parseInt(key.substring(0, 4));
                    int month = Integer.parseInt(key.substring(4, 6));
                    int day = Integer.parseInt(key.substring(6, 8));

                    String startTimeHour = hm.get(key).get(0).substring(0,2);
                    String startTimeMinute = hm.get(key).get(0).substring(2,4);
                    String endTimeHour = hm.get(key).get(1).substring(0,2);
                    String endTimeMinute = hm.get(key).get(1).substring(2,4);

                    reservationTimeString += year + "년 " + month + "월 " + day + "일 " + startTimeHour + ":" + startTimeMinute + "부터 " + endTimeHour + ":" + endTimeMinute + "까지\n";
                }
                if (!reservationTimeString.isEmpty()) {
                    reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1); // 마지막 줄바꿈 제거
                }
                shareParkShareTimeTxt.setText(reservationTimeString);

                String status = sharePark.getStatus();
                int statusColor = shareParkUpTimeTxt.getCurrentTextColor();
                switch (status) {
                    case "취소됨" :
                        statusColor = Color.rgb(255, 0, 0);
                        break;
                    case "정산됨" :
                        statusColor = Color.rgb(0, 128, 0);
                        break;
                    case "승인됨" :
                        statusColor = Color.rgb(36, 0, 88);
                        break;
                    case "공유 중" :
                        statusColor = Color.rgb(0, 0, 255);
                        break;
                    case "정산 대기 중" :
                        statusColor = Color.rgb(12, 152, 12);
                        break;
                    case "승인 대기 중" :
                        break;
                    case "승인 실패" :
                        statusColor = Color.rgb(255, 76, 140);
                        break;
                }
                shareParkIsCancelledTxt.setText(status);
                shareParkIsCancelledTxt.setTextColor(statusColor);


                if (sharePark.getPrice() == 0) {
                    shareParkPriceTxt.setText("무료");
                }
                else {
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    String formattedPriceString = formatter.format(sharePark.getPrice());

                    shareParkPriceTxt.setText("시간 당 " + formattedPriceString + "pt");
                }

                Timestamp timestamp = sharePark.getUpTime();
                if (timestamp != null) {
                    Date date = timestamp.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                    String dateString = sdf.format(date);
                    shareParkUpTimeTxt.setText(dateString);
                }
            }
        }
        else if (activity instanceof ApproveShareParkActivity) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_approve_share_park, parent, false);
            }

            ShareParkItem sharePark = firstItems.get(position);

            TextView approveShareParkLocationTxt = convertView.findViewById(R.id.approveShareParkLocationTxt);
            TextView approveShareParkShareTimeTxt = convertView.findViewById(R.id.approveShareParkShareTimeTxt);
            TextView approveShareParkOwnerIdTxt = convertView.findViewById(R.id.approveShareParkOwnerIdTxt);
            TextView approveShareParkPriceTxt = convertView.findViewById(R.id.approveShareParkPriceTxt);
            TextView approveShareParkUpTimeTxt = convertView.findViewById(R.id.approveShareParkUpTimeTxt);

            TMapData tMapdata = new TMapData();
            tMapdata.reverseGeocoding(sharePark.getLat(), sharePark.getLon(), "A10", new TMapData.reverseGeocodingListenerCallback() {
                @Override
                public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                    if (tMapAddressInfo != null) {
                        String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                        String address = adrresses[2] + " / " + sharePark.getParkDetailAddress();
                        // UI 업데이트를 메인(UI) 스레드로 보내기
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                approveShareParkLocationTxt.setText(address);
                            }
                        });
                    }
                }
            });

            HashMap<String, ArrayList<String>> hm = sharePark.getTime();
            ArrayList<String> keys = new ArrayList<>(hm.keySet());
            Collections.sort(keys);

            String reservationTimeString = "";
            for (String key : keys) {
                int year = Integer.parseInt(key.substring(0, 4));
                int month = Integer.parseInt(key.substring(4, 6));
                int day = Integer.parseInt(key.substring(6, 8));

                String startTimeHour = hm.get(key).get(0).substring(0,2);
                String startTimeMinute = hm.get(key).get(0).substring(2,4);
                String endTimeHour = hm.get(key).get(1).substring(0,2);
                String endTimeMinute = hm.get(key).get(1).substring(2,4);

                reservationTimeString += year + "년 " + month + "월 " + day + "일 " + startTimeHour + ":" + startTimeMinute + "부터 " + endTimeHour + ":" + endTimeMinute + "까지\n";
            }
            if(!reservationTimeString.isEmpty()) {
                reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1); // 마지막 줄바꿈 제거
            }
            approveShareParkShareTimeTxt.setText(reservationTimeString);

            approveShareParkOwnerIdTxt.setText(sharePark.getOwnerId());

            if (sharePark.getPrice() == 0) {
                approveShareParkPriceTxt.setText("무료");
            }
            else {
                DecimalFormat formatter = new DecimalFormat("#,###");
                String formattedPrice = formatter.format(sharePark.getPrice());

                approveShareParkPriceTxt.setText("시간 당 " + formattedPrice + "pt");
            }

            Timestamp timestamp = sharePark.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                approveShareParkUpTimeTxt.setText(dateString);
            }
        }
        return convertView;
    }
}
