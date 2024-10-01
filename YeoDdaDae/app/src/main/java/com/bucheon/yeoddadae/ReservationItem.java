package com.bucheon.yeoddadae;

import android.view.View;

import com.google.firebase.Timestamp;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ReservationItem {
    String id;
    boolean isCancelled;
    String shareParkDocumentName;
    HashMap<String, ArrayList<String>> reservationTime;
    Timestamp upTime;
    String documentId;
    String address;

    public ReservationItem(String id, boolean isCancelled, String shareParkDocumentName, HashMap<String, ArrayList<String>> reservationTime, Timestamp upTime, String documentId) {
        this.id = id;
        this.isCancelled = isCancelled;
        this.shareParkDocumentName = shareParkDocumentName;
        this.reservationTime = reservationTime;
        this.upTime = upTime;
        this.documentId = documentId;
    }

    public String getId() {
        return id;
    }

    public boolean getIsCancelled() {
        return isCancelled;
    }

    public String getShareParkDocumentName() {
        return shareParkDocumentName;
    }

    public HashMap<String, ArrayList<String>> getReservationTime() {
        return reservationTime;
    }

    public Timestamp getUpTime() {
        return upTime;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        if (isCancelled) {
            return "취소됨";
        }
        else {
            Calendar ca = Calendar.getInstance();

            int year = ca.get(Calendar.YEAR);
            int month = ca.get(Calendar.MONTH) + 1;
            int day = ca.get(Calendar.DAY_OF_MONTH);
            int hour = ca.get(Calendar.HOUR_OF_DAY);
            int minute = ca.get(Calendar.MINUTE);

            String nowString = "";
            nowString += year;
            if (month < 10) {
                nowString += "0";
            }
            nowString += month;
            if (day < 10) {
                nowString += "0";
            }
            nowString += day;
            if (hour < 10) {
                nowString += "0";
            }
            nowString += hour;
            if (minute < 10) {
                nowString += "0";
            }
            nowString += minute;
            List<String> sortedKeys = new ArrayList<>(reservationTime.keySet());
            Collections.sort(sortedKeys);
            String firstTime = sortedKeys.get(0) + reservationTime.get(sortedKeys.get(0)).get(0);
            String endTime = sortedKeys.get(sortedKeys.size() - 1) + reservationTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

            if (nowString.compareTo(firstTime) < 0) {
                return "사용 예정";
            }
            else if (nowString.compareTo(endTime) < 0) {
                return "사용 중";
            }
            else {
                return "사용 종료";
            }
        }
    }
}
