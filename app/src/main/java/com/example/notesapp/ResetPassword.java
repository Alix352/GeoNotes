package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPassword extends AppCompatActivity {

    private EditText userPassword, userConfPassword;
    private Button saveBtn;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        userPassword = findViewById(R.id.newPass);
        userConfPassword = findViewById(R.id.newConfPass);
        saveBtn = findViewById(R.id.resetPassBtn);
        user = FirebaseAuth.getInstance().getCurrentUser();

        saveBtn.setOnClickListener(view -> {
            if(userPassword.getText().toString().isEmpty()){
                userPassword.setError("Required Field");
                return;
            }
            if(userConfPassword.getText().toString().isEmpty()){
                userConfPassword.setError("Required Field");
                return;
            }

            if(!userPassword.getText().toString().equals(userConfPassword.getText().toString())){
                userConfPassword.setError("Password does not match");
                return;
            }

            user.updatePassword(userPassword.getText().toString())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Password has been Updated!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        });
    }
}