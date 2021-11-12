package com.batzonis.shiftcalendar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    int year;
    AlertDialog.Builder messageBuilder;
    FloatingActionButton addNewPattern;
    Calendar today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cal = findViewById(R.id.myCalendar);
        addNewPattern = findViewById(R.id.addNewPattern);
        cal.setCalendarDayLayout(R.layout.custom_calendar_view);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        if(sharedPreferences.contains("PatternFound")) {
            sharedPreferences.getInt("PatternYear",year);
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