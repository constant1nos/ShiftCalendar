package com.batzonis.shiftcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SetShiftPattern extends AppCompatActivity {

    // holds pattern
    List<EventDay> events = new ArrayList<>();
    // holds dates of a shift each time
    List<ShiftPattern> shiftPatternList =  new ArrayList<>();
    // the views
    CalendarView patternCalendar;
    Button addShiftButton, doneButton;
    // Two AlertDialogs. One for shift selection and one for messages
    AlertDialog.Builder shiftBuilder, messageBuilder;

    EventDay event;
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
        // Do when done button is pressed
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneButtonPressed();
            }
        });
    }

    // Manage selected dates to add them in a shift
    public void onDayClickInstructions(EventDay eventDay){
        selectedDay = eventDay.getCalendar();
        // Scan calendarDays list and check if this date is already selected. There are 2 cases.
        for(int i = 0; i < events.size(); i++){
            if(events.get(i).getCalendar().get(Calendar.DATE) == selectedDay.get(Calendar.DATE)){
                // Case 1: A shift has already connected with this date. Remove it from the list.
                if(i < shiftDaysCounter){
                    events.remove(i);
                    shiftPatternList.remove(i);
                    patternCalendar.setEvents(events);
                    // counter of final dates should also decrease
                    shiftDaysCounter--;
                }
                // Case 2: This date is not connected with a shift (fresh pickup). Just remove it.
                else if(i >= shiftDaysCounter){
                    events.remove(i);
                    shiftPatternList.remove(i);
                    patternCalendar.setEvents(events);
                }
                thisDateIsAlreadySelected = true;   // Raise flag to avoid next if statement
                break;                              // Break for loop, since same date already found
            }
        }
        if(!thisDateIsAlreadySelected){
            events.add(new EventDay(selectedDay,R.color.selected_date));
            shiftPatternList.add(new ShiftPattern(selectedDay));
            patternCalendar.setEvents(events);

        }
        thisDateIsAlreadySelected = false; //reset flag
    }

    // Open dialog for user to select shift and add selected dates to shift pattern
    public void addShiftToPattern(){
        // Check if there is at least one date selected by user
        if(events.size() > shiftDaysCounter){
            shiftBuilder.setItems(R.array.shifts, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0: // day
                            for(int i = shiftDaysCounter; i < events.size(); i++){
                                selectedDay = Calendar.getInstance();
                                selectedDay = events.get(i).getCalendar();
                                events.set(i,(new EventDay(selectedDay,R.color.day)));
                                shiftPatternList.get(i).setShift(ShiftPattern.DAY);
                            }
                            completeShiftAddition();
                            break;
                        case 1: // afternoon
                            for(int i = shiftDaysCounter; i < events.size(); i++){
                                selectedDay = Calendar.getInstance();
                                selectedDay = events.get(i).getCalendar();
                                events.set(i,(new EventDay(selectedDay,R.color.evening)));
                                shiftPatternList.get(i).setShift(ShiftPattern.EVENING);
                            }
                            completeShiftAddition();
                            break;
                        case 2: // night
                            for(int i = shiftDaysCounter; i < events.size(); i++){
                                selectedDay = Calendar.getInstance();
                                selectedDay = events.get(i).getCalendar();
                                events.set(i,(new EventDay(selectedDay,R.color.night)));
                                shiftPatternList.get(i).setShift(ShiftPattern.NIGHT);
                            }
                            completeShiftAddition();
                            break;
                        case 3: // off
                            for(int i = shiftDaysCounter; i < events.size(); i++){
                                selectedDay = Calendar.getInstance();
                                selectedDay = events.get(i).getCalendar();
                                events.set(i,(new EventDay(selectedDay,R.color.off)));
                                shiftPatternList.get(i).setShift(ShiftPattern.OFF);
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
            // Message if no date selected
            messageBuilder.setMessage(R.string.dialog_alert_message)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // nothing happens here
                        }
                    });
            // create and show the alert dialog
            AlertDialog messageDialog = messageBuilder.create();
            messageDialog.show();
       }
   }

    // Simple function to set CalendarView new colors, add dates to pattern and clear tempCalDays
    public void completeShiftAddition(){
        patternCalendar.setEvents(events);
        // Set new starting point in calendarDays list
        shiftDaysCounter = events.size();
   }

    public void doneButtonPressed(){
        // Sort the calendarDays list by date
        Collections.sort(shiftPatternList, new Comparator<ShiftPattern>() {
            @Override
            public int compare(ShiftPattern o1, ShiftPattern o2) {
                return o1.getCalendar().compareTo(o2.getCalendar());
            }
        });
        // Store to shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences),MODE_PRIVATE);
        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Store flag that a pattern is created
        editor.putBoolean("PatternFound",true);
        // Store pattern's year
        editor.putInt("PatternYear",shiftPatternList.get(0).getCalendar().get(Calendar.YEAR));
        // Store pattern's DAY_OF_YEAR. This is the first day of pattern
        editor.putInt("PatternDayOfYear",shiftPatternList.get(0).getCalendar().get(Calendar.DAY_OF_YEAR));
        // Store pattern's size (total days)
        editor.putInt("PatternSize",shiftPatternList.size());
        for(int i = 0; i < shiftPatternList.size(); i++){
            editor.putInt("PatternDay"+i,shiftPatternList.get(i).getShift());
        }
        editor.commit();
        Intent mainActivityIntent = new Intent(SetShiftPattern.this, MainActivity.class);
        startActivity(mainActivityIntent);
    }
}