package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private TextView gotoLogin;
    private EditText regFullName, regEmail, regPass, regPassConf;
    private Button regUser;
    FirebaseAuth fAuth;
    FirebaseDatabase db;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        gotoLogin = findViewById(R.id.goToLogin);
        regFullName = findViewById(R.id.regFullName);
        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPassword);
        regPassConf = findViewById(R.id.confirmPassword);
        regUser = findViewById(R.id.regBtn);

        fAuth = FirebaseAuth.getInstance();

        gotoLogin.setOnClickListener(view -> {
            startActivity(new Intent(this, Login.class));
        });

        // Read from the database
        db = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/");
        reference = db.getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      Object value = dataSnapshot.getValue();
                      Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        regUser.setOnClickListener(view -> {
            //extract data
            String fullName = regFullName.getText().toString();
            String email = regEmail.getText().toString();
            String password = regPass.getText().toString();
            String confPassword = regPassConf.getText().toString();

            //validate data
            if(fullName.isEmpty()){
                regFullName.setError("Full Name is required");
                return;
            }
            if(email.isEmpty()){
                regEmail.setError("Email is required");
                return;
            }
            if(password.isEmpty()){
                regPass.setError("Password is required");
                return;
            }
            if(confPassword.isEmpty()){
                regPassConf.setError("Confirm your password");
                return;
            }
            if(!password.equals(confPassword)){
                regPassConf.setError("Password does not match");
                return;
            }

            //register user
            Users user = new Users(fullName);

            fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                //send to next phase

                reference.child(fAuth.getCurrentUser().getUid()).child(user.getFullName()).setValue(user);

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        });
    }
}