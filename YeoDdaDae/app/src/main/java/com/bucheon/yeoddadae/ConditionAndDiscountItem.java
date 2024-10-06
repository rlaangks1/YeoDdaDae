package com.bucheon.yeoddadae;

import com.google.firebase.Timestamp;

public class ConditionAndDiscountItem {
    String condition;
    long discount;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public long getDiscount() {
        return discount;
    }

    public void setDiscount(long discount) {
        this.discount = discount;
    }
}
