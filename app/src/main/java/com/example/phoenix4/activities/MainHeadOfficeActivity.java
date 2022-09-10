package com.example.phoenix4.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.CustomToast;
import com.example.phoenix4.R;
import com.example.phoenix4.databinding.ActivityMainHeadOfficeBinding;
import com.example.phoenix4.fragments.MainHeadOfficeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainHeadOfficeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // TAG
    private static final String TAG = "MainHeadOfficeActivity";

    // view binding
    private ActivityMainHeadOfficeBinding activityMainHeadOfficeBinding;

    //    private ProgressDialog progressDialog;
    private DrawerLayout drawerLayout;
    private TextView textViewUserName;

    private boolean doubleBackToExitPressedOnce;
    private Toast backPressToast;

    // Firebase...
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceSensors;
    private DatabaseReference databaseReferenceFireLocation;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"SetTextI18n", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityMainHeadOfficeBinding = ActivityMainHeadOfficeBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityMainHeadOfficeBinding.getRoot ( ) );

        drawerLayout = findViewById ( R.id.drawerLayout );
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle ( this, activityMainHeadOfficeBinding.drawerLayout, R.string.drawer_open, R.string.drawer_closed );
        activityMainHeadOfficeBinding.drawerLayout.addDrawerListener ( actionBarDrawerToggle );
        actionBarDrawerToggle.setDrawerIndicatorEnabled ( true );
        actionBarDrawerToggle.syncState ( );
        activityMainHeadOfficeBinding.navigationDrawerMenu.setNavigationItemSelectedListener ( this );

        FragmentTransaction fragmentTransaction = getSupportFragmentManager ( ).beginTransaction ( );
        fragmentTransaction.replace ( R.id.fragment_container, new MainHeadOfficeFragment ( ) );
        fragmentTransaction.commit ( );

        NavigationView navigationView = findViewById ( R.id.navigationDrawerMenu );
        View navigationViewHeaderView = navigationView.getHeaderView ( 0 );
        textViewUserName = navigationViewHeaderView.findViewById ( R.id.textViewUserName );

        try {
            String headOfficeUserName = getIntent ( ).getStringExtra ( "head_office_user_name" );
            Log.i ( TAG, "headOfficeUserName: " + headOfficeUserName );
            activityMainHeadOfficeBinding.textViewUserName.setText ( headOfficeUserName );
            textViewUserName.setText ( headOfficeUserName );
        } catch (Exception e) {
            activityMainHeadOfficeBinding.textViewUserName.setText ( "Head Office" );
        }

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat ( "dd/MM/yy" );
        Date date = new Date ( );
        String currentDate = simpleDateFormat.format ( date );
        activityMainHeadOfficeBinding.textViewDate.setText ( currentDate + " " );
        Log.i ( TAG, "onCreate: " + currentDate );
        Log.i ( TAG, "currentTime: " + activityMainHeadOfficeBinding.digitalClock.getText ( ) );
        activityMainHeadOfficeBinding.digitalClock.setText ( activityMainHeadOfficeBinding.digitalClock.getText ( ) );

//        progressDialog = new ProgressDialog ( getApplicationContext ( ) );
//        progressDialog.setMessage ( "Please wait..." );
//        progressDialog.setCancelable ( false );
//        progressDialog.setCanceledOnTouchOutside ( false );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceSensors = FirebaseDatabase.getInstance ( ).getReference ( "Sensors" );
        databaseReferenceFireLocation = FirebaseDatabase.getInstance ( ).getReference ( "FireLocation" );

        // handle click, open drawer
        activityMainHeadOfficeBinding.imageViewMenu.setOnClickListener ( v -> drawerLayout.openDrawer ( GravityCompat.START ) );

        // handle click, logout
        activityMainHeadOfficeBinding.imageButtonLogout.setOnClickListener ( view -> signOut ( ) );
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId ( )) {
            case R.id.item_regional_offices:
                // handle click, go to new activity i.e. RegionalOfficesListActivity
                Intent intentRegionalOfficesListActivity = new Intent ( MainHeadOfficeActivity.this, RegionalOfficesListActivity.class );
                startActivity ( intentRegionalOfficesListActivity );
                Animatoo.animateInAndOut ( this );
                drawerLayout.closeDrawer ( GravityCompat.START );
                break;
            case R.id.item_report:
                // handle click, go to new activity i.e. ReportListHeadOfficeActivity
                Intent intentReportHeadOfficeActivity = new Intent ( MainHeadOfficeActivity.this, ReportListHeadOfficeActivity.class );
                startActivity ( intentReportHeadOfficeActivity );
                Animatoo.animateShrink ( this );
                drawerLayout.closeDrawer ( GravityCompat.START );
                break;
            case R.id.item_email:
                // handle click, open email
                openEmail ( );
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

    @SuppressLint("IntentReset")
    private void openEmail() {
        try {
            Intent intentBrowser = new Intent ( Intent.ACTION_VIEW, Uri.parse ( "https://mail.google.com/mail" ) );
            startActivity ( intentBrowser );
        } catch (ActivityNotFoundException e) {
            CustomToast.makeErrorToast ( getApplicationContext ( ), "Please install a web browser " + e, Toast.LENGTH_LONG ).show ( );
            e.printStackTrace ( );
        }
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder ( MainHeadOfficeActivity.this );
        builder.setTitle ( "Do you want to logout?" );
        builder.setPositiveButton ( "Yes", (dialogInterface, i) -> {
//            progressDialog.show ( );
            firebaseAuth.signOut ( );
//            progressDialog.dismiss ( );
            onBackPressed ( );
            onBackPressed ( );
            backPressToast.cancel ( );
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
}