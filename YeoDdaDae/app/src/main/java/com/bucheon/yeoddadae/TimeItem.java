package com.bucheon.yeoddadae;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.time.LocalTime;

public class TimeItem {
    private CalendarDay date;
    private String startTime;
    private String endTime;

    public TimeItem(CalendarDay date) {
        this.date = date;
        this.startTime = "0000";
        this.endTime = "0000";
    }

    public CalendarDay getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
