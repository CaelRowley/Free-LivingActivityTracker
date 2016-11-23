package com.hungryappysleepy.free_livingactivitytracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    // Layout features
    private Button buttonExit, buttonSave;
    private Switch switchInterval;
    private DatePicker datePicker;
    private EditText editTextInterval, editTextName;

    // Preference data that is written
    private boolean useInterval = false;
    private String inputName;
    private int inputDay, inputMonth, inputYear, inputIntervalTime;

    // Keys for the saved preferences
    private final String keyName = "NAME", keyIntervalTime = "INTERVALTIME", keyDay = "DAY", keyMonth = "MONTH", keyYear = "YEAR", keyUseInterval = "USEINTERVAL";


    // Initialises everything for settings screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialise();
        setListeners();
    }


    private void initialise() {
        // Buttons
        buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonSave = (Button) findViewById(R.id.buttonSave);

        // Switches
        switchInterval = (Switch) findViewById(R.id.switchInterval);

        // EditTexts
        editTextInterval = (EditText) findViewById(R.id.inputInterval);
        editTextName = (EditText) findViewById(R.id.inputName);

        // DatePickers
        datePicker = (DatePicker) findViewById(R.id.datePicker);
    }

    // Creates the listeners
    private void setListeners() {
        // Switches to main activity
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newActivity = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(newActivity);
            }
        });

        // Saves settings and switches to main activity
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readUserInput();
                savePreferences();

                // Notifies the user that their settings are saved
                Toast.makeText(SettingsActivity.this, "Settings Saved", Toast.LENGTH_SHORT).show();

                // Switches to the main activity
                Intent newActivity = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(newActivity);
            }
        });

        switchInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                useInterval = isChecked;
            }
        });
    }

    // Reads the users input data
    private void readUserInput() {
        inputIntervalTime = Integer.parseInt(editTextInterval.getText().toString());
        inputName = editTextName.getText().toString();

        // Datepicker defaults to current date
        inputDay = datePicker.getDayOfMonth();
        inputMonth = datePicker.getMonth();
        inputYear = datePicker.getYear();
    }

    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("recordSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Saves interval settings
        editor.putBoolean(keyUseInterval, useInterval);
        editor.putInt(keyIntervalTime, inputIntervalTime);

        // Saves Date
        editor.putInt(keyDay, inputDay);
        editor.putInt(keyMonth, inputMonth);
        editor.putInt(keyYear, inputYear);

        // Saves Name
        editor.putString(keyName, inputName);

        editor.apply();
    }
}
