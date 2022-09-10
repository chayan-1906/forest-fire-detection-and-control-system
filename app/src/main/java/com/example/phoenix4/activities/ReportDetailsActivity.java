package com.example.phoenix4.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.MyApplication;
import com.example.phoenix4.databinding.ActivityReportDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReportDetailsActivity extends AppCompatActivity {

    // TAG
    private static final String TAG = "RegionalOfficeDetailsActivity";

    // view binding
    private ActivityReportDetailsBinding activityReportDetailsBinding;

    private String reportId;

    // Firebase...
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceReports;

    private String fullName;
    private String email;
    private String officeAddress;
    private Double officeLatitude;
    private Double officeLongitude;
    private String officeLocation;
    private Double fireLocationLat;
    private Double fireLocationLong;
    private String fireLocation;
    private Double distance;
    private Long reportTimestamp;
    private String formattedDateTime;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityReportDetailsBinding = ActivityReportDetailsBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityReportDetailsBinding.getRoot ( ) );

        reportId = String.valueOf ( getIntent ( ).getLongExtra ( "reportId", 0 ) );
        Log.i ( TAG, "reportId: " + reportId );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceReports = FirebaseDatabase.getInstance ( ).getReference ( "Reports" );

        getReportDetails ( );

        // handle click, go back
        activityReportDetailsBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );


    }

    private void getReportDetails() {
        // fetch all details of that report
        databaseReferenceReports.child ( reportId ).addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get data
                fullName = snapshot.child ( "userName" ).getValue ( String.class );
                email = snapshot.child ( "email" ).getValue ( String.class );
                officeAddress = snapshot.child ( "officeAddress" ).getValue ( String.class );
                officeLatitude = snapshot.child ( "currentLocationRegOfficeLat" ).getValue ( Double.class );
                officeLongitude = snapshot.child ( "currentLocationRegOfficeLong" ).getValue ( Double.class );
                officeLocation = officeLatitude + ",\n" + officeLongitude;
                fireLocationLat = snapshot.child ( "fireLocationLat" ).getValue ( Double.class );
                fireLocationLong = snapshot.child ( "fireLocationLong" ).getValue ( Double.class );
                fireLocation = fireLocationLat + ",\n" + fireLocationLong;
                distance = snapshot.child ( "distance" ).getValue ( Double.class );
                reportTimestamp = snapshot.child ( "report_timestamp" ).getValue ( Long.class );
                // format date to dd/MM/yyyy
                formattedDateTime = MyApplication.formatTimestampToDateTime ( String.valueOf ( reportTimestamp ) );
                // set data to ui
                activityReportDetailsBinding.textViewTitle.setText ( fullName );
                activityReportDetailsBinding.textViewFullName.setText ( fullName );
                activityReportDetailsBinding.textViewEmail.setText ( email );
                activityReportDetailsBinding.textViewOfficeAddressValue.setText ( officeAddress );
                activityReportDetailsBinding.textViewFireLocationValue.setText ( fireLocation );
                activityReportDetailsBinding.textViewOfficeLocationValue.setText ( officeLocation );
                activityReportDetailsBinding.textViewDistanceValue.setText ( distance + " km" );
                activityReportDetailsBinding.textViewDateValue.setText ( formattedDateTime );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ( );
        Animatoo.animateWindmill ( this );
    }
}