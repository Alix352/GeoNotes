package com.example.notesapp;

import static com.example.notesapp.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.notesapp.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<Note> notes;
    private TextView verifyEmail;
    private TextView myUid;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase db;
    private DatabaseReference reference;
    private DatabaseReference locationRef;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocClient;


    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //recyclerView==========================================================
        recyclerView = findViewById(R.id.listOfNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notes = new ArrayList<>();
        adapter = new Adapter(this, notes);
        recyclerView.setAdapter(adapter);

        //Location===============================================================
        mFusedLocClient = LocationServices.getFusedLocationProviderClient(this);

        //Firebase===============================================================
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/");
        reference = db.getReference().child("Notes");
        locationRef = db.getReference().child("Users");

        //others=================================================================
        verifyEmail = findViewById(R.id.verifyEmail);
        myUid = findViewById(R.id.myUid);
        myUid.setText("UID: " + firebaseAuth.getCurrentUser().getUid());

        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
            verifyEmail.setVisibility(View.VISIBLE);
        }
        verifyEmail.setOnClickListener(view -> {
            firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(unused -> {
                Toast.makeText(MainActivity.this, "Verification sent!", Toast.LENGTH_SHORT).show();
                verifyEmail.setVisibility(View.GONE);
            });
        });

    }

    private void startLocationService(){
        //starts service if there isn't one already running also starts it in different ways
        //depending on the android version
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MainActivity.this.startForegroundService(serviceIntent);
            }
            else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            //The path needs to match the path of where your LocationService class is
            if("com.example.notesapp.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLastKnownLocation: access_fine_location");
            return;
        }
        mFusedLocClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                Location location = task.getResult();
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "onComplete: lat: " + geoPoint.getLatitude());
                Log.d(TAG, "onComplete: long: " + geoPoint.getLongitude());
                startLocationService();
            }
            Log.d(TAG, "getLastKnownLocation: if failed : " + task.getResult() + " " + task.isSuccessful());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            Log.d(TAG, "onResume: :" + mLocationPermissionGranted);
            //if mLocationPersmissionGrated is true then we retrieve the notes and also get current location
            if(mLocationPermissionGranted){
                retrieveAllNotes();
                getLastKnownLocation();
                Log.d(TAG, "onResume: getLastKnownLoc");
            }
            //if mLocationPersmissionGrated is false request permissions again
            else{
                getLocationPermission();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //this is the dropdown list on top right
        if(item.getItemId() == R.id.add) {
            Intent i = new Intent(this, AddNote.class);
            startActivity(i);
            Toast.makeText(this, "Add1 btn clicked", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId() == R.id.add2) {
            Intent i = new Intent(this, MapActivity.class);
            startActivity(i);
            Toast.makeText(this, "Add2 btn clicked", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId() == R.id.add3) {
            Intent i = new Intent(this, AddNoteUser.class);
            startActivity(i);
            Toast.makeText(this, "Add3 btn clicked", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId() == R.id.logoutBtn) {
            FirebaseAuth.getInstance().signOut();
            Intent j = new Intent(this, Login.class);
            startActivity(j);
        }
        if(item.getItemId() == R.id.resetPass) {
            startActivity(new Intent(this, ResetPassword.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveAllNotes() {
        //get all notes from database and then notify adapter
        reference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                notes.clear();
                for(DataSnapshot child: snapshot.getChildren()){
                    if(child.getValue(Note.class) != null){
                        Note note = child.getValue(Note.class);
                        notes.add(note);
                        notes.get(i).setID(child.getKey());
                        i++;
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //CHECK PERMISSIONS ON PHONE====================================================================
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        //When you press yes the app will take you to enable location on your phone
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        //google maps permissions are checked here
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission: gets in?");
            mLocationPermissionGranted = true;
            retrieveAllNotes();
            getLastKnownLocation();
        } else {
            //request permission if we don't have them
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            //this gives you a link to a google documentary on how to resolve error
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            //an error occured but is not resolvable
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    retrieveAllNotes();
                    getLastKnownLocation();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
}