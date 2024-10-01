package com.bucheon.yeoddadae;

public interface OnParkHistoryListener {
    void onParkHistoryFavorite(ParkHistoryItem item, int position);
    void onParkHistoryDelete(ParkHistoryItem item, int position);
}
