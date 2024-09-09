package com.bucheon.yeoddadae;

import android.graphics.Typeface;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class BoldTextDecorator implements DayViewDecorator {

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true; // 모든 날짜에 대해 적용
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new android.text.style.StyleSpan(Typeface.BOLD)); // 텍스트를 두껍게
    }
}
