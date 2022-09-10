package com.example.phoenix4.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phoenix4.CustomToast;
import com.example.phoenix4.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {

    // TAG
    private static final String TAG = "ForgotPasswordActivity";

    // view binding
    private ActivityForgotPasswordBinding activityForgotPasswordBinding;

    private ProgressDialog progressDialog;

    // Firebase...
    private FirebaseAuth firebaseAuth;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityForgotPasswordBinding = ActivityForgotPasswordBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityForgotPasswordBinding.getRoot ( ) );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );

        // Firebase...
        firebaseAuth = FirebaseAuth.getInstance ( );

        // handle click, go back
        activityForgotPasswordBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, recover password
        activityForgotPasswordBinding.btnRecover.setOnClickListener ( v -> recoverPassword ( ) );
    }

    private void recoverPassword() {
        email = Objects.requireNonNull ( activityForgotPasswordBinding.textInputLayoutEmail.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        if (!Patterns.EMAIL_ADDRESS.matcher ( email ).matches ( )) {
            activityForgotPasswordBinding.textInputLayoutEmail.setError ( "Invalid email id" );
            activityForgotPasswordBinding.textInputLayoutEmail.requestFocus ( );
            return;
        }
        progressDialog.setMessage ( "Sending instructions to reset password..." );
        progressDialog.show ( );
        firebaseAuth.sendPasswordResetEmail ( email ).addOnSuccessListener ( unused -> {
            // instructions sent
            progressDialog.dismiss ( );
            com.example.phoenix4.CustomToast.makeSuccessToast ( getApplicationContext ( ), "Link sent, Check your email", Toast.LENGTH_SHORT ).show ( );
            startActivity ( new Intent ( ForgotPasswordActivity.this, LoginActivity.class ) );
            finish ( );
        } ).addOnFailureListener ( e -> {
            // failed to send instructions
            progressDialog.dismiss ( );
            CustomToast.makeErrorToast ( getApplicationContext ( ), e.getMessage ( ), Toast.LENGTH_SHORT ).show ( );
        } );
    }
}