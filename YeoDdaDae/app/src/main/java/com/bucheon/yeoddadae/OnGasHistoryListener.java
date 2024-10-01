package com.bucheon.yeoddadae;

public interface OnGasHistoryListener {
    void onGasHistoryFavorite(GasHistoryItem item, int position);
    void onGasHistoryDelete(GasHistoryItem item, int position);
}
