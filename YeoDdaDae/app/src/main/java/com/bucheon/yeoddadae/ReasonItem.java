package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class ReasonItem {
    String id;
    String reason;
    Timestamp reasonUpTime;

    public ReasonItem(String id, String reason, Timestamp reasonUpTime) {
        this.id = id;
        this.reason = reason;
        this.reasonUpTime = reasonUpTime;
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public Timestamp getReasonUpTime() {
        return reasonUpTime;
    }
}
