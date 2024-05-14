package com.bucheon.yeoddadae;

import android.app.Activity;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReportDiscountParkAdapter extends BaseAdapter {
    ArrayList<ReportDiscountParkItem> items = new ArrayList<>();
    Activity activity;

    public ReportDiscountParkAdapter(Activity activity) {
        this.activity = activity;
    }

    public void addItem(ReportDiscountParkItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public ReportDiscountParkItem findItem(String poiId, String id) {
        for (ReportDiscountParkItem item : items) {
            if (item.getPoiId().equals(poiId) && item.getId().equals(id)) {
                return item;
            }
        }
        return null; // 못 찾은 경우 null 반환
    }

    public void sortByUpTime() {
        Collections.sort(items, new Comparator<ReportDiscountParkItem>() {
            @Override
            public int compare(ReportDiscountParkItem o1, ReportDiscountParkItem o2) {
                return o1.getUpTime().compareTo(o2.getUpTime());
            }
        });
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
        ReportDiscountParkItem reservation = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.report_discount_park_item, parent, false);
        }

        // 파인드 뷰
        TextView reportDiscountParkAddressTxt = convertView.findViewById(R.id.reportDiscountParkAddressTxt);
        TextView reportDiscountParkConditionAndDiscountTxt = convertView.findViewById(R.id.reportDiscountParkConditionAndDiscountTxt);
        TextView reportDiscountParkIsCancelledTxt = convertView.findViewById(R.id.reportDiscountParkIsCancelledTxt);
        TextView reportDiscountParkUpTimeTxt = convertView.findViewById(R.id.reportDiscountParkUpTimeTxt);




        /*

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

        if (reservation.getIsCancelled()) {
            reservationIsCancelledTxt.setText("취소됨");
        }
        else {
            reservationIsCancelledTxt.setVisibility(View.GONE);
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
        reservationTimeString = reservationTimeString.substring(0, reservationTimeString.length() - 1); // 마지막 줄바꿈 제거
        reservationTimeTxt.setText(reservationTimeString);

        Timestamp timestamp = reservation.getUpTime();
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA);
        String dateString = sdf.format(date);
        upTimeTxt.setText(dateString);

         */

        return convertView;
    }
}
