package com.bucheon.yeoddadae;

import java.text.ParseException;

// 파이어스토어 데이터를 모두 찾기까지 기다리는 인터페이스
public interface OnFirestoreDataLoadedListener {
    void onDataLoaded(Object data);
    void onDataLoadError(String errorMessage);
}

