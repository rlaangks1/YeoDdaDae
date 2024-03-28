package com.bucheon.yeoddadae;

public class ParkItem {
    private String name;
    private String radius;
    private String gasolinePrice; // 휘발유가
    private String dieselPrice; // 경유가
    private String lpgPrice; // LPG가
    private String phone;
    private String addition;
    private int starRate; // 0:☆☆☆☆☆, 5: ★★★★★
    private double lat;
    private double lon;

    public ParkItem(String name, String radius, String gasolinePrice, String dieselPrice, String lpgPrice, String phone, String addition, int starRate, String lat, String lon) {
        this.name = name;
        this.radius = radius;
        this.gasolinePrice = gasolinePrice;
        this.dieselPrice = dieselPrice;
        this.lpgPrice = lpgPrice;
        this.phone = phone;
        this.addition = addition;
        this.starRate = starRate;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
    }

    public String getName() {
        return name;
    }

    public String getRadius() {
        return radius;
    }

    public String getGasolinePrice() {
        return gasolinePrice;
    }

    public String getDieselPrice() {
        return dieselPrice;
    }

    public String getLpgPrice() {
        return lpgPrice;
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
}
