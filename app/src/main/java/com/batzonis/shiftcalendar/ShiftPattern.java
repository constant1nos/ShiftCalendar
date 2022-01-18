package com.batzonis.shiftcalendar;

import java.util.Calendar;

/* This class created to store shifts and dates. Initial goal was
*  to extend EventDay class, but this is not possible because it
*  is final */

public class ShiftPattern {

    // Constants for the shift selection and storage to shared preferences
    public static final int DAY = 0;
    public static final int EVENING = 1;
    public static final int NIGHT = 2;
    public static final int OFF = 3;
    public static final int CUSTOM = 4;

    private Calendar calendar;
    private int shift;

    public ShiftPattern() {}

    public ShiftPattern(int shift){ this.shift = shift; }

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

    // finds the day in the pattern for a specific date
    public static int findShiftThisDay(Calendar thisDay, int year, int dayOfYear, int patternSize) {

        int thisYear, thisDayOfYear, dayInPattern = 0;

        thisYear = thisDay.get(Calendar.YEAR);
        thisDayOfYear = thisDay.get(Calendar.DAY_OF_YEAR);

        // check if wanted year is greater than pattern's year
        if(thisYear == year) {
            if(thisDayOfYear >= dayOfYear)
                dayInPattern = (thisDayOfYear - dayOfYear) % patternSize;
            else{
                dayInPattern = dayOfYear - thisDayOfYear;
                while(dayInPattern > patternSize)
                    dayInPattern -= patternSize;
                dayInPattern = patternSize - dayInPattern;
            }
        }
        else if(thisYear > year) {
            // take the remaining days of pattern's year
            dayInPattern = totalDaysOfYear(year) - dayOfYear;
            // add all days from full years
            for(int i = year+1; i < thisYear; i++) {
                dayInPattern += totalDaysOfYear(i);
            }
            // add days from the beginning of selected year, till DAY_OF_YEAR
            dayInPattern += thisDayOfYear;
            dayInPattern = dayInPattern % patternSize;
        }
        return dayInPattern;
    }

    // returns the total days of selected year
    private static int totalDaysOfYear(int thisYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, thisYear);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }
}
