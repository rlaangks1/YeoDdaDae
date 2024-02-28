package com.bucheon.yeoddadae;

// 데이터를 모두 찾기까지 기다리는 인터페이스
public interface OnDataLoadedListener {
    void onDataLoaded(Object data);
    void onDataLoadError(String errorMessage);
}

