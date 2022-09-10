package com.example.phoenix4.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.CustomToast;
import com.example.phoenix4.R;
import com.example.phoenix4.databinding.ActivityMainRegionalOfficeBinding;
import com.example.phoenix4.fragments.MainHeadOfficeFragment;
import com.example.phoenix4.fragments.MainRegionalOfficeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainRegionalOfficeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // TAG
    private static final String TAG = "MainRegionalOfficeActivity";

    // view binding
    private ActivityMainRegionalOfficeBinding activityMainRegionalOfficeBinding;

    //    private ProgressDialog progressDialog;
    private DrawerLayout drawerLayout;
    private TextView textViewUserName;

    private boolean doubleBackToExitPressedOnce;
    private Toast backPressToast;

    // Firebase...
    private FirebaseAuth firebaseAuth;
//    private DatabaseReference databaseReferenceSensors;
//    private DatabaseReference databaseReferenceFireLocation;

    @SuppressLint({"LongLogTag", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityMainRegionalOfficeBinding = ActivityMainRegionalOfficeBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityMainRegionalOfficeBinding.getRoot ( ) );

        drawerLayout = findViewById ( R.id.drawerLayout );
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle ( this, activityMainRegionalOfficeBinding.drawerLayout, R.string.drawer_open, R.string.drawer_closed );
        activityMainRegionalOfficeBinding.drawerLayout.addDrawerListener ( actionBarDrawerToggle );
        actionBarDrawerToggle.setDrawerIndicatorEnabled ( true );
        actionBarDrawerToggle.syncState ( );
        activityMainRegionalOfficeBinding.navigationDrawerMenu.setNavigationItemSelectedListener ( this );

        FragmentTransaction fragmentTransaction = getSupportFragmentManager ( ).beginTransaction ( );
        fragmentTransaction.replace ( R.id.fragment_container, new MainRegionalOfficeFragment ( ) );
        fragmentTransaction.commit ( );

        NavigationView navigationView = findViewById ( R.id.navigationDrawerMenu );
        View navigationViewHeaderView = navigationView.getHeaderView ( 0 );
        textViewUserName = navigationViewHeaderView.findViewById ( R.id.textViewUserName );

        try {
            String regionalOfficeUserName = getIntent ( ).getStringExtra ( "regional_office_user_name" );
            Log.i ( TAG, "regionalOfficeUserName: " + regionalOfficeUserName );
            activityMainRegionalOfficeBinding.textViewUserName.setText ( regionalOfficeUserName );
            textViewUserName.setText ( regionalOfficeUserName );
        } catch (Exception e) {
            activityMainRegionalOfficeBinding.textViewUserName.setText ( "Regional Office" );
        }

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat ( "dd/MM/yy" );
        Date date = new Date ( );
        String currentDate = simpleDateFormat.format ( date );
        activityMainRegionalOfficeBinding.textViewDate.setText ( currentDate + " " );
        Log.i ( TAG, "onCreate: " + currentDate );
        Log.i ( TAG, "currentTime: " + activityMainRegionalOfficeBinding.digitalClock.getText ( ) );
        activityMainRegionalOfficeBinding.digitalClock.setText ( activityMainRegionalOfficeBinding.digitalClock.getText ( ) );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );

        // handle click, open drawer
        activityMainRegionalOfficeBinding.imageViewMenu.setOnClickListener ( v -> drawerLayout.openDrawer ( GravityCompat.START ) );

        // handle click, logout
        activityMainRegionalOfficeBinding.imageButtonLogout.setOnClickListener ( view -> signOut ( ) );
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId ( )) {
            case R.id.item_direction:
                // open a new map activity or open Google Map
                openGoogleMap ( );
                drawerLayout.closeDrawer ( GravityCompat.START );
                break;
            case R.id.item_about_us:
                CustomToast.makeInfoToast ( getApplicationContext ( ), "About Us is clicked", Toast.LENGTH_SHORT ).show ( );
                Animatoo.animateSpin ( this );
                drawerLayout.closeDrawer ( GravityCompat.START );
                break;
            default:
                break;
        }
        return false;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder ( MainRegionalOfficeActivity.this );
        builder.setTitle ( "Do you want to logout?" );
        builder.setPositiveButton ( "Yes", (dialogInterface, i) -> {
            firebaseAuth.signOut ( );
            onBackPressed ( );
            onBackPressed ( );
        } ).setNegativeButton ( "No", (dialogInterface, i) -> {

        } );
        builder.create ( ).show ( );
    }

    /**
     * tap back button twice to exit from the app
     * also sign out user
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            backPressToast.cancel ( );
            super.onBackPressed ( );
            return;
        }
        doubleBackToExitPressedOnce = true;
        backPressToast = CustomToast.makeWarningToast ( getApplicationContext ( ), "Click again to exit", Toast.LENGTH_SHORT );
        backPressToast.show ( );

        new Handler ( ).postDelayed ( () -> doubleBackToExitPressedOnce = false, 1500 );
    }

    private void openGoogleMap() {
        Intent intentBrowser = new Intent ( Intent.ACTION_VIEW, Uri.parse ( "https://www.google.com/maps/dir/24.1776634,88.2703958/22.6876265,88.44669732" ) );
        startActivity ( intentBrowser );
    }
    
}