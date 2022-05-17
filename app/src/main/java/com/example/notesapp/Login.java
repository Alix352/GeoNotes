package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private TextView gotoReg, forgotPass;
    private EditText email, password;
    private Button loginBtn;
    FirebaseAuth firebaseAuth;
    LayoutInflater inflater;
 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        gotoReg = findViewById(R.id.goToRegister);
        forgotPass = findViewById(R.id.forgotPass);
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        inflater = this.getLayoutInflater();

        //Register
        gotoReg.setOnClickListener(view -> {
            startActivity(new Intent(this, Register.class));
        });
        //forgot password
        forgotPass.setOnClickListener(view -> {
            View view1 = inflater.inflate(R.layout.reset_popup, null);
            AlertDialog.Builder reset_alert = new AlertDialog.Builder(this);

            reset_alert.setTitle("Forgot Password?")
                    .setMessage("Enter Email to get reset link.")
                    .setPositiveButton("Reset", (dialogInterface, i) -> {
                       //validate email and send link
                        EditText email = view1.findViewById(R.id.reset_email_popup);
                        if(email.getText().toString().isEmpty()){
                            email.setError("Required field");
                            return;
                        }
                        firebaseAuth.sendPasswordResetEmail(email.getText().toString()).addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Reset Email Sent", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                    }).setNegativeButton("Cancel", null)
                    .setView(view1)
                    .create().show();
        });

        loginBtn.setOnClickListener(view -> {
            //extract and validate
            if(email.getText().toString().isEmpty()){
                email.setError("Email is missing.");
                return;
            }
            if(password.getText().toString().isEmpty()){
                password.setError("Password is missing.");
                return;
            }
            //login user
            firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),
                    password.getText().toString()).addOnSuccessListener(authResult -> {
                        //login is success
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}