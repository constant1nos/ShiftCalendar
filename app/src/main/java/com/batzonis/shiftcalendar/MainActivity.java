package com.batzonis.shiftcalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
    FloatingActionButton addNewPattern;
    Calendar today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cal = findViewById(R.id.myCalendar);
        addNewPattern = findViewById(R.id.addNewPattern);
        cal.setCalendarDayLayout(R.layout.custom_calendar_view);

        addNewPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(MainActivity.this, SetShiftPattern.class);
                startActivity(calendarIntent);
            }
        });

/* Start test: fill day cells manually */
//        for(int i = 1; i<30; i++){
//            today = Calendar.getInstance();
//            today.add(Calendar.DATE,i);
//            events.add(new EventDay(today,R.drawable.day));
//            calendarDays.add(new CalendarDay(today));
//            calendarDays.get(i-1).setLabelColor(R.color.white);
//            calendarDays.get(i-1).setBackgroundResource(R.color.purple_200);
//        }
//        cal.setEvents(events);
//        cal.setCalendarDays(calendarDays);
/* End of test*/

    }
}