package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class UsageHistoryItem {
    String type;
    double startLat;
    double startLon;
    String poiId;
    String poiName;
    double endLat;
    double endLon;
    Timestamp upTime;

    public UsageHistoryItem(String type, double startLat, double startLon, String poiId, String poiName, double endLat, double endLon, Timestamp upTime) {
        this.type = type;
        this.startLat = startLat;
        this.startLon = startLon;
        this.poiId = poiId;
        this.poiName = poiName;
        this.endLat = endLat;
        this.endLon = endLon;
        this.upTime = upTime;
    }

    public String getType() {
        return type;
    }

    public double getStartLat() {
        return startLat;
    }

    public double getStartLon() {
        return startLon;
    }

    public String getPoiId() {
        return poiId;
    }

    public String getPoiName() {
        return poiName;
    }

    public double getEndLat() {
        return endLat;
    }

    public double getEndLon() {
        return endLon;
    }

    public Timestamp getUpTime() {
        return upTime;
    }
}
