package com.example.phoenix4.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.phoenix4.CustomToast;
import com.example.phoenix4.MyApplication;
import com.example.phoenix4.R;
import com.example.phoenix4.activities.LoginActivity;
import com.example.phoenix4.databinding.FragmentMainRegionalOfficeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class MainRegionalOfficeFragment extends Fragment {

    private static final String TAG = "MainRegionalOfficeFragment";

    // view binding
    private FragmentMainRegionalOfficeBinding fragmentMainRegionalOfficeBinding;

    private ProgressDialog progressDialog;

    // Firebase...
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceRequests;
    private DatabaseReference databaseReferenceNotifications;
    private DatabaseReference databaseReferenceReports;
    private DatabaseReference databaseReferenceFireLocation;
    private DatabaseReference databaseReferenceJobs;

    private String fullName;
    private String email;
    private Double fireLocationLat;
    private Double fireLocationLong;
    private String officeAddress;
    private Double currentLocationRegOfficeLat;
    private Double currentLocationRegOfficeLong;
    private Double distance;
    private Long timestamp;
    private Long acceptedTimestamp;
    private Long reportTimestamp;
    private String formattedDate;

//    private String camera_alert;
//    private String flame_alert;
//    private String smoke_alert;

    private String headOfficeUid;

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentMainRegionalOfficeBinding = FragmentMainRegionalOfficeBinding.inflate ( inflater, container, false );
        View inflaterView = fragmentMainRegionalOfficeBinding.getRoot ( );

        progressDialog = new ProgressDialog ( getActivity ( ) );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceRequests = FirebaseDatabase.getInstance ( ).getReference ( "Requests" );
        databaseReferenceNotifications = FirebaseDatabase.getInstance ( ).getReference ( "Notifications" );
        databaseReferenceReports = FirebaseDatabase.getInstance ( ).getReference ( "Reports" );
        databaseReferenceFireLocation = FirebaseDatabase.getInstance ( ).getReference ( "FireLocation" );
        databaseReferenceJobs = FirebaseDatabase.getInstance ( ).getReference ( "Users" ).child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( "Jobs" );

        if (!Objects.requireNonNull ( firebaseAuth.getCurrentUser ( ) ).isEmailVerified ( )) {
            Intent intentLogin = new Intent ( getContext ( ), LoginActivity.class );
            intentLogin.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity ( intentLogin );
            CustomToast.makeWarningToast ( getContext ( ), "Kindly verify your email", Toast.LENGTH_LONG ).show ( );
        }

        headOfficeUid = getHeadOfficeUid ( );
        initializeButton ( );
        getRegionalOfficeDetails ( );
        getFireLocation ( );

        // handle click, accept the request of head office
        fragmentMainRegionalOfficeBinding.btnAcceptRequest.setOnClickListener ( view -> acceptRequest ( ) );

        // handle click, decline the request of head office
        fragmentMainRegionalOfficeBinding.btnDeclineRequest.setOnClickListener ( v -> declineRequest ( ) );

        // handle click, decline the request of head office
        fragmentMainRegionalOfficeBinding.textViewJobCompleted.setOnClickListener ( v -> {
            informJobCompleted ( );
            Log.i ( TAG, "onCreateView: delay started" );
            new Handler ( ).postDelayed ( () -> {
                // Do Something, after delay completed
                generateReport ( );
                Log.i ( TAG, "onCreateView: delay completed" );
            }, 10000 );
        } );

        /**
         * create Fire Location in realtime database
         */
        MyApplication.createFireLocation ( );

        /**
         * create Sensors in realtime database
         */
        MyApplication.createSensors ( );

        /**
         * create WaterSystem in realtime database
         */
        MyApplication.createWaterSystem ( );

        /** check fire detected or not detected
         * check camera_alert, flame_alert & smoke_alert value
         */
        MyApplication.fireDetection ( fragmentMainRegionalOfficeBinding.linearLayoutFireDetected, fragmentMainRegionalOfficeBinding.linearLayoutFireNotDetected );

        /**
         * calculate distance between fire location & all regional offices' location
         */
        MyApplication.calculateDistance ( );

        return inflaterView;
    }

    private void initializeButton() {
        Animation fadeOut = AnimationUtils.loadAnimation ( getContext ( ), R.anim.fade_out );
        Animation fadeIn = AnimationUtils.loadAnimation ( getContext ( ), R.anim.fade_in );
        databaseReferenceRequests.addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) )) {
                    // // if current regional office received any request from head office
                    String isRequestAccepted = snapshot.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( headOfficeUid ).child ( "requestType" ).getValue ( String.class );
                    switch (Objects.requireNonNull ( isRequestAccepted )) {
                        case "requestReceived":
                            Log.i ( TAG, "onDataChange: requestReceived" );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.startAnimation ( fadeOut );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnAcceptRequest.setVisibility ( View.VISIBLE );
                            fragmentMainRegionalOfficeBinding.btnDeclineRequest.setVisibility ( View.VISIBLE );
                            break;
                        case "requestAccepted":
                            Log.i ( TAG, "onDataChange: requestAccepted" );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.setVisibility ( View.VISIBLE );
                            fragmentMainRegionalOfficeBinding.btnAcceptRequest.startAnimation ( fadeOut );
                            fragmentMainRegionalOfficeBinding.btnAcceptRequest.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnDeclineRequest.setVisibility ( View.GONE );
                            break;
                        case "requestDeclined":
                            Log.i ( TAG, "onDataChange: requestDeclined" );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.startAnimation ( fadeOut );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnAcceptRequest.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnDeclineRequest.setVisibility ( View.GONE );
                            break;
                        case "jobCompleted":
                            Log.i ( TAG, "onDataChange: jobCompleted" );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.startAnimation ( fadeOut );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnAcceptRequest.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnDeclineRequest.setVisibility ( View.GONE );
                            break;
                        case "reportGenerated":
                            Log.i ( TAG, "onDataChange: reportGenerated" );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.startAnimation ( fadeOut );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnAcceptRequest.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnDeclineRequest.setVisibility ( View.GONE );
                            break;
                        default:
                            Log.i ( TAG, "onDataChange: request not received yet" );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.startAnimation ( fadeOut );
                            fragmentMainRegionalOfficeBinding.textViewJobCompleted.setVisibility ( View.GONE );
                            fragmentMainRegionalOfficeBinding.btnDeclineRequest.setVisibility ( View.GONE );
                            break;
                    }
                } else {
                    Log.i ( TAG, "onDataChange: request not received" );
                    fragmentMainRegionalOfficeBinding.textViewJobCompleted.startAnimation ( fadeOut );
                    fragmentMainRegionalOfficeBinding.textViewJobCompleted.setVisibility ( View.GONE );
                    fragmentMainRegionalOfficeBinding.btnAcceptRequest.setVisibility ( View.GONE );
                    fragmentMainRegionalOfficeBinding.btnDeclineRequest.setVisibility ( View.GONE );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private String getHeadOfficeUid() {
        databaseReferenceUsers.addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    String userTypeId = dataSnapshot.child ( "userTypeId" ).getValue ( String.class );
                    Log.i ( TAG, "onDataChange: userTypeId" + userTypeId );
                    if (userTypeId.equals ( "1" )) {
                        headOfficeUid = dataSnapshot.getKey ( );
                        Log.i ( TAG, "onDataChange: headOfficeUid" + headOfficeUid );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.makeErrorToast ( getActivity ( ), error.toString ( ), Toast.LENGTH_LONG ).show ( );
            }
        } );
        return headOfficeUid;
    }

    private void getRegionalOfficeDetails() {
        // fetch all details of that regional office
        databaseReferenceUsers.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullName = snapshot.child ( "username" ).getValue ( String.class );
                email = snapshot.child ( "email" ).getValue ( String.class );
                officeAddress = snapshot.child ( "address" ).getValue ( String.class );
                currentLocationRegOfficeLat = snapshot.child ( "latitude" ).getValue ( Double.class );
                currentLocationRegOfficeLong = snapshot.child ( "longitude" ).getValue ( Double.class );
                distance = snapshot.child ( "distance" ).getValue ( Double.class );
                timestamp = snapshot.child ( "timestamp" ).getValue ( Long.class );
                // format date to dd/MM/yyyy
                formattedDate = MyApplication.formatTimestampToDate ( String.valueOf ( timestamp ) );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void getFireLocation() {
        databaseReferenceFireLocation.addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // fetching the fire location
                fireLocationLat = snapshot.child ( "latitude" ).getValue ( Double.class );
                fireLocationLong = snapshot.child ( "longitude" ).getValue ( Double.class );
                Log.i ( TAG, "onDataChange: " + fireLocationLat + ", " + fireLocationLong );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void acceptRequest() {
        // ----------------- ACCEPT REQUEST STATE ----------------- //
        acceptedTimestamp = System.currentTimeMillis ( );
        HashMap<String, Object> requestAcceptHashMap = new HashMap<> ( );
        requestAcceptHashMap.put ( "requestType", "requestAccepted" );
        requestAcceptHashMap.put ( "acceptedTimestamp", acceptedTimestamp );
        databaseReferenceRequests.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( headOfficeUid ).updateChildren ( requestAcceptHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReferenceRequests.child ( headOfficeUid ).child ( firebaseAuth.getUid ( ) ).updateChildren ( requestAcceptHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String, String> notificationHashMap = new HashMap<> ( );
                        notificationHashMap.put ( "from", firebaseAuth.getUid ( ) );
                        notificationHashMap.put ( "type", "requestAccepted" );
                        databaseReferenceNotifications.child ( headOfficeUid ).push ( ).setValue ( notificationHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                CustomToast.makeSuccessToast ( getActivity ( ), "Request Accepted", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Request Accepted", Snackbar.LENGTH_LONG );
                                View snackBarView = snackbar.getView ( );
                                snackBarView.setBackgroundColor ( Color.rgb ( 127, 200, 169 ) );
                                snackbar.setTextColor ( Color.rgb ( 23, 0, 85 ) );
                                snackbar.show ( );
                            }
                        } ).addOnFailureListener ( new OnFailureListener ( ) {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                                CustomToast.makeErrorToast ( getActivity ( ), "Failed to accept request", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Failed to accept request", Snackbar.LENGTH_LONG );
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
//                        CustomToast.makeErrorToast ( getActivity ( ), "Failed to accept request", Toast.LENGTH_SHORT ).show ( );
                        Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Failed to accept request", Snackbar.LENGTH_LONG );
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

    private void declineRequest() {
        // ----------------- DECLINE REQUEST STATE ----------------- //
        HashMap<String, Object> requestDeclineHashMap = new HashMap<> ( );
        requestDeclineHashMap.put ( "requestType", "requestDeclined" );
        databaseReferenceRequests.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( headOfficeUid ).updateChildren ( requestDeclineHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReferenceRequests.child ( headOfficeUid ).child ( firebaseAuth.getUid ( ) ).updateChildren ( requestDeclineHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String, String> notificationHashMap = new HashMap<> ( );
                        notificationHashMap.put ( "from", firebaseAuth.getUid ( ) );
                        notificationHashMap.put ( "type", "requestDeclined" );
                        databaseReferenceNotifications.child ( headOfficeUid ).push ( ).setValue ( notificationHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                CustomToast.makeSuccessToast ( getActivity ( ), "Request Declined", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Request Declined", Snackbar.LENGTH_LONG );
                                View snackBarView = snackbar.getView ( );
                                snackBarView.setBackgroundColor ( Color.rgb ( 230, 62, 109 ) );
                                snackbar.setTextColor ( Color.rgb ( 255, 247, 174 ) );
                                snackbar.show ( );
                            }
                        } ).addOnFailureListener ( new OnFailureListener ( ) {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                                CustomToast.makeErrorToast ( getActivity ( ), "Failed to decline request", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Failed to decline request", Snackbar.LENGTH_LONG );
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

    private void informJobCompleted() {
        // ----------------- JOB COMPLETED STATE ----------------- //
        HashMap<String, Object> jobCompletedHashMap = new HashMap<> ( );
        jobCompletedHashMap.put ( "requestType", "jobCompleted" );
        databaseReferenceRequests.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( headOfficeUid ).updateChildren ( jobCompletedHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReferenceRequests.child ( headOfficeUid ).child ( firebaseAuth.getUid ( ) ).updateChildren ( jobCompletedHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String, String> notificationHashMap = new HashMap<> ( );
                        notificationHashMap.put ( "from", firebaseAuth.getUid ( ) );
                        notificationHashMap.put ( "type", "jobCompleted" );
                        databaseReferenceNotifications.child ( headOfficeUid ).push ( ).setValue ( notificationHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                CustomToast.makeSuccessToast ( getActivity ( ), "Job Completed", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Job Completed", Snackbar.LENGTH_LONG );
                                View snackBarView = snackbar.getView ( );
                                snackBarView.setBackgroundColor ( Color.rgb ( 127, 200, 169 ) );
                                snackbar.setTextColor ( Color.rgb ( 23, 0, 85 ) );
                                snackbar.show ( );
                            }
                        } ).addOnFailureListener ( new OnFailureListener ( ) {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                                CustomToast.makeErrorToast ( getActivity ( ), "Failed to inform Job Completed", Toast.LENGTH_SHORT ).show ( );
                                Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Failed to inform Job Completed", Snackbar.LENGTH_LONG );
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
        // will continue for 10 sec...
        progressDialog.setMessage ( "Generating Report..." );
        progressDialog.show ( );
    }

    private void generateReport() {
        reportTimestamp = System.currentTimeMillis ( );
        HashMap<String, Object> reportHashMap = new HashMap<> ( );
        reportHashMap.put ( "uid", firebaseAuth.getUid ( ) );
        reportHashMap.put ( "reportId", reportTimestamp );
        reportHashMap.put ( "userName", fullName );
        reportHashMap.put ( "email", email );
        reportHashMap.put ( "fireLocationLat", fireLocationLat );
        reportHashMap.put ( "fireLocationLong", fireLocationLong );
        reportHashMap.put ( "officeAddress", officeAddress );
        reportHashMap.put ( "currentLocationRegOfficeLat", currentLocationRegOfficeLat );
        reportHashMap.put ( "currentLocationRegOfficeLong", currentLocationRegOfficeLong );
        reportHashMap.put ( "distance", distance );
        reportHashMap.put ( "report_timestamp", reportTimestamp );
        databaseReferenceReports.child ( String.valueOf ( reportTimestamp ) ).setValue ( reportHashMap ).addOnSuccessListener ( aVoid -> {
//            CustomToast.makeSuccessToast ( getActivity ( ), "Report Generated", Toast.LENGTH_SHORT ).show ( );
            progressDialog.dismiss ( );
            Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Report Generated", Snackbar.LENGTH_LONG );
            View snackBarView = snackbar.getView ( );
            snackBarView.setBackgroundColor ( Color.rgb ( 127, 200, 169 ) );
            snackbar.setTextColor ( Color.rgb ( 23, 0, 85 ) );
            snackbar.show ( );
        } ).addOnFailureListener ( e -> {
//            CustomToast.makeErrorToast ( getActivity ( ), "Failed to generate report", Toast.LENGTH_SHORT ).show ( );
            progressDialog.dismiss ( );
            Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Failed to generate report", Snackbar.LENGTH_LONG );
            View snackBarView = snackbar.getView ( );
            snackBarView.setBackgroundColor ( Color.rgb ( 230, 62, 109 ) );
            snackbar.setTextColor ( Color.rgb ( 255, 247, 174 ) );
            snackbar.show ( );
        } );

        HashMap<String, Object> reportGeneratedHashMap = new HashMap<> ( );
        reportGeneratedHashMap.put ( "requestType", "reportGenerated" );
        reportGeneratedHashMap.put ( "reportId", reportTimestamp );
        databaseReferenceRequests.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).child ( headOfficeUid ).updateChildren ( reportGeneratedHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReferenceRequests.child ( headOfficeUid ).child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).updateChildren ( reportGeneratedHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Report Generated", Snackbar.LENGTH_LONG );
                        View snackBarView = snackbar.getView ( );
                        snackBarView.setBackgroundColor ( Color.rgb ( 127, 200, 169 ) );
                        snackbar.setTextColor ( Color.rgb ( 23, 0, 85 ) );
                        snackbar.show ( );
                        addJob ( );
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

    private void addJob() {
        getFireLocation ( );
        getRegionalOfficeDetails ( );
        databaseReferenceRequests.child ( firebaseAuth.getUid ( ) ).child ( headOfficeUid ).addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long acceptedTimestamp = snapshot.child ( "acceptedTimestamp" ).getValue ( Long.class );
                HashMap<String, Object> jobHashMap = new HashMap<> ( );
                jobHashMap.put ( "fireLocationLat", fireLocationLat );
                jobHashMap.put ( "fireLocationLong", fireLocationLong );
                jobHashMap.put ( "distance", distance );
                jobHashMap.put ( "acceptedTimestamp", acceptedTimestamp );
                jobHashMap.put ( "reportTimestamp", reportTimestamp );
                databaseReferenceJobs.child ( String.valueOf ( reportTimestamp ) ).setValue ( jobHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Job Added to your profile", Snackbar.LENGTH_LONG );
                        View snackBarView = snackbar.getView ( );
                        snackBarView.setBackgroundColor ( Color.rgb ( 127, 200, 169 ) );
                        snackbar.setTextColor ( Color.rgb ( 23, 0, 85 ) );
                        snackbar.show ( );
                    }
                } ).addOnFailureListener ( e -> {
                    Snackbar snackbar = Snackbar.make ( fragmentMainRegionalOfficeBinding.relativeLayoutRegionalMain, "Failed to add job to your profile", Snackbar.LENGTH_LONG );
                    View snackBarView = snackbar.getView ( );
                    snackBarView.setBackgroundColor ( Color.rgb ( 230, 62, 109 ) );
                    snackbar.setTextColor ( Color.rgb ( 255, 247, 174 ) );
                    snackbar.show ( );
                } );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

}