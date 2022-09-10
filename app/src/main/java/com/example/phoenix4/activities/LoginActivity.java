package com.example.phoenix4.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.phoenix4.CustomToast;
import com.example.phoenix4.MyApplication;
import com.example.phoenix4.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements LocationListener {

    // TAG
    private static final String TAG = "LoginActivity";

    // view binding
    private ActivityLoginBinding activityLoginBinding;

    private ProgressDialog progressDialog;

    // firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReferenceUsers;

    // Permission Constants...
    private static final int LOCATION_REQUEST_CODE = 100;

    // Permission Arrays...
    private String[] locationPermissions;

    // Location Constants...
    private LocationManager locationManager;
    private Geocoder geocoder;
    private double latitude;
    private double longitude;

    private String email;
    private String password;
    private String country;
    private String state;
    private String city;
    private String completeAddress;
    private Double distance;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityLoginBinding = ActivityLoginBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityLoginBinding.getRoot ( ) );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        // Permission Arrays...
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        latitude = 0.0;
        longitude = 0.0;
        if (checkLocationPermission ( )) {
            // already allowed
            detectLocation ( );
        } else {
            // not allowed, request again
            requestLocationPermission ( );
        }

        // handle click, fetch current location
        activityLoginBinding.imageButtonGPS.setOnClickListener ( v -> {
            if (checkLocationPermission ( )) {
                // already allowed
                CustomToast.makeInfoToast ( getApplicationContext ( ), "Please wait...", Toast.LENGTH_LONG ).show ( );
                detectLocation ( );
            } else {
                // not allowed, request again
                requestLocationPermission ( );
            }
        } );

        // handle click, forgot password
        activityLoginBinding.textViewForgotPassword.setOnClickListener ( view -> {
            Intent intentForgotPasswordActivity = new Intent ( LoginActivity.this, ForgotPasswordActivity.class );
            startActivity ( intentForgotPasswordActivity );
        } );

        // handle click, login user
        activityLoginBinding.btnLogin.setOnClickListener ( v -> validateData ( ) );

        // handle click, go to register screen
        activityLoginBinding.textViewRegister.setOnClickListener ( v -> {
            Intent intentRegisterActivity = new Intent ( LoginActivity.this, RegisterActivity.class );
            startActivity ( intentRegisterActivity );
        } );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void validateData() {
        /** Before login, lets do some data validation */
        // get data
        email = Objects.requireNonNull ( activityLoginBinding.textInputLayoutEmail.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        password = Objects.requireNonNull ( activityLoginBinding.textInputLayoutPassword.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        country = Objects.requireNonNull ( activityLoginBinding.textViewCountry ).getText ( ).toString ( ).trim ( );
        state = Objects.requireNonNull ( activityLoginBinding.textViewState ).getText ( ).toString ( ).trim ( );
        city = Objects.requireNonNull ( activityLoginBinding.textViewCity ).getText ( ).toString ( ).trim ( );
        completeAddress = Objects.requireNonNull ( activityLoginBinding.textViewCompleteAddress ).getText ( ).toString ( ).trim ( );
        // validate data
        String passwordRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{6,}$";
        Matcher passwordMatcher = Pattern.compile ( passwordRegex ).matcher ( password );
        if (!Patterns.EMAIL_ADDRESS.matcher ( email ).matches ( )) {
            activityLoginBinding.textInputLayoutEmail.setError ( "" );
            activityLoginBinding.textInputLayoutEmail.requestFocus ( );
            com.example.phoenix4.CustomToast.makeWarningToast ( getApplicationContext ( ), "Enter a valid email", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (password.length ( ) < 6 || password.contains ( " " )) {
            activityLoginBinding.textInputLayoutPassword.setError ( "" );
            activityLoginBinding.textInputLayoutPassword.requestFocus ( );
            com.example.phoenix4.CustomToast.makeWarningToast ( getApplicationContext ( ), "Password length should be at least 6", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (!passwordMatcher.matches ( )) {
            activityLoginBinding.textInputLayoutPassword.setError ( "" );
            activityLoginBinding.textInputLayoutPassword.requestFocus ( );
            com.example.phoenix4.CustomToast.makeWarningToast ( getApplicationContext ( ), "Password should contain at least one lower case, upper case, special character, number", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (latitude == 0.0 || longitude == 0.0 || country.isEmpty ( ) || state.isEmpty ( ) || city.isEmpty ( ) || completeAddress.isEmpty ( )) {
//            activityLoginBinding.textInputLayoutCountry.setError ( "" );
//            activityLoginBinding.textInputLayoutState.setError ( "" );
//            activityLoginBinding.textInputLayoutCity.setError ( "" );
//            activityLoginBinding.textInputLayoutCompleteAddress.setError ( "" );
//            activityLoginBinding.textInputLayoutCountry.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Press on GPS button", Toast.LENGTH_LONG ).show ( );
            return;
        }
        loginUser ( );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loginUser() {
        progressDialog.setMessage ( "Login in progress..." );
        progressDialog.show ( );

        firebaseAuth.signInWithEmailAndPassword ( email, password ).addOnSuccessListener ( authResult -> {
            if (Objects.requireNonNull ( firebaseAuth.getCurrentUser ( ) ).isEmailVerified ( )) {
                // login successful, check if user is head office or regional office
                updateLocation ( );
            } else {
                CustomToast.makeWarningToast ( getApplicationContext ( ), "Kindly verify your email", com.example.phoenix4.CustomToast.LENGTH_LONG ).show ( );
                progressDialog.dismiss ( );
            }
        } ).addOnFailureListener ( e -> {
            // login failed
            progressDialog.dismiss ( );
            CustomToast.makeErrorToast ( getApplicationContext ( ), e.getMessage ( ), Toast.LENGTH_LONG ).show ( );
        } );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("DefaultLocale")
    private void updateLocation() {
        progressDialog.show ( );
        // get current user id
        String uid = firebaseAuth.getUid ( );
        // set data in firebase database
        HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
        stringObjectHashMap.put ( "country", "" + country );
        stringObjectHashMap.put ( "state", "" + state );
        stringObjectHashMap.put ( "city", "" + city );
        stringObjectHashMap.put ( "address", "" + completeAddress );
        latitude = Double.parseDouble ( String.format ( "%.6f", latitude ) );
        longitude = Double.parseDouble ( String.format ( "%.6f", longitude ) );
        stringObjectHashMap.put ( "latitude", latitude );
        stringObjectHashMap.put ( "longitude", longitude );
        stringObjectHashMap.put ( "isEmailVerified", true );
        // add data to firebase database
        databaseReferenceUsers.child ( Objects.requireNonNull ( uid ) ).updateChildren ( stringObjectHashMap ).addOnSuccessListener ( unused -> {
            // location updated in database
            // check user if Head Office or Regional Office
            checkUser ( );
        } ).addOnFailureListener ( e -> {
            // data failed to add db
            progressDialog.dismiss ( );
            CustomToast.makeErrorToast ( getApplicationContext ( ), e.getMessage ( ), Toast.LENGTH_LONG ).show ( );
        } );
    }

    private void checkUser() {
        progressDialog.setMessage ( "Checking User..." );
        // check if user is head office or regional office from realtime database
        firebaseUser = FirebaseAuth.getInstance ( ).getCurrentUser ( );
        // check in firebase database
        databaseReferenceUsers.child ( Objects.requireNonNull ( firebaseUser ).getUid ( ) ).addValueEventListener ( new ValueEventListener ( ) {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss ( );
                // get user type
                String userType = "" + snapshot.child ( "userTypeTitle" ).getValue ( );
                // check user type
                if (Objects.equals ( userType, "Head Office" )) {
                    // this is Head Office, open MainHeadOfficeActivity
                    String headOfficeUserName = snapshot.child ( "username" ).getValue ( String.class );
                    Intent intentHeadOfficeMainActivity = new Intent ( LoginActivity.this, MainHeadOfficeActivity.class );
                    intentHeadOfficeMainActivity.putExtra ( "head_office_user_name", headOfficeUserName );
                    Log.i ( TAG, "headOfficeUserName: " + headOfficeUserName );
                    intentHeadOfficeMainActivity.setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
                    startActivity ( intentHeadOfficeMainActivity );
                    finish ( );
                } else if (Objects.equals ( userType, "Regional Office" )) {
                    // this is Regional Office, open MainRegionalOfficeActivity
                    String regionalOfficeUserName = snapshot.child ( "username" ).getValue ( String.class );
                    Intent intentRegionalOfficeMainActivity = new Intent ( LoginActivity.this, MainRegionalOfficeActivity.class );
                    intentRegionalOfficeMainActivity.putExtra ( "regional_office_user_name", regionalOfficeUserName );
                    Log.i ( TAG, "regionalOfficeUserName: " + regionalOfficeUserName );
                    intentRegionalOfficeMainActivity.setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
                    startActivity ( intentRegionalOfficeMainActivity );
                    finish ( );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        locationManager = (LocationManager) getSystemService ( Context.LOCATION_SERVICE );
//        locationManager.requestLocationUpdates ( LocationManager.GPS_PROVIDER, 0, 0, this );
        if (locationManager != null) {
            List<String> providers = locationManager.getAllProviders ( );
            for (String provider : providers) {
                locationManager.requestLocationUpdates ( provider, 0, 0, this );
            }
        }
    }

    private void findAddress() {
        // find address, country, state, city
        List<Address> addressList;
        geocoder = new Geocoder ( this, Locale.getDefault ( ) );
        try {
            addressList = geocoder.getFromLocation ( latitude, longitude, 1 );
            country = addressList.get ( 0 ).getCountryName ( );   // country
            state = addressList.get ( 0 ).getAdminArea ( );   // state
            city = addressList.get ( 0 ).getLocality ( );     // city
            completeAddress = addressList.get ( 0 ).getAddressLine ( 0 );    // complete address
            Log.i ( TAG, "findAddress: " + country );
            Log.i ( TAG, "findAddress: " + state );
            Log.i ( TAG, "findAddress: " + city );
            Log.i ( TAG, "findAddress: " + completeAddress );
            // set addresses in input field
            Objects.requireNonNull ( activityLoginBinding.textViewCountry ).setText ( country );
            Objects.requireNonNull ( activityLoginBinding.textViewState ).setText ( state );
            Objects.requireNonNull ( activityLoginBinding.textViewCity ).setText ( city );
            Objects.requireNonNull ( activityLoginBinding.textViewCompleteAddress ).setText ( completeAddress );
        } catch (Exception exception) {
            exception.printStackTrace ( );
            CustomToast.makeErrorToast ( getApplicationContext ( ), exception.getMessage ( ), Toast.LENGTH_LONG ).show ( );
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_FINE_LOCATION ) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions ( this, locationPermissions, LOCATION_REQUEST_CODE );
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // location detected
        latitude = location.getLatitude ( );
        longitude = location.getLongitude ( );
        Log.i ( TAG, "onLocationChanged: " + latitude );
        Log.i ( TAG, "onLocationChanged: " + longitude );
        findAddress ( );
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // gps / location disabled
        CustomToast.makeWarningToast ( getApplicationContext ( ), "Please turn on GPS", Toast.LENGTH_LONG ).show ( );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean locationAccepted = grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED;
                if (locationAccepted) {
                    // permission allowed
                    detectLocation ( );
                } else {
                    // permission denied
                    CustomToast.makeWarningToast ( getApplicationContext ( ), "Kindly allow Location Permission", Toast.LENGTH_LONG ).show ( );
                }
            }
        }
        super.onRequestPermissionsResult ( requestCode, permissions, grantResults );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ( );
        finish ( );
    }
}