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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.CustomToast;
import com.example.phoenix4.R;
import com.example.phoenix4.adapters.ReportAdapter;
import com.example.phoenix4.databinding.ContentReportListHeadOfficeBinding;
import com.example.phoenix4.models.ModelReport;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class ReportListHeadOfficeActivity extends AppCompatActivity {

    private static final String TAG = "ReportListHeadOfficeActivity";

    // view binding
//    private ActivityReportListHeadOfficeBinding activityReportListHeadOfficeBinding;
    private ContentReportListHeadOfficeBinding contentReportListHeadOfficeBinding;

    // arraylist to hold list of data of type ModelReport
    private static ArrayList<ModelReport> modelReportArrayList;

    private ProgressDialog progressDialog;

    // adapter
    private ReportAdapter reportAdapter;

    // firebase
//    private FirebaseAuth firebaseAuth;
//    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReferenceReports;

    private Long reportId;
    private String username;
    private String email;
    private Double fireLocationLat;
    private Double fireLocationLong;
    private Double currentLocationRegOfficeLat;
    private Double currentLocationRegOfficeLong;
    private Double distance;
    private Long timestamp;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        contentReportListHeadOfficeBinding = ContentReportListHeadOfficeBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( contentReportListHeadOfficeBinding.getRoot ( ) );

        modelReportArrayList = new ArrayList<> ( );

        // firebase
        databaseReferenceReports = FirebaseDatabase.getInstance ( ).getReference ( "Reports" );

        contentReportListHeadOfficeBinding.recyclerViewReports.setHasFixedSize ( true );
        contentReportListHeadOfficeBinding.recyclerViewReports.setLayoutManager ( new LinearLayoutManager ( this ) );
//        activityReportListHeadOfficeBinding.recyclerViewReports.setLayoutManager ( new StaggeredGridLayoutManager ( 2, StaggeredGridLayoutManager.VERTICAL ) );

        progressDialog = new ProgressDialog ( ReportListHeadOfficeActivity.this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setMessage ( "Loading Report..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );
//        progressDialog.show ( );

        contentReportListHeadOfficeBinding.linearLayoutNothingToDisplay.setVisibility ( View.GONE );

        loadReportList ( );

        // handle search
        contentReportListHeadOfficeBinding.editTextSearch.addTextChangedListener ( new TextWatcher ( ) {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter ( s.toString ( ) );
            }
        } );

        // handle click, close keyboard
        contentReportListHeadOfficeBinding.linearLayoutRecyclerView.setOnClickListener ( v -> closeKeyboard ( ) );

        // handle click, go back
        contentReportListHeadOfficeBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, open popup
        contentReportListHeadOfficeBinding.imageButtonMore.setOnClickListener ( v -> openMore ( ) );
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString ( "searchText", contentReportListHeadOfficeBinding.editTextSearch.getText ( ).toString ( ).trim ( ) );
        super.onSaveInstanceState ( outState );
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        String searchText = savedInstanceState.getString ( "searchText" );
        Log.i ( TAG, ": " + searchText );
        if (searchText == null)
            searchText = "";
        contentReportListHeadOfficeBinding.editTextSearch.setText ( searchText );
        filter ( searchText );
        super.onRestoreInstanceState ( savedInstanceState );
    }

    @SuppressLint("LongLogTag")
    private void loadReportList() {
        Log.i ( TAG, "loadReportList: started" );
        // TODO: FETCH ALL REPORTS FROM DATABASE_REFERENCE_REPORTS WHILE REGIONAL OFFICE WILL BE RESPONSIBLE FOR A LOCATION
        databaseReferenceReports.addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    // get data
                    reportId = dataSnapshot.child ( "reportId" ).getValue ( Long.class );
                    username = dataSnapshot.child ( "userName" ).getValue ( String.class );
                    email = dataSnapshot.child ( "email" ).getValue ( String.class );
                    fireLocationLat = dataSnapshot.child ( "fireLocationLat" ).getValue ( Double.class );
                    fireLocationLong = dataSnapshot.child ( "fireLocationLong" ).getValue ( Double.class );
                    currentLocationRegOfficeLat = dataSnapshot.child ( "currentLocationRegOfficeLat" ).getValue ( Double.class );
                    currentLocationRegOfficeLong = dataSnapshot.child ( "currentLocationRegOfficeLong" ).getValue ( Double.class );
                    distance = dataSnapshot.child ( "distance" ).getValue ( Double.class );
                    timestamp = dataSnapshot.child ( "report_timestamp" ).getValue ( Long.class );
                    // add data in model class
                    modelReportArrayList.add ( new ModelReport ( reportId, username, email, fireLocationLat, fireLocationLong, currentLocationRegOfficeLat, currentLocationRegOfficeLong, distance, timestamp ) );
                    Log.i ( TAG, "onDataChange: " + modelReportArrayList.toString ( ) );
                    // setup adapter
                    reportAdapter = new ReportAdapter ( ReportListHeadOfficeActivity.this, modelReportArrayList );
                    contentReportListHeadOfficeBinding.recyclerViewReports.setAdapter ( reportAdapter );
                    reportAdapter.notifyDataSetChanged ( );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.makeErrorToast ( getApplicationContext ( ), "Failed to load report " + error, Toast.LENGTH_SHORT ).show ( );
            }
        } );

//        progressDialog.dismiss ( );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void openMore() {
        // init/setup popup menu
        PopupMenu popupMenu = new PopupMenu ( this, contentReportListHeadOfficeBinding.imageButtonMore );
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
            modelReportArrayList.sort ( Comparator.comparing ( ModelReport::getUserName ) );
        } else if (which == 1) {
            modelReportArrayList.sort ( Comparator.comparing ( ModelReport::getEmail ) );
        } else if (which == 2) {
            modelReportArrayList.sort ( Comparator.comparing ( ModelReport::getDistance ) );
        } else if (which == 3) {
            modelReportArrayList.sort ( Comparator.comparing ( ModelReport::getTimestamp ) );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder ( ReportListHeadOfficeActivity.this );
        builder.setTitle ( "Sort" );
        String[] sortItems = {"User Name", "Email", "Distance", "Date"};
        int checkedItem = 3;
        builder.setSingleChoiceItems ( sortItems, checkedItem, (dialog, which) -> {
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
            reportAdapter.notifyDataSetChanged ( );
        } );
        builder.create ( ).show ( );
    }

    @SuppressLint("LongLogTag")
    private void filter(String text) {
        ArrayList<ModelReport> filteredList = new ArrayList<> ( );
        for (ModelReport modelReport : modelReportArrayList) {
            if (modelReport.getUserName ( ).toLowerCase ( ).contains ( text.toLowerCase ( ) )) {
                Log.i ( TAG, "filter: found" );
                filteredList.add ( modelReport );
            }
            if (filteredList.isEmpty ( )) {
                Log.i ( TAG, "filter: not found" );
                Objects.requireNonNull ( contentReportListHeadOfficeBinding.linearLayoutNothingToDisplay ).setVisibility ( View.VISIBLE );
                contentReportListHeadOfficeBinding.recyclerViewReports.setVisibility ( View.GONE );
            } else {
                Objects.requireNonNull ( contentReportListHeadOfficeBinding.linearLayoutNothingToDisplay ).setVisibility ( View.GONE );
                contentReportListHeadOfficeBinding.recyclerViewReports.setVisibility ( View.VISIBLE );
            }
            reportAdapter.filterList ( filteredList, text );
        }
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
        Animatoo.animateCard ( this );
    }
}