package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class UsageHistoryItem {
    //private String searchQuery; // 검색어
    //private long timestamp; // 검색 시간이 저장된 타임스탬프
    private int type;
    private String starts;
    private String ends;
    private Timestamp times;


    public UsageHistoryItem(int type, String starts, String ends, Timestamp times) {
        //this.searchQuery = searchQuery;
        //this.timestamp = timestamp;
        this.type=type;
        this.starts=starts;
        this.ends=ends;
        this.times=times;

    }

    public int getType() {
        return type;
    }
    public String getStarts() {
        return starts;
    }
    public String getEnds() {
        return ends;
    }
    public Timestamp getFormattedTimestamp() {return times;}

}
