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

        for(int i = 0; i <= numberOfLoops; i++) {
            for(int j = 0; j < shiftPatternList.size(); j++) {
                // set calendar to shift pattern's first date
                shiftCalendar = Calendar.getInstance();
                shiftCalendar.set(Calendar.YEAR, year);
                shiftCalendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
                // move shiftCalendar to the next day
                shiftCalendar.add(Calendar.DATE, (i*patternSize)+j);
                // add this date to calendarDays
                calendarDays.add(new CalendarDay(shiftCalendar));
                // setup selected calendarDay background color
                if(shiftPatternList.get(j).getShift() == ShiftPattern.DAY) {
                    calendarDays.get((i*patternSize)+j).setBackgroundResource(R.color.day);
                }
                else if(shiftPatternList.get(j).getShift() == ShiftPattern.EVENING) {
                    calendarDays.get((i*patternSize)+j).setBackgroundResource(R.color.evening);
                }
                else if(shiftPatternList.get(j).getShift() == ShiftPattern.NIGHT) {
                    calendarDays.get((i*patternSize)+j).setBackgroundResource(R.color.night);
                }
                else if(shiftPatternList.get(j).getShift() == ShiftPattern.OFF) {
                    calendarDays.get((i*patternSize)+j).setBackgroundResource(R.color.off);
                }
                else {
                    calendarDays.get((i*patternSize)+j).setBackgroundResource(R.color.black);
                }
            }
        }
        cal.setCalendarDays(calendarDays);
    }
}