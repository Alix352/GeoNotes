package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.databinding.ActivityDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Details extends AppCompatActivity {
    private static final String TAG = "Details";
    private TextView mDetails;
    private Note note;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase db;
    DatabaseReference reference;
    DatabaseReference placeLocRef;
    DatabaseReference userLocRef;
    DatabaseReference alarmRef;

    private String date;
    private String time;
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        note = new Note();
        mDetails = findViewById(R.id.detailsOfNote);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/");
        reference = db.getReference().child("Notes");
        placeLocRef = db.getReference().child("PendingMaps").child(firebaseAuth.getCurrentUser().getUid());
        userLocRef = db.getReference().child("PendingUsers").child(firebaseAuth.getCurrentUser().getUid());
        alarmRef = db.getReference().child("Alarms").child(firebaseAuth.getCurrentUser().getUid());

        Intent i = getIntent();
        date = i.getStringExtra("Date");
        time = i.getStringExtra("Time");
        title = i.getStringExtra("Title");

        Log.d("Check extras", "Title -> " + title);
        //retrieve data from database and set on screen
        reference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child: snapshot.getChildren()){
                    Note tempNote = child.getValue(Note.class);
                    Log.d(TAG, "onDataChange: " + tempNote.getID() + ", " + tempNote.getTitle());
                    try {
                        if(tempNote.getDate().equals(date) && tempNote.getTime().equals(time) && tempNote.getTitle().equals(title)){
                            note = tempNote;
                            Log.d(TAG, "onDataChange: new note: " + note.getTitle());
                                getSupportActionBar().setTitle(note.getTitle());
                                mDetails.setText(AESUtils.decrypt(note.getContent()));
                                mDetails.setMovementMethod(new ScrollingMovementMethod());
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Log.d(TAG, "onCreate: title: " + note.getTitle());

        //delete button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) ->{
            reference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot child: snapshot.getChildren()){
                        Note tempNote = child.getValue(Note.class);
                            if(tempNote.getDate().equals(date) && tempNote.getTime().equals(time) && tempNote.getTitle().equals(title)){
                                child.getRef().removeValue();
                                return;
                            }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
            //check for pending notifications and delete them
            placeLocRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot child: snapshot.getChildren()){
                        MapsPos temp = child.getValue(MapsPos.class);
                        try {
                            if(temp.getName().equals(title)){
                                child.getRef().removeValue();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            userLocRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot child: snapshot.getChildren()){
                        PendingUsers temp = child.getValue(PendingUsers.class);
                        try {
                            if(temp.getName().equals(title)){
                                child.getRef().removeValue();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            alarmRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot child: snapshot.getChildren()){
                        Alarm temp = child.getValue(Alarm.class);
                        try {
                            if(temp.getTitle().equals(title)){
                                child.getRef().removeValue();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.editNote) {
            //send user to edit activity
            Toast.makeText(this, "Edit note", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, Edit.class);
            i.putExtra("Date", date);
            i.putExtra("Time", time);
            i.putExtra("Title", title);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}