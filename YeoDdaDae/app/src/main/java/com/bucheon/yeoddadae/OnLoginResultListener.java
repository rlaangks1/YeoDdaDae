package com.bucheon.yeoddadae;

public interface OnLoginResultListener {
    void onLoginSuccess();

    void onLoginFailure(String errorMessage);
}