package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ShareParkItem {
    String ownerId;
    double lat;
    double lon;
    String parkDetailAddress;
    boolean isApproval;
    boolean isCancelled;
    boolean isCalculated;
    long price;
    HashMap<String, ArrayList<String>> time;
    Timestamp upTime;
    String documentId;
    String address;

    public ShareParkItem(String ownerId, double lat, double lon, String parkDetailAddress, boolean isApproval, boolean isCancelled, boolean isCalculated, long price, HashMap<String, ArrayList<String>> time, Timestamp upTime, String documentId) {
        this.ownerId = ownerId;
        this.lat = lat;
        this.lon = lon;
        this.parkDetailAddress = parkDetailAddress;
        this.isApproval = isApproval;
        this.isCancelled = isCancelled;
        this.isCalculated = isCalculated;
        this.price = price;
        this.time = time;
        this.upTime = upTime;
        this.documentId = documentId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getParkDetailAddress() {
        return parkDetailAddress;
    }

    public boolean isApproval() {
        return isApproval;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isCalculated() {
        return isCalculated;
    }

    public long getPrice() {
        return price;
    }

    public HashMap<String, ArrayList<String>> getTime() {
        return time;
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
        Calendar ca = Calendar.getInstance();

        if (isCancelled()) {
            return "취소됨";
        }
        else if (isCalculated()) {
            return "정산됨";
        }
        else if (isApproval()) {
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

            HashMap<String, ArrayList<String>> shareTime = getTime();
            List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
            Collections.sort(sortedKeys);
            String firstTime = sortedKeys.get(0) + shareTime.get(sortedKeys.get(0)).get(0);
            String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

            if (nowString.compareTo(firstTime) < 0) {
                return "승인됨";
            }
            else if (nowString.compareTo(endTime) <= 0){
                return "공유 중";
            }
            else {
                return "정산 대기 중";
            }
        }
        else {
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

            HashMap<String, ArrayList<String>> shareTime = getTime();
            List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
            Collections.sort(sortedKeys);
            String firstTime = sortedKeys.get(0) + shareTime.get(sortedKeys.get(0)).get(0);

            if (nowString.compareTo(firstTime) < 0){
                return "승인 대기 중";
            }
            else {
                return "승인 실패";
            }
        }
    }
}
