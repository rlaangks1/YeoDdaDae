package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class SearchHistoryItem {
    private String keyword;
    private Timestamp upTime;

    public SearchHistoryItem(String keyword, Timestamp upTime) {
        this.keyword = keyword;
        this.upTime = upTime;
    }

    public String getKeyword() {
        return keyword;
    }

    public Timestamp getUpTime() {
        return upTime;
    }
}
