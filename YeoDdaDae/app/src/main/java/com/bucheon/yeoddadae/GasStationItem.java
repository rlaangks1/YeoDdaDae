package com.bucheon.yeoddadae;

public class GasStationItem {
    private String name;
    private String distance;
    private String gasolinePrice; // 휘발유가
    private String dieselPrice; // 경유가
    private String phone;
    private int starRate; // 0:☆☆☆☆☆, 5: ★★★★★

    public GasStationItem(String name, String distance, String gasolinePrice, String dieselPrice, String phone, int starRate) {
        this.name = name;
        this.distance = distance;
        this.gasolinePrice = gasolinePrice;
        this.dieselPrice = dieselPrice;
        this.phone = phone;
        this.starRate = starRate;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getGasolinePrice() {
        return gasolinePrice;
    }

    public String getDieselPrice() {
        return dieselPrice;
    }

    public String getPhone() {
        return phone;
    }

    public int getStarRate() {
        return starRate;
    }
}
