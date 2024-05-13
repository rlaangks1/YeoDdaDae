package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportDiscountParkItem {
    String poiId;
    String id;
    String condition;
    String discount;
    int ratePerfectCount;
    int rateMistakeCount;
    int rateWrongCount;
    boolean isCancelled;
    Timestamp upTime;
    String documentId;

    public ReportDiscountParkItem(String poiId, String id, String condition, String discount, int ratePerfectCount, int rateMistakeCount, int rateWrongCount, boolean isCancelled, Timestamp upTime, String documentId) {
        this.poiId = poiId;
        this.id = id;
        this.condition = condition;
        this.discount = discount;
        this.ratePerfectCount = ratePerfectCount;
        this.rateMistakeCount = rateMistakeCount;
        this.rateWrongCount = rateWrongCount;
        this.isCancelled = isCancelled;
        this.upTime = upTime;
        this.documentId = documentId;
    }

    public String getPoiId() {
        return poiId;
    }

    public String getId() {
        return id;
    }

    public String getCondition() {
        return condition;
    }

    public String getDiscount() {
        return discount;
    }

    public int getRatePerfectCount() {
        return ratePerfectCount;
    }

    public int getRateMistakeCount() {
        return rateMistakeCount;
    }

    public int getRateWrongCount() {
        return rateWrongCount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public Timestamp getUpTime() {
        return upTime;
    }

    public String getDocumentId() {
        return documentId;
    }
}
