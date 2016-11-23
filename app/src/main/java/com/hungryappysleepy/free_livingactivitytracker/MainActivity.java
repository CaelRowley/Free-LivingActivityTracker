package com.hungryappysleepy.free_livingactivitytracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // Components for MainActivity
    private Spinner lyingDropdown, sittingDropdown, standingDropdown, walkingDropdown,
            conditioningDropdown, runningDropdown, sportsDropdown, otherDropdown;
    private Button buttonRecord, buttonExport, buttonExit, buttonSettings;
    private TextView textTime, recordingName;
    private Handler customHandler = new Handler();

    // Data for the timer
    private long startTime = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L, updateTime = 0L;
    private boolean isRecording = false;
    private int currentTime = 0;

    // Preference data that is read
    private int intervalTime, dateDay, dateMonth, dateYear;
    private String name;
    private boolean useInterval;

    // Keys for the saved preferences
    private final String keyName = "NAME", keyIntervalTime = "INTERVALTIME", keyDay = "DAY", keyMonth = "MONTH", keyYear = "YEAR", keyUseInterval = "USEINTERVAL";

    // Start of code for each activity
    private final String codeLying = "L", codeSitting = "S", codeStanding = "ST", codeWalking = "W", codeConditioning = "C", codeRunning = "R", codeSports = "SP", codeOther = "O";

    // Contains all the activities and their corresponding codes
    private List<String[]> activityList = new ArrayList<>();

    // ArrayLists and Strings for the current recording data
    private ArrayList<String> recordedActivityNames = new ArrayList<>();
    private ArrayList<String> recordedActivityCodes = new ArrayList<>();
    private ArrayList<String> recordedActivityTimes = new ArrayList<>();
    String recordedActivityTime = "", recordedActivityName = "", recordedActivityCode = "";

    // User input for custom activity name
    String activityNameInput;

    // Directory for the recordings (the root is /Documents)
    String recordingDirName = "FreeLivingActivityTrackerRecordings";

    // Thread to run timer
    Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuff + timeInMilliseconds;

            // Calculating seconds, minutes and hours
            int seconds = (int) updateTime / 1000;
            int minutes = seconds / 60;
            seconds %= 60;
            int hours = minutes / 60;
            minutes %= 60;

            // Updating the displayed time
            textTime.setText(hours + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            customHandler.postDelayed(this, 0);
        }
    };

    // Thread to notify user to add activity after interval time
    Runnable intervalNotificationThread = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, "Interval Passed: Add Activity", Toast.LENGTH_SHORT).show();
            customHandler.postDelayed(intervalNotificationThread, intervalTime * 1000);
        }
    };


    // Initialises everything for main menu screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialise();
        addItemsToDropdownMenus();
        setListeners();
        loadPreferences();
        populateActivityList();
    }


    // Populates ActivityList with the activity names and codes
    private void populateActivityList() {
        ArrayAdapter adapter;
        int inputSize, totalSize = 0;

        // For lying activities
        adapter = ArrayAdapter.createFromResource(this, R.array.lyingDropdownOptions, R.layout.dropdown_layout);
        inputSize = adapter.getCount();
        for (int i = 0; i < inputSize; i++) {
            activityList.add(new String[2]);
            activityList.get(totalSize + i)[0] = adapter.getItem(i).toString();
            activityList.get(totalSize + i)[1] = codeLying + i;
        }
        totalSize += inputSize;

        // For sitting activities
        adapter = ArrayAdapter.createFromResource(this, R.array.sittingDropdownOptions, R.layout.dropdown_layout);
        inputSize = adapter.getCount();
        for (int i = 0; i < inputSize; i++) {
            activityList.add(new String[2]);
            activityList.get(totalSize + i)[0] = adapter.getItem(i).toString();
            activityList.get(totalSize + i)[1] = codeSitting + i;
        }
        totalSize += inputSize;

        // For standing activities
        adapter = ArrayAdapter.createFromResource(this, R.array.standingDropdownOptions, R.layout.dropdown_layout);
        inputSize = adapter.getCount();
        for (int i = 0; i < inputSize; i++) {
            activityList.add(new String[2]);
            activityList.get(totalSize + i)[0] = adapter.getItem(i).toString();
            activityList.get(totalSize + i)[1] = codeStanding + i;
        }
        totalSize += inputSize;

        // For walking activities
        adapter = ArrayAdapter.createFromResource(this, R.array.walkingDropdownOptions, R.layout.dropdown_layout);
        inputSize = adapter.getCount();
        for (int i = 0; i < inputSize; i++) {
            activityList.add(new String[2]);
            activityList.get(totalSize + i)[0] = adapter.getItem(i).toString();
            activityList.get(totalSize + i)[1] = codeWalking + i;
        }
        totalSize += inputSize;

        // For conditioning activities
        adapter = ArrayAdapter.createFromResource(this, R.array.conditioningDropdownOptions, R.layout.dropdown_layout);
        inputSize = adapter.getCount();
        for (int i = 0; i < inputSize; i++) {
            activityList.add(new String[2]);
            activityList.get(totalSize + i)[0] = adapter.getItem(i).toString();
            activityList.get(totalSize + i)[1] = codeConditioning + i;
        }
        totalSize += inputSize;

        // For running activities
        adapter = ArrayAdapter.createFromResource(this, R.array.runningDropdownOptions, R.layout.dropdown_layout);
        inputSize = adapter.getCount();
        for (int i = 0; i < inputSize; i++) {
            activityList.add(new String[2]);
            activityList.get(totalSize + i)[0] = adapter.getItem(i).toString();
            activityList.get(totalSize + i)[1] = codeRunning + i;
        }
        totalSize += inputSize;

        // For sports activities
        adapter = ArrayAdapter.createFromResource(this, R.array.sportsDropdownOptions, R.layout.dropdown_layout);
        inputSize = adapter.getCount();
        for (int i = 0; i < inputSize; i++) {
            activityList.add(new String[2]);
            activityList.get(totalSize + i)[0] = adapter.getItem(i).toString();
            activityList.get(totalSize + i)[1] = codeSports + i;
        }
    }

    // Initialises all buttons, dropdown menus and textviews
    private void initialise() {
        // Buttons
        buttonRecord = (Button) findViewById(R.id.buttonRecord);
        buttonExport = (Button) findViewById(R.id.buttonExport);
        buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonSettings = (Button) findViewById(R.id.buttonSettings);

        // Dropdown menus
        lyingDropdown = (Spinner) findViewById(R.id.lyingDropdown);
        sittingDropdown = (Spinner) findViewById(R.id.sittingDropdown);
        standingDropdown = (Spinner) findViewById(R.id.standingDropdown);
        walkingDropdown = (Spinner) findViewById(R.id.walkingDropdown);
        conditioningDropdown = (Spinner) findViewById(R.id.conditioningDropdown);
        runningDropdown = (Spinner) findViewById(R.id.runningDropdown);
        sportsDropdown = (Spinner) findViewById(R.id.sportsDropdown);
        otherDropdown = (Spinner) findViewById(R.id.otherDropdown);

        // Text Views
        textTime = (TextView) findViewById(R.id.timerValue);
        recordingName = (TextView) findViewById(R.id.recordingName);
    }

    // Adds items to dropdown menus
    private void addItemsToDropdownMenus() {
        ArrayAdapter adapter;
        adapter = ArrayAdapter.createFromResource(this, R.array.lyingDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lyingDropdown.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(this, R.array.sittingDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sittingDropdown.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(this, R.array.standingDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        standingDropdown.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(this, R.array.walkingDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        walkingDropdown.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(this, R.array.conditioningDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conditioningDropdown.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(this, R.array.runningDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        runningDropdown.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(this, R.array.sportsDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sportsDropdown.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(this, R.array.otherDropdownOptions, R.layout.dropdown_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        otherDropdown.setAdapter(adapter);
    }

    // Creates the listeners
    private void setListeners() {
        lyingDropdown.setOnItemSelectedListener(MainActivity.this);
        sittingDropdown.setOnItemSelectedListener(MainActivity.this);
        standingDropdown.setOnItemSelectedListener(MainActivity.this);
        walkingDropdown.setOnItemSelectedListener(MainActivity.this);
        conditioningDropdown.setOnItemSelectedListener(MainActivity.this);
        runningDropdown.setOnItemSelectedListener(MainActivity.this);
        sportsDropdown.setOnItemSelectedListener(MainActivity.this);
        otherDropdown.setOnItemSelectedListener(MainActivity.this);

        // Record Button
        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If not recording, start recording otherwise stop recording
                if (!isRecording)
                    startRecording();
                else
                    stopRecording();
            }
        });

        // Saves the recordings to csv file
        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
                if (isStoragePermissionGranted())
                    exportFile();
                //Toast.makeText(MainActivity.this, "CSV File created", Toast.LENGTH_SHORT).show();
            }
        });

        // Loads a saved file
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });

        // Switches to settings activity
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newActivity = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(newActivity);
            }
        });
    }

    // Loads the users preferences for recording and saving the file
    public void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("recordSettings", Context.MODE_PRIVATE);
        // Loads interval settings
        useInterval = sharedPreferences.getBoolean(keyUseInterval, false);
        intervalTime = sharedPreferences.getInt(keyIntervalTime, 0);

        // Loads Date
        dateDay = sharedPreferences.getInt(keyDay, 0);
        dateMonth = sharedPreferences.getInt(keyMonth, 0);
        dateYear = sharedPreferences.getInt(keyYear, 0);

        // Loads Name
        name = sharedPreferences.getString(keyName, "Recording");

        recordingName.setText(name);
    }


    // Starts new recording and wipes previous record data
    public void startRecording() {
        emptyRecording();

        isRecording = true;
        currentTime = 0;
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

        if (useInterval)
            customHandler.postDelayed(intervalNotificationThread, intervalTime * 1000);

        ((TextView) findViewById(R.id.buttonRecord)).setText("Stop");
    }

    // Stops the recording
    public void stopRecording() {
        isRecording = false;
        customHandler.removeCallbacks(updateTimerThread);

        if (useInterval)
            customHandler.removeCallbacks(intervalNotificationThread);

        ((TextView) findViewById(R.id.buttonRecord)).setText("Record");
    }

    // Empties the current recordings
    public void emptyRecording() {
        recordedActivityNames = new ArrayList<>();
        recordedActivityCodes = new ArrayList<>();
        recordedActivityTimes = new ArrayList<>();
    }

    private void addRecording(TextView selectedText) {
        // If the user isn't using intervals then read current time or else use interval times
        if (!useInterval) {
            recordedActivityTime = textTime.getText().toString();
        } else {
            // Calculates interval time
            currentTime += intervalTime;
            int seconds = currentTime;
            seconds %= 60;
            int minutes = currentTime / 60;
            int hours = minutes / 60;
            minutes %= 60;
            recordedActivityTime = hours + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        }
        // adds time to the list
        recordedActivityTimes.add(recordedActivityTime);

        // Checks if the user input an item or wants custom input
        if (!selectedText.getText().toString().equals("Other: User Input")) {
            // Reads the input
            recordedActivityName = selectedText.getText().toString();
            // Finds matching code for activity
            for (int i = 0; i < activityList.size(); i++) {
                if (activityList.get(i)[0].equals(recordedActivityName)) {
                    recordedActivityCode = activityList.get(i)[1];
                    i = activityList.size();
                }
            }

            // Adds activity name and code to their corresponding lists
            recordedActivityNames.add(recordedActivityName);
            recordedActivityCodes.add(recordedActivityCode);

            // Notifies user of their choice
            Toast.makeText(this, selectedText.getText().toString() + " Added", Toast.LENGTH_SHORT).show();
        } else {
            displayActivityInput();
        }
    }

    // Saves the recording to csv file
    public void exportFile() {
        String storageState = Environment.getExternalStorageState();
        // Checks of storage is available

        if (Environment.MEDIA_MOUNTED.equals(storageState)) {

            // Creates the directory to store files
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File recordingDir = new File(root, recordingDirName);
            if (!recordingDir.exists()) {
                Toast.makeText(getApplicationContext(), "Directory Made", Toast.LENGTH_SHORT).show();
                recordingDir.mkdir();
            }

            // Creates titles for columns
            String outputString = "Time,Activity Name,Code\n";

            // Generates the output for the csv file
            for (int i = 0; i < recordedActivityNames.size(); i++) {
                outputString += recordedActivityTimes.get(i) + "," + recordedActivityNames.get(i) + "," + recordedActivityCodes.get(i) + "\n";
            }

            // Creates the name for the csv file and creates export file
            String fileName = name + "_" + dateDay + "_" + dateMonth + "_" + dateYear + ".csv";
            File exportFile = new File(recordingDir, fileName);

            FileOutputStream fileOutputStream;
            // Writes the file
            try {
                fileOutputStream = new FileOutputStream(exportFile);
                fileOutputStream.write(outputString.toString().getBytes());
                fileOutputStream.close();
                Toast.makeText(getApplicationContext(), "Recording Exported", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "File not found exception", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "IO Exception", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "External storage not available", Toast.LENGTH_LONG).show();
        }
    }

    // When a dropdown menu item is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // To prevent the default position from being used
        if (position == 0)
            return;

        // Gets text selected from dropdown
        TextView selectedText = (TextView) view;
        // Adds selection to recording
        if(isRecording)
            addRecording(selectedText);
        else
            Toast.makeText(getApplicationContext(), "You are not currently recording", Toast.LENGTH_SHORT).show();

        // To reset each dropdown menu after use
        parent.setSelection(0);
    }

    private void displayActivityInput() {
        // Gets prompt_activity_input
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.prompt_activity_input, null);

        // Sets prompt_activity_input to alert dialog builder view
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText userInput = (EditText) promptView.findViewById(R.id.editTextUserInput);

        // Shows user the input dialog
        alertDialogBuilder.setCancelable(false).setPositiveButton("Record", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // get user input and set it to result
                activityNameInput = userInput.getText().toString();
                // Adds recordings to the recorded lists
                recordedActivityNames.add(activityNameInput);
                recordedActivityCodes.add(codeOther);

                // Notifies the user of their choice
                Toast.makeText(MainActivity.this, activityNameInput + " Added", Toast.LENGTH_SHORT).show();
            }
        });

        // Display the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            exportFile();
        }
    }
}
