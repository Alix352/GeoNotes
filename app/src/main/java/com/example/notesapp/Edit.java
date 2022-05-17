package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

public class Edit extends AppCompatActivity {

    private static final String TAG = "Edit";

    Toolbar toolbar;
    EditText noteTitle,noteDetails;
    Calendar c;
    String todaysDate;
    String currentTime;
    Note note;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase db;
    DatabaseReference reference;
    String title;
    String time;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent i = getIntent();
        title = i.getStringExtra("Title");
        time = i.getStringExtra("Time");
        date = i.getStringExtra("Date");

        note = new Note();

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/");
        reference = db.getReference().child("Notes");

        reference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child: snapshot.getChildren()){
                    Note tempNote = child.getValue(Note.class);
                    Log.d(TAG, "onDataChange: " + tempNote.getID() + ", " + tempNote.getTitle());
                    if(tempNote.getDate().equals(date) && tempNote.getTime().equals(time) && tempNote.getTitle().equals(title)){
                        note = tempNote;
                        Log.d(TAG, "onDataChange: new note: " + note.getTitle());
                        getSupportActionBar().setTitle(note.getTitle());
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        noteTitle.setText(note.getTitle());
                        try {
                            noteDetails.setText(AESUtils.decrypt(note.getContent()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);


        noteTitle = findViewById(R.id.noteTitle);
        noteDetails = findViewById(R.id.noteDetails);

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
            note.setTitle(noteTitle.getText().toString());
            note.setContent(noteDetails.getText().toString());

            reference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot child: snapshot.getChildren()) {
                        Note tempNote = child.getValue(Note.class);
                        Log.d(TAG, "onDataChangeEdit: " + tempNote.getTitle());
                        if (tempNote.getDate().equals(date) && tempNote.getTime().equals(time) && tempNote.getTitle().equals(title)) {
                            tempNote.setTitle(noteTitle.getText().toString());
                            try {
                                tempNote.setContent(AESUtils.encrypt(noteDetails.getText().toString()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            tempNote.setDate(todaysDate);
                            tempNote.setTime(currentTime);
                            Log.d(TAG, "onDataChangeEdited: " + tempNote.getTitle());
                            child.getRef().setValue(tempNote);
                            return;
                        }
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            Intent i = new Intent(getApplicationContext(),Details.class);
            i.putExtra("Date", todaysDate);
            i.putExtra("Time", currentTime);
            i.putExtra("Title", note.getTitle());
            startActivity(i);
        }
        if(item.getItemId() == R.id.delete) {
            Toast.makeText(this, "Not edited", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}