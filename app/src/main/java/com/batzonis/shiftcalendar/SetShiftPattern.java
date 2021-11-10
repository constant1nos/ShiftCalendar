package com.batzonis.shiftcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SetShiftPattern extends AppCompatActivity {

    // holds pattern
    List<CalendarDay> calendarDays = new ArrayList<>();
    // holds dates of a shift each time
    List<CalendarDay> temporaryCalendarDays = new ArrayList<>();
    List<Calendar> calendars = new ArrayList<>();
    List<EventDay> events = new ArrayList<>();
    CalendarView patternCalendar;
    Button addShiftButton, doneButton;
    // Two AlertDialogs. One for shift selection and one for messages
    AlertDialog.Builder shiftBuilder, messageBuilder;
    Calendar selectedDay;
    int shiftDaysCounter = 0;                   // Starting point of a new shift in calendarDays list
    boolean thisDateIsAlreadySelected = false;  // flag to check if a date is already selected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_shift_pattern);

        patternCalendar = findViewById(R.id.myPatternCalendar);
        addShiftButton = findViewById(R.id.addShiftButton);
        doneButton = findViewById(R.id.doneButton);

        // Dialogs initialisation
        shiftBuilder = new AlertDialog.Builder(this);
        shiftBuilder.setTitle(R.string.dialog_shift_title);

        messageBuilder = new AlertDialog.Builder(this);
        messageBuilder.setTitle(R.string.dialog_alert_title);

        // Do when a date is selected
        patternCalendar.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(@NonNull EventDay eventDay) {
                onDayClickInstructions(eventDay);
            }
        });
        // Do when add shift button is pressed
        addShiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addShiftToPattern();
            }
        });

    }

    // Manage selected dates to add them in a shift
    public void onDayClickInstructions(EventDay eventDay){
        selectedDay = eventDay.getCalendar();
        // Check if the date is already selected. If it is, reset colors and raise flag
        for(int i = 0; i < calendarDays.size(); i++){
            if(calendarDays.get(i).getCalendar().get(Calendar.DATE) == selectedDay.get(Calendar.DATE)){
                if(i < shiftDaysCounter){
                    calendarDays.remove(i);
                    patternCalendar.setCalendarDays(calendarDays);
                    shiftDaysCounter--;
                }
                else if(i >= shiftDaysCounter){
                    calendarDays.remove(i);
                    patternCalendar.setCalendarDays(calendarDays);
                }
                thisDateIsAlreadySelected = true;
                break;
            }
        }
        if(!thisDateIsAlreadySelected){
            calendarDays.add(new CalendarDay(selectedDay));
            calendarDays.get(calendarDays.size()-1).setBackgroundResource(R.color.selected_date);
            patternCalendar.setCalendarDays(calendarDays);
        }
        thisDateIsAlreadySelected = false; //reset flag
    }

    // Open dialog for user to select shift and add selected dates to shift pattern
    public void addShiftToPattern(){
        // Check if there is at least one date selected by user
        if(calendarDays.size() > shiftDaysCounter){
            shiftBuilder.setItems(R.array.shifts, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0: // day
                            for(int i = shiftDaysCounter; i < calendarDays.size(); i++){
                                calendarDays.get(i).setBackgroundResource(R.color.day);
                            }
                            completeShiftAddition();
                            break;
                        case 1: // afternoon
                            for(int i = shiftDaysCounter; i < calendarDays.size(); i++){
                                calendarDays.get(i).setBackgroundResource(R.color.evening);
                            }
                            completeShiftAddition();
                            break;
                        case 2: // night
                            for(int i = shiftDaysCounter; i < calendarDays.size(); i++){
                                calendarDays.get(i).setBackgroundResource(R.color.night);
                            }
                            completeShiftAddition();
                            break;
                        case 3: // off
                            for(int i = shiftDaysCounter; i < calendarDays.size(); i++){
                                calendarDays.get(i).setBackgroundResource(R.color.off);
                            }
                            completeShiftAddition();
                            break;
                    }
                }
            });
            // create and show the alert dialog
            AlertDialog shiftDialog = shiftBuilder.create();
            shiftDialog.show();
        }
        else{
            messageBuilder.setMessage(R.string.dialog_alert_message)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            // create and show the alert dialog
            AlertDialog messageDialog = messageBuilder.create();
            messageDialog.show();
        }
    }

    // Simple function to set CalendarView new colors, add dates to pattern and clear tempCalDays
    public void completeShiftAddition(){
        patternCalendar.setCalendarDays(calendarDays);
        shiftDaysCounter = calendarDays.size();         // Set new starting point in calendarDays list
    }
}