package com.example.notesapp;

import static com.example.notesapp.appChannels.CHANNEL_2_ID;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase db;
    private DatabaseReference locationRef;
    private DatabaseReference placeLocRef;
    private DatabaseReference userLocRef;
    private DatabaseReference alarmRef;

    private List<MapsPos> mapsPos;
    private List<String> keysPosMaps;
    private List<PendingUsers> allUsers;
    private List<String> keysPosUsers;
    private List<Alarm> allAlarms;
    private List<String> keysPosAlarms;
    private Users user;
    private Users tempUser;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Firebase db===============================================================================
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://t-collective-342314-default-rtdb.europe-west1.firebasedatabase.app/");
        locationRef = db.getReference().child("Users");
        placeLocRef = db.getReference().child("PendingMaps").child(firebaseAuth.getCurrentUser().getUid());
        userLocRef = db.getReference().child("PendingUsers").child(firebaseAuth.getCurrentUser().getUid());
        alarmRef = db.getReference().child("Alarms").child(firebaseAuth.getCurrentUser().getUid());

        mapsPos = new ArrayList<>();
        keysPosMaps = new ArrayList<>();
        allUsers = new ArrayList<>();
        keysPosUsers = new ArrayList<>();
        allAlarms = new ArrayList<>();
        keysPosAlarms = new ArrayList<>();

        //Location and SDK checks and inits=========================================================
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
        //get current user from database
        locationRef.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child: snapshot.getChildren()){
                    if(child.getValue(Users.class) != null) {
                        user = child.getValue(Users.class);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = LocationRequest.create();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");

        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "onLocationResult: got location result.");

                        Location location = locationResult.getLastLocation();

                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        saveUserLocation(geoPoint);
                    }
                }, Looper.getMainLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    private void saveUserLocation(final GeoPoint geoPoint){

        try{
            //save location in database
            locationRef.child(firebaseAuth.getCurrentUser().getUid()).child(user.getFullName())
                    .child("latitude").setValue(AESUtils.encrypt(String.valueOf(geoPoint.getLatitude())));
            locationRef.child(firebaseAuth.getCurrentUser().getUid()).child(user.getFullName())
                    .child("longitude").setValue(AESUtils.encrypt(String.valueOf(geoPoint.getLongitude())));
            //all pending notification checks
            checkPositionToLocation();
            checkPositionOfUser();
            checkAlarms();
            Log.d(TAG, "checkAlarms: " + System.currentTimeMillis());

        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAlarms() throws Exception {
        alarmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAlarms.clear();
                keysPosAlarms.clear();
                for (DataSnapshot child: snapshot.getChildren()){
                    if(child.getValue(Alarm.class) != null){
                        allAlarms.add(child.getValue(Alarm.class));
                        keysPosAlarms.add(child.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        if(!allAlarms.isEmpty()){
            for (int i = 0; i < allAlarms.size(); i++){
                if(allAlarms.get(i).time <= System.currentTimeMillis()){

                    Intent intent1 = new Intent(LocationService.this, MainActivity.class);
                    PendingIntent detailsIntent = PendingIntent.getActivity(LocationService.this, 1
                            , intent1, PendingIntent.FLAG_IMMUTABLE);
                    NotificationCompat.Builder notification1 = new NotificationCompat.Builder(LocationService.this, CHANNEL_2_ID)
                            .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                            .setContentTitle(allAlarms.get(i).getTitle())
                            .setContentText("Check your notes!")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(detailsIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(LocationService.this);

                    notificationManager.notify(314, notification1.build());

                    alarmRef.child(keysPosAlarms.get(i)).removeValue();
                }
            }
        }
    }

    //this is the user to user check
    private void checkPositionOfUser() throws Exception {
        userLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                keysPosUsers.clear();
                for(DataSnapshot child: snapshot.getChildren()){
                    if(child.getValue(PendingUsers.class) != null){
                        allUsers.add(child.getValue(PendingUsers.class));
                        keysPosUsers.add(child.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        if(!allUsers.isEmpty()){
            for (int i = 0; i < allUsers.size(); i++){
                locationRef.child(allUsers.get(i).getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getValue(Users.class) != null) {
                                tempUser = child.getValue(Users.class);
                                Log.d(TAG, "onDataChange: user?" + tempUser.getFullName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

                Log.d(TAG, "checkPositionOfUser: user: " + user.getLatitude() + " " + user.getLongitude());
                Log.d(TAG, "checkPositionOfUser: tempUser: " + tempUser.getLatitude() + " " + tempUser.getLongitude());
                    if (Double.parseDouble(AESUtils.decrypt(user.getLatitude())) <= Double.parseDouble(AESUtils.decrypt(tempUser.getLatitude())) + 0.0001000 && Double.parseDouble(AESUtils.decrypt(user.getLatitude())) >= Double.parseDouble(AESUtils.decrypt(tempUser.getLatitude()))  - 0.0001000
                            && Double.parseDouble(AESUtils.decrypt(user.getLongitude()))  <= Double.parseDouble(AESUtils.decrypt(tempUser.getLongitude()))  + 0.0001000 && Double.parseDouble(AESUtils.decrypt(user.getLongitude())) >= Double.parseDouble(AESUtils.decrypt(tempUser.getLongitude())) - 0.0001000) {
                        Intent intent1 = new Intent(LocationService.this, MainActivity.class);
                        PendingIntent detailsIntent = PendingIntent.getActivity(LocationService.this, 1
                                , intent1, PendingIntent.FLAG_IMMUTABLE);
                        NotificationCompat.Builder notification1 = new NotificationCompat.Builder(LocationService.this, CHANNEL_2_ID)
                                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                                .setContentTitle("You are close to " + tempUser.getFullName() + "!")
                                .setContentText("Check your notes!")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(detailsIntent)
                                .setAutoCancel(true);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(LocationService.this);

                        notificationManager.notify(33, notification1.build());

                        userLocRef.child(keysPosUsers.get(i)).removeValue();
                    }
                }
        }
    }
    //google maps note
    private void checkPositionToLocation(){
        placeLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mapsPos.clear();
                keysPosMaps.clear();
                for(DataSnapshot child: snapshot.getChildren()){
                    if(child.getValue(MapsPos.class) != null){
                        MapsPos temp = child.getValue(MapsPos.class);
                        keysPosMaps.add(child.getKey());
                        Log.d(TAG, "onDataChange: checkPosToLoc: " + temp.getLongitude() + " " + temp.getLatitude() + " " + temp.getName());
                        mapsPos.add(temp);
                    }
                }
                Log.d(TAG, "checkPositionToLocation: lat/long user: " + user.getLatitude() + "  " + user.getLongitude());
                //Log.d(TAG, "checkPositionToLocation: lat/long user: " + mapsPos.get(0).getLatitude() + "  " + mapsPos.get(0).getLongitude());

                if(!mapsPos.isEmpty()) {
                    for (int i = 0; i < mapsPos.size(); i++) {
                        Log.d(TAG, "checkPositionToLocation: checking " + i);
                        try {
                            if (Double.parseDouble(AESUtils.decrypt(user.getLatitude())) <= mapsPos.get(i).getLatitude() + 0.0001000 && Double.parseDouble(AESUtils.decrypt(user.getLatitude())) >= mapsPos.get(i).getLatitude() - 0.0001000
                                    && Double.parseDouble(AESUtils.decrypt(user.getLongitude())) <= mapsPos.get(i).getLongitude() + 0.0001000 && Double.parseDouble(AESUtils.decrypt(user.getLongitude())) >= mapsPos.get(i).getLongitude() - 0.0001000) {
                                Log.d(TAG, "checkPositionToLocation: arrived at pos!!!");

                                Intent intent1 = new Intent(LocationService.this, MainActivity.class);
                                PendingIntent detailsIntent = PendingIntent.getActivity(LocationService.this, 1
                                        , intent1, PendingIntent.FLAG_IMMUTABLE);
                                NotificationCompat.Builder notification1 = new NotificationCompat.Builder(LocationService.this, CHANNEL_2_ID)
                                        .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                                        .setContentTitle(mapsPos.get(i).getName())
                                        .setContentText("You arrived at the location!")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setContentIntent(detailsIntent)
                                        .setAutoCancel(true);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(LocationService.this);

                                notificationManager.notify(430, notification1.build());

                                placeLocRef.child(keysPosMaps.get(i)).removeValue();

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



}