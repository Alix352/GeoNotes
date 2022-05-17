package com.example.notesapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddNoteMaps extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{
    private static final String TAG = "AddNote";
    private Toolbar toolbar;
    private EditText noteTitle,noteDetails;
    private Calendar c;
    private String todaysDate;
    private String currentTime;
    private Button btn_timePicker;

    private String noteType;
    private int day = 0;
    private int month = 0;
    private int year = 0;
    private int hour = 0;
    private int minute = 0;

    private int savedDay = 0;
    private int savedMonth = 0;
    private int savedYear = 0;
    private int savedHour = 0;
    private int savedMinute = 0;

    NotificationManagerCompat notificationManager;

    FirebaseAuth fAuth;
    DatabaseReference reference;
    DatabaseReference mapsRef;
    DatabaseReference alarmRef;

    private Double mLatitude;
    private Double mLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note_map);

        toolbar =findViewById(R.id.toolbarMap);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Note");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        mLatitude = getIntent().getDoubleExtra("lat", 0);
        mLongitude = getIntent().getDoubleExtra("long", 0);
        Log.d(TAG, "onCreate: long: " + mLongitude + " lat: " + mLatitude);

        notificationManager = NotificationManagerCompat.from(this);

        noteTitle = findViewById(R.id.noteTitleMap);
        noteDetails = findViewById(R.id.noteDetailsMap);
        btn_timePicker = findViewById(R.id.btn_timePickerMap);
        noteType = "M";

        fAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
        mapsRef = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("PendingMaps").child(fAuth.getCurrentUser().getUid());
        alarmRef = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Alarms").child(fAuth.getCurrentUser().getUid());

        pickDate();

        noteTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() !=0){
                    getSupportActionBar().setTitle(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //get current date and time
        c = Calendar.getInstance();
        todaysDate = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.DAY_OF_MONTH);
        currentTime = pad(c.get(Calendar.HOUR))+":"+pad(c.get(Calendar.MINUTE));

        Log.d("calendar", "Date and Time: " + todaysDate + " and " + currentTime);
    }

    private void getDateTimeCalendar(){
        Calendar cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        hour = cal.get(Calendar.HOUR);
        minute = cal.get(Calendar.MINUTE);
    }

    private void pickDate(){
        //pop up calendar
        btn_timePicker.setOnClickListener(view -> {
            getDateTimeCalendar();
            new DatePickerDialog(this, this, year, month, day).show();
        });
    }

    private String pad(int i) {
        if(i<10){
            return "0" + i;
        }
        return String.valueOf(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.save) {
            //create a note and then store it in database
            Note note = null;
            try {
                note = new Note(noteTitle.getText().toString(), AESUtils.encrypt(noteDetails.getText().toString()), todaysDate, currentTime, noteType, savedDay, savedMonth, savedYear, savedHour, savedMinute);
            } catch (Exception e) {
                e.printStackTrace();
            }

            DatabaseReference newNoteRef = reference.push();

            newNoteRef.setValue(note);

            MapsPos temp = new MapsPos(mLatitude, mLongitude, note.getTitle());

            DatabaseReference newMapsRef = mapsRef.push();
            newMapsRef.setValue(temp);

            setAlarm(); //set alarm for notifications


            Toast.makeText(this, "save btn: " + savedMinute, Toast.LENGTH_SHORT).show();
            goToMain();
        }
        if(item.getItemId() == R.id.delete) {
            Toast.makeText(this, "Not saved note", Toast.LENGTH_SHORT).show();
            goToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToMain() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        savedDay = i2;
        savedMonth = i1;
        savedYear = i;

        getDateTimeCalendar();
        new TimePickerDialog(this, this, hour, minute, true).show();

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        savedHour = i;
        savedMinute = i1;
    }

    public void setAlarm(){
        //calculate exact time from calendar
        Calendar alarmDate = Calendar.getInstance();
        alarmDate.setTimeInMillis(System.currentTimeMillis());
        alarmDate.set(savedYear, savedMonth, savedDay, savedHour, savedMinute, 1);
        long temp = alarmDate.getTimeInMillis();

        if(temp > System.currentTimeMillis()){
            Alarm newAlarm = new Alarm(temp, noteTitle.getText().toString());
            DatabaseReference newUserRef = alarmRef.push();
            newUserRef.setValue(newAlarm);
        }
    }

}
