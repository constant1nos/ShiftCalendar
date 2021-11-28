package com.batzonis.shiftcalendar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

//import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    // holds calendarDays list of shifts and colors
//    List<CalendarDay> calendarDays = new ArrayList<>();
    // created to delete a specified part of calendarDays
//    List<CalendarDay> calendarDaysSubList = new ArrayList<>();
    // holds events list of shifts and icons
    List<EventDay> events = new ArrayList<>();
    List<EventDay> eventsSubList = new ArrayList<>();
    CalendarView cal;
    AlertDialog.Builder messageBuilder;
    FloatingActionButton addNewPattern;
    // variables to hold data locally from shared preferences
    int year, dayOfYear, patternSize, shift;
    // List to hold the shifts
    List<ShiftPattern> shiftPatternList =  new ArrayList<>();
    Calendar shiftCalendar;
    // executor to make calculations of calendarDays on another thread
    ExecutorService executor = Executors.newFixedThreadPool(4);
    // handler to handle calendar's set events
    Handler handler = new Handler(Looper.getMainLooper());

    boolean initialSetup = false;   // flag to know if the initial setup is completed
    boolean patternIsSet = false;   // check if a pattern already created from shared preferences
    int calendarLeftRightValue = 0; // value to count when user moved to next or previous month
    // 2 flags to know if user moved to next or previous month of calendar view
    boolean drawForward = false, drawBackward = false;
    // counters of first and last day of calendarDays list
    int firstDayOfList, lastDayOfList;
    // holds the difference between this day's shift and pattern's remaining days
    int difference = 0;
    long startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // binding of views
        cal = findViewById(R.id.myCalendar);
        addNewPattern = findViewById(R.id.addNewPattern);
//        cal.setCalendarDayLayout(R.layout.custom_calendar_view);

        // read data from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        // Check if a pattern has already been set
        if(sharedPreferences.contains("PatternFound"))
            patternIsSet = sharedPreferences.getBoolean("PatternFound",patternIsSet);
        if(patternIsSet) {
            getShiftPatternData(sharedPreferences);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    drawShiftsToCalendar();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
//                            cal.setCalendarDays(calendarDays);
                            cal.setEvents(events);
                        }
                    });
                }
            });
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

        // add new pattern. Move to SetShiftPattern activity
       addNewPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(MainActivity.this, SetShiftPattern.class);
                startActivity(calendarIntent);
            }
        });

/*        // listener of moving to next month of calendar view
       cal.setOnForwardPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                calendarLeftRightValue++;
                drawForward = true;
                drawBackward = false;
                if(patternIsSet) {
                    drawShiftsToCalendar();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
//                            cal.setCalendarDays(calendarDays);
                            cal.setEvents(events);
                        }
                    });
                }
            }
        });

        // listener of moving to next month of calendar view
        cal.setOnPreviousPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                calendarLeftRightValue--;
                drawBackward = true;
                drawForward = false;
                if(patternIsSet) {
                    drawShiftsToCalendar();
                    handler.post(new Runnable(){
                        @Override
                        public void run() {
//                            cal.setCalendarDays(calendarDays);
                            cal.setEvents(events);
                        }
                    });
                }
            }
        });
*/
    }
    // Retrieve data from shared preferences
    public void getShiftPatternData(SharedPreferences sharedPreferences) {

        // first date of pattern (year and day of year)
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
        // holds today's shift
        int shiftThisDay;
        // firstly fill the first days from now till the end of the pattern
        if(!initialSetup) {
            shiftThisDay = ShiftPattern.findShiftThisDay(Calendar.getInstance(), year, dayOfYear, patternSize);
            difference = patternSize - shiftThisDay;
            for(int k = shiftThisDay; k < shiftPatternList.size(); k++) {
                shiftCalendar = Calendar.getInstance();
                shiftCalendar.add(Calendar.DATE, k - shiftThisDay);
                // add this date to calendarDays
//                calendarDays.add(new CalendarDay(shiftCalendar));
                setShiftEvents(k, shiftCalendar);
            }
            lastDayOfList = 0;
            while (events.size() < 400) {
                // set calendar to shift pattern's first date
                shiftCalendar = Calendar.getInstance();
                // move shiftCalendar to the next day
                shiftCalendar.add(Calendar.DATE, difference + lastDayOfList);
                // add this date to calendarDays
//                calendarDays.add(new CalendarDay(shiftCalendar));
                // setup selected calendarDay background color
                setShiftEvents(lastDayOfList % patternSize, shiftCalendar);
                lastDayOfList++;
            }
            initialSetup = true;    // declare that initial setup has been done
            lastDayOfList--;        // the last index of calendarDays after initial setup
            firstDayOfList = 0;     // the first index od calendarDays
        }
        // do if user moved to next month
        if(calendarLeftRightValue >= 1 && drawForward) {
            // delete the first 30 days of shifts on calendarDays
//            calendarDaysSubList = calendarDays.subList(0, 29);
//            calendarDaysSubList.clear();
            // delete the first 30 days of shifts on events
            eventsSubList = events.subList(0, 29);
            eventsSubList.clear();
            for(int i = 0; i < 30; i++) {
                shiftCalendar = Calendar.getInstance();
                // move shiftCalendar to the next day
                lastDayOfList++;
                firstDayOfList++;
                shiftCalendar.add(Calendar.DATE, lastDayOfList);
                // add this date to calendarDays
//                calendarDays.add(new CalendarDay(shiftCalendar));
                setShiftEvents((lastDayOfList - difference) % patternSize, shiftCalendar);
            }
            drawForward = false; // reset flag
        }
        // do if user moved to previous month
        else if(calendarLeftRightValue >= 2 && drawBackward) {
            // delete the last 30 days of shifts on calendarDays
//            calendarDaysSubList = calendarDays.subList(calendarDays.size()-30, calendarDays.size());
//            calendarDaysSubList.clear();
            // delete the last 30 days of shifts on events
            eventsSubList = events.subList(events.size()-30, events.size());
            eventsSubList.clear();
            // add 30 days at the beginning of the list
            for(int i = 0; i < 30; i++) {
                shiftCalendar = Calendar.getInstance();
                //Log.d("TEST2", "calendar get(0): "+calendarDays.get(0).getCalendar().get(Calendar.DAY_OF_YEAR));
                lastDayOfList--;
                firstDayOfList--;
                shiftCalendar.add(Calendar.DATE, firstDayOfList);
                // add this date to calendarDays
//                calendarDays.add(new CalendarDay(shiftCalendar));
                if(firstDayOfList < difference) {
                    setShiftEvents((patternSize - difference + firstDayOfList) % patternSize, shiftCalendar);
                }
                else
                    setShiftEvents((firstDayOfList - difference) % patternSize, shiftCalendar);
            }
            // sort calendarDays by date
 /*           Collections.sort(calendarDays, new Comparator<CalendarDay>() {
                @Override
                public int compare(CalendarDay o1, CalendarDay o2) {
                    return o1.getCalendar().compareTo(o2.getCalendar());
                }
            });
  */
            // sort events by date
            Collections.sort(events, new Comparator<EventDay>() {
                @Override
                public int compare(EventDay o1, EventDay o2) {
                    return o1.getCalendar().compareTo(o2.getCalendar());
                }
            });
            drawBackward = false; // reset flag
        }
    }

    // setup color and icon for a specific shift
    public void setShiftEvents(int i, Calendar calendar) {
        if(shiftPatternList.get(i).getShift() == ShiftPattern.DAY) {
//            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.day);
            events.add(new EventDay(calendar, R.drawable.day));
        }
        else if(shiftPatternList.get(i).getShift() == ShiftPattern.EVENING) {
//            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.evening);
            events.add(new EventDay(calendar, R.drawable.evening));
        }
        else if(shiftPatternList.get(i).getShift() == ShiftPattern.NIGHT) {
//            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.night);
            events.add(new EventDay(calendar, R.drawable.night));
        }
        else if(shiftPatternList.get(i).getShift() == ShiftPattern.OFF) {
//            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.off);
            events.add(new EventDay(calendar, R.drawable.off));
        }
        else {
//            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.black);
        }
    }
}