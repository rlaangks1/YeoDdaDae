package com.bucheon.yeoddadae;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReservationAdapter extends BaseAdapter {
    ArrayList<ReservationItem> firstItems = new ArrayList<ReservationItem>();
    ArrayList<ReservationItem> secondItems = new ArrayList<ReservationItem>();
    ArrayList<ReservationItem> savedFirstItems = new ArrayList<ReservationItem>();
    ArrayList<ReservationItem> savedSecondItems = new ArrayList<ReservationItem>();
    Activity activity;
    boolean isItemsSaved = false;
    final LayoutInflater inflater;

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
            if (!item.getDocumentId().contains(keyword) && (item.getAddress() == null || !item.getAddress().contains(keyword))) {
                removeItems.add(item);
            }
        }
        firstItems.removeAll(removeItems);

        removeItems.clear();
        for (ReservationItem item : secondItems) {
            if (!item.getDocumentId().contains(keyword) && (item.getAddress() == null || !item.getAddress().contains(keyword))) {
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
            count ++; // For enable_item.xml
        }
        if (!secondItems.isEmpty()) {
            count ++; // For disable_item.xml
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0 && !firstItems.isEmpty()) {
            return null; // enable_item.xml
        }
        else if (secondItems.size() > 0 && (position == firstItems.size() + 1 || firstItems.isEmpty() && position == 0)) {
            return null; // disable_item.xml
        }
        return getReservationItem(position);
    }

    ReservationItem getReservationItem(int position) {
        if (firstItems.isEmpty()) {
            return secondItems.get(position - 1);
        }
        else if (secondItems.isEmpty()) {
            return firstItems.get(position - 1);
        }
        else if (position > 0 && position < firstItems.size() + 1) {
            return firstItems.get(position - 1); // Account for enable_item.xml
        }
        else {
            return secondItems.get(position - firstItems.size() - 2); // Account for both enable_item.xml and disable_item.xml
        }
    }

    @Override
    public long getItemId(int position) {
        // Handle the header items (enable_item and disable_item)
        if (position == 0 && !firstItems.isEmpty()) {
            return -1; // ID for the enable item
        }
        else if (!secondItems.isEmpty() && (!firstItems.isEmpty() && position == firstItems.size() + 1 ||  (firstItems.isEmpty() && position == 0))) {
            return -2; // ID for the disable item
        }
        else {
            if (firstItems.size() == 0) {
                return secondItems.get(position - 1).getId().hashCode();
            }
            else if (secondItems.size() == 0) {
                return firstItems.get(position - 1).getId().hashCode();
            }
            else {
                ReservationItem item = getReservationItem(position);

                if (item != null) {
                    return item.getId().hashCode();
                }
                else {
                    return 0;
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (firstItems.size() > 0 && position == 0) {
            return inflater.inflate(R.layout.enable_item, parent, false);
        }
        else if (secondItems.size() > 0 && (position == firstItems.size() + 1 || firstItems.isEmpty() && position == 0)) {
            return inflater.inflate(R.layout.disable_item, parent, false);
        }
        else { // 예약 항목 뷰
            convertView = inflater.inflate(R.layout.reservation_item, parent, false);

            ReservationItem reservation = getReservationItem(position);

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

            reservationIsCancelledTxt.setText(reservation.getStatus());

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
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
            String dateString = sdf.format(date);
            upTimeTxt.setText(dateString);

            return convertView;
        }
    }
}
