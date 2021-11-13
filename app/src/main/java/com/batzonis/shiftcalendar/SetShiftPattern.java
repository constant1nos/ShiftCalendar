package com.batzonis.shiftcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SetShiftPattern extends AppCompatActivity {

    // holds pattern
    List<CalendarDay> calendarDays = new ArrayList<>();
    // holds dates of a shift each time
    List<ShiftPattern> shiftPatternList =  new ArrayList<>();
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
        for(int i = 0; i < calendarDays.size(); i++){
            if(calendarDays.get(i).getCalendar().get(Calendar.DATE) == selectedDay.get(Calendar.DATE)){
                // Case 1: A shift has already connected with this date. Remove it from the list.
                if(i < shiftDaysCounter){
                    calendarDays.remove(i);
                    shiftPatternList.remove(i);
                    patternCalendar.setCalendarDays(calendarDays);
                    // counter of final dates should also decrease
                    shiftDaysCounter--;
                }
                // Case 2: This date is not connected with a shift (fresh pickup). Just remove it.
                else if(i >= shiftDaysCounter){
                    calendarDays.remove(i);
                    shiftPatternList.remove(i);
                    patternCalendar.setCalendarDays(calendarDays);
                }
                thisDateIsAlreadySelected = true;   // Raise flag to avoid next if statement
                break;                              // Break for loop, since same date already found
            }
        }
        if(!thisDateIsAlreadySelected){
            calendarDays.add(new CalendarDay(selectedDay));
            shiftPatternList.add(new ShiftPattern(selectedDay));
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
                                shiftPatternList.get(i).setShift(ShiftPattern.DAY);
                            }
                            completeShiftAddition();
                            break;
                        case 1: // afternoon
                            for(int i = shiftDaysCounter; i < calendarDays.size(); i++){
                                calendarDays.get(i).setBackgroundResource(R.color.evening);
                                shiftPatternList.get(i).setShift(ShiftPattern.EVENING);
                            }
                            completeShiftAddition();
                            break;
                        case 2: // night
                            for(int i = shiftDaysCounter; i < calendarDays.size(); i++){
                                calendarDays.get(i).setBackgroundResource(R.color.night);
                                shiftPatternList.get(i).setShift(ShiftPattern.NIGHT);
                            }
                            completeShiftAddition();
                            break;
                        case 3: // off
                            for(int i = shiftDaysCounter; i < calendarDays.size(); i++){
                                calendarDays.get(i).setBackgroundResource(R.color.off);
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
        patternCalendar.setCalendarDays(calendarDays);
        // Set new starting point in calendarDays list
        shiftDaysCounter = calendarDays.size();
    }

    public void doneButtonPressed(){
        // Sort the calendarDays list by date
        Collections.sort(shiftPatternList, new Comparator<ShiftPattern>() {
            @Override
            public int compare(ShiftPattern o1, ShiftPattern o2) {
                return o1.getCalendar().compareTo(o2.getCalendar());
            }
        });
        for(int i = 0; i < shiftPatternList.size(); i++){
            Log.d("SPSIZE", "Date: "+shiftPatternList.get(i).getCalendar().get(Calendar.DATE)+", shift: "+shiftPatternList.get(i).getShift());
        }
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
        for(int i = 0; i < shiftPatternList.size(); i++){
            editor.putInt("PatternDay"+i,shiftPatternList.get(i).getShift());
        }
        editor.commit();
        Intent mainActivityIntent = new Intent(SetShiftPattern.this, MainActivity.class);
        startActivity(mainActivityIntent);
    }
}