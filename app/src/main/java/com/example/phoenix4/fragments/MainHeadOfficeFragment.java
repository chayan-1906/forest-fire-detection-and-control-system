package com.example.phoenix4.fragments;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.phoenix4.CustomToast;
import com.example.phoenix4.MyApplication;
import com.example.phoenix4.activities.LoginActivity;
import com.example.phoenix4.databinding.FragmentMainHeadOfficeBinding;
import com.example.phoenix4.models.ModelReport;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class MainHeadOfficeFragment extends Fragment {

    private static final String TAG = "MainHeadOfficeFragment";

    // view binding
    private FragmentMainHeadOfficeBinding fragmentMainHeadOfficeBinding;

    // Firebase...
    private FirebaseAuth firebaseAuth;
//    private DatabaseReference databaseReferenceSensors;
//    private DatabaseReference databaseReferenceFireLocation;
//    private DatabaseReference databaseReferenceUsers;

//    private String userTypeId;
//    private Double latitude;
//    private Double longitude;
//    private String camera_alert;
//    private String flame_alert;
//    private String smoke_alert;

    //    private LatLng fireLocation;
//    private LatLng regionalOfficeLocation;
//    private static Double distance;
    private static ArrayList<Double> distancesList;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentMainHeadOfficeBinding = FragmentMainHeadOfficeBinding.inflate ( inflater, container, false );
        View inflaterView = fragmentMainHeadOfficeBinding.getRoot ( );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
//        databaseReferenceSensors = FirebaseDatabase.getInstance ( ).getReference ( "Sensors" );
//        databaseReferenceFireLocation = FirebaseDatabase.getInstance ( ).getReference ( "FireLocation" );
//        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        if (!Objects.requireNonNull ( firebaseAuth.getCurrentUser ( ) ).isEmailVerified ( )) {
            Intent intentLogin = new Intent ( getContext ( ), LoginActivity.class );
            intentLogin.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity ( intentLogin );
            CustomToast.makeWarningToast ( getContext ( ), "Kindly verify your email", Toast.LENGTH_LONG ).show ( );
        }

        distancesList = new ArrayList<> ( );

        // handle click, open a new map activity or open Google Map
        fragmentMainHeadOfficeBinding.mapView.setOnClickListener ( view -> openGoogleMap ( ) );

        /**
         * create Fire Location in realtime database
         */
        MyApplication.createFireLocation ( );

        /**
         * create Sensors in realtime database
         */
        MyApplication.createSensors ( );

        /**
         * check fire detected or not detected
         * check camera_alert, flame_alert & smoke_alert value
         */
        MyApplication.fireDetection ( fragmentMainHeadOfficeBinding.linearLayoutFireDetected, fragmentMainHeadOfficeBinding.linearLayoutFireNotDetected );

        /**
         * calculate distance between fire location & all regional offices' location
         */
        MyApplication.calculateDistance ( );

        return inflaterView;
    }

    private void openGoogleMap() {
        Intent intentGoogleMap = new Intent ( Intent.ACTION_VIEW, Uri.parse ( "https://www.google.com/maps/dir/24.1776634,88.2703958/22.6876265,88.44669732" ) );
        startActivity ( intentGoogleMap );
    }

}