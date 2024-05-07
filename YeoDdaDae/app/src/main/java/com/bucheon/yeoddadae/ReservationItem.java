package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class ReservationItem {
    String id;
    boolean isCancelled;
    String shareParkDocumentName;
    HashMap<String, ArrayList<String>> reservationTime;
    Timestamp upTime;

    public ReservationItem(String id, boolean isCancelled, String shareParkDocumentName, HashMap<String, ArrayList<String>> reservationTime, Timestamp upTime) {
        this.id = id;
        this.isCancelled = isCancelled;
        this.shareParkDocumentName = shareParkDocumentName;
        this.reservationTime = reservationTime;
        this.upTime = upTime;
    }

    public String getId() {
        return id;
    }

    public boolean isCancelled() {
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
}
