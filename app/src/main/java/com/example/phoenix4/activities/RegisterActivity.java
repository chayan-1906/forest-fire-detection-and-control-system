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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.phoenix4.CustomToast;
import com.example.phoenix4.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements LocationListener {

    // TAG
    private static final String TAG = "RegisterActivity";

    // view binding
    private ActivityRegisterBinding activityRegisterBinding;
    private ProgressDialog progressDialog;

    // firebase
    private FirebaseAuth firebaseAuth;
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

    private String userName;
    private String email;
    private String password;
    private String confirmPassword;
    private String country;
    private String state;
    private String city;
    private String completeAddress;
    private String selectedUserTypeId;
    private String selectedUserTypeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityRegisterBinding = ActivityRegisterBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityRegisterBinding.getRoot ( ) );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        // Permission Arrays...
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        latitude = 0.0;
        longitude = 0.0;
        if (checkLocationPermission ( )) {
            // already allowed
//            CustomToast.makeInfoToast ( getApplicationContext ( ), "Please wait...", Toast.LENGTH_LONG ).show ( );
            detectLocation ( );
        } else {
            // not allowed, request again
            requestLocationPermission ( );
        }

        // handle click, go back
        activityRegisterBinding.imageButtonBack.setOnClickListener ( view -> onBackPressed ( ) );

        // handle click, fetch current location
        activityRegisterBinding.imageButtonGPS.setOnClickListener ( v -> {
            if (checkLocationPermission ( )) {
                // already allowed
                CustomToast.makeInfoToast ( getApplicationContext ( ), "Please wait...", Toast.LENGTH_LONG ).show ( );
                detectLocation ( );
            } else {
                // not allowed, request again
                requestLocationPermission ( );
            }
        } );

        // handle click, pick user type
        activityRegisterBinding.textViewUserType.setOnClickListener ( v -> userTypePickDialog ( ) );

        // handle click, go back to login screen
        activityRegisterBinding.textViewSignIn.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, begin register
        activityRegisterBinding.btnRegister.setOnClickListener ( v -> validateData ( ) );
    }

    private void userTypePickDialog() {
        // options to display in dialog
        String[] options = {"Head Office", "Regional Office"};
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder ( this );
        builder.setTitle ( "Choose User Type" )
                .setItems ( options, (dialog, which) -> {
                    // handle clicks
                    if (which == 0) {
                        // head office clicked
                        selectedUserTypeId = "1";
                        selectedUserTypeTitle = "Head Office";
                    } else {
                        // regional office clicked
                        selectedUserTypeId = "2";
                        selectedUserTypeTitle = "Regional Office";
                    }
                    // set it to selected user type textview
                    activityRegisterBinding.textViewUserType.setText ( selectedUserTypeTitle );
                } ).show ( );
    }

    private void validateData() {
        /** Before creating account, lets do some data validation */
        // get data
        userName = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutName.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        email = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutEmail.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        password = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutPassword.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        confirmPassword = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutConfirmPassword.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        country = Objects.requireNonNull ( activityRegisterBinding.textViewCountry ).getText ( ).toString ( ).trim ( );
        state = Objects.requireNonNull ( activityRegisterBinding.textViewState ).getText ( ).toString ( ).trim ( );
        city = Objects.requireNonNull ( activityRegisterBinding.textViewCity ).getText ( ).toString ( ).trim ( );
        completeAddress = Objects.requireNonNull ( activityRegisterBinding.textViewCompleteAddress ).getText ( ).toString ( ).trim ( );
        selectedUserTypeTitle = activityRegisterBinding.textViewUserType.getText ( ).toString ( );

        // validate data
        String passwordRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{6,}$";
        Matcher passwordMatcher = Pattern.compile ( passwordRegex ).matcher ( password );
        if (TextUtils.isEmpty ( userName )) {
            activityRegisterBinding.textInputLayoutName.setError ( "" );
            activityRegisterBinding.textInputLayoutName.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Enter your full name", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher ( email ).matches ( )) {
            activityRegisterBinding.textInputLayoutEmail.setError ( "" );
            activityRegisterBinding.textInputLayoutEmail.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Enter a valid email", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (password.length ( ) < 6 || password.contains ( " " )) {
            activityRegisterBinding.textInputLayoutPassword.setError ( "" );
            activityRegisterBinding.textInputLayoutPassword.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Password length should be at least 6", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (!passwordMatcher.matches ( )) {
            activityRegisterBinding.textInputLayoutPassword.setError ( "" );
            activityRegisterBinding.textInputLayoutPassword.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Password should contain at least one lower case, upper case, special character, number", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (TextUtils.isEmpty ( confirmPassword ) || !confirmPassword.equals ( password )) {
            activityRegisterBinding.textInputLayoutConfirmPassword.setError ( "" );
            activityRegisterBinding.textInputLayoutConfirmPassword.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Password didn't match", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (latitude == 0.0 || longitude == 0.0 || country.isEmpty ( ) || state.isEmpty ( ) || city.isEmpty ( ) || completeAddress.isEmpty ( )) {
//            activityLoginBinding.textInputLayoutCountry.setError ( "" );
//            activityLoginBinding.textInputLayoutState.setError ( "" );
//            activityLoginBinding.textInputLayoutCity.setError ( "" );
//            activityLoginBinding.textInputLayoutCompleteAddress.setError ( "" );
//            activityLoginBinding.textInputLayoutCountry.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Press on GPS button", Toast.LENGTH_LONG ).show ( );
            return;
        } else if (TextUtils.isEmpty ( selectedUserTypeTitle )) {
            activityRegisterBinding.textViewUserType.setError ( "" );
            activityRegisterBinding.textViewUserType.requestFocus ( );
            CustomToast.makeWarningToast ( getApplicationContext ( ), "Choose User Type", Toast.LENGTH_LONG ).show ( );
            return;
        }
        createAccount ( email, password );
    }

    private void createAccount(String email, String password) {
        Log.i ( TAG, "createAccount: started" );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setMessage ( "Creating Account..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );
        progressDialog.show ( );
        firebaseAuth.createUserWithEmailAndPassword ( email, password ).addOnSuccessListener ( authResult -> {
            Log.i ( TAG, "onSuccess: account created" );
            emailVerification ( );
            updateUserInfo ( );
        } ).addOnFailureListener ( e -> {
            Log.i ( TAG, "onSuccess: account not created" );
            CustomToast.makeErrorToast ( getApplicationContext ( ), "Account not Created " + e, Toast.LENGTH_LONG ).show ( );
            progressDialog.dismiss ( );
        } );
    }

    private void emailVerification() {
        Log.i ( TAG, "emailVerification: started" );
        Objects.requireNonNull ( firebaseAuth.getCurrentUser ( ) ).sendEmailVerification ( ).addOnSuccessListener ( aVoid -> {
            Log.i ( TAG, "onSuccess: Verification Link sent" );
            CustomToast.makeInfoToast ( getApplicationContext ( ), "Verification Link sent, Kindly verify", Toast.LENGTH_LONG ).show ( );
            progressDialog.dismiss ( );
        } ).addOnFailureListener ( e -> {
            Log.i ( TAG, "onFailure: Failed to send Verification Link" );
            CustomToast.makeErrorToast ( getApplicationContext ( ), "Failed to send Verification Link " + e, Toast.LENGTH_LONG ).show ( );
            progressDialog.dismiss ( );
        } );
    }

    @SuppressLint("DefaultLocale")
    private void updateUserInfo() {
        progressDialog.setMessage ( "Saving User Info..." );
        progressDialog.show ( );
        // timestamp
        Long timestamp = System.currentTimeMillis ( );
        // get current user id
        String uid = firebaseAuth.getUid ( );
        // set data in firebase database
        HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
        stringObjectHashMap.put ( "uid", uid );
        stringObjectHashMap.put ( "username", userName );
        stringObjectHashMap.put ( "email", email );
        stringObjectHashMap.put ( "country", "" + country );
        stringObjectHashMap.put ( "state", "" + state );
        stringObjectHashMap.put ( "city", "" + city );
        stringObjectHashMap.put ( "address", "" + completeAddress );
        latitude = Double.parseDouble ( String.format ( "%.6f", latitude ) );
        longitude = Double.parseDouble ( String.format ( "%.6f", longitude ) );
        stringObjectHashMap.put ( "latitude", latitude );
        stringObjectHashMap.put ( "longitude", longitude );
        stringObjectHashMap.put ( "userTypeId", selectedUserTypeId );
        stringObjectHashMap.put ( "userTypeTitle", selectedUserTypeTitle );
        stringObjectHashMap.put ( "timestamp", timestamp );
        stringObjectHashMap.put ( "isEmailVerified", false );
        stringObjectHashMap.put ( "distance", 0.0 );
        // add data to firebase database
        databaseReferenceUsers.child ( Objects.requireNonNull ( uid ) ).setValue ( stringObjectHashMap ).addOnSuccessListener ( unused -> {
            // data added to db
            progressDialog.dismiss ( );
            Log.i ( TAG, "onSuccess: Database updated" );
            CustomToast.makeSuccessToast ( getApplicationContext ( ), "Account created successfully", Toast.LENGTH_SHORT ).show ( );
            Intent intentLogin = new Intent ( RegisterActivity.this, LoginActivity.class );
            intentLogin.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity ( intentLogin );
        } ).addOnFailureListener ( e -> {
            // data failed to add db
            progressDialog.dismiss ( );
            Log.i ( TAG, "onFailure: failed to update database" );
            CustomToast.makeErrorToast ( getApplicationContext ( ), e.getMessage ( ), Toast.LENGTH_LONG ).show ( );
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
            String country = addressList.get ( 0 ).getCountryName ( );   // country
            String state = addressList.get ( 0 ).getAdminArea ( );   // state
            String city = addressList.get ( 0 ).getLocality ( );     // city
            String completeAddress = addressList.get ( 0 ).getAddressLine ( 0 );    // complete address
            Log.i ( TAG, "findAddress: " + country );
            Log.i ( TAG, "findAddress: " + state );
            Log.i ( TAG, "findAddress: " + city );
            Log.i ( TAG, "findAddress: " + completeAddress );
            // set addresses in input field
            Objects.requireNonNull ( activityRegisterBinding.textViewCountry ).setText ( country );
            Objects.requireNonNull ( activityRegisterBinding.textViewState ).setText ( state );
            Objects.requireNonNull ( activityRegisterBinding.textViewCity ).setText ( city );
            Objects.requireNonNull ( activityRegisterBinding.textViewCompleteAddress ).setText ( completeAddress );
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
}