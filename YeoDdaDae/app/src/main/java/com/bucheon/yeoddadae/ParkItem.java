package com.bucheon.yeoddadae;

public class ParkItem {
    private int type; // 1:일반, 2:공영, 3:공유, 4:주소, 5:장소
    private String name;
    private String radius;
    private String parkPrice;
    private String phone;
    private String addition;
    private int starRate; // 0:☆☆☆☆☆, 5: ★★★★★
    private double lat;
    private double lon;
    private String poiId;
    private String firestoreDocumentId;

    public ParkItem(int type, String name, String radius, String parkPrice, String phone, String addition, int starRate, String lat, String lon, String poiId, String firestoreDocumentId) {
        this.type = type;
        this.name = name;
        this.radius = radius;
        this.parkPrice = parkPrice;
        this.phone = phone;
        this.addition = addition;
        this.starRate = starRate;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        this.poiId = poiId;
        this.firestoreDocumentId = firestoreDocumentId;
    }

    public int getType() {
        return type;
    }
    public String getName() {
        return name;
    }

    public String getRadius() {
        return radius;
    }

    public String getParkPrice() {
        return parkPrice;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddition() {
        return addition;
    }

    public int getStarRate() {
        return starRate;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getPoiId() {
        return poiId;
    }

    public String getFirebaseDocumentId() {
        return firestoreDocumentId;
    }
}
