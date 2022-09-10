package com.example.phoenix4.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.MyApplication;
import com.example.phoenix4.R;
import com.example.phoenix4.adapters.JobAdapter;
import com.example.phoenix4.databinding.ActivityRegionalOfficeDetailsBinding;
import com.example.phoenix4.models.ModelJob;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class RegionalOfficeDetailsActivity extends AppCompatActivity {

    // TAG
    private static final String TAG = "RegionalOfficeDetailsActivity";

    // view binding
    private ActivityRegionalOfficeDetailsBinding activityRegionalOfficeDetailsBinding;

    private String regionalOfficeUid;

    // Firebase...
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceRequests;
    private DatabaseReference databaseReferenceNotifications;

    // arraylist to hold list of data of type ModelPdf
    private ArrayList<ModelJob> jobArrayList;

    // adapter to set recycler view
    private JobAdapter jobAdapter;

    private String fullName;
    private String email;
    private String address;
    private Double latitude;
    private Double longitude;
    private String location;
    private Double distance;
    private Long timestamp;
    private String formattedDateMemberSince;

    Calendar calendar;

//    private String currentRequestState;

    @SuppressLint({"LongLogTag", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityRegionalOfficeDetailsBinding = ActivityRegionalOfficeDetailsBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityRegionalOfficeDetailsBinding.getRoot ( ) );

        regionalOfficeUid = getIntent ( ).getStringExtra ( "regionalOfficeUid" );
        Log.i ( TAG, "regionalOfficeUserName: " + regionalOfficeUid );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceRequests = FirebaseDatabase.getInstance ( ).getReference ( "Requests" );
        databaseReferenceNotifications = FirebaseDatabase.getInstance ( ).getReference ( "Notifications" );

        calendar = Calendar.getInstance ( Locale.ENGLISH );

        jobArrayList = new ArrayList<> ( );
//        currentRequestState = "NA";
        /**
         * 1. Not sent yet: NA
         * 2. Head Office sent request: requestSent
         * 3. Sent but request declined: declined
         * 4. Sent but request accepted: accepted
         */
        getRegionalOfficeDetails ( );
        loadJobs ( );
        initializeRequestButton ( );

        // handle click, go back
        activityRegionalOfficeDetailsBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        activityRegionalOfficeDetailsBinding.btnSendRequest.setOnClickListener ( v -> sendRequestRegionalOffice ( ) );

        Objects.requireNonNull ( activityRegionalOfficeDetailsBinding.btnCancelRequest ).setOnClickListener ( v -> cancelRequestRegionalOffice ( ) );
    }

    private void initializeRequestButton() {
        Animation fadeOut = AnimationUtils.loadAnimation ( this, R.anim.fade_out );
        Animation fadeIn = AnimationUtils.loadAnimation ( this, R.anim.fade_in );
        databaseReferenceRequests.addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint({"SetTextI18n", "LongLogTag"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) )) {
                    // if head office sent request to any regional office
                    if (snapshot.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( regionalOfficeUid ).exists ( )) {
                        String isRequested = snapshot.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( regionalOfficeUid ).child ( "requestType" ).getValue ( String.class );
                        Log.i ( TAG, "onDataChange: isRequested : " + isRequested );
                        // if requestType == requestSent or requestAccepted or requestDeclined
                        switch (Objects.requireNonNull ( isRequested )) {
                            case "requestSent":
                                Log.i ( TAG, "onDataChange: requestSent" );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setVisibility ( View.GONE );
//                                activityRegionalOfficeDetailsBinding.btnSendRequest.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.btnSendRequest.setVisibility ( View.GONE );
//                                activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeIn );
                                activityRegionalOfficeDetailsBinding.btnCancelRequest.setVisibility ( View.VISIBLE );
                                break;
                            case "requestAccepted":
                                Log.i ( TAG, "onDataChange: requestAccepted" );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setText ( "Request Accepted" );
//                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.startAnimation ( fadeIn );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setVisibility ( View.VISIBLE );
                                activityRegionalOfficeDetailsBinding.btnSendRequest.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.btnSendRequest.setVisibility ( View.GONE );
//                                activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.btnCancelRequest.setVisibility ( View.GONE );
                                break;
                            case "requestDeclined":
                                Log.i ( TAG, "onDataChange: requestDeclined" );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setText ( "Request Declined" );
//                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.startAnimation ( fadeIn );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setVisibility ( View.VISIBLE );
//                                activityRegionalOfficeDetailsBinding.btnSendRequest.startAnimation ( fadeIn );
                                activityRegionalOfficeDetailsBinding.btnSendRequest.setVisibility ( View.VISIBLE );
//                                activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeIn );
                                activityRegionalOfficeDetailsBinding.btnCancelRequest.setVisibility ( View.VISIBLE );
                                break;
                            case "jobCompleted":
                                Log.i ( TAG, "onDataChange: jobCompleted" );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setText ( "Job Completed" );
//                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.startAnimation ( fadeIn );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setVisibility ( View.VISIBLE );
                                activityRegionalOfficeDetailsBinding.btnSendRequest.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.btnSendRequest.setVisibility ( View.GONE );
//                                activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.btnCancelRequest.setVisibility ( View.GONE );
                                break;
                            case "reportGenerated":
                                Log.i ( TAG, "onDataChange: reportGenerated" );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.textViewRequestResponse.setVisibility ( View.GONE );
//                                activityRegionalOfficeDetailsBinding.btnSendRequest.startAnimation ( fadeIn );
                                activityRegionalOfficeDetailsBinding.btnSendRequest.setVisibility ( View.VISIBLE );
//                                activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeOut );
                                activityRegionalOfficeDetailsBinding.btnCancelRequest.setVisibility ( View.GONE );
                                break;
                        }
                    } else {
                        Log.i ( TAG, "onDataChange: request not sent yet" );
                        activityRegionalOfficeDetailsBinding.textViewRequestResponse.startAnimation ( fadeOut );
                        activityRegionalOfficeDetailsBinding.textViewRequestResponse.setVisibility ( View.GONE );
//                        activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeOut );
                        activityRegionalOfficeDetailsBinding.btnCancelRequest.setVisibility ( View.GONE );
//                        activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeIn );
                        activityRegionalOfficeDetailsBinding.btnSendRequest.setVisibility ( View.VISIBLE );
                    }
                } else {
                    Log.i ( TAG, "onDataChange: request not sent yet" );
                    activityRegionalOfficeDetailsBinding.textViewRequestResponse.startAnimation ( fadeOut );
                    activityRegionalOfficeDetailsBinding.textViewRequestResponse.setVisibility ( View.GONE );
//                    activityRegionalOfficeDetailsBinding.btnCancelRequest.startAnimation ( fadeOut );
                    activityRegionalOfficeDetailsBinding.btnCancelRequest.setVisibility ( View.GONE );
//                    activityRegionalOfficeDetailsBinding.btnSendRequest.startAnimation ( fadeIn );
                    activityRegionalOfficeDetailsBinding.btnSendRequest.setVisibility ( View.VISIBLE );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

    }

    private void getRegionalOfficeDetails() {
        // fetch all details of that regional office
        databaseReferenceUsers.child ( regionalOfficeUid ).addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get data
                fullName = snapshot.child ( "username" ).getValue ( String.class );
                email = snapshot.child ( "email" ).getValue ( String.class );
                address = snapshot.child ( "address" ).getValue ( String.class );
                latitude = snapshot.child ( "latitude" ).getValue ( Double.class );
                longitude = snapshot.child ( "longitude" ).getValue ( Double.class );
                location = latitude + ",\n" + longitude;
                distance = snapshot.child ( "distance" ).getValue ( Double.class );
                timestamp = snapshot.child ( "timestamp" ).getValue ( Long.class );
                // format date to dd/MM/yyyy
                formattedDateMemberSince = MyApplication.formatTimestampToDate ( String.valueOf ( timestamp ) );
                // textViewDateRange...
                calendar.setTimeInMillis ( timestamp );
                String dateRangeMemberSince = DateFormat.format ( "dd MMM, yyyy", calendar ).toString ( );
                String dateRangeCurrentDate = DateFormat.format ( "dd MMM, yyyy", System.currentTimeMillis ( ) ).toString ( );
                // set data to ui
                activityRegionalOfficeDetailsBinding.textViewTitle.setText ( fullName );
                activityRegionalOfficeDetailsBinding.textViewDateRange.setText ( dateRangeMemberSince + " to " + dateRangeCurrentDate );
                activityRegionalOfficeDetailsBinding.textViewFullName.setText ( fullName );
                activityRegionalOfficeDetailsBinding.textViewEmail.setText ( email );
                activityRegionalOfficeDetailsBinding.textViewAddressValue.setText ( address );
                activityRegionalOfficeDetailsBinding.textViewLocationValue.setText ( location );
                activityRegionalOfficeDetailsBinding.textViewDistanceValue.setText ( distance + " km" );
                activityRegionalOfficeDetailsBinding.textViewMemberSinceValue.setText ( formattedDateMemberSince );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void sendRequestRegionalOffice() {
        // ----------------- NOT FRIENDS STATE ----------------- //
        databaseReferenceRequests.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( regionalOfficeUid ).child ( "requestType" ).setValue ( "requestSent" ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReferenceRequests.child ( regionalOfficeUid ).child ( firebaseAuth.getUid ( ) ).child ( "requestType" ).setValue ( "requestReceived" ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String, String> notificationHashMap = new HashMap<> ( );
                        notificationHashMap.put ( "from", firebaseAuth.getUid ( ) );
                        notificationHashMap.put ( "type", "request" );
                        databaseReferenceNotifications.child ( regionalOfficeUid ).push ( ).setValue ( notificationHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                currentRequestState = "requestSent";
//                                CustomToast.makeSuccessToast ( getApplicationContext ( ), "Request sent", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( activityRegionalOfficeDetailsBinding.relativeLayoutRegionalOfficeDetails, "Request sent", Snackbar.LENGTH_LONG );
                                View snackBarView = snackbar.getView ( );
                                snackBarView.setBackgroundColor ( Color.rgb ( 127, 200, 169 ) );
                                snackbar.setTextColor ( Color.rgb ( 23, 0, 85 ) );
                                snackbar.show ( );
                            }
                        } ).addOnFailureListener ( new OnFailureListener ( ) {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                                CustomToast.makeErrorToast ( getApplicationContext ( ), "Failed to send request", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( activityRegionalOfficeDetailsBinding.relativeLayoutRegionalOfficeDetails, "Failed to send request", Snackbar.LENGTH_LONG );
                                View snackBarView = snackbar.getView ( );
                                snackBarView.setBackgroundColor ( Color.rgb ( 230, 62, 109 ) );
                                snackbar.setTextColor ( Color.rgb ( 255, 247, 174 ) );
                                snackbar.show ( );
                            }
                        } );
                    }
                } ).addOnFailureListener ( new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                } );
            }
        } ).addOnFailureListener ( new OnFailureListener ( ) {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        } );
    }

    private void cancelRequestRegionalOffice() {
        // ----------------- CANCEL REQUEST STATE ----------------- //
        databaseReferenceRequests.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( regionalOfficeUid ).removeValue ( ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReferenceRequests.child ( regionalOfficeUid ).child ( firebaseAuth.getUid ( ) ).removeValue ( ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Void aVoid) {
//                        currentRequestState = "NA";
//                        CustomToast.makeSuccessToast ( getApplicationContext ( ), "Request cancelled", Toast.LENGTH_SHORT ).show ( );
                        Snackbar snackbar = Snackbar.make ( activityRegionalOfficeDetailsBinding.relativeLayoutRegionalOfficeDetails, "Request cancelled", Snackbar.LENGTH_LONG );
                        View snackBarView = snackbar.getView ( );
                        snackBarView.setBackgroundColor ( Color.rgb ( 127, 200, 169 ) );
                        snackbar.setTextColor ( Color.rgb ( 23, 0, 85 ) );
                        snackbar.show ( );
                    }
                } ).addOnFailureListener ( new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        CustomToast.makeErrorToast ( getApplicationContext ( ), "Failed to cancel request", Toast.LENGTH_SHORT ).show ( );
                        Snackbar snackbar = Snackbar.make ( activityRegionalOfficeDetailsBinding.relativeLayoutRegionalOfficeDetails, "Failed to cancel request", Snackbar.LENGTH_LONG );
                        View snackBarView = snackbar.getView ( );
                        snackBarView.setBackgroundColor ( Color.rgb ( 230, 62, 109 ) );
                        snackbar.setTextColor ( Color.rgb ( 255, 247, 174 ) );
                        snackbar.show ( );
                    }
                } );
            }
        } ).addOnFailureListener ( new OnFailureListener ( ) {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        } );
    }

    @SuppressLint("LongLogTag")
    private void loadJobs() {
        Log.i ( TAG, "loadJobs: started" );
        // load jobs from database: Users > userId > Jobs
//        ModelJob modelJob = new ModelJob ( );
//        modelJob.setUid ( regionalOfficeUid );
        databaseReferenceUsers.child ( Objects.requireNonNull ( regionalOfficeUid ) ).child ( "Jobs" ).addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear list before adding data
                jobArrayList.clear ( );
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    // get data
                    Double fireLocationLat = dataSnapshot.child ( "fireLocationLat" ).getValue ( Double.class );
                    Double fireLocationLong = dataSnapshot.child ( "fireLocationLong" ).getValue ( Double.class );
                    Double distance = dataSnapshot.child ( "distance" ).getValue ( Double.class );
                    Long acceptedTimestamp = dataSnapshot.child ( "acceptedTimestamp" ).getValue ( Long.class );
                    Long reportTimestamp = dataSnapshot.child ( "reportTimestamp" ).getValue ( Long.class );
                    Long timeSpent = reportTimestamp - acceptedTimestamp;
                    // add model to model class
                    jobArrayList.add ( new ModelJob ( regionalOfficeUid, fireLocationLat, fireLocationLong, distance, reportTimestamp, timeSpent ) );
                    Log.i ( TAG, "onDataChange: jobArrayList" + Arrays.toString ( jobArrayList.toArray ( ) ) );
                }
                // set total number of jobs
                activityRegionalOfficeDetailsBinding.textViewTotalJobsCount.setText ( String.valueOf ( jobArrayList.size ( ) ) );
                // setup adapter
                jobAdapter = new JobAdapter ( RegionalOfficeDetailsActivity.this, jobArrayList );
                // set adapter to recycler view
                activityRegionalOfficeDetailsBinding.recyclerViewJobs.setAdapter ( jobAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ( );
        Animatoo.animateDiagonal ( this );
    }
}