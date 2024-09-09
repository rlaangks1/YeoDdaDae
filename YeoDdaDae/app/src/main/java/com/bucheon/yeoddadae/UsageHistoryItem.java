package com.bucheon.yeoddadae;

public class UsageHistoryItem {
    private String searchQuery; // 검색어
    private long timestamp; // 검색 시간이 저장된 타임스탬프

    public UsageHistoryItem(String searchQuery, long timestamp) {
        this.searchQuery = searchQuery;
        this.timestamp = timestamp;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // 필요 시 타임스탬프를 포맷팅하는 메서드 추가 가능
    public String getFormattedTimestamp() {
        // 예: 시간 포맷을 변경하려면 SimpleDateFormat 등을 사용해 구현
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }
}
