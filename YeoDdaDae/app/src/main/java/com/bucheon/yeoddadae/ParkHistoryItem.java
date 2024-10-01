package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class ParkHistoryItem {
    String id;
    String type;
    String poiId;
    String poiName;
    double lat;
    double lon;
    boolean favorites;
    Timestamp upTime;

    public ParkHistoryItem(String id, String type, String poiId, String poiName, double lat, double lon, boolean favorites, Timestamp upTime) {
        this.id = id;
        this.type = type;
        this.poiId = poiId;
        this.poiName = poiName;
        this.lat = lat;
        this.lon = lon;
        this.favorites = favorites;
        this.upTime = upTime;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPoiId() {
        return poiId;
    }

    public String getPoiName() {
        return poiName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public boolean isFavorites() {
        return favorites;
    }

    public Timestamp getUpTime() {
        return upTime;
    }

    public void setFavorites(boolean favorites) {
        this.favorites = favorites;
    }
}
