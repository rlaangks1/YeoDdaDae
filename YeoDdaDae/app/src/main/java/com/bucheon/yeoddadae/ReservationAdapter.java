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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ReservationAdapter extends BaseAdapter {
    ArrayList<ReservationItem> items = new ArrayList<ReservationItem>();

    public void addItem(ReservationItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public ReservationItem findItem(String id, Timestamp ts) {
        for (ReservationItem item : items) {
            if (item.getId().equals(id) && item.getUpTime().equals(ts)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void sortByUpTime() {
        if (items != null && items.size() > 1) {
            Collections.sort(items, new Comparator<ReservationItem>() {
                @Override
                public int compare(ReservationItem o1, ReservationItem o2) {
                    Timestamp upTime1 = o1.getUpTime();
                    Timestamp upTime2 = o2.getUpTime();

                    // Timestamp를 Date로 변환 후 비교
                    // 오름차순 정렬입니다. 내림차순으로 하고 싶다면 o2와 o1의 위치를 바꿉니다.
                    return upTime1.toDate().compareTo(upTime2.toDate());
                }
            });
            notifyDataSetChanged(); // 데이터셋이 변경됨을 어댑터에 알림
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
        ReservationItem reservation = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.reservation_item, parent, false);
        }

        // 파인드 뷰
        TextView shareParkInfoTxt = convertView.findViewById(R.id.shareParkInfoTxt);
        TextView reservationTimeTxt = convertView.findViewById(R.id.reservationTimeTxt);
        TextView upTimeTxt = convertView.findViewById(R.id.upTimeTxt);

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
                            String [] adrresses = tMapAddressInfo.strFullAddress.split(",");
                            shareParkInfoTxt.setText(adrresses[2] + " / " + detailAddress);
                        }
                    }
                });
            }

            @Override
            public void onDataLoadError(String errorMessage) {
                shareParkInfoTxt.setText("오류 " + errorMessage);
            }
        });

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
        reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1); // 마지막 줄바꿈 제거

        reservationTimeTxt.setText(reservationTimeString);

        upTimeTxt.setText(reservation.getUpTime().toString());

        return convertView;
    }
}
