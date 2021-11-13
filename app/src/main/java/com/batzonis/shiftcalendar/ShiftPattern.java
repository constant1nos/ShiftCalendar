package com.batzonis.shiftcalendar;

import com.applandeo.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShiftPattern {

    // Constants for the shift selection and storage to shared preferences
    public static final int DAY = 0;
    public static final int EVENING = 1;
    public static final int NIGHT = 2;
    public static final int OFF = 3;
    public static final int CUSTOM = 4;

    private Calendar calendar;
    private int shift;

    public ShiftPattern(Calendar calendar) {
        this.calendar = calendar;
    }

    public ShiftPattern(Calendar calendar, int shift) {
        this.calendar = calendar;
        this.shift = shift;
    }

    public static int getDAY() {
        return DAY;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }
}
