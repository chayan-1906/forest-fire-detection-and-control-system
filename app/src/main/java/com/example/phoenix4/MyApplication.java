package com.example.phoenix4;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.phoenix4.activities.MainHeadOfficeActivity;
import com.example.phoenix4.activities.MainRegionalOfficeActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static com.example.phoenix4.CustomFirebaseMessagingService.channelId;
import static com.example.phoenix4.CustomFirebaseMessagingService.channelName;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    // Firebase
    private static FirebaseAuth firebaseAuth;
    private static DatabaseReference databaseReferenceFireLocation;
    private static DatabaseReference databaseReferenceUsers;
    private static DatabaseReference databaseReferenceSensors;
    private static DatabaseReference databaseReferenceWaterSystem;

    private static Double latitude;
    private static Double longitude;
    private static LatLng fireLocation;
    private static LatLng regionalOfficeLocation;
    private static Double distance;

    private static String camera_alert;
    private static String flame_alert;
    private static String smoke_alert;

    private static ProgressDialog progressDialog;

    @Override
    public void onCreate() {
        super.onCreate ( );
    }

    /*private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin ( deg2rad ( lat1 ) )
                * Math.sin ( deg2rad ( lat2 ) )
                + Math.cos ( deg2rad ( lat1 ) )
                * Math.cos ( deg2rad ( lat2 ) )
                * Math.cos ( deg2rad ( theta ) );
        dist = Math.acos ( dist );
        dist = rad2deg ( dist );
        dist = dist * 60 * 1.1515;
        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @SuppressLint("DefaultLocale")
    public static double distance(LatLng start, LatLng end) {
        try {
            Location location1 = new Location ( "locationA" );
            location1.setLatitude ( start.latitude );
            location1.setLongitude ( start.longitude );
            Location location2 = new Location ( "locationB" );
            location2.setLatitude ( end.latitude );
            location2.setLongitude ( end.longitude );
            String distance = String.valueOf ( location1.distanceTo ( location2 ) );
            distance = String.format ( "%.2f", distance );
            return Double.parseDouble ( distance );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
        return 0;
    }*/

    // for sensors... smoke, smoke_alert, flame, flame_alert, camera, camera_alert
    public static void createSensors() {
        databaseReferenceSensors = FirebaseDatabase.getInstance ( ).getReference ( "Sensors" );
        HashMap<String, String> sensorsHashMap = new HashMap<> ( );
        sensorsHashMap.put ( "smoke", "0" );
        sensorsHashMap.put ( "smoke_alert", "0" );
        sensorsHashMap.put ( "flame", "0" );
        sensorsHashMap.put ( "flame_alert", "0" );
        sensorsHashMap.put ( "camera", "0" );
        sensorsHashMap.put ( "camera_alert", "0" );
        databaseReferenceSensors.setValue ( sensorsHashMap );
    }

    // for Fire Location... Latitude & Longitude
    public static void createFireLocation() {
        databaseReferenceFireLocation = FirebaseDatabase.getInstance ( ).getReference ( "FireLocation" );
        HashMap<String, Double> fireLocationHashMap = new HashMap<> ( );
        fireLocationHashMap.put ( "latitude", 0.0 );
        fireLocationHashMap.put ( "longitude", 0.0 );
        databaseReferenceFireLocation.setValue ( fireLocationHashMap );
    }

    // for Water Sensor... pump & Longitude
    public static void createWaterSystem() {
        databaseReferenceWaterSystem = FirebaseDatabase.getInstance ( ).getReference ( "WaterSystem" );
        HashMap<String, Object> waterSystemHashMap = new HashMap<> ( );
        waterSystemHashMap.put ( "pump", "0" );
        databaseReferenceWaterSystem.setValue ( waterSystemHashMap );
    }

    // check fire detected or not detected
    public static void fireDetection(LinearLayout linearLayoutFireDetected, LinearLayout linearLayoutFireNotDetected) {
        databaseReferenceSensors = FirebaseDatabase.getInstance ( ).getReference ( "Sensors" );
        databaseReferenceSensors.addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                flame_alert = snapshot.child ( "flame_alert" ).getValue ( String.class );
                smoke_alert = snapshot.child ( "smoke_alert" ).getValue ( String.class );
                Log.i ( TAG, "onDataChange: flame_alert " + flame_alert );
                /** if camera_alert & flame_alert & smoke_alert value all are 1, fire detected
                 * else fire not detected
                 */
                if (flame_alert.equals ( "1" ) && smoke_alert.equals ( "1" )) {
                    linearLayoutFireDetected.setVisibility ( View.VISIBLE );
                    linearLayoutFireNotDetected.setVisibility ( View.GONE );
                } else {
                    linearLayoutFireDetected.setVisibility ( View.GONE );
                    linearLayoutFireNotDetected.setVisibility ( View.VISIBLE );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    // calculate distance between fire location & all regional offices' location
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void calculateDistance() {
        // fetching the fire location: latitude & longitude
        databaseReferenceFireLocation = FirebaseDatabase.getInstance ( ).getReference ( "FireLocation" );
        databaseReferenceFireLocation.addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double latitude = snapshot.child ( "latitude" ).getValue ( Double.class );
                Double longitude = snapshot.child ( "longitude" ).getValue ( Double.class );
                fireLocation = new LatLng ( latitude, longitude );
                Log.i ( TAG, "firelocation: latitude: " + latitude );
                Log.i ( TAG, "firelocation: longitude: " + longitude );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        // fetching all regional offices' location: latitude & longitude, to find distance
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceUsers.addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("DefaultLocale")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    String userTypeId = dataSnapshot.child ( "userTypeId" ).getValue ( String.class );
                    if (Objects.equals ( userTypeId, "2" )) {
                        // if user is Regional Office, store current location
                        latitude = dataSnapshot.child ( "latitude" ).getValue ( Double.class );
                        longitude = dataSnapshot.child ( "longitude" ).getValue ( Double.class );
                        regionalOfficeLocation = new LatLng ( latitude, longitude );
//                        distance = MyApplication.distance ( fireLocation, regionalOfficeLocation );
                        distance = SphericalUtil.computeDistanceBetween ( fireLocation, regionalOfficeLocation );
                        distance = Double.valueOf ( String.format ( "%.2f", distance / 1000 ) );
                        Log.i ( TAG, "onDataChange: " + distance );
                        HashMap<String, Object> distancesHashMap = new HashMap<> ( );
                        distancesHashMap.put ( "distance", distance );
                        Log.i ( TAG, "onDataChange: distance: " + distance );
                        databaseReferenceUsers.child ( Objects.requireNonNull ( dataSnapshot.getKey ( ) ) ).updateChildren ( distancesHashMap );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    public static String formatTimestampToDate(String timestamp) {
        Calendar calendar = Calendar.getInstance ( Locale.ENGLISH );
        calendar.setTimeInMillis ( Long.parseLong ( timestamp ) );
        // format timestamp to dd/MM/yyyy
        return DateFormat.format ( "dd/MM/yyyy", calendar ).toString ( );
    }

    public static String formatTimestampToDateTime(String timestamp) {
        Calendar calendar = Calendar.getInstance ( Locale.ENGLISH );
        calendar.setTimeInMillis ( Long.parseLong ( timestamp ) );
        // format timestamp to dd/MM/yyyy hh:mm:ss a
        return DateFormat.format ( "dd/MM/yyyy hh:mm:ss a", calendar ).toString ( );
    }

    public static void generateNotification(Context context, String title, String description) {
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceUsers.child ( firebaseAuth.getUid ( ) ).addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent = null;
                // get user type
                String userType = "" + snapshot.child ( "userTypeTitle" ).getValue ( );
                // check user type
                if (Objects.equals ( userType, "Head Office" )) {
                    // this is Head Office, open MainHeadOfficeActivity
                    String headOfficeUserName = snapshot.child ( "username" ).getValue ( String.class );
                    intent = new Intent ( context, MainHeadOfficeActivity.class );
                    intent.addFlags ( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                    intent.putExtra ( "head_office_user_name", headOfficeUserName );
                } else if (Objects.equals ( userType, "Regional Office" )) {
                    // this is Regional Office, open MainRegionalOfficeActivity
                    String regionalOfficeUserName = snapshot.child ( "username" ).getValue ( String.class );
                    intent = new Intent ( context, MainRegionalOfficeActivity.class );
                    intent.addFlags ( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                    intent.putExtra ( "regional_office_user_name", regionalOfficeUserName );
                }
                PendingIntent pendingIntent = PendingIntent.getActivity ( context, 0, intent, PendingIntent.FLAG_ONE_SHOT );
                NotificationCompat.Builder builder = new NotificationCompat.Builder ( context, channelId );
                long[] vibrateArray = {1000, 1000, 1000, 1000};
                builder.setSmallIcon ( R.drawable.ic_notifications )
                        .setAutoCancel ( true )
                        .setVibrate ( vibrateArray )
                        .setOnlyAlertOnce ( true )
                        .setContentIntent ( pendingIntent );
                builder = builder.setContent ( CustomFirebaseMessagingService.getRemoteView ( title, description ) );

                NotificationManager notificationManager = (NotificationManager) context.getSystemService ( Context.NOTIFICATION_SERVICE );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel ( channelId, channelName, NotificationManager.IMPORTANCE_HIGH );
                    notificationManager.createNotificationChannel ( notificationChannel );
                }
                notificationManager.notify ( 0, builder.build ( ) );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

}