package com.batzonis.shiftcalendar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<CalendarDay> calendarDays = new ArrayList<>();
    List<Calendar> calendars = new ArrayList<>();
    List<EventDay> events = new ArrayList<>();
    CalendarView cal;
    AlertDialog.Builder messageBuilder;
    FloatingActionButton addNewPattern;
    int year, dayOfYear, patternSize, shift;
    int numberOfLoops = 10; // how many times the pattern will cover calendarView
    // List to hold the shifts
    List<ShiftPattern> shiftPatternList =  new ArrayList<>();
    Calendar shiftCalendar;

    boolean pattern = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cal = findViewById(R.id.myCalendar);
        addNewPattern = findViewById(R.id.addNewPattern);
        cal.setCalendarDayLayout(R.layout.custom_calendar_view);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        // Check if a pattern has already been set
        if(sharedPreferences.contains("PatternFound"))
            pattern = sharedPreferences.getBoolean("PatternFound",pattern);
        if(pattern) {
            getShiftPatternData(sharedPreferences);
            drawShiftsToCalendar();
        }
        else {
            // Show message that no stored pattern found
            messageBuilder = new AlertDialog.Builder(this);
            messageBuilder.setTitle(R.string.dialog_alert_title);
            messageBuilder.setMessage(R.string.dialog_alert_no_pattern)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // nothing happens here
                        }
                    });
            // create and show the alert dialog
            AlertDialog messageDialog = messageBuilder.create();
            messageDialog.show();
        }

        addNewPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(MainActivity.this, SetShiftPattern.class);
                startActivity(calendarIntent);
            }
        });

    }

    // Retrieve data from shared preferences
    public void getShiftPatternData(SharedPreferences sharedPreferences) {

        year = sharedPreferences.getInt("PatternYear" ,year);
        dayOfYear = sharedPreferences.getInt("PatternDayOfYear", dayOfYear);
        patternSize = sharedPreferences.getInt("PatternSize", patternSize);
        // save shifts on the list
        for(int i = 0; i < patternSize; i++) {
            shift = sharedPreferences.getInt("PatternDay"+i,shift);
            shiftPatternList.add(new ShiftPattern(shift));
        }

    }

    // change color and icon on each date, base on the shift
    public void drawShiftsToCalendar() {

        // firstly fill the first days from now till the end of the pattern
        int shiftThisDay = findShiftThisDay(Calendar.getInstance());
        for(int k = shiftThisDay; k < shiftPatternList.size(); k++) {
            shiftCalendar = Calendar.getInstance();
            shiftCalendar.add(Calendar.DATE, k-shiftThisDay);
            // add this date to calendarDays
            calendarDays.add(new CalendarDay(shiftCalendar));
            setShiftEvents(k, shiftCalendar);
        }
        // then fill calendar with the shifts, for as many days as numberOfLoops
        for(int i = 0; i <= numberOfLoops; i++) {
            for(int j = 0; j < shiftPatternList.size(); j++) {
                // set calendar to shift pattern's first date
                shiftCalendar = Calendar.getInstance();
                // move shiftCalendar to the next day
                shiftCalendar.add(Calendar.DATE, (i*patternSize)+j+(patternSize-shiftThisDay));
                // add this date to calendarDays
                calendarDays.add(new CalendarDay(shiftCalendar));
                // setup selected calendarDay background color
                setShiftEvents(j, shiftCalendar);
            }
        }
        cal.setCalendarDays(calendarDays);
        cal.setEvents(events);
    }

    public void setShiftEvents(int i, Calendar calendar) {
        if(shiftPatternList.get(i).getShift() == ShiftPattern.DAY) {
            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.day);
            events.add(new EventDay(calendar, R.drawable.day));
        }
        else if(shiftPatternList.get(i).getShift() == ShiftPattern.EVENING) {
            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.evening);
            events.add(new EventDay(calendar, R.drawable.evening));
        }
        else if(shiftPatternList.get(i).getShift() == ShiftPattern.NIGHT) {
            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.night);
            events.add(new EventDay(calendar, R.drawable.night));
        }
        else if(shiftPatternList.get(i).getShift() == ShiftPattern.OFF) {
            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.off);
            events.add(new EventDay(calendar, R.drawable.off));
        }
        else {
            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.black);
        }
    }

    // finds the day in the pattern for a specific date
    public int findShiftThisDay(Calendar thisDay) {

        int thisYear, thisDayOfYear, dayInPattern = 0;

        thisYear = thisDay.get(Calendar.YEAR);
        thisDayOfYear = thisDay.get(Calendar.DAY_OF_YEAR);

        // check if wanted year is greater than pattern's year
        if(thisYear == year) {
            dayInPattern = (thisDayOfYear - dayOfYear)%patternSize;
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
            dayInPattern = dayInPattern%patternSize;
        }
        return dayInPattern;
    }

    // returns the total days of selected year
    public int totalDaysOfYear(int thisYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, thisYear);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }
}