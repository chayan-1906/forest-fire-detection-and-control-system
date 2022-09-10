package com.example.phoenix4.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.phoenix4.CustomToast;
import com.example.phoenix4.R;
import com.example.phoenix4.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // TAG
    private static final String TAG = "SplashActivity";

    // view binding
    private ActivitySplashBinding activitySplashBinding;

    // Firebase...
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activitySplashBinding = ActivitySplashBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activitySplashBinding.getRoot ( ) );

        getWindow ( ).setFlags ( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

//        activitySplashBinding.gifView.animate ().alpha ( 0f ).setDuration ( 0 );
        activitySplashBinding.textViewMemberNames.animate ( ).alpha ( 0f ).setDuration ( 0 );

        activitySplashBinding.gifView.animate ( ).alpha ( 1f ).setDuration ( 700 ).setListener ( new AnimatorListenerAdapter ( ) {
            @Override
            public void onAnimationEnd(Animator animation) {
                activitySplashBinding.textViewMemberNames.animate ( ).alpha ( 1f ).setDuration ( 800 );
            }
        } );

        new Handler ( ).postDelayed ( () -> {
            firebaseAuth = FirebaseAuth.getInstance ( );
            if (firebaseAuth.getCurrentUser ( ) != null) {
//                if (firebaseAuth.getCurrentUser ( ).isEmailVerified ( )) {
                firebaseAuth = FirebaseAuth.getInstance ( );
                firebaseUser = firebaseAuth.getCurrentUser ( );
                databaseReference = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
                databaseReference.child ( firebaseUser.getUid ( ) ).addValueEventListener ( new ValueEventListener ( ) {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userType = "" + snapshot.child ( "userTypeTitle" ).getValue ( );
                        if (Objects.equals ( userType, "Head Office" )) {
                            String headOfficeUserName = "" + snapshot.child ( "username" ).getValue ( );
                            Intent intentHeadOfficeMainActivity = new Intent ( SplashActivity.this, MainHeadOfficeActivity.class );
                            intentHeadOfficeMainActivity.putExtra ( "head_office_user_name", headOfficeUserName );
                            Log.i ( TAG, "headOfficeUserName: " + headOfficeUserName );
                            startActivity ( intentHeadOfficeMainActivity );
                            finish ( );
                        } else if (Objects.equals ( userType, "Regional Office" )) {
                            String regionalOfficeUserName = "" + snapshot.child ( "username" ).getValue ( );
                            Intent intentRegionalOfficeMainActivity = new Intent ( SplashActivity.this, MainRegionalOfficeActivity.class );
                            intentRegionalOfficeMainActivity.putExtra ( "regional_office_user_name", regionalOfficeUserName );
                            Log.i ( TAG, "regionalOfficeUserName: " + regionalOfficeUserName );
                            startActivity ( intentRegionalOfficeMainActivity );
                            finish ( );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        CustomToast.makeErrorToast ( getApplicationContext ( ), error.getMessage ( ), Toast.LENGTH_LONG ).show ( );
                    }
                } );
//                }
//                else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder ( SplashActivity.this );
//                    builder.setTitle ( "Email not verified" );
//                    builder.setMessage ( "Please verify your email" );
//                    builder.setCancelable ( false );
//                    builder.setPositiveButton ( "OK", (dialog, which) -> {
//                        dialog.dismiss ( );
//                        Intent intent = new Intent ( SplashActivity.this, MainMenuActivity.class );
//                        startActivity ( intent );
//                        finish ( );
//                    } );
//                    AlertDialog alertDialog = builder.create ( );
//                    alertDialog.show ( );
//                    firebaseAuth.signOut ( );
//                }
            } else {
                Intent intentLogin = new Intent ( SplashActivity.this, LoginActivity.class );
                startActivity ( intentLogin );
                finish ( );
            }
        }, 1500 );
    }
}