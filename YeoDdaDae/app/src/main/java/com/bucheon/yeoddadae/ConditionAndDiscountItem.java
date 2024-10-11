package com.bucheon.yeoddadae;

public class ConditionAndDiscountItem {
    String condition = "";
    long discount = -1;

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
