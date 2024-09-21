package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class UserItem {
    String id;
    String email;
    long ydPoint;
    Timestamp registerTime;

    public UserItem(String id, String email, long ydPoint, Timestamp registerTime) {
        this.id = id;
        this.email = email;
        this.ydPoint = ydPoint;
        this.registerTime = registerTime;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public long getYdPoint() {
        return ydPoint;
    }

    public Timestamp getRegisterTime() {
        return registerTime;
    }
}