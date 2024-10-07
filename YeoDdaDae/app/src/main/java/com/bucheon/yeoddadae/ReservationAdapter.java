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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReservationAdapter extends BaseAdapter {
    ArrayList<ReservationItem> firstItems = new ArrayList<>();
    ArrayList<ReservationItem> secondItems = new ArrayList<>();
    ArrayList<ReservationItem> savedFirstItems = new ArrayList<>();
    ArrayList<ReservationItem> savedSecondItems = new ArrayList<>();
    Activity activity;
    LayoutInflater inflater;
    boolean isItemsSaved = false;

    public ReservationAdapter (Activity activity) {
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(ReservationItem item) {
        if (item.getStatus().equals("사용 예정") || item.getStatus().equals("사용 중")) {
            firstItems.add(item);
        }
        else {
            secondItems.add(item);
        }
    }

    public void clear () {
        firstItems.clear();
        secondItems.clear();
        savedFirstItems.clear();
        savedSecondItems.clear();
        isItemsSaved = false;
        notifyDataSetChanged();
    }

    public ReservationItem findItem(String id, Timestamp ts) {
        for (ReservationItem item : firstItems) {
            if (item.getId().equals(id) && item.getUpTime().equals(ts)) {
                return item;
            }
        }
        for (ReservationItem item : secondItems) {
            if (item.getId().equals(id) && item.getUpTime().equals(ts)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public int searchReservation(String keyword) {
        if (!isItemsSaved) {
            savedFirstItems = new ArrayList<>(firstItems);
            savedSecondItems = new ArrayList<>(secondItems);
            isItemsSaved = true;
        }

        ArrayList<ReservationItem> removeItems = new ArrayList<>();
        for (ReservationItem item : firstItems) {
            if (!item.getDocumentId().contains(keyword) && !item.getStatus().contains(keyword) && (item.getAddress() == null || !item.getAddress().contains(keyword))) {
                removeItems.add(item);
            }
        }
        firstItems.removeAll(removeItems);

        removeItems.clear();
        for (ReservationItem item : secondItems) {
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
        int count = firstItems.size() + secondItems.size();
        if (!firstItems.isEmpty()) {
            count += 1; // For enable_item.xml
        }
        if (!secondItems.isEmpty()) {
            count += 1; // For disable_item.xml
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        // Adjust for the enable item
        if (position == 0 && !firstItems.isEmpty()) {
            return null; // This is the enable_item, we return null since it doesn't have an item.
        }

        // Adjust for the disable item
        else if (!secondItems.isEmpty() && (!firstItems.isEmpty() && position == firstItems.size() + 1 ||  (firstItems.isEmpty() && position == 0))) {
            return null; // This is the disable_item, we return null since it doesn't have an item.
        }
        else {
            ReservationItem reservation;

            if (firstItems.isEmpty()) {
                reservation = secondItems.get(position - 1);
            }
            else if (secondItems.isEmpty()) {
                reservation = firstItems.get(position - 1);
            }
            else {
                if (position > 0 && position < firstItems.size() + 1) {
                    reservation = firstItems.get(position - 1); // Account for the enable_item.xml
                }
                else {
                    reservation = secondItems.get(position - firstItems.size() - 2); // Account for both enable_item.xml and disable_item.xml
                }
            }

            return  reservation;
        }
    }

    @Override
    public long getItemId(int position) {
        if (position == 0 && !firstItems.isEmpty()) {
            return -1; // ID for the enable item
        }
        else if (!secondItems.isEmpty() && (!firstItems.isEmpty() && position == firstItems.size() + 1 ||  (firstItems.isEmpty() && position == 0))) {
            return -2; // ID for the disable item
        }
        else {
            if (firstItems.isEmpty()) {
                return secondItems.get(position - 1).getId().hashCode();
            }
            else if (secondItems.isEmpty()) {
                return firstItems.get(position - 1).getId().hashCode();
            }
            else {
                if (position > 0 && position < firstItems.size() + 1) {
                    return firstItems.get(position - 1).getId().hashCode(); // Account for the enable_item.xml
                }
                else {
                    return secondItems.get(position - firstItems.size() - 2).getId().hashCode(); // Account for both enable_item.xml and disable_item.xml
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        else { // 예약 항목 뷰
            convertView = inflater.inflate(R.layout.reservation_item, parent, false);

            ReservationItem reservation;
            if (firstItems.isEmpty()) {
                reservation = secondItems.get(position - 1);
            }
            else if (secondItems.isEmpty()) {
                reservation = firstItems.get(position - 1);
            }
            else {
                if (position > 0 && position < firstItems.size() + 1) {
                    reservation = firstItems.get(position - 1); // Account for the enable_item.xml
                }
                else {
                    reservation = secondItems.get(position - firstItems.size() - 2); // Account for both enable_item.xml and disable_item.xml
                }
            }

            // 파인드 뷰
            TextView shareParkInfoTxt = convertView.findViewById(R.id.shareParkInfoTxt);
            TextView reservationTimeTxt = convertView.findViewById(R.id.reservationTimeTxt);
            TextView reservationIsCancelledTxt = convertView.findViewById(R.id.reservationIsCancelledTxt);
            TextView upTimeTxt = convertView.findViewById(R.id.upTimeTxt);

            shareParkInfoTxt.setText("로드 중...");

            FirestoreDatabase fd = new FirestoreDatabase();
            // 뷰 내용
            fd.loadShareParkInfo(reservation.getShareParkDocumentName(), new OnFirestoreDataLoadedListener() {
                @Override
                public void onDataLoaded(Object data) {
                    HashMap<String, Object> hm = (HashMap<String, Object>) data;

                    double lat = (double) hm.get("lat");
                    double lon = (double) hm.get("lon");
                    String detailAddress = (String) hm.get("parkDetailAddress");

                    TMapData tMapdata = new TMapData();
                    tMapdata.reverseGeocoding(lat, lon, "A10", new TMapData.reverseGeocodingListenerCallback() {
                        @Override
                        public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                            if (tMapAddressInfo != null) {
                                String[] adrresses = tMapAddressInfo.strFullAddress.split(",");
                                String address = adrresses[2] + " / " + detailAddress;

                                reservation.setAddress(address);
                                // UI 업데이트를 메인(UI) 스레드로 보내기
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        shareParkInfoTxt.setText(address);
                                    }
                                });
                            }
                        }
                    });
                }
                @Override
                public void onDataLoadError(String errorMessage) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            shareParkInfoTxt.setText("오류");
                        }
                    });
                }
            });

            String status = reservation.getStatus();
            reservationIsCancelledTxt.setText(status);

            switch(status) {
                case "취소됨" :
                    reservationIsCancelledTxt.setTextColor(Color.rgb(255, 0 ,0));
                    break;
                case "사용 예정" :
                    reservationIsCancelledTxt.setTextColor(Color.rgb(0, 0 ,255));
                    break;
                case "사용 중" :
                    reservationIsCancelledTxt.setTextColor(Color.rgb(0, 255 ,0));
                    break;
                case "사용 종료" :
                    reservationIsCancelledTxt.setTextColor(Color.rgb(96, 96 ,96));
                    break;
            }

            HashMap<String, ArrayList<String>> hm = reservation.getReservationTime();
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
            reservationTimeTxt.setText(reservationTimeString);

            Timestamp timestamp = reservation.getUpTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
                String dateString = sdf.format(date);
                upTimeTxt.setText(dateString);
            }

            return convertView;
        }
    }
}