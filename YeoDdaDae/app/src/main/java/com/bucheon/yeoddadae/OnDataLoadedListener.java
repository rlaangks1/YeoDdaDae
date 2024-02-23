package com.bucheon.yeoddadae;

import java.util.List;

// 데이터를 모두 찾기까지 기다리는 인터페이스
public interface OnDataLoadedListener {
    void onSelectAllLoaded(List<Object> resultList);
    void onSelectOneLoaded(Object resultValue);
    void onDataLoadError(String errorMessage);
}

