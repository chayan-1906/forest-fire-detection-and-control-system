package com.example.phoenix4.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.R;
import com.example.phoenix4.adapters.RegionalOfficesAdapter;
import com.example.phoenix4.databinding.ActivityRegionalOfficesListBinding;
import com.example.phoenix4.models.ModelRegionalOffice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RegionalOfficesListActivity extends AppCompatActivity {

    private static final String TAG = "RegionalOfficesListActivity";

    // view binding
    private ActivityRegionalOfficesListBinding activityRegionalOfficesListBinding;

    // arraylist to hold list of data of type ModelRegionalOffice
    private static ArrayList<ModelRegionalOffice> modelRegionalOfficesArrayList;

    private ProgressDialog progressDialog;

    // adapter
    private RegionalOfficesAdapter regionalOfficesAdapter;

    // firebase
    private DatabaseReference databaseReferenceUsers;

    private String userTypeId;
    private Boolean isEmailVerified;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityRegionalOfficesListBinding = ActivityRegionalOfficesListBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityRegionalOfficesListBinding.getRoot ( ) );

        modelRegionalOfficesArrayList = new ArrayList<> ( );

        // firebase
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        activityRegionalOfficesListBinding.recyclerViewRegionalOffices.setHasFixedSize ( true );
//        activityRegionalOfficesListBinding.recyclerViewRegionalOffices.setLayoutManager ( new LinearLayoutManager ( this ) );
        activityRegionalOfficesListBinding.recyclerViewRegionalOffices.setLayoutManager ( new GridLayoutManager ( this, GridLayoutManager.VERTICAL ) );

        progressDialog = new ProgressDialog ( RegionalOfficesListActivity.this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setMessage ( "Loading Regional Offices..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );
//        progressDialog.show ( );

        activityRegionalOfficesListBinding.linearLayoutNothingToDisplay.setVisibility ( View.GONE );

        loadRegionalOfficesList ( );

        // handle search
        activityRegionalOfficesListBinding.editTextSearch.addTextChangedListener ( new TextWatcher ( ) {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                filter ( s.toString ( ) );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter ( s.toString ( ) );
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter ( s.toString ( ) );
            }
        } );

        // handle click, close keyboard
        activityRegionalOfficesListBinding.linearLayoutRecyclerView.setOnClickListener ( v -> closeKeyboard ( ) );

        // handle click, go back
        activityRegionalOfficesListBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, open popup
        activityRegionalOfficesListBinding.imageButtonMore.setOnClickListener ( v -> openMore ( ) );
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString ( "searchText", activityRegionalOfficesListBinding.editTextSearch.getText ( ).toString ( ).trim ( ) );
        super.onSaveInstanceState ( outState );
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        String searchText = savedInstanceState.getString ( "searchText" );
        Log.i ( TAG, ": " + searchText );
        if (searchText == null)
            searchText = "";
        activityRegionalOfficesListBinding.editTextSearch.setText ( searchText );
        filter ( searchText );
        super.onRestoreInstanceState ( savedInstanceState );
    }

    private void loadRegionalOfficesList() {
        // fetching all distances and store in list, then sort the list
        databaseReferenceUsers.addValueEventListener ( new ValueEventListener ( ) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    userTypeId = dataSnapshot.child ( "userTypeId" ).getValue ( String.class );
                    isEmailVerified = dataSnapshot.child ( "isEmailVerified" ).getValue ( Boolean.class );
                    Log.i ( TAG, "userTypeId: " + userTypeId );
                    if (Objects.equals ( userTypeId, "2" ) && isEmailVerified) {
                        Double distance = dataSnapshot.child ( "distance" ).getValue ( Double.class );
                        String email = dataSnapshot.child ( "email" ).getValue ( String.class );
                        Double regionalOfficeLat = dataSnapshot.child ( "latitude" ).getValue ( Double.class );
                        Double regionalOfficeLong = dataSnapshot.child ( "longitude" ).getValue ( Double.class );
                        String regionalOfficeId = dataSnapshot.child ( "uid" ).getValue ( String.class );
                        String username = dataSnapshot.child ( "username" ).getValue ( String.class );
                        Log.i ( TAG, "onDataChange: distance = " + distance );
                        modelRegionalOfficesArrayList.add ( new ModelRegionalOffice ( regionalOfficeId, username, email, regionalOfficeLat, regionalOfficeLong, distance ) );
                    }
                    // setup adapter
                    regionalOfficesAdapter = new RegionalOfficesAdapter ( RegionalOfficesListActivity.this, modelRegionalOfficesArrayList );
                    Objects.requireNonNull ( activityRegionalOfficesListBinding.recyclerViewRegionalOffices ).setAdapter ( regionalOfficesAdapter );
                    regionalOfficesAdapter.notifyDataSetChanged ( );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
//        progressDialog.dismiss ( );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void openMore() {
        // init/setup popup menu
        PopupMenu popupMenu = new PopupMenu ( this, activityRegionalOfficesListBinding.imageButtonMore );
        popupMenu.getMenuInflater ( ).inflate ( R.menu.menu_report_list, popupMenu.getMenu ( ) );
        // handle menu item click
        popupMenu.setOnMenuItemClickListener ( item -> {
            // get id of the item is clicked
            int which = item.getItemId ( );
            if (which == R.id.action_sortlist) {
                // Sort clicked
                showAlertDialog ( );
            }
            return false;
        } );
        popupMenu.show ( );
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortList(int which) {
        if (which == 0) {
            modelRegionalOfficesArrayList.sort ( Comparator.comparing ( ModelRegionalOffice::getUserName ) );
        } else if (which == 1) {
            modelRegionalOfficesArrayList.sort ( Comparator.comparing ( ModelRegionalOffice::getEmail ) );
        } else if (which == 2) {
            modelRegionalOfficesArrayList.sort ( Comparator.comparing ( ModelRegionalOffice::getDistance ) );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder ( RegionalOfficesListActivity.this );
        builder.setTitle ( "Sort" );
        String[] sortItems = {"User Name", "Email", "Distance"};
        AtomicInteger checkedItem = new AtomicInteger ( 2 );
        builder.setSingleChoiceItems ( sortItems, checkedItem.get ( ), (dialog, which) -> {
            switch (which) {
                case 0:
                    sortList ( 0 );
                    break;
                case 1:
                    sortList ( 1 );
                    break;
                case 2:
                    sortList ( 2 );
                    break;
                case 3:
                    sortList ( 3 );
                    break;
            }
            dialog.dismiss ( );
            regionalOfficesAdapter.notifyDataSetChanged ( );
            checkedItem.set ( which );
        } );
        builder.create ( ).show ( );
    }

    @SuppressLint({"LongLogTag", "NotifyDataSetChanged"})
    private void filter(String text) {
        ArrayList<ModelRegionalOffice> filteredList = new ArrayList<> ( );
        for (ModelRegionalOffice modelRegionalOffice : modelRegionalOfficesArrayList) {
            if (modelRegionalOffice.getUserName ( ).toLowerCase ( ).contains ( text.toLowerCase ( ) )) {
                Log.i ( TAG, "filter: found" );
                filteredList.add ( modelRegionalOffice );
            }
            if (filteredList.isEmpty ( )) {
                Log.i ( TAG, "filter: not found" );
                Objects.requireNonNull ( activityRegionalOfficesListBinding.linearLayoutNothingToDisplay ).setVisibility ( View.VISIBLE );
                activityRegionalOfficesListBinding.recyclerViewRegionalOffices.setVisibility ( View.GONE );
            } else {
                Objects.requireNonNull ( activityRegionalOfficesListBinding.linearLayoutNothingToDisplay ).setVisibility ( View.GONE );
                activityRegionalOfficesListBinding.recyclerViewRegionalOffices.setVisibility ( View.VISIBLE );
            }
            regionalOfficesAdapter.filterList ( filteredList, text );
        }
        Log.i ( TAG, ": filtered" );
    }

    @SuppressLint("LongLogTag")
    private void closeKeyboard() {
        View view = this.getCurrentFocus ( );
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext ( ).getSystemService ( Context.INPUT_METHOD_SERVICE );
            inputMethodManager.hideSoftInputFromWindow ( view.getWindowToken ( ), 0 );
            Log.i ( TAG, "Close KeyBoard: called" );
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ( );
        Animatoo.animateFade ( this );
    }
}